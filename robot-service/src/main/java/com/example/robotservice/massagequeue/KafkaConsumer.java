package com.example.robotservice.massagequeue;


import com.example.robotservice.dto.Payload;
import com.example.robotservice.service.RobotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final RobotService robotService;
    //요청받은 주문과 아이템 정보 orderInfo로 produce
    private final KafkaProducer kafkaProducer;
    @KafkaListener(topics = "targetInfo")
    public void targetInfo(String message) throws IOException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Payload> payloadList = objectMapper.readValue(message, List.class);
        Deque<Payload> payloadDeque = new ArrayDeque<>();
        for(Payload payload : payloadList){
            payloadDeque.add(payload);
        }
        //불가능하면 덱 마지막으로 넣기
        while (! payloadDeque.isEmpty()){
            Payload payload = payloadDeque.peek();
            if (! robotService.find(payload)){
                payloadDeque.addLast(payload);
            }
        }
        //다처리 하면 다시 호출
        kafkaProducer.requestOrderInfo();
        log.info(String.format("Consumed message : %s", message));
    }
}
