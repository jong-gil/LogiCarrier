package com.example.robotservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class WorkerReq {
    private String robotId;
    private long shelfId;
    private boolean orderStatus;
    private ArrayList<ResponseItem> responseItemList;

}
