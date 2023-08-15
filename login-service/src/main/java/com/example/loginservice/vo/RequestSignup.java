package com.example.loginservice.vo;

import com.example.loginservice.entity.UserType;
import lombok.Getter;

@Getter
public class RequestSignup {
    private String username;
    private String email;
    private String password;

    private UserType userType;
}
