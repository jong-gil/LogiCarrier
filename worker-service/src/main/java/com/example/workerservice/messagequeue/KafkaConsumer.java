package com.example.workerservice.messagequeue;


import com.example.workerservice.dto.WorkerRes;
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
        WorkerRes workerRes = objectMapper.convertValue(message, WorkerRes.class);
        String workerId = workerRes.getWorkerId();

        int workerInt = Integer.parseInt(workerId) + 1;
        long turn = workerRes.getTurn();

        StringBuilder sb = new StringBuilder("worker");
        if (0 < workerInt && workerInt < 6) {
            String workerBitKey = String.valueOf(sb.append(workerInt));

            redisSortedSet.add(workerBitKey, workerRes, (double) turn);
        }

    }

    @KafkaListener(topics = "pusherRes")
    public void getPushInfo(String message) throws Exception {
        ZSetOperations<String, Object> redisSortedSet = redisTemplate.opsForZSet();

    }
}
