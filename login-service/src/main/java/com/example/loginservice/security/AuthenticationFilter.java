package com.example.loginservice.security;

import com.example.loginservice.dto.TokenDto;
import com.example.loginservice.dto.UserDto;
import com.example.loginservice.service.UserService;
import com.example.loginservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final UserService userService;
    private final Environment env;
    private final TokenProvider tokenProvider;

    public AuthenticationFilter(UserService userService, Environment env, TokenProvider tokenProvider) {
        this.userService = userService;
        this.env = env;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        try {
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

           UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                   creds.getEmail(),
                   creds.getPassword()
           );

            Map<String, String> additionalDetails = new HashMap<>();
            additionalDetails.put("userType", creds.getUserType().toString());
            additionalDetails.put("positionNum", String.valueOf(creds.getPositionNum()));
            authenticationToken.setDetails(additionalDetails);

            return getAuthenticationManager().authenticate(authenticationToken);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        String userName = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDto = userService.getUserByEmail(userName);

        // token detail에서 추가정보 추출하기
        Map<String, String> additionalDetails = (Map<String, String>) authResult.getDetails();
        String userType = additionalDetails.get("userType");
        int positionNum = Integer.parseInt(additionalDetails.get("positionNum"));

        // jwt claim에 넣기
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        claims.put("positionNum", positionNum);

        TokenDto token = tokenProvider.generateToken(authResult, claims);

        response.addHeader("token", token.getToken());
        response.addHeader("userId", userDto.getUserId());

    }
}
