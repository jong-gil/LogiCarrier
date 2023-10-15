package com.example.robotservice.service;

import com.example.robotservice.dto.Payload;

public interface RobotService {
    Boolean find(Payload payload) throws Exception;
}
