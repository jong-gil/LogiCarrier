package com.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockInfoDto {
    private Long id;
    private String name;
    private String about;
    private String image;
}
