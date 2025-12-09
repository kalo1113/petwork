package org.example.petbackend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NotNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        // 注册 LocalDate 转换器，支持 "yyyy-MM-dd" 格式
        registrar.setDateFormatter(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        registrar.registerFormatters(registry);
    }
}
