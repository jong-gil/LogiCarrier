package com.example.robotservice.massagequeue;


import com.example.robotservice.dto.WorkerReq;
import com.example.robotservice.dto.WorkerRes;
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

    public void toWorker(WorkerRes workerRes, String topic) {

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(workerRes);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonInString);
        log.info("Robot-service to worker-service: " + jsonInString);
    }

    public void requestOrderInfo() {
        kafkaTemplate.send("nextOrder", "from robot-service!");
        log.info("OrderInfo Requested!");
    }
    public void requestPushInfo() {
        kafkaTemplate.send("nextPush", "from robot-service!");
        log.info("push Info Requested!");
    }
}
