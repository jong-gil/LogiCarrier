package com.example.workerservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class WorkerRes {
    private String workerId;
    private long orderId;
    private long shelfId;
    private String robotId;
    private long turn;
    private ArrayList<ResponseItem> responseItemList;

}
