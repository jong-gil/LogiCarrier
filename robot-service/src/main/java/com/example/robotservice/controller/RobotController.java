package com.example.robotservice.controller;

import com.example.robotservice.massagequeue.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("robot-service")
@RequiredArgsConstructor
public class RobotController {
    private final KafkaProducer kafkaProducer;

    @GetMapping("")
    public String find(){
        kafkaProducer.requestOrderInfo();
        return "success";
    }
}
