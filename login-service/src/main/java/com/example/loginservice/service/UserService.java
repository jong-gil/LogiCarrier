package com.example.loginservice.service;

import com.example.loginservice.dto.UserDto;
import com.example.loginservice.vo.ResponseCreatedUser;
import com.example.loginservice.vo.ResponseUsers;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService extends UserDetailsService {
    ResponseCreatedUser createUser(UserDto userDto);

    List<ResponseUsers> getUsers();

    UserDto getUserByUserId(String userId);

    UserDto getUserByEmail(String email);
}
