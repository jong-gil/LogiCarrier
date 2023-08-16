package com.example.orderservice.massagequeue;

import com.example.orderservice.dto.ItemFinishDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderRequestDto;
import com.example.orderservice.dto.ResponseItem;
import com.example.orderservice.jpa.ItemEntity;
import com.example.orderservice.jpa.ItemRepository;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.jpa.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderProducer orderProducer;
    //요청받은 주문과 아이템 정보 orderInfo로 produce
    @KafkaListener(topics = "orders")
    public void orders(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OrderRequestDto orderRequestDto = objectMapper.readValue(message, OrderRequestDto.class);
        OrderEntity orderEntity = orderRepository.findById(orderRequestDto.getId()).orElseThrow(NoSuchElementException::new);
        ModelMapper mapper = new ModelMapper();
        OrderDto orderDto = mapper.map(orderEntity, OrderDto.class);
        List<ItemEntity> itemEntityList= itemRepository.findAllByOrderEntity(orderEntity);
        List<ResponseItem> responseItemList = new ArrayList<>();
        for(ItemEntity itemEntity : itemEntityList){
            ResponseItem responseItem = mapper.map(itemEntity, ResponseItem.class);
            responseItemList.add(responseItem);
        }
        orderDto.setResponseItemList(responseItemList);
        orderProducer.send("orderInfo", orderDto);
        System.out.println(String.format("Consumed message : %s", message));
    }


    // 배송준비된 아이템의 준비시간 status  변경후 저장
    @KafkaListener(topics = "itemFinsih")
    public void itemFinish(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ItemFinishDto itemFinishDto = objectMapper.readValue(message, ItemFinishDto.class);
        ItemEntity itemEntity = itemRepository.findById(itemFinishDto.getId()).orElseThrow(NoSuchElementException::new);

        itemEntity.setFinishedTime(LocalDateTime.now().toString());
        itemEntity.setStatus(true);
        itemRepository.save(itemEntity);
    }
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
    @KafkaListener(topics = "nextOrders")
    public void nextOrders(String message) throws IOException {
        OrderEntity orderEntity = orderRepository.findTopByStatusOrderByCreatedTime(0).orElseThrow(NoSuchElementException::new);
        ModelMapper mapper = new ModelMapper();
        OrderDto orderDto = mapper.map(orderEntity, OrderDto.class);
        System.out.println(orderDto.getId());
        List<ItemEntity> itemEntityList= itemRepository.findAllByOrderEntity(orderEntity);
        List<ResponseItem> responseItemList = new ArrayList<>();
        for(ItemEntity itemEntity : itemEntityList){
            ResponseItem responseItem = mapper.map(itemEntity, ResponseItem.class);
            responseItemList.add(responseItem);
        }
        orderDto.setResponseItemList(responseItemList);
        orderProducer.send("targetInfo", orderDto);
        System.out.println(String.format("Consumed message : %s", message));
    }
}
