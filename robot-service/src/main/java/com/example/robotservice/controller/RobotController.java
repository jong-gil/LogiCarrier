package com.example.robotservice.controller;

import com.example.robotservice.entity.Payload;
import com.example.robotservice.massagequeue.KafkaProducer;
import com.example.robotservice.service.RobotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("robot-service")
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class RobotController {
    private final KafkaProducer kafkaProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final RobotService robotService;

    @GetMapping("")
    public String find(){
        kafkaProducer.requestOrderInfo();
        return "success";
    }

    @Scheduled(cron = "0/2 * * * * ?")          //2초에 1턴
    @GetMapping("/turn")
    public boolean turn() throws JsonProcessingException {
        ValueOperations <String, String> valueOperations = redisTemplate.opsForValue();
        ObjectMapper objectMapper = new ObjectMapper();
        Long turn = objectMapper.readValue(valueOperations.get("turn"),Long.class);
        turn ++;

        valueOperations.set("turn", objectMapper.writeValueAsString(turn));
        log.info("turn increase!"+ turn.toString());
        return true;
    }

    //주문 정보 가능한 피커라인 x3만큼 덱에 저장
    @GetMapping("/full")
    public String full() {
        ValueOperations <String, String> valueOperations = redisTemplate.opsForValue();
        ObjectMapper objectMapper = new ObjectMapper();

        String workerBit = valueOperations.get("workerBit");
        String progressBit = valueOperations.get("progressBit");
        int ablePicker = 0;                                                //redis에 캐쉬된 가동가능한 picker찾기
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '0' && progressBit.charAt(i) == '0'){
                ablePicker ++;
            }
        }

        for (int i = 0; i < ablePicker * 3; i++) {
            kafkaProducer.requestOrderInfo();
        }

        return "orderDeque is full!";
    }
    //가능한 라인 만큼 경로 계획하는 api
    @GetMapping("/start")
    public String start() throws Exception {
        ValueOperations <String, String> valueOperations = redisTemplate.opsForValue();
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        ObjectMapper objectMapper = new ObjectMapper();

        String workerBit = valueOperations.get("workerBit");
        String progressBit = valueOperations.get("progressBit");
        int ablePicker = 0;
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '0' && progressBit.charAt(i) == '0'){
                ablePicker ++;
            }
        }
        int disable = 0;
        for (int i = 0; i < ablePicker; i++) {
            if( disable > ablePicker * 2){
                break;
            }
            String leftPop = listOperations.leftPop("orderDeque");
            Payload payload = objectMapper.readValue(leftPop, Payload.class);
            if(! robotService.find(payload)){
                listOperations.rightPush("orderDeque", leftPop);
                i --;
                disable++;
            }
        }

        return "started!";
    }
}
