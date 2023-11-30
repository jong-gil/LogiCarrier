package com.example.robotservice.dto;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Road {
    private ArrayList<long[]> schedule;
    private boolean isCorner;


}
