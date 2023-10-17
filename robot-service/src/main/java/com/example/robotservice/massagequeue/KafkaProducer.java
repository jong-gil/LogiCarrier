package com.example.robotservice.massagequeue;


import com.example.robotservice.dto.CalculateResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void plan(String topic, CalculateResultDto calculateResultDto) {

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(calculateResultDto);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonInString);
        log.info("Order Producer sent data from the Order microservice: " + calculateResultDto);
    }
    //수정 필요
//    public void result(String topic, Payload payload, CalculateResultDto calculateResultDto) {
//
//        ObjectMapper mapper = new ObjectMapper();
//        StringBuilder st = new StringBuilder();
//        st.append(payload.toString());
//
//
//        kafkaTemplate.send(topic, st.toString());
//        log.info("Order Producer sent data from the Order microservice: " + calculateResultDto);
//    }
    public void requestOrderInfo() {
        kafkaTemplate.send("nextOrders", "from robot-service!");
        log.info("OrderInfo Requested!");
    }
}
