package com.example.robotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class Pick {
    private HashMap<Long, Integer> stockInfo;
}
