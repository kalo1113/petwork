package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.Cart;
import java.util.List;
import java.util.Map;

public interface CartService extends IService<Cart> {
    // 1. 添加商品到购物车（支持重复添加时累加数量）
    boolean addToCart(Integer userId, Integer productId, Integer count);

    // 2. 获取用户的购物车列表（关联商品信息）
    List<Map<String, Object>> getCartList(Integer userId);

    // 3. 修改购物车商品数量
    boolean updateCartCount(Integer cartId, Integer count);

    // 5. 删除购物车项
    boolean deleteCartItem(Integer cartId);
}
