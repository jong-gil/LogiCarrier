package com.example.workerservice.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class OrderDto {
    private long id;
    private String createdTime;
    private String finishedTime;
    private Integer status;
    private Long userId;
    private List<ResponseItem> responseItemList;
}
