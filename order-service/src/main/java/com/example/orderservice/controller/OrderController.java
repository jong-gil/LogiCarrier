package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.RequestOrder;
import com.example.orderservice.dto.ResponseOrder;
import com.example.orderservice.jpa.ItemEntity;
import com.example.orderservice.massagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order-service")
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
}
