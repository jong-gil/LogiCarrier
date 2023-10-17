package com.example.robotservice.service;

import com.example.robotservice.entity.Payload;

public interface RobotService {
    Boolean find(Payload payload) throws Exception;
}
