package com.example.orderservice.massagequeue;


import com.example.orderservice.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, OrderDto orderDto) {
        StringBuilder sb = new StringBuilder();
        orderDto.getResponseItemList().forEach(responseItem -> {
            sb.append(responseItem.getId())
                    .append(":")
                    .append(responseItem.getQty())
                    .append(",");
        });
        sb.deleteCharAt(sb.length() - 1);
        Payload payload = Payload.builder()
                .id(orderDto.getId())
                .responseItemList(orderDto.getResponseItemList())
                .build();


        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonInString);
        log.info("Order Producer sent data from the Order microservice: " + payload);
    }
}
