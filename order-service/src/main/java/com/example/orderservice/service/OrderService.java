package com.example.orderservice.service;

import com.example.orderservice.dto.FinishedOrderDto;
import com.example.orderservice.dto.OrderDetailDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderReq;
import org.aspectj.weaver.ast.Or;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<OrderDto> createOrder();
    OrderDto createOrderManually(OrderReq orderReq);

    OrderDetailDto get(long id);

    OrderDto complete(FinishedOrderDto finishedOrderDto);
    Boolean redisToDB();
}
