package com.example.robotservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class WorkerRes {
    private String pickerId;
    private long orderId;
    private long shelfId;
    private String robotId;
    private long turn;
    private ArrayList<ResponseItem> responseItemList;       //선반에서  뽑는 아이템

}
