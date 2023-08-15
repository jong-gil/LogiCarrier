package com.example.loginservice.vo;


import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Data
public class RequestLogin {
    @NotNull
    private String email;

    @NotNull
    private String password;
}
