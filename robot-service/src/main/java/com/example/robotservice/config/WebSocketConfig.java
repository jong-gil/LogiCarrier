package com.example.robotservice.config;

import com.example.robotservice.handler.RobotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {
    private final RobotHandler robotHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(robotHandler, "/ws/robot/{robotId}").setAllowedOrigins("*")
                .addInterceptors(new CustomHandshakeInterceptor());
    }
}
