package com.example.orderservice.dto;

import lombok.Data;

import java.util.List;
@Data
public class OrderReq {
    private int status;
    private long itemId;
    private int qty;
}
