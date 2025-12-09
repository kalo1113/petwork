package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.User;
import org.example.petbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        // 1. 参数校验错误（如邮箱格式、非空）→ 400
        if (bindingResult.hasErrors()) {
            FieldError firstError = bindingResult.getFieldErrors().get(0);
            String errorMsg = firstError.getDefaultMessage();
            log.error("注册参数错误：{}", errorMsg);
            return Result.failParam(errorMsg); // 快捷方法：400+错误信息
        }

        // 2. 邮箱已注册 → 409（冲突）
        User existingUser = userService.getUserByEmail(user.getEmail());
        if (existingUser != null) {
            log.error("邮箱已注册：{}", user.getEmail());
            return Result.failConflict("该邮箱已被注册"); // 快捷方法：409+错误信息
        }

        // 3. 数据库插入失败 → 500（默认）
        boolean success = userService.save(user);
        if (!success) {
            return Result.fail("注册失败，请稍后重试");
        }

        // 4. 注册成功 → 200+数据+提示
        return Result.success(user, "注册成功");
    }

    // 登录接口
    @PostMapping("/login")
    public Result<User> loginUser(@RequestBody User user) {
        // 从user中获取email和password
        String email = user.getEmail();
        String password = user.getPassword();
        // 1. 用户不存在 → 404
        User existingUser = userService.getUserByEmail(email);
        if (existingUser == null) {
            return Result.failNotFound("用户不存在"); // 快捷方法：404+错误信息
        }

        // 2. 密码错误 → 401（认证失败）
        if (!password.equals(existingUser.getPassword())) {
            return Result.failAuth("密码错误"); // 快捷方法：401+错误信息
        }

        // 3. 登录成功 → 200+数据+提示
        return Result.success(existingUser, "登录成功");
    }

    // 3. 根据ID查询用户信息
    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable Integer userId) {
        User user = userService.getById(userId);
        if (user != null) {
            // 传入User对象，泛型自动匹配
            return Result.success(user, "查询成功");
        }
        return Result.fail("用户不存在");
    }
}
