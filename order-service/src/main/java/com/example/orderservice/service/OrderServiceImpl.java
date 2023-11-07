package com.example.orderservice.service;

import com.example.orderservice.dto.*;
import com.example.orderservice.jpa.*;
import com.example.orderservice.massagequeue.OrderProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final StockRepository stockRepository;
    private final Environment env;
    private final RedisTemplate<String, Long> stockTemplate;
    private final RedisTemplate<Long, Integer> qtyTemplate;

    @Override
    public List<OrderDto> createOrder() {
        SetOperations<String, Long> setOperations = stockTemplate.opsForSet();
        ValueOperations<Long, Integer> valueOperations = qtyTemplate.opsForValue();
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
                int qty = stockEntity.getAmount();
                if (setOperations.isMember("stocks",stockId)){
                    qty += valueOperations.get(stockId);
                }
                //재고가 있다면
                if (qty > randomQty) {
                    ItemEntity itemEntity = ItemEntity.builder()
                            .stockId(stockId)
                            .orderEntity(savedOrderEntity)
                            .qty(randomQty)
                            .build();
                    ItemEntity savedItemEntity = itemRepository.save(itemEntity);
                    ResponseItem responseItem = mapper.map(savedItemEntity, ResponseItem.class);
                    responseItemList.add(responseItem);
                    //캐싱
                    if (setOperations.isMember("stocks",stockId)){
                        int stockQty = valueOperations.get(stockId);
                        valueOperations.set(stockId, stockQty - randomQty);
                    }else{
                        setOperations.add("stocks", stockId);
                        valueOperations.set(stockId, - randomQty);
                    }
                    j++;
                }
            }
            orderDto.setResponseItemList(responseItemList);
            orderDtoList.add(orderDto);
        }


        return orderDtoList;
    }

    @Override
    public OrderDto createOrderManually(OrderReq orderReq) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        ItemEntity itemEntity = itemRepository.findById(orderReq.getItemId()).orElseThrow();
        if(orderReq.getStatus() != 0 || itemEntity.getQty() >= orderReq.getQty()){
            OrderEntity orderEntity = new OrderEntity().builder().build();
            orderEntity.setStatus(orderReq.getStatus());
            orderRepository.save(orderEntity);
            OrderDto orderDto = mapper.map(orderEntity, OrderDto.class);

            return orderDto;
        }
        Exception e = new Exception("생성 불가능한 주문");
        e.printStackTrace();
        return null;
    }
    //주문 정보와 제품 설명 리턴
    @Override
    public OrderDetailDto get(long id) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderEntity orderEntity = orderRepository.findById(id).orElseThrow();

        OrderDetailDto orderDetailDto = mapper.map(orderEntity, OrderDetailDto.class);
        List<DetailResponseItem> detailList = new ArrayList<>();
        for (ItemEntity itemEntity: orderEntity.getItemEntityList()){
            StockEntity stock = stockRepository.findById(itemEntity.getStockId()).orElseThrow();
            DetailResponseItem detail = new DetailResponseItem();
            detail.setId(stock.getId());
            detail.setQty(itemEntity.getQty());
            detail.setAbout(stock.getAbout());
            detail.setImage(stock.getImage());
            detailList.add(detail);
        }
        orderDetailDto.setResponseItemList(detailList);
        return orderDetailDto;
    }
    //주문 status 변경후 push면 redis에 수량 저장
    @Override
    public OrderDto complete(FinishedOrderDto finishedOrderDto) {
        SetOperations<String, Long> setOperations = stockTemplate.opsForSet();
        ValueOperations<Long, Integer> valueOperations = qtyTemplate.opsForValue();
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        long id = finishedOrderDto.getOrderId();
        long userId = finishedOrderDto.getUserId();
        OrderEntity orderEntity = orderRepository.findById(id).orElseThrow();
        int status = orderEntity.getStatus();
        if(status%3 != 1){
            Exception e = new Exception("잘못된 접근입니다. 주문 번호: " + id);
            e.printStackTrace();
        } else if (status == 4) {
            for(ItemEntity itemEntity : orderEntity.getItemEntityList()){
                long stockId = itemEntity.getStockId();
                setOperations.add("stocks", stockId);
                int qty = valueOperations.get(stockId);
                qty += itemEntity.getQty();
                valueOperations.set(stockId, qty);
            }
            orderEntity.setUserId(userId);
            orderEntity.setFinishedTime(LocalDateTime.now().toString());
            orderEntity.setStatus(status + 1);
            orderRepository.save(orderEntity);

        }else {
            orderEntity.setStatus(status + 1);
            orderRepository.save(orderEntity);
        }
        OrderDto orderDto = mapper.map(orderEntity, OrderDto.class);

        return orderDto;
    }

    // redis에 저장된 바뀐 items목록을 돌면서 확인후 db에 적용하고 레디스에서 삭제
    @Override
    public Boolean redisToDB() {
        SetOperations<String, Long> setOperations = stockTemplate.opsForSet();
        ValueOperations<Long, Integer> valueOperations = qtyTemplate.opsForValue();
        Set<Long> set = setOperations.members("stocks");
        for (long id : set) {
            int qty = valueOperations.get(id);
            StockEntity stock = stockRepository.findById(id).orElseThrow();
            int DBQty = stock.getAmount();
            DBQty += qty;
            stock.setAmount(DBQty);
            stockRepository.save(stock);
            setOperations.remove("stocks", id);
            valueOperations.getOperations().delete(id);
        }
        return null;
    }
}
