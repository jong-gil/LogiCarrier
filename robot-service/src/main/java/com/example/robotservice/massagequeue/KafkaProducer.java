package com.example.robotservice.massagequeue;


import com.example.robotservice.dto.CalculateResultDto;
import com.example.robotservice.dto.PickerRes;
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

    public void pickerRes(PickerRes pickerRes) {

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(pickerRes);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        kafkaTemplate.send("pickerRes", jsonInString);
        log.info("Robot-service to worker-service: " + jsonInString);
    }

    public void requestOrderInfo() {
        kafkaTemplate.send("nextOrders", "from robot-service!");
        log.info("OrderInfo Requested!");
    }
}
