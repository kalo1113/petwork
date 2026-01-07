package org.example.petbackend.controller;

import org.example.petbackend.entity.Product;
import org.example.petbackend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/products")
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

    // ========== 新增：校验商品ID是否存在（供前端加购前调用） ==========
    @GetMapping("/checkExist")
    public ResponseEntity<Boolean> checkProductExist(@RequestParam Integer productId) {
        // 调用Service层判断商品是否存在
        boolean isExist = productService.isProductExist(productId);
        return ResponseEntity.ok(isExist);
    }

    // ========== 原有接口：根据ID查询商品（适配订单详情） ==========
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer productId) {
        Product product = productService.getProductById(productId);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }
}
