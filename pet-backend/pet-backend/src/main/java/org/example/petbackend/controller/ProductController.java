package org.example.petbackend.controller;

import org.example.petbackend.entity.Product;
import org.example.petbackend.service.ProductService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin  // 解决跨域问题
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping("/list")
    public List<Product> getProductList() {
        return productService.getAllProducts();
    }

    // 查询所有商品
    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // 按分类查询商品（示例：mainCategory 为“狗狗主粮”）
    @GetMapping("/category")
    public List<Product> getProductsByCategory(@RequestParam String mainCategory) {
        return productService.getProductsByCategory(mainCategory);
    }
}
