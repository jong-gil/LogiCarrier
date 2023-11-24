package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderReq;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<OrderDto> createOrder();
    OrderDto createOrderManually(OrderReq orderReq);

    OrderDto get(long id);
}
