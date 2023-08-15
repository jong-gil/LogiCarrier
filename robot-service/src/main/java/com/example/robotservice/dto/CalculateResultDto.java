package com.example.robotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
public class CalculateResultDto {
    //왜 안됨?
    private int distance;
    private int meanCost;
    private boolean isChange;
    private HashMap<Long, Pick> pickHash;
}
