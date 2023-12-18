package com.example.workerservice;

import com.example.workerservice.dto.WorkerRes;
import com.example.workerservice.dto.ResponseItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.ArrayList;



@SpringBootTest
class PickerServiceApplicationTests {

    @Autowired
    RedisTemplate<String, String> redisTemplate;


    @Test
    void addToWorker() {
        ZSetOperations<String, String> redisSortedSet = redisTemplate.opsForZSet();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseItem item = ResponseItem.builder()
                .id(1)
                .qty(2)
                .build();
        ArrayList list = new ArrayList<>();
        list.add(item);

        WorkerRes worker = WorkerRes.builder()
                .workerId("1")
                .orderId(1)
                .shelfId(2)
                .robotId("2")
                .turn(2)
                .responseItemList(list)
                .build();
        try{

            String workerToString = objectMapper.writeValueAsString(worker);
            redisSortedSet.add("worker1", workerToString, worker.getTurn());
            redisSortedSet.add("worker1", "2", 1);
            redisSortedSet.add("worker1", "3", 4);
            redisSortedSet.add("worker1", "4", 6);

            String firstPicked = redisSortedSet.popMin("worker1").getValue().toString();
            System.out.println(firstPicked);
            Assertions.assertThat(firstPicked).isEqualTo("2");
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
    }

}
