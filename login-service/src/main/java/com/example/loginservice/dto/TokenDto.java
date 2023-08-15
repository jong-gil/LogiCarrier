package com.example.loginservice.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class TokenDto {
    // 인증 타입 e.g) Bearer
    private String grantType;

    // 토큰
    private String token;

    // 토큰 만료기한
    private Long expiresAt;
    private String email;

}
