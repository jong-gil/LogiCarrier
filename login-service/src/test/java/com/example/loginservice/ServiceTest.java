package com.example.loginservice;

import com.example.loginservice.entity.UserEntity;
import com.example.loginservice.entity.UserType;
import com.example.loginservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
// 자동으로 설정된 in-memory 방식 DB 설정을 기존 DB로 이용
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ServiceTest {
    @Autowired
    private UserRepository userRepo;

    @Test
    void loadUser() {
        UserEntity userEntity = UserEntity.builder()
                .email("halo@naver.com")
                .password("abcd123")
                .userId("asdfasdf")
                .userType(UserType.manager)
                .encryptedPassword("hello")
                .username("ho")
                .build();

        userRepo.save(userEntity);

        Optional<UserEntity> target = userRepo.findByEmail("halo@naver.com");
        if (target.isPresent()) {
            Assertions.assertEquals(target.get().getEmail(), "halo@naver.com");
        }

    }
}