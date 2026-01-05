package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.Cart;
import org.example.petbackend.entity.Product;
import org.example.petbackend.mapper.CartMapper;
import org.example.petbackend.mapper.ProductMapper;
import org.example.petbackend.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductMapper productMapper;

    public CartServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    // 1. 添加商品到购物车（手动赋值时间 + 商品存在性校验）
    @Override
    @Transactional(rollbackFor = Exception.class) // 明确回滚所有异常
    public boolean addToCart(Integer userId, Integer productId, Integer count) {
        // 第一步：校验商品是否存在（核心！避免插入无效商品）
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return false;
        }

        // 第二步：查询是否已添加该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);
        Cart existingCart = baseMapper.selectOne(wrapper);

        if (existingCart != null) {
            // 已存在：累加数量 + 手动更新时间
            existingCart.setProductCount(existingCart.getProductCount() + count);
            existingCart.setUpdateTime(LocalDateTime.now()); // 手动赋值
            return baseMapper.updateById(existingCart) > 0;
        } else {
            // 不存在：新增 + 手动赋值时间（绕过自动填充）
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setProductId(productId);
            newCart.setProductCount(count);
            newCart.setCreateTime(LocalDateTime.now()); // 手动赋值
            newCart.setUpdateTime(LocalDateTime.now()); // 手动赋值
            return baseMapper.insert(newCart) > 0;
        }
    }

    // 2. 获取购物车列表（字段名完全匹配前端）
    @Override
    public List<Map<String, Object>> getCartList(Integer userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> cartList = baseMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) continue;

            Map<String, Object> cartMap = new HashMap<>();
            // 完全匹配前端期望的字段名（下划线 + 驼峰兼容）
            cartMap.put("cart_id", cart.getCartId());
            cartMap.put("product_id", product.getId());
            cartMap.put("title", product.getTitle()); // 前端期望title
            cartMap.put("old_price", product.getOldPrice()); // 前端期望old_price
            cartMap.put("now_price", product.getNowPrice()); // 前端期望now_price
            cartMap.put("img_path", product.getImgPath()); // 前端期望img_path
            cartMap.put("product_count", cart.getProductCount()); // 数量
            result.add(cartMap);
        }
        return result;
    }

    // 3. 修改购物车商品数量
    @Override
    public boolean updateCartCount(Integer cartId, Integer count) {
        if (count < 1) return false;
        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setProductCount(count);
        cart.setUpdateTime(LocalDateTime.now()); // 手动更新时间
        return baseMapper.updateById(cart) > 0;
    }

    // 4. 删除购物车项
    @Override
    public boolean deleteCartItem(Integer cartId) {
        return baseMapper.deleteById(cartId) > 0;
    }
}
