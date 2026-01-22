package org.example.petbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 用户头像配置
    @Value("${upload.user-avatar-path}")
    private String userAvatarPath;
    @Value("${upload.avatar-access-path}")
    private String avatarAccessPath;

    // 宠物图片配置
    @Value("${upload.pet-photo-path}")
    private String petPhotoPath;
    @Value("${upload.pet-photo-access-path}")
    private String petPhotoAccessPath;

    // 产品图片配置
    @Value("${upload.product-img-path}")
    private String productImgPath;
    @Value("${upload.product-img-access-path}")
    private String productImgAccessPath;

    // 保险图片配置（新增）
    @Value("${upload.insurance-img-path}")
    private String insuranceImgPath;
    @Value("${upload.insurance-img-access-path}")
    private String insuranceImgAccessPath;


    // 日期格式化（保持不变）
    @Override
    public void addFormatters(@NotNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        registrar.registerFormatters(registry);
    }


    // 静态资源映射（核心修正）
    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        // 1. 用户头像映射（classpath路径，无file:前缀）
        log.info("用户头像映射：{} → {}", avatarAccessPath + "**", userAvatarPath);
        registry.addResourceHandler(avatarAccessPath + "**")
                .addResourceLocations(userAvatarPath);

        // 2. 宠物图片映射
        log.info("宠物图片映射：{} → {}", petPhotoAccessPath + "**", petPhotoPath);
        registry.addResourceHandler(petPhotoAccessPath + "**")
                .addResourceLocations(petPhotoPath);

        // 3. 产品图片映射
        log.info("产品图片映射：{} → {}", productImgAccessPath + "**", productImgPath);
        registry.addResourceHandler(productImgAccessPath + "**")
                .addResourceLocations(productImgPath);

        // 4. 保险图片映射（关键：与数据库路径/insurance-img/匹配）
        log.info("保险图片映射：{} → {}", insuranceImgAccessPath + "**", insuranceImgPath);
        registry.addResourceHandler(insuranceImgAccessPath + "**")
                .addResourceLocations(insuranceImgPath);
    }
}
