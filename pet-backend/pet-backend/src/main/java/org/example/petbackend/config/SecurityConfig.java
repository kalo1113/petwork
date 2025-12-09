package org.example.petbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 6.x 部分版本需要显式添加，确保生效
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（必须，否则 POST 请求可能被拦截）
                .csrf(csrf -> csrf.disable())
                // 关闭 HTTP Basic 认证（避免弹出登录框）
                .httpBasic(httpBasic -> httpBasic.disable())
                // 关闭表单登录（无需登录页面）
                .formLogin(formLogin -> formLogin.disable())
                // 允许所有请求访问
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}