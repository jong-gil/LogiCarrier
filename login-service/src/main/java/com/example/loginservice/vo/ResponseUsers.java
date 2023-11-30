package com.example.loginservice.vo;

import com.example.loginservice.entity.UserType;
import lombok.Data;

@Data
public class ResponseUsers {
    private String username;
    private String email;
    private UserType userType;

}
