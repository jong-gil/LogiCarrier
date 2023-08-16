package com.example.robotservice.controller;

import com.example.robotservice.dto.Pick;
import com.example.robotservice.massagequeue.PlanProducer;
import com.example.robotservice.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("robot-service")
@RequiredArgsConstructor
public class RobotController {
    private final PlanProducer planProducer;

    @GetMapping("")
    public String find(){
        planProducer.requestOrderInfo();
        return "success";
    }
}
