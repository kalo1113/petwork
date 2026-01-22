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
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
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

    // ========== 注册接口（适配User实体，新增待入账金额初始化） ==========
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

        // 3. 初始化默认值（适配User实体字段，新增待入账金额初始化）
        if (user.getAccountBalance() == null) {
            user.setAccountBalance(BigDecimal.ZERO); // 余额默认0
        }
        if (user.getPendingAmount() == null) {
            user.setPendingAmount(BigDecimal.ZERO); // 待入账金额默认0
        }

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

    // ========== 头像上传接口（修复文件写入问题） ==========
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
                return Result.failParam("文件格式错误，仅支持jpg/png/gif格式");
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!suffix.matches("\\.(jpg|jpeg|png|gif)$")) {
                return Result.failParam("仅支持jpg/jpeg/png/gif格式的图片");
            }

            // 4. 生成安全文件名（UUID替代时间戳，避免并发重复）
            String safeUsername = user.getUsername() == null
                    ? "unknown"
                    : user.getUsername().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
            String fileName = userId + "_" + safeUsername + "_" + UUID.randomUUID().toString().replace("-", "") + suffix;

            // 5. 解析上传目录（核心修复：兼容classpath路径，解决ResourceUtils坑）
            File dir;
            try {
                if (userAvatarPath.startsWith("classpath:")) {
                    String classpathRelativePath = userAvatarPath.replace("classpath:", "");
                    ClassPathResource resource = new ClassPathResource(classpathRelativePath);

                    // 处理路径编码问题（Windows下%20等）
                    String realPath = resource.getURL().getPath();
                    realPath = java.net.URLDecoder.decode(realPath, "UTF-8");
                    dir = new File(realPath);
                } else {
                    dir = new File(userAvatarPath);
                }

                // 确保目录存在（多级目录）
                if (!dir.exists()) {
                    boolean mkdirSuccess = dir.mkdirs();
                    if (mkdirSuccess) {
                        log.info("头像上传目录创建成功：{}", dir.getAbsolutePath());
                    } else {
                        log.error("头像上传目录创建失败：{}", dir.getAbsolutePath());
                        return Result.fail("头像上传失败：无法创建存储目录");
                    }
                }

                // 校验目录可写
                if (!dir.canWrite()) {
                    log.error("头像上传目录无写入权限：{}", dir.getAbsolutePath());
                    return Result.fail("头像上传失败：存储目录无写入权限");
                }
            } catch (Exception e) {
                log.error("解析上传目录失败", e);
                return Result.fail("头像上传失败：目录解析错误");
            }

            // 6. 保存文件（核心修复：用Files.copy替代transferTo，更稳定）
            File destFile = new File(dir, fileName);
            try {
                Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("头像文件保存成功：{}", destFile.getCanonicalPath());
            } catch (IOException e) {
                log.error("头像文件保存失败", e);
                return Result.fail("头像上传失败：文件写入失败，原因：" + e.getMessage());
            }

            // 7. 更新数据库（仅存储纯文件名）
            user.setAvatarUrl(fileName);
            boolean updateSuccess = userService.updateById(user);
            if (!updateSuccess) {
                log.error("用户头像字段更新失败，userId={}", userId);
                // 回滚：删除已写入的文件
                if (destFile.exists()) {
                    boolean deleteSuccess = destFile.delete();
                    log.info("回滚删除文件：{}，结果：{}", destFile.getAbsolutePath(), deleteSuccess);
                }
                return Result.fail("头像上传失败：用户信息更新失败");
            }

            // 8. 返回纯文件名
            return Result.success(fileName, "头像上传成功");

        } catch (Exception e) {
            log.error("头像上传异常", e);
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

    // ========== 新增：用户钱包充值接口 ==========
    @PostMapping("/recharge")
    public Result<User> rechargeWallet(@RequestBody Map<String, Object> paramMap) {
        // 1. 解析参数
        Integer userId = (Integer) paramMap.get("userId");
        BigDecimal amount = new BigDecimal(paramMap.get("amount").toString());

        // 2. 参数校验
        if (userId == null) {
            return Result.failParam("用户ID不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failParam("充值金额必须大于0");
        }

        // 3. 查询用户
        User user = userService.getById(userId);
        if (user == null) {
            log.error("充值失败：用户不存在，userId={}", userId);
            return Result.failNotFound("用户不存在");
        }

        // 4. 执行充值（累加账户余额）
        user.setAccountBalance(user.getAccountBalance().add(amount));
        boolean success = userService.updateById(user);

        if (success) {
            log.info("用户充值成功：userId={}，充值金额={}，当前余额={}", userId, amount, user.getAccountBalance());
            return Result.success(user, "充值成功");
        } else {
            log.error("用户充值失败：userId={}，充值金额={}", userId, amount);
            return Result.fail("充值失败，请稍后重试");
        }
    }

    // ========== 新增：查询用户金额信息（余额+待入账） ==========
    @GetMapping("/amount/{userId}")
    public Result<Map<String, BigDecimal>> getUserAmountInfo(@PathVariable Integer userId) {
        // 1. 查询用户
        User user = userService.getById(userId);
        if (user == null) {
            return Result.failNotFound("用户不存在");
        }

        // 2. 组装金额信息
        Map<String, BigDecimal> amountInfo = Map.of(
                "accountBalance", user.getAccountBalance() == null ? BigDecimal.ZERO : user.getAccountBalance(),
                "pendingAmount", user.getPendingAmount() == null ? BigDecimal.ZERO : user.getPendingAmount()
        );

        return Result.success(amountInfo, "查询金额信息成功");
    }

    // ========== 新增：更新待入账金额接口（如退款、奖励到账等场景） ==========
    @PostMapping("/updatePendingAmount")
    public Result<User> updatePendingAmount(@RequestBody Map<String, Object> paramMap) {
        // 1. 解析参数
        Integer userId = (Integer) paramMap.get("userId");
        BigDecimal pendingAmount = new BigDecimal(paramMap.get("pendingAmount").toString());

        // 2. 参数校验
        if (userId == null) {
            return Result.failParam("用户ID不能为空");
        }
        if (pendingAmount == null) {
            return Result.failParam("待入账金额不能为空");
        }

        // 3. 查询用户
        User user = userService.getById(userId);
        if (user == null) {
            log.error("更新待入账金额失败：用户不存在，userId={}", userId);
            return Result.failNotFound("用户不存在");
        }

        // 4. 更新待入账金额
        user.setPendingAmount(pendingAmount);
        boolean success = userService.updateById(user);

        if (success) {
            log.info("用户待入账金额更新成功：userId={}，待入账金额={}", userId, pendingAmount);
            return Result.success(user, "待入账金额更新成功");
        } else {
            log.error("用户待入账金额更新失败：userId={}，待入账金额={}", userId, pendingAmount);
            return Result.fail("待入账金额更新失败，请稍后重试");
        }
    }

    // ========== 新增：待入账金额转正式余额接口（如提现审核通过） ==========
    @PostMapping("/pendingToBalance/{userId}")
    public Result<User> pendingToBalance(@PathVariable Integer userId) {
        // 1. 查询用户
        User user = userService.getById(userId);
        if (user == null) {
            return Result.failNotFound("用户不存在");
        }

        // 2. 校验待入账金额
        BigDecimal pendingAmount = user.getPendingAmount() == null ? BigDecimal.ZERO : user.getPendingAmount();
        if (pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failParam("暂无待入账金额可转入");
        }

        // 3. 待入账金额转入余额
        user.setAccountBalance(user.getAccountBalance().add(pendingAmount));
        user.setPendingAmount(BigDecimal.ZERO); // 清空待入账金额
        boolean success = userService.updateById(user);

        if (success) {
            log.info("用户待入账金额转入余额成功：userId={}，转入金额={}", userId, pendingAmount);
            return Result.success(user, "待入账金额已成功转入账户余额");
        } else {
            log.error("用户待入账金额转入余额失败：userId={}，转入金额={}", userId, pendingAmount);
            return Result.fail("待入账金额转入失败，请稍后重试");
        }
    }

    // ========== 新增：钱包扣款接口（结算时扣减余额） ==========
    @PostMapping("/deductBalance")
    public Result<User> deductWalletBalance(@RequestBody Map<String, Object> paramMap) {
        // 解析参数
        Integer userId = (Integer) paramMap.get("userId");
        Object amountObj = paramMap.get("amount");

        // 参数校验
        if (userId == null) {
            return Result.failParam("用户ID不能为空");
        }
        if (amountObj == null) {
            return Result.failParam("扣款金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountObj.toString());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failParam("扣款金额必须大于0");
        }

        // 3. 查询用户（加锁查询，防止并发扣款导致余额异常）
        User user = userService.getById(userId);
        if (user == null) {
            log.error("扣款失败：用户不存在，userId={}", userId);
            return Result.failNotFound("用户不存在");
        }

        // 4. 校验余额是否充足
        BigDecimal currentBalance = user.getAccountBalance() == null ? BigDecimal.ZERO : user.getAccountBalance();
        if (currentBalance.compareTo(amount) < 0) {
            log.error("扣款失败：余额不足，userId={}，扣款金额={}，当前余额={}", userId, amount, currentBalance);
            return Result.fail("余额不足，扣款失败");
        }

        // 5. 执行扣款（扣减账户余额）
        user.setAccountBalance(currentBalance.subtract(amount));
        boolean success = userService.updateById(user);

        if (success) {
            log.info("用户扣款成功：userId={}，扣款金额={}，当前余额={}", userId, amount, user.getAccountBalance());
            return Result.success(user, "扣款成功");
        } else {
            log.error("用户扣款失败：userId={}，扣款金额={}", userId, amount);
            return Result.fail("扣款失败，请稍后重试");
        }
    }

    // ========== 补充：获取用户ByEmail方法（适配UserService） ==========
    public User getUserByEmail(String email) {
        return userService.getOne(new QueryWrapper<User>().eq("email", email));
    }
}
