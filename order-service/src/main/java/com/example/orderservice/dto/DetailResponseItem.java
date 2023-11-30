package com.example.orderservice.dto;

import lombok.Data;

@Data
public class DetailResponseItem {
    private long id;
    private String image;
    private String about;
    private int qty;
}
