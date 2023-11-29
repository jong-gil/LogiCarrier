package com.example.robotservice.controller;

import com.example.robotservice.service.RobotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class RobotController {

    private final RobotService robotService;



    //@Scheduled(cron = "0/2 * * * * ?")          //2초에 1턴
    @GetMapping("/turn")
    public boolean turn() throws JsonProcessingException, InterruptedException{
        return robotService.turn();
    }

    //주문 정보 가능한 피커라인 x3만큼 덱에 저장
    @GetMapping("/pick")
    public ResponseEntity<String> pick() {
        try {
            robotService.pick();
            return ResponseEntity.status(HttpStatus.OK).body("orderDeque is full!");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body("orderDeque is broken!");
        }
    }

    @GetMapping("/push")
    public ResponseEntity<String> push() {
        try {
            robotService.push();
            return ResponseEntity.status(HttpStatus.OK).body("pushDeque is full!");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body("pushDeque is broken!");
        }
    }
    //가능한 라인 만큼 경로 계획하는 api
    @GetMapping("/pick/start")
    public ResponseEntity<String> start(){
        robotService.pickStart();
        log.info("picker start!");
        return ResponseEntity.status(HttpStatus.OK).body("picker started!");
    }
    @GetMapping("/push/start")
    public ResponseEntity<String> pushStart(){
        robotService.pushStart();
        log.info("pusher start!");
        return ResponseEntity.status(HttpStatus.OK).body("pusher started!");
    }
}
