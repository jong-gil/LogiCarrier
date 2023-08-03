package com.example.orderservice.dto;

import lombok.Data;

@Data
public class ResponseItem {
    private Long id;
    private String name;
    private Long StockId;
}
