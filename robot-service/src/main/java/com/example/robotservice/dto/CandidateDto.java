package com.example.robotservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
public class CandidateDto implements Comparable<CandidateDto>{
    private Long stockId;
    private Long id;
    private int x;
    private int y;

    public CandidateDto fromObj(Object[] obj){
        CandidateDto candidateDto = new CandidateDto();
        candidateDto.setStockId(((BigInteger)obj[0]).longValue());
        candidateDto.setId(((BigInteger)obj[1]).longValue());;
        candidateDto.setX((int)obj[2]);
        candidateDto.setY((int)obj[3]);
        return candidateDto;
    }

    @Override
    public int compareTo(CandidateDto o) {
        return this.getX() - o.getX();
    }

    public static CandidateDto build(Long stockId, Long id, int x, int y) {
        CandidateDto candidateDto = new CandidateDto();
        candidateDto.setStockId(stockId);
        candidateDto.setId(id);
        candidateDto.setX(x);
        candidateDto.setY(y);
        return candidateDto;
    }
}
