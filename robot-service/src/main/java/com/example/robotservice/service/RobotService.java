package com.example.robotservice.service;

import com.example.robotservice.entity.Payload;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface RobotService {
    boolean turn() throws JsonProcessingException, InterruptedException;
    void pick();
    void push();
    void pickStart();
    void pushStart();
    Boolean findSpace(Payload payload) throws Exception;
    Boolean find(Payload payload) throws Exception;
    void receive(int[] start, Long shelfId)throws Exception;
    void turnLock();
    void turnUnlock();
    void bitLock();
    void bitUnlock();
    void fieldLock();
    void fieldUnlock();

}
