package org.example.petbackend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置（整合日期格式化 + 头像静态资源映射）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // ========== 注入头像上传相关配置 ==========
    // 从application.yml读取头像存储路径
    @Value("${upload.user-avatar-path}")
    private String userAvatarPath;


    // ========== 日期格式化配置 ==========
    @Override
    public void addFormatters(@NotNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        // 注册 LocalDate 转换器，支持 "yyyy-MM-dd" 格式
        registrar.setDateFormatter(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        registrar.registerFormatters(registry);
    }

    // ========== 头像静态资源映射配置 ==========
    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        // 配置规则：访问 http://localhost:8080/avatar/xxx.jpg → 映射到本地userphoto文件夹
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + userAvatarPath);
    }
}
