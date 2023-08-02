package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.ResponseItem;
import com.example.orderservice.jpa.ItemEntity;
import com.example.orderservice.jpa.ItemRepository;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.jpa.OrderRepository;
import com.example.orderservice.massagequeue.OrderProducer;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final Environment env;
    private final OrderProducer orderProducer;

    @Override
    public List<OrderDto> createOrder() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        List<OrderDto> orderDtoList = new ArrayList<>();
        for (int i = 0; i< Integer.parseInt(env.getProperty("item.once")); i++){
            OrderEntity orderEntity = OrderEntity.builder().build();
            OrderEntity savedOrderEntity = orderRepository.save(orderEntity);
            OrderDto orderDto = mapper.map(orderEntity, OrderDto.class);
            List<ResponseItem> responseItemList = new ArrayList<>();
            orderProducer.send("orders", orderDto);
            for (int j = 0; j < (int)(Integer.parseInt(env.getProperty("item.maxCnt"))*Math.random()) + 1; j ++){
                int randomId = (int)(Integer.parseInt(env.getProperty("item.max"))*Math.random()) + 1;
                Long stockId = new Long(randomId);
                ItemEntity itemEntity = ItemEntity.builder()
                        .stockId(stockId)
                        .orderEntity(savedOrderEntity)
                        .build();
                ItemEntity savedItemEntity = itemRepository.save(itemEntity);
                ResponseItem responseItem = mapper.map(savedItemEntity, ResponseItem.class);
                responseItemList.add(responseItem);

            }
            orderDto.setResponseItemList(responseItemList);
            orderDtoList.add(orderDto);
        }


        return orderDtoList;
    }
}
