package com.example.loginservice.dto;

import com.example.loginservice.entity.UserType;
import lombok.Data;

@Data
public class UserDto {
    private String username;
    private String email;
    private String password;
    private UserType userType;

    private String userId;
    private String encryptedPassword;

}
