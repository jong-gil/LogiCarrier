package com.example.loginservice.security;

import com.example.loginservice.dto.TokenDto;
import com.example.loginservice.dto.UserDto;
import com.example.loginservice.repository.UserRepository;
import com.example.loginservice.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {
    private final Key key;
    private final UserService userService;
    private final UserRepository userRepo;
    private final Environment env;

    public TokenProvider(UserService userService, UserRepository userRepo, Environment env) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.env = env;
        byte[] keyBytes = Decoders.BASE64URL.decode(env.getProperty("token.secret"));
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto generateToken(Authentication authentication) {
        String userName = ((User) authentication.getPrincipal()).getUsername();
        UserDto userDetails = userService.getUserByEmail(userName);
        Date expirationDate = new Date(System.currentTimeMillis() +
                Long.parseLong(Objects.requireNonNull(env.getProperty("token.expiration_time"))));

        String token = Jwts.builder()
                .setSubject(userDetails.getUserId())
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenDto.builder()
                .grantType("Bearer")
                .token(token)
                .expiresAt(expirationDate.getTime())
                .email(userName)
                .build();
    }

}
