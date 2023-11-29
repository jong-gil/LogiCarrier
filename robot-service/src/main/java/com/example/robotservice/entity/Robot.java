package com.example.robotservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "robot")
public class Robot {
    //대기중인 로봇        args
    @Id
    private String robotId;
    private int positionX;
    private int positionY;
    private Long shelfId;
    private int battery;
}
