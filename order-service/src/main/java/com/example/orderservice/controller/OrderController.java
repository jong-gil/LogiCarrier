package com.example.orderservice.controller;

import com.example.orderservice.dto.*;
import com.example.orderservice.massagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@EnableScheduling
@RequestMapping("")
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final Environment env;
    private final OrderService orderService;
    private final OrderProducer orderProducer;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in Order Service on PORT %s",
                env.getProperty("local.server.port"));
    }

    @PostMapping("/orders")
    public ResponseEntity<List<ResponseOrder>> createOrder() {
        log.info("Before add orders data");
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        /* jpa */
        List<OrderDto> orderDtoList = orderService.createOrder();
        List<ResponseOrder> responseOrderList = new ArrayList<>();
        for(OrderDto orderDto : orderDtoList){
            ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);
            responseOrderList.add(responseOrder);
        }


        log.info("After added orders data");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrderList);
    }
    @PostMapping("/orders/manually")
    public ResponseEntity<OrderDto> createOrderManually(OrderReq orderReq) {
        log.info("Before add orders data manually");
        OrderDto orderDto = orderService.createOrderManually(orderReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDetailDto> getOrderManually(@PathVariable("id") long id) {
        log.info("read order info: " + id);
        OrderDetailDto orderDetailDto = orderService.get(id);
        return ResponseEntity.status(HttpStatus.OK).body(orderDetailDto);
    }
    @PostMapping("/orders/complete")
    public ResponseEntity<OrderDto> setOrderManually(FinishedOrderDto finishedOrderDto) {
        OrderDto orderDto = orderService.complete(finishedOrderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }


    @Scheduled(cron = "0 0 1 * * *")
    @PostMapping("/redis")
    public ResponseEntity<Boolean> redisToDB(){
        try{
            orderService.redisToDB();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.LOCKED).body(false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }
}
