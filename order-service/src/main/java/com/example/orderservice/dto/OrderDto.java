package com.example.orderservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private long id;
    private String createdTime;
    private String finishedTime;
    private Integer status;
    private Long userId;
    private List<ResponseItem> responseItemList;
}
