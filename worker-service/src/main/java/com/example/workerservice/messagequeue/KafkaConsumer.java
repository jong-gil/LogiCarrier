package com.example.workerservice.messagequeue;

import com.example.workerservice.dto.PickerRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "pickerRes")
    public void getOrderInfo(String message) throws Exception {
        ZSetOperations<String, Object> redisSortedSet = redisTemplate.opsForZSet();

        ObjectMapper objectMapper = new ObjectMapper();
        PickerRes pickerRes = objectMapper.convertValue(message, PickerRes.class);
        String pickerId = pickerRes.getPickerId();
        long turn = pickerRes.getTurn();
        
        if (pickerId.equals("1")) {
            redisSortedSet.add("worker1", pickerRes, (double) turn);
        }if (pickerId.equals("2")) {
            redisSortedSet.add("worker2", pickerRes, (double) turn);
        }if (pickerId.equals("3")) {
            redisSortedSet.add("worker3", pickerRes, (double) turn);
        }if (pickerId.equals("4")) {
            redisSortedSet.add("worker4", pickerRes, (double) turn);
        }if (pickerId.equals("5")) {
            redisSortedSet.add("worker5", pickerRes, (double) turn);
        }
    }

    @KafkaListener(topics = "pusherRes")
    public void getPushInfo(String message) throws Exception {
        ZSetOperations<String, Object> redisSortedSet = redisTemplate.opsForZSet();

    }
}
