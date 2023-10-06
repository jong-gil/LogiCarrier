package com.example.robotservice.massagequeue;


import com.example.robotservice.dto.Payload;
import com.example.robotservice.service.RobotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final RobotService robotService;
    //요청받은 주문과 아이템 정보 orderInfo로 produce

    //요청받은 주문과 아이템 정보 orderInfo로 produce
    @KafkaListener(topics = "targetInfo")
    public void targetInfo(String message) throws IOException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Payload payload = objectMapper.readValue(message, Payload.class);
        robotService.find(payload);
        log.info(String.format("Consumed message : %s", message));
    }
}
