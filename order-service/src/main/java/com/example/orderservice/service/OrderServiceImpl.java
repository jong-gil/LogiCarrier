package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.ResponseItem;
import com.example.orderservice.jpa.*;
import com.example.orderservice.massagequeue.OrderProducer;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final StockRepository stockRepository;
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
            OrderDto orderDto = mapper.map(savedOrderEntity, OrderDto.class);
            List<ResponseItem> responseItemList = new ArrayList<>();
            int j = 0;
            //랜덤한 아이템수를 채울떄 까지
            while ( j < (int)(Integer.parseInt(env.getProperty("item.maxCnt"))*Math.random()) + 1) {
                int randomId = (int)(Integer.parseInt(env.getProperty("item.max"))*Math.random()) + 1;
                int randomQty = (int)(Integer.parseInt(env.getProperty("item.maxQty"))*Math.random()) + 1;
                Long stockId = (long)randomId;
                StockEntity  stockEntity = stockRepository.findById(stockId).orElseThrow(NoSuchElementException::new);
                //재고가 있다면
                if (stockEntity.getAmount() > randomQty) {
                    ItemEntity itemEntity = ItemEntity.builder()
                            .stockId(stockId)
                            .orderEntity(savedOrderEntity)
                            .qty(randomQty)
                            .build();
                    ItemEntity savedItemEntity = itemRepository.save(itemEntity);
                    ResponseItem responseItem = mapper.map(savedItemEntity, ResponseItem.class);
                    responseItemList.add(responseItem);
                    stockEntity.setAmount(stockEntity.getAmount() - randomQty);
                    stockRepository.save(stockEntity);
                    j++;
                }
            }
            orderDto.setResponseItemList(responseItemList);
            orderDtoList.add(orderDto);
        }


        return orderDtoList;
    }
}
