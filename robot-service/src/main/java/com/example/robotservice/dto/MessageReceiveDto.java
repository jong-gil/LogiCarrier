package com.example.robotservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageReceiveDto {

    // 로봇 배터리 잔량
    private int batteryPercent;
    private int x;
    private int y;

}
