package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.User;
import org.example.petbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // ========== 注入头像上传配置 ==========
    @Value("${upload.user-avatar-path}")
    private String userAvatarPath;

    @Value("${upload.avatar-access-path}")
    private String avatarAccessPath;
    // ========== 注册接口 ==========
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
    // ========== 新增：头像上传接口 ==========
    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(
            @RequestParam("userId") Integer userId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1. 校验用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                log.error("头像上传：用户不存在，userId={}", userId);
                return Result.failNotFound("用户不存在"); // 复用404错误格式
            }

            // 2. 校验上传文件
            if (file.isEmpty()) {
                log.error("头像上传：文件为空，userId={}", userId);
                return Result.failParam("上传文件不能为空"); // 复用400错误格式
            }

            // 3. 获取文件后缀并校验格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                log.error("头像上传：文件格式错误，userId={}", userId);
                return Result.failParam("文件格式错误，仅支持jpg/png等格式");
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 过滤非法后缀（可选，增强安全性）
            if (!suffix.matches("\\.(jpg|jpeg|png|gif)$")) {
                return Result.failParam("仅支持jpg/jpeg/png/gif格式的图片");
            }

            // 4. 生成安全的文件名：用户ID_用户昵称.后缀（过滤特殊字符）
            String safeUsername = user.getUsername().replaceAll("[\\\\/:*?\"<>|]", "_");
            String fileName = userId + "_" + safeUsername + suffix;

            // 5. 创建存储目录（不存在则自动创建）
            File dir = new File(userAvatarPath);
            if (!dir.exists()) {
                dir.mkdirs();
                log.info("头像上传：创建存储目录，路径={}", userAvatarPath);
            }

            // 6. 保存文件到本地
            File destFile = new File(userAvatarPath + fileName);
            file.transferTo(destFile);
            log.info("头像上传成功：userId={}，文件名={}", userId, fileName);

            // 7. 更新用户头像字段
            user.setAvatarUrl(fileName);
            userService.updateById(user);

            // 8. 返回头像完整访问URL（前端可直接使用）
            return Result.success(fileName, "头像上传成功");

        } catch (IOException e) {
            log.error("头像上传失败：userId={}，异常信息={}", userId, e.getMessage(), e);
            return Result.fail("头像上传失败：" + e.getMessage());
        }
    }
}
