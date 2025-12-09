package org.example.petbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 前端访问路径 ↔ 本地图片存储路径
        // 映射前端访问路径 /pet-images/** 到本地图片存储目录
        registry.addResourceHandler("/pet-images/**")
                // 注意：路径末尾必须加 /，且使用 file: 协议
                .addResourceLocations("file:F:/毕设/新建文件夹/pet-backend/petphoto/");
        // 商品图片映射：前端访问路径 /product-images/** 到本地商品图片存储目录
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:F:/毕设/新建文件夹/pet-backend/productimg/");
    }
}
