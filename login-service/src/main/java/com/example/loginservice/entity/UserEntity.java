package com.example.loginservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Email
    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(unique = true)
    private String userId;

    private String encryptedPassword;
}
