package org.example.petbackend.entity;

import lombok.Data;

@Data
public class Product {
    private Integer id;
    private String mainCategory;
    private String title;
    private String description; // 原字段 desc 改为 description
    private String nowPrice;
    private String oldPrice;
    private String imgPath;
}
