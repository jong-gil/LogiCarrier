package com.example.workerservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class WorkerReq {
    private String workerId;
    private String robotId;
    private long shelfId;
    private boolean orderStatus;
    private ArrayList<ResponseItem> responseItemList;

}
