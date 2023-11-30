package com.example.loginservice.controller;

import com.example.loginservice.config.CustomModelMapper;
import com.example.loginservice.dto.UserDto;
import com.example.loginservice.service.UserService;
import com.example.loginservice.vo.RequestSignup;
import com.example.loginservice.vo.ResponseCreatedUser;
import com.example.loginservice.vo.ResponseUsers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final CustomModelMapper customModelMapper;
    private final Environment env;

    @GetMapping("/health_check")
    public String status(HttpServletRequest request) {
        log.info("Server port={}", request.getServerPort());
        return String.format("It's working in Worker Service on PORT %s"
                , env.getProperty("local.server.port"));
    }

    // 전체 회원 조회
    @GetMapping("/users")
    public List<ResponseUsers> getUsers() {
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
    public ResponseEntity<ResponseCreatedUser> createUser(@RequestBody RequestSignup requestSignup) {

//        ModelMapper mapper = new ModelMapper();
//        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//        ModelMapper mapper = customModelMapper.strictMapper();

        UserDto createdUserDto = customModelMapper.strictMapper().map(requestSignup, UserDto.class);
      
        ResponseCreatedUser createdUser = userService.createUser(createdUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

}
