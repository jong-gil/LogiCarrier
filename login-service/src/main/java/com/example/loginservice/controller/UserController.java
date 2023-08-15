package com.example.loginservice.controller;

import com.example.loginservice.config.CustomModelMapper;
import com.example.loginservice.dto.UserDto;
import com.example.loginservice.service.UserService;
import com.example.loginservice.vo.RequestLogin;
import com.example.loginservice.vo.RequestSignup;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CustomModelMapper customModelMapper;

    // 전체 회원 조회
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

/*    // 개별 회원 조회
    @GetMapping("/users/{userId}")
    public UserDto getUserByUserId(@PathVariable("userId") String userId) {

        return userService.getUserByUserId(userId);
    }*/

    @GetMapping("/users/{email}")
    public UserDto getUserByEmail(@PathVariable("email") String email) {
        return userService.getUserByEmail(email);
    }

    // 회원가입
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody RequestSignup requestSignup) {
//        ModelMapper mapper = new ModelMapper();
//        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//        ModelMapper mapper = customModelMapper.strictMapper();

        UserDto createdUserDto = customModelMapper.strictMapper().map(requestSignup, UserDto.class);

        UserDto createdUser = userService.createUser(createdUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

/*    // 로그인
    @PostMapping("/users")
    public ResponseEntity<UserDto> login(@RequestBody RequestLogin requestLogin) {
        ModelMapper mapper = customModelMapper.strictMapper();
        UserDto loginUser = mapper.map(requestLogin, UserDto.class);

    }*/


}
