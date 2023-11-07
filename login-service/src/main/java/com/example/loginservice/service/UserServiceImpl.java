package com.example.loginservice.service;

import com.example.loginservice.config.CustomModelMapper;
import com.example.loginservice.dto.UserDto;
import com.example.loginservice.entity.UserEntity;
import com.example.loginservice.repository.UserRepository;
import com.example.loginservice.vo.ResponseCreatedUser;
import com.example.loginservice.vo.ResponseUsers;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepo;
    private final CustomModelMapper customModelMapper;

    @Override
    public UserDto getUserByEmail(String email) {
        Optional<UserEntity> userEntity = userRepo.findByEmail(email);

        if (userEntity.isEmpty()) {
            throw new IllegalArgumentException("noEmail exists");
        }
        return customModelMapper.strictMapper().map(userEntity.get(), UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // email로 검색
        Optional<UserEntity> userEntity = userRepo.findByEmail(username);

        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException(username + "Not Found");
        }
        return new User(userEntity.get().getEmail(), userEntity.get().getEncryptedPassword()
                ,true, true, true, true
                // 권한 추가
                , new ArrayList<>());
    }
    @Override
    public List<ResponseUsers> getUsers() {
        ModelMapper mapper = customModelMapper.strictMapper();

        List<ResponseUsers> result = new ArrayList<>();
        List<UserEntity> userList = userRepo.findAll();
        userList.forEach(v -> {
            result.add(mapper.map(v, ResponseUsers.class));
        });
        return result;
    }

    @Override
    @Transactional
    public ResponseCreatedUser createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

//        ModelMapper mapper = new ModelMapper();
//        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        ModelMapper mapper = customModelMapper.strictMapper();

        UserEntity createdUserEntity = mapper.map(userDto, UserEntity.class);
        createdUserEntity.setEncryptedPassword(passwordEncoder.encode(userDto.getPassword()));

        userRepo.save(createdUserEntity);

        return mapper.map(createdUserEntity, ResponseCreatedUser.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        Optional<UserEntity> userEntity = userRepo.findByUserId(userId);
        ModelMapper mapper = customModelMapper.strictMapper();
        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found");
        }
        return mapper.map(userEntity.get(), UserDto.class);
    }

}
