package com.example.orderservice.massagequeue;

import com.example.orderservice.dto.*;
import com.example.orderservice.jpa.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderProducer orderProducer;
    private final StockRepository stockRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    // 배송준비된 주문의 준비시간 status  변경후 저장
    @KafkaListener(topics = "itemFinsih")
    public void orderFinish(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OrderRequestDto orderRequestDto = objectMapper.readValue(message, OrderRequestDto.class);
        OrderEntity orderEntity = orderRepository.findById(orderRequestDto.getId()).orElseThrow(NoSuchElementException::new);

        orderEntity.setFinishedTime(LocalDateTime.now().toString());
        orderEntity.setStatus(1);
        orderRepository.save(orderEntity);
    }

    //요청받은 주문과 아이템 정보 orderInfo로 produce
    @KafkaListener(topics = "nextOrder")
    public void nextOrders(String message) throws IOException {
        OrderEntity orderEntity = orderRepository.findTopByStatusOrderByCreatedTime(0).orElseThrow(NoSuchElementException::new);
        ModelMapper mapper = new ModelMapper();
        OrderDto orderDto = mapper.map(orderEntity, OrderDto.class);

        List<ItemEntity> itemEntityList= itemRepository.findAllByOrderEntity(orderEntity);
        List<ResponseItem> responseItemList = new ArrayList<>();
        for(ItemEntity itemEntity : itemEntityList){
            ResponseItem responseItem = mapper.map(itemEntity, ResponseItem.class);
            responseItemList.add(responseItem);
        }
        orderDto.setResponseItemList(responseItemList);
        orderProducer.send("targetInfo", orderDto);
        log.info(String.format("Consumed message : %s", message));
    }

    @KafkaListener(topics = "stockInfoRequest")
    public void stockInfo(String message) throws IOException {
        StockEntity stockEntity = stockRepository.findById(Long.parseLong(message)).orElseThrow();
        StockInfoDto stockInfoDto = StockInfoDto.builder()
                .id(stockEntity.getId())
                .about(stockEntity.getAbout())
                .image(stockEntity.getImage())
                .name(stockEntity.getName())
                .build();
        kafkaTemplate.send("stockInfo", stockInfoDto.toString());
        log.info(String.format("Consumed message : %s", message));
    }

    @KafkaListener(topics = "nextPush")
    public void nextPush(String message) throws IOException {
        OrderEntity orderEntity = orderRepository.findTopByStatusOrderByCreatedTime(3).orElseThrow(NoSuchElementException::new);
        ModelMapper mapper = new ModelMapper();
        OrderDto orderDto = mapper.map(orderEntity, OrderDto.class);

        List<ItemEntity> itemEntityList= itemRepository.findAllByOrderEntity(orderEntity);
        List<ResponseItem> responseItemList = new ArrayList<>();
        for(ItemEntity itemEntity : itemEntityList){
            ResponseItem responseItem = mapper.map(itemEntity, ResponseItem.class);
            responseItemList.add(responseItem);
        }
        orderDto.setResponseItemList(responseItemList);
        orderProducer.send("pushInfo", orderDto);
        log.info(String.format("Consumed message : %s", message));
    }
}
