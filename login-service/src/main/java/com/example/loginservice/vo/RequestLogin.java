package com.example.loginservice.vo;


import com.example.loginservice.entity.UserType;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Data
public class RequestLogin {
    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private UserType userType;

    private int positionNum;
}
