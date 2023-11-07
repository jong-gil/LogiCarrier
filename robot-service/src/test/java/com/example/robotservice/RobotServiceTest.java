package com.example.robotservice;

import com.example.robotservice.dto.Road;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;

@SpringBootTest
public class RobotServiceTest {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final String[][] field = new String[9][13];
    private final HashMap<String, Road> roadHash = new HashMap<>();
    @Test
    public void field() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        HashOperations<String, String,String> hashOperations = redisTemplate.opsForHash();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 13; j++) {
                field[i][j] = "";
            }
        }
        //field 초기값
        field[0][2] = "0";
        field[0][5] = "1";
        field[0][8] = "2";
        field[1][2] = "3";
        field[2][2] = "3";
        field[3][2] = "3";
        field[4][2] = "6";
        field[5][2] = "11";
        field[6][2] = "11";
        field[7][2] = "11";
        field[8][2] = "14";
        field[1][5] = "4";
        field[2][5] = "4";
        field[3][5] = "4";
        field[4][5] = "8";
        field[5][5] = "12";
        field[6][5] = "12";
        field[7][5] = "12";
        field[8][5] = "16";
        field[1][8] = "5";
        field[2][8] = "5";
        field[3][8] = "5";
        field[4][8] = "10";
        field[5][8] = "13";
        field[6][8] = "13";
        field[7][8] = "13";
        field[8][8] = "18";
        field[4][3] = "7";
        field[4][4] = "7";
        field[4][6] = "9";
        field[4][7] = "9";
        field[4][9] = "25";
        field[4][10] = "25";
        field[8][3] = "15";
        field[8][4] = "15";
        field[8][6] = "17";
        field[8][7] = "17";
        field[8][9] = "26";
        field[8][10] = "26";
        field[0][11] = "24";
        field[1][11] = "32";
        field[2][11] = "32";
        field[3][11] = "32";
        field[4][11] = "28";
        field[5][11] = "29";
        field[6][11] = "29";
        field[7][11] = "29";
        field[8][11] = "30";

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 13; j++) {
                StringBuilder sb = new StringBuilder();
                String key = sb.append(i).append(".").append(j).toString();
                hashOperations.put("field",key,field[i][j]);
            }
        }

        for(int i = 0; i< 33; i++){
            Road road = new Road();
            road.setCorner(false);
            road.setSchedule(new ArrayList<>());
            roadHash.put(String.valueOf(i), road);
        }
        roadHash.get("0").setCorner(true);
        roadHash.get("1").setCorner(true);
        roadHash.get("2").setCorner(true);
        roadHash.get("24").setCorner(true);
        roadHash.get("6").setCorner(true);
        roadHash.get("8").setCorner(true);
        roadHash.get("10").setCorner(true);
        roadHash.get("28").setCorner(true);
        roadHash.get("14").setCorner(true);
        roadHash.get("16").setCorner(true);
        roadHash.get("18").setCorner(true);
        roadHash.get("30").setCorner(true);

        for(int i = 0; i< 33; i++){
            hashOperations.put("roadHash", String.valueOf(i), objectMapper.writeValueAsString(roadHash.get(String.valueOf(i))));
        }

    }
}
