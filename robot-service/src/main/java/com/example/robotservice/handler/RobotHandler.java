package com.example.websocketdemo.handler;

import com.example.websocketdemo.dto.MessageReceiveDto;
import com.example.websocketdemo.dto.MessageSendDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;


import java.util.HashMap;


@Component
@Slf4j
@RequiredArgsConstructor
public class RobotHandler implements WebSocketHandler {
    private final static HashMap<String, WebSocketSession> sessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String robotId = session.getAttributes().get("robotId").toString();
        sessionMap.put(robotId, session);

        log.info("{} robot session connected", robotId);
    }

    // 로봇이 보낼 때
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        // 로봇 선택
        String robotId = session.getAttributes().get("robotId").toString();

        MessageReceiveDto messageReceiveDto = MessageReceiveDto.builder()
                .batteryPercent(Integer.parseInt(message.getPayload().toString()))
                .build();

    }

    // 서버가 보낼 때
    public void sendCommand(Long robotId, String route, int positionX, int positionY) throws Exception {
        WebSocketSession session = sessionMap.get(robotId);

        MessageSendDto messageSendDto = MessageSendDto.builder()
                .positionX(positionX)
                .positionY(positionY)
                .route(route)
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
