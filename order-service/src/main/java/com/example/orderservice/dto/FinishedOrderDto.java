package com.example.orderservice.dto;

import lombok.Data;

@Data
public class FinishedOrderDto {
    private final long userId;
    private final long orderId;
}
