package com.example.robotservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageSendDto {
    // 로봇 위치
    private int positionX;
    private int positionY;

    // 로봇 경로
    // "U2 R3 C1 L3 U3 R1 P1"
    private String route;
    private long now;
    private long turn;
    private long shelfId;

}
