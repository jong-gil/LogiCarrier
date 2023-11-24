package com.example.loginservice.vo;

import com.example.loginservice.entity.UserType;
import lombok.Data;

@Data
public class ResponseCreatedUser {
    private String username;
    private String email;
    private UserType userType;
    private String userId;
}
