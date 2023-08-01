package com.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
@Builder
public class Payload {
    private Long id;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime finishedTime;
    private Long userId;

}