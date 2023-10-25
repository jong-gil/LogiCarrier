package com.example.robotservice.service;

import com.example.robotservice.entity.Payload;

public interface RobotService {
    Boolean findSpace(Payload payload) throws Exception;
    Boolean find(Payload payload) throws Exception;
    void receive(int[] start, Long shelfId)throws Exception;
}
