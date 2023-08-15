package com.example.loginservice.service;

import com.example.loginservice.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserDto createUser(UserDto userDto);

    List<UserDto> getUsers();

    UserDto getUserByUserId(String userId);

    UserDto getUserByEmail(String email);
}
