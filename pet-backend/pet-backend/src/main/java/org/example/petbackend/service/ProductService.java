package org.example.petbackend.service;

import org.example.petbackend.entity.Product;
import java.util.List;

// 方案1：不依赖MP（推荐，保持原有接口风格）
public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String mainCategory);
    // 新增：根据ID查询商品（适配订单详情接口）
    Product getProductById(Integer productId);
    // ========== 新增：判断商品ID是否存在 ==========
    boolean isProductExist(Integer productId);
}
