package com.example.robotservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;


@Data
public class SpaceDto {
    private long shelfId;
    private int qty;
    private int x;
    private int y;

    public SpaceDto fromObj(Object[] obj){
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setShelfId(((BigInteger)obj[0]).longValue());
        spaceDto.setQty(((BigDecimal)obj[1]).intValue());           //BigDecimal은 Java 언어에서 숫자를 정밀하게 저장하고 표현할 수 있는 유일한 방법
        spaceDto.setX((int) obj[2]);
        spaceDto.setY((int) obj[3]);
        return spaceDto;
    }
}
