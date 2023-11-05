package com.example.robotservice.handler;

import com.example.robotservice.dto.MessageReceiveDto;
import com.example.robotservice.dto.MessageSendDto;
import com.example.robotservice.entity.Robot;
//import com.example.robotservice.service.RobotService;
import com.example.robotservice.event.RobotEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.HashMap;


@Component
@Slf4j
@RequiredArgsConstructor
public class RobotHandler implements WebSocketHandler {
    private final static HashMap<String, WebSocketSession> sessionMap = new HashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String robotId = session.getAttributes().get("robotId").toString();
        sessionMap.put(robotId, session);

        log.info("{} robot session connected", robotId);
    }

    // 로봇이 보낼 때
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        SetOperations<String, String> setOperations = stringRedisTemplate.opsForSet();

        // 로봇 선택
        String robotId = session.getAttributes().get("robotId").toString();
        //받은 정보 레디스로
        MessageReceiveDto messageReceiveDto = objectMapper.readValue((String) message.getPayload(), MessageReceiveDto.class);
        Robot robot = objectMapper.readValue((String) hashOperations.get("robot", robotId), Robot.class);
        robot.setBattery(messageReceiveDto.getBatteryPercent());
        robot.setPositionX(messageReceiveDto.getX());
        robot.setPositionY(messageReceiveDto.getY());
        hashOperations.put("robot", robotId, objectMapper.writeValueAsString(robot));
        StringBuilder sb = new StringBuilder();
        sb.append(robot.getPositionX());
        sb.append(robot.getPositionY());
        //돌아가는 출발지면 돌아가는 경로 탐색
        if(setOperations.isMember("readyToGo", sb.toString())){
            int[] start = new int[]{robot.getPositionX(), robot.getPositionY()};
            Long shelfId = robot.getShelfId();
            applicationEventPublisher.publishEvent(new RobotEvent(start, shelfId));         //robotService.receive(start, shelfId) 이벤트

        }

    }

    // 서버가 보낼 때
    public void sendCommand(String robotId, String route, int positionX, int positionY, long shelfId, long now, long turn) throws Exception {
        WebSocketSession session = sessionMap.get(robotId);

        MessageSendDto messageSendDto = MessageSendDto.builder()
                .positionX(positionX)
                .positionY(positionY)
                .route(route)
                .now(now)
                .turn(turn)
                .shelfId(shelfId)
                .build();

        WebSocketMessage<?> message = new TextMessage(messageSendDto.toString());
        session.sendMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // WebSocket 통신 중 에러 발생 시 실행
        System.out.println("error");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // 클라이언트 연결 종료 시 실행
        String robotId = session.getAttributes().get("robotId").toString();
        sessionMap.remove(robotId);
        log.info("{} robot session disconnected", robotId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
