package com.example.robotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class CalculateResultDto {
    private int distance;
    private int meanCost;
    private boolean isChange;
    private HashMap<Long, Pick> pickHash;
}
