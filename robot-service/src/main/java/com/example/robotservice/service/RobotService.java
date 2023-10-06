package com.example.robotservice.service;

import com.example.robotservice.dto.CandidateDto;
import com.example.robotservice.dto.Payload;
import com.example.robotservice.dto.Pick;
import com.example.robotservice.jpa.Shelf;

import java.util.HashMap;
import java.util.List;

public interface RobotService {
    HashMap<Long, Pick> find(Payload payload) throws Exception;
}
