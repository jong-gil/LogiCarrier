package com.example.robotservice;

import com.example.robotservice.Repoistory.RedisRepository;
import com.example.robotservice.controller.RobotController;
import com.example.robotservice.entity.Person;
import com.example.robotservice.Repoistory.PersonRedisRepository;
import com.example.robotservice.entity.Robot;
import com.example.robotservice.entity.RobotStack;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@SpringBootTest
public class RedisRepositoryTest {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PersonRedisRepository repo;
    @Autowired
    private RobotController robotController;
    @Autowired
    private RedisRepository redisRepository;
    @Test
    void test() {
        Person person = new Person("Park", 20);

        // 저장
        repo.save(person);

        // `keyspace:id` 값을 가져옴
        Person found = repo.findById(person.getId()).get();
        System.out.println(found.getName());
        System.out.println(found.getId());
        // Person Entity 의 @RedisHash 에 정의되어 있는 keyspace (people) 에 속한 키의 갯수를 구함
        repo.count();
        // 삭제
        repo.delete(person);
    }

    @Test
    public void redisTemplateTest() {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, String> map = new HashMap<>();
        Robot testRobot = Robot.builder()
                .robotId("1")
                .positionX(0)
                .positionY(1)
                .shelfId(null)
                .battery(100)
                .build();
        map.put("firstName", testRobot.toString());
        hashOperations.putAll("key", map);
        String firstName = hashOperations.get("key", "firstName").toString();
        System.out.println(firstName);

        //redisTemplate.delete("key");
    }
    @Test
    public void robotStackTest() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        Map<String, String> map = new HashMap<>();
        Robot testRobot = Robot.builder()
                .robotId("1")
                .positionX(0)
                .positionY(1)
                .shelfId(null)
                .battery(100)
                .build();
        map.put("1", objectMapper.writeValueAsString(testRobot));
        hashOperations.putAll("robot", map);

        Map<String, String> map2 = new HashMap<>();
        Stack<String> stack = new Stack<>();
        stack.add("1");
        RobotStack robotStack = RobotStack.builder()
                .robotIdStack(stack)
                .build();

        String jsonInString = "";
        try {
            jsonInString = objectMapper.writeValueAsString(robotStack);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        map2.put("0", jsonInString);

        hashOperations.putAll("robotStack", map2);
        RobotStack robotStack1 = objectMapper.readValue((String) hashOperations.get("robotStack", "0"), RobotStack.class);
        for (String id : robotStack1.getRobotIdStack()) {
            System.out.println(id);
            System.out.println(hashOperations.get("robot", id));
        }

        //redisTemplate.delete("robot");
        //redisTemplate.delete("robotStack");
    }

    @Test
    public void spinLock() throws  InterruptedException{
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        ObjectMapper objectMapper = new ObjectMapper();
        IntStream.range(0, 100).forEach(e -> executorService.submit(() ->{
            try {
                while (!redisRepository.lock("lock")) {
                    //SpinLock 방식이 redis 에게 주는 부하를 줄여주기위한 sleep
                    Thread.sleep(100);
                }

                //lock 획득 성공시
                try{
                    Long turn = objectMapper.readValue(redisRepository.get("turn"),Long.class);
                    turn ++;
                    System.out.println(turn);
                    redisRepository.set("turn", objectMapper.writeValueAsString(turn));
                }catch (JsonProcessingException j){
                    throw new RuntimeException(j);
                }finally{
                    //락 해제
                    redisRepository.unlock("lock");
                }
            }catch (InterruptedException ex){
                throw new RuntimeException(ex);
            }finally {
                countDownLatch.countDown();
            }
        }));
        countDownLatch.await();
    }
}