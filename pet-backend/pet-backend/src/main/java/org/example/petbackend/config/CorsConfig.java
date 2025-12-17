package org.example.petbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// Vue 前端和后端分离存在跨域问题，添加跨域配置
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 关键1：允许携带凭据（匹配前端 withCredentials: true）
        config.setAllowCredentials(true);

        // 关键2：用 allowedOriginPatterns 替代 allowedOrigin，支持通配符+凭据
        // Spring 5.3+ 推荐用法，兼容所有前端域名（localhost:8081、生产域名等）
        config.addAllowedOriginPattern("*");

        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有HTTP方法（GET/POST/PUT/DELETE等）
        config.setMaxAge(3600L); // 预检请求缓存1小时，减少OPTIONS请求

        source.registerCorsConfiguration("/**", config); // 对所有接口生效
        return new CorsFilter(source);
    }
}
