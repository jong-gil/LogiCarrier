package com.example.workerservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class PickerRes {
    private String pickerId;
    private long orderId;
    private long shelfId;
    private String robotId;
    private long turn;
    private ArrayList<ResponseItem> responseItemList;

}
