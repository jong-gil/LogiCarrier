package com.example.robotservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@Builder
public class PickerRes {
    private String pickerId;
    private long orderId;
    private long shelfId;
    private String robotId;
    private long turn;
    private ArrayList<ResponseItem> responseItemList;       //선반에서  뽑는 아이템

}
