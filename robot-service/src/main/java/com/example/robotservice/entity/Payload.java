package com.example.robotservice.entity;

import com.example.robotservice.dto.ResponseItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "order")
public class Payload {
    private Long id;
    private ArrayList<ResponseItem> responseItemList;
}