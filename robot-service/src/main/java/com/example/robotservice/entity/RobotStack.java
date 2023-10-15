package com.example.robotservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Stack;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "robotStack")
public class RobotStack {
    private Stack<String> robotIdList;
}