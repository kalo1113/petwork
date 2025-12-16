package org.example.petbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置（日期格式化 + 静态资源映射）
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 注入用户头像配置
    @Value("${upload.user-avatar-path}")
    private String userAvatarPath;
    @Value("${upload.avatar-access-path}")
    private String avatarAccessPath;

    // 注入宠物图片配置
    @Value("${upload.pet-photo-path}")
    private String petPhotoPath;
    @Value("${upload.pet-photo-access-path}")
    private String petPhotoAccessPath;

    // 日期格式化配置
    @Override
    public void addFormatters(@NotNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        registrar.registerFormatters(registry);
    }

    // 静态资源映射
    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        // 1. 用户头像映射
        String avatarResourcePath = "file:" + userAvatarPath;
        log.info("用户头像资源映射：{} → {}", avatarAccessPath + "**", avatarResourcePath);
        registry.addResourceHandler(avatarAccessPath + "**")
                .addResourceLocations(avatarResourcePath);

        // 2. 宠物图片映射
        String petPhotoResourcePath = "file:" + petPhotoPath;
        log.info("宠物图片资源映射：{} → {}", petPhotoAccessPath + "**", petPhotoResourcePath);
        registry.addResourceHandler(petPhotoAccessPath + "**")
                .addResourceLocations(petPhotoResourcePath);
    }
}
