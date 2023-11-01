package com.example.robotservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RobotEvent {
    private int[] start;
    private Long shelfId;
}
