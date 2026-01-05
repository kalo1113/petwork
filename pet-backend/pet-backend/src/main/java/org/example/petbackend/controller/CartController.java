package org.example.petbackend.controller;

import org.example.petbackend.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 1. 添加商品到购物车
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestParam Integer userId,
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer count) {
        boolean success = cartService.addToCart(userId, productId, count);
        return success ? ResponseEntity.ok("添加购物车成功") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("添加失败");
    }

    // 2. 获取用户购物车列表
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getCartList(@RequestParam Integer userId) {
        List<Map<String, Object>> cartList = cartService.getCartList(userId);
        return ResponseEntity.ok(cartList);
    }

    // 3. 修改商品数量
    @PostMapping("/update/count")
    public ResponseEntity<String> updateCount(
            @RequestParam Integer cartId,
            @RequestParam Integer count) {
        boolean success = cartService.updateCartCount(cartId, count);
        return success ? ResponseEntity.ok("数量修改成功") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("修改失败");
    }

    // 5. 删除购物车项
    @PostMapping("/delete")
    public ResponseEntity<String> deleteCartItem(@RequestParam Integer cartId) {
        boolean success = cartService.deleteCartItem(cartId);
        return success ? ResponseEntity.ok("删除成功") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("删除失败");
    }

}
