package com.example.loginservice.security;

import com.example.loginservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ObjectPostProcessor<Object> objectPostProcessor;
    private final Environment env;
    private final TokenProvider tokenProvider;


    private static final String[] permitAllArray = {
            "/users/**",
            "/login/**",
            "/ws/**"
    };

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests().antMatchers(permitAllArray)
                .permitAll()
                .and()
                .addFilter(getAuthenticationFilter());

        return http.build();
    }

    // select password from users where email=?
    // db_pwd (encrypted_ == input_pwd(encrypted)
    public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
        return auth.build();
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(userService, env, tokenProvider);

        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        authenticationFilter.setAuthenticationManager(authenticationManager(builder));
        return authenticationFilter;
    }
}
