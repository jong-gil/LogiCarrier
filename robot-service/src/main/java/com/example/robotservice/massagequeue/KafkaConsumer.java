package com.example.robotservice.massagequeue;


import com.example.robotservice.Repoistory.ShelfRepository;
import com.example.robotservice.Repoistory.ShelfStockRepository;
import com.example.robotservice.dto.PickerReq;
import com.example.robotservice.dto.ResponseItem;
import com.example.robotservice.entity.Payload;
import com.example.robotservice.entity.Robot;
import com.example.robotservice.entity.Shelf;
import com.example.robotservice.entity.ShelfStock;
import com.example.robotservice.handler.RobotHandler;
import com.example.robotservice.service.RobotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final RedisTemplate<String, String> redisTemplate;
    private final ShelfStockRepository shelfStockRepository;
    private final ShelfRepository shelfRepository;
    private final RobotHandler robotHandler;
    private final RobotService robotService;

    //요청받은 주문과 아이템 정보 orderInfo로 produce

    @KafkaListener(topics = "targetInfo")           //요청받은 주문과 아이템 정보 orderInfo로 produce
    public void targetInfo(String message) throws IOException, Exception {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        ObjectMapper objectMapper = new ObjectMapper();
        // 주문 정보 받아서 덱에 저장
        listOperations.rightPush("orderDeque", message);

        log.info(String.format("Consumed message : %s", message));
    }
    @Transactional
    @KafkaListener(topics = "pickerReq")            //worker-service로 부터  shelf에서 특정 item 뺀 정보를 받음
    public void pickerReq(String message) throws IOException, Exception {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        ListOperations<String, String> orderDeque = redisTemplate.opsForList();
        ObjectMapper objectMapper = new ObjectMapper();


        Long turn = objectMapper.readValue(valueOperations.get("turn"),Long.class);
        PickerReq pickerReq = objectMapper.readValue(message, PickerReq.class);
        String robotId = pickerReq.getRobotId();
        long shelfId = pickerReq.getShelfId();
        Shelf shelf = shelfRepository.findById(shelfId).orElseThrow();
        ArrayList<ResponseItem> responseItemList = pickerReq.getResponseItemList();
        for (ResponseItem responseItem : responseItemList){
            long itemId = responseItem.getId();
            int qty = responseItem.getQty();

            //뽑은 아이템 수만큼 해당 아이템 제거
            List<ShelfStock> shelfStockList = shelfStockRepository.findByShelfAndStockId(shelf, itemId);
            for (int i = 0; i < qty; i ++){
                ShelfStock shelfStock = shelfStockList.get(i);
                shelfStockRepository.delete(shelfStock);
            }
        }
        //헤당 로봇에게 앞으로 한칸 요청
        Robot robot = objectMapper.readValue((String)hashOperations.get("robot", robotId), Robot.class);
        robotHandler.sendCommand(robot.getRobotId(), "U", robot.getPositionX(), robot.getPositionY(), shelfId, turn,turn+1);//해당 로봇에게 메세지 전달 다음턴에 출발
        if(pickerReq.isOrderStatus()){      //한개의 주문이 처리됬다면 새로운 주문 경로계획
            System.out.println("로직 추가 필요");
        }

        log.info(String.format("Consumed message : %s", message));
    }
}
