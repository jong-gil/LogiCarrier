package com.example.workerservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseItem {
    private long id;
    private int qty;
}
