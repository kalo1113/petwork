package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.petbackend.entity.Product;
import org.example.petbackend.mapper.ProductMapper;
import org.example.petbackend.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Resource
    private ProductMapper productMapper;

    // 查询所有商品（等价于原 findAll()）
    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectList(null); // 无查询条件 → 查询所有
    }

    // 按分类查询商品（等价于原 findByCategory()）
    @Override
    public List<Product> getProductsByCategory(String mainCategory) {
        // 构建查询条件（where main_category = ?）
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("main_category", mainCategory); // 字段名与数据库一致（main_category）
        return productMapper.selectList(queryWrapper);
    }
}
