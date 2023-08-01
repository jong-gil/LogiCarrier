package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;

import java.util.List;

public interface OrderService {
    List<OrderDto> createOrder();

}
