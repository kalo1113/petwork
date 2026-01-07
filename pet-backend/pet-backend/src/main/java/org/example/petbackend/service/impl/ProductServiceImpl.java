package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.petbackend.entity.Product;
import org.example.petbackend.mapper.ProductMapper;
import org.example.petbackend.service.ProductService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    // 1. 查询所有商品（基于MP的selectList）
    @Override
    public List<Product> getAllProducts() {
        // BaseMapper自带的selectList，传null表示无条件查询所有
        return productMapper.selectList(null);
    }

    // 2. 按分类查询商品（基于MP的条件构造器）
    @Override
    public List<Product> getProductsByCategory(String mainCategory) {
        // LambdaQueryWrapper：MP的条件构造器，避免硬编码字段名
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        // 匹配main_category字段（注意：要和实体类Product的属性名一致）
        queryWrapper.eq(Product::getMainCategory, mainCategory);
        return productMapper.selectList(queryWrapper);
    }

    // 3. 根据ID查询商品（MP自带selectById）
    @Override
    public Product getProductById(Integer productId) {
        // BaseMapper自带的selectById，直接根据主键查询
        return productMapper.selectById(productId);
    }

    // 4. 核心：判断商品ID是否存在（两种实现方式，选其一即可）
    @Override
    public boolean isProductExist(Integer productId) {
        // ========== 方式1：推荐（性能更高，仅查计数） ==========
        LambdaQueryWrapper<Product> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Product::getId, productId);
        // selectCount：MP自带的计数方法，返回符合条件的记录数
        Long count = productMapper.selectCount(countWrapper);
        return count != null && count > 0;

        // ========== 方式2：更简洁（直接查商品是否存在） ==========
        // return productMapper.selectById(productId) != null;
    }
}
