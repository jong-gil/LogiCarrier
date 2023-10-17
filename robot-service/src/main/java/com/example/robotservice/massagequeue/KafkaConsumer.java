package com.example.robotservice.massagequeue;


import com.example.robotservice.entity.Payload;
import com.example.robotservice.service.RobotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final RedisTemplate<String, String> redisTemplate;
    private final RobotService robotService;
    //요청받은 주문과 아이템 정보 orderInfo로 produce

    @KafkaListener(topics = "targetInfo")
    public void targetInfo(String message) throws IOException, Exception {
        ListOperations<String, String> orderDeque = redisTemplate.opsForList();
        ObjectMapper objectMapper = new ObjectMapper();

        orderDeque.rightPush("orderDeque", message);
        String leftPop = orderDeque.leftPop("orderDeque");
        Payload payload = objectMapper.readValue(leftPop, Payload.class);
        if (! robotService.find(payload)){ //불가능하면 덱 마지막에 넣기
            orderDeque.rightPush("orderDeque", leftPop);
        }
        log.info(String.format("Consumed message : %s", message));
    }
}
