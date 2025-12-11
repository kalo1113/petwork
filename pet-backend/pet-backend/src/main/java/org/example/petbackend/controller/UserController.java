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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${server.domain:http://localhost:8080}")
    private String serverDomain;

    // ========== 注入头像上传配置 ==========
    @Value("${upload.user-avatar-path}")
    private String userAvatarPath;
    @Value("${upload.avatar-access-path}")
    private String avatarAccessPath;

    // ========== 注册接口（适配User实体） ==========
    @PostMapping("/register")
    public Result<User> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        // 1. 参数校验错误（如邮箱格式、非空）→ 400
        if (bindingResult.hasErrors()) {
            FieldError firstError = bindingResult.getFieldErrors().get(0);
            String errorMsg = firstError.getDefaultMessage();
            log.error("注册参数错误：{}", errorMsg);
            return Result.failParam(errorMsg);
        }

        // 2. 邮箱已注册 → 409（冲突）
        User existingUser = userService.getOne(new QueryWrapper<User>().eq("email", user.getEmail()));
        if (existingUser != null) {
            log.error("邮箱已注册：{}", user.getEmail());
            return Result.failConflict("该邮箱已被注册");
        }

        // 3. 初始化默认值（适配User实体字段）
        if (user.getAccountBalance() == null) {
            user.setAccountBalance(BigDecimal.ZERO); // 余额默认0
        }
        user.setIsDefaultAddress(0); // 默认地址0-否

        // 4. 数据库插入
        boolean success = userService.save(user);
        if (!success) {
            return Result.fail("注册失败，请稍后重试");
        }

        return Result.success(user, "注册成功");
    }

    // ========== 登录接口（适配User实体） ==========
    @PostMapping("/login")
    public Result<User> loginUser(@RequestBody User user) {
        String email = user.getEmail();
        String password = user.getPassword();

        // 1. 校验参数
        if (email == null || password == null) {
            return Result.failParam("邮箱和密码不能为空");
        }

        // 2. 查询用户
        User existingUser = userService.getOne(new QueryWrapper<User>().eq("email", email));
        if (existingUser == null) {
            return Result.failNotFound("用户不存在");
        }

        // 3. 密码校验
        if (!password.equals(existingUser.getPassword())) {
            return Result.failAuth("密码错误");
        }

        // 4. 更新最后登录时间（适配User实体）
        existingUser.setLastLoginTime(LocalDateTime.now());
        userService.updateById(existingUser);

        // 5. 拼接头像完整URL
        if (existingUser.getAvatarUrl() != null && !existingUser.getAvatarUrl().startsWith("http")) {
            existingUser.setAvatarUrl(serverDomain + avatarAccessPath + existingUser.getAvatarUrl());
        }

        return Result.success(existingUser, "登录成功");
    }

    // ========== 根据ID查询用户 ==========
    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable Integer userId) {
        User user = userService.getById(userId);
        if (user != null) {
            // 拼接头像URL
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().startsWith("http")) {
                user.setAvatarUrl(serverDomain + avatarAccessPath + user.getAvatarUrl());
            }
            return Result.success(user, "查询成功");
        }
        return Result.fail("用户不存在");
    }

    // ========== 头像上传接口（适配User实体） ==========
    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(
            @RequestParam("userId") Integer userId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1. 校验用户
            User user = userService.getById(userId);
            if (user == null) {
                log.error("头像上传：用户不存在，userId={}", userId);
                return Result.failNotFound("用户不存在");
            }

            // 2. 校验文件
            if (file.isEmpty()) {
                return Result.failParam("上传文件不能为空");
            }

            // 3. 处理文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.failParam("文件格式错误，仅支持jpg/png等格式");
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!suffix.matches("\\.(jpg|jpeg|png|gif)$")) {
                return Result.failParam("仅支持jpg/jpeg/png/gif格式的图片");
            }

            // 4. 生成安全文件名
            String safeUsername = user.getUsername().replaceAll("[\\\\/:*?\"<>|]", "_");
            String fileName = userId + "_" + safeUsername + "_" + System.currentTimeMillis() + suffix;

            // 5. 创建目录并保存文件
            File dir = new File(userAvatarPath);
            if (!dir.exists()) {
                dir.mkdirs();
                log.info("创建头像目录：{}", userAvatarPath);
            }
            File destFile = new File(dir, fileName);
            file.transferTo(destFile);

            // 6. 更新用户头像字段（适配User实体的avatarUrl）
            user.setAvatarUrl(fileName);
            userService.updateById(user);

            // 7. 返回完整URL
            String fullAvatarUrl = serverDomain + avatarAccessPath + fileName;
            return Result.success(fullAvatarUrl, "头像上传成功");

        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.fail("头像上传失败：" + e.getMessage());
        }
    }

    // ========== 修改昵称接口（适配User实体） ==========
    @PostMapping("/updateNickname")
    public Result<User> updateNickname(@RequestBody User user) {
        // 1. 校验参数
        if (user.getUserId() == null) {
            return Result.failParam("用户ID不能为空");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.failParam("昵称不能为空");
        }
        if (user.getUsername().length() > 10) {
            return Result.failParam("昵称长度不能超过10个字符");
        }

        // 2. 查询用户
        User existingUser = userService.getById(user.getUserId());
        if (existingUser == null) {
            return Result.failNotFound("用户不存在");
        }

        // 3. 更新昵称（适配User实体的username）
        existingUser.setUsername(user.getUsername().trim());
        boolean success = userService.updateById(existingUser);

        if (success) {
            // 拼接头像URL返回
            if (existingUser.getAvatarUrl() != null && !existingUser.getAvatarUrl().startsWith("http")) {
                existingUser.setAvatarUrl(serverDomain + avatarAccessPath + existingUser.getAvatarUrl());
            }
            return Result.success(existingUser, "昵称修改成功");
        } else {
            return Result.fail("昵称修改失败，请稍后重试");
        }
    }

    // ========== 修改密码接口（适配User实体） ==========
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody Map<String, Object> paramMap) {
        // 1. 解析参数（解决User实体无oldPassword/newPassword字段问题）
        Integer userId = (Integer) paramMap.get("userId");
        String oldPassword = (String) paramMap.get("oldPassword");
        String newPassword = (String) paramMap.get("newPassword");

        // 2. 校验参数
        if (userId == null) {
            return Result.failParam("用户ID不能为空");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.failParam("原密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.failParam("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.failParam("新密码长度不能少于6位");
        }

        // 3. 查询用户
        User existingUser = userService.getById(userId);
        if (existingUser == null) {
            return Result.failNotFound("用户不存在");
        }

        // 4. 校验原密码
        if (!oldPassword.equals(existingUser.getPassword())) {
            return Result.failAuth("原密码错误");
        }

        // 5. 校验新密码与原密码是否一致
        if (oldPassword.equals(newPassword)) {
            return Result.failParam("新密码不能与原密码相同");
        }

        // 6. 更新密码（适配User实体的password）
        existingUser.setPassword(newPassword.trim());
        boolean success = userService.updateById(existingUser);

        if (success) {
            return Result.success("", "密码修改成功");
        } else {
            return Result.fail("密码修改失败，请稍后重试");
        }
    }

    // ========== 补充：获取用户ByEmail方法（适配UserService） ==========
    public User getUserByEmail(String email) {
        return userService.getOne(new QueryWrapper<User>().eq("email", email));
    }
}
