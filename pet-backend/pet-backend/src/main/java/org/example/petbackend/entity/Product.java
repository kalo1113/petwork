package org.example.petbackend.entity;

import lombok.Data;

@Data
public class Product {
    private Integer id;
    private String mainCategory;
    private String title;
    private String description;
    private String nowPrice;
    private String oldPrice;
    private String imgPath;
}
