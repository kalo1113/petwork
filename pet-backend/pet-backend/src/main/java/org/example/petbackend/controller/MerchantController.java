package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.Merchant;
import org.example.petbackend.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商家控制器
 */
@RestController
@RequestMapping("/merchant")
public class MerchantController {
    private static final Logger log = LoggerFactory.getLogger(MerchantController.class);

    @Autowired
    private MerchantService merchantService;

    // ========== 商家注册接口（适配Merchant实体） ==========
    @PostMapping("/register")
    public Result<Merchant> registerMerchant(@Valid @RequestBody Merchant merchant, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError firstError = bindingResult.getFieldErrors().get(0);
            return Result.failParam(firstError.getDefaultMessage());
        }

        // 调用封装方法查重
        if (merchantService.isPhoneRegistered(merchant.getPhone())) {
            return Result.failConflict("该手机号已被注册");
        }
        if (merchantService.isUsernameExist(merchant.getUsername())) {
            return Result.failConflict("账号已存在，请重新生成");
        }

        merchant.setStatus(1);
        boolean success = merchantService.save(merchant);
        return success ? Result.success(merchant, "注册成功") : Result.fail("注册失败");
    }
    // ========== 商家登录接口（适配Merchant实体） ==========
// ========== 商家登录接口（适配手机号登录 + 修正提示文案） ==========
    @PostMapping("/login")
    public Result<Merchant> loginMerchant(@RequestBody Merchant merchant) {
        String phone = merchant.getPhone(); // 接收手机号
        String password = merchant.getPassword();

        // 1. 校验参数（核心修改：提示文案从「登录账号」改为「手机号」）
        if (phone == null || password == null) {
            return Result.failParam("手机号和密码不能为空");
        }

        // 2. 根据手机号查询商家
        Merchant existingMerchant = merchantService.getOne(new QueryWrapper<Merchant>().eq("phone", phone));
        if (existingMerchant == null) {
            return Result.failNotFound("该手机号未注册，请先注册"); // 优化：提示更精准
        }

        // 3. 校验商家状态（禁用状态无法登录）
        if (existingMerchant.getStatus() == 0) {
            return Result.failAuth("该商家账号已被禁用");
        }

        // 4. 密码校验
        if (!password.equals(existingMerchant.getPassword())) {
            return Result.failAuth("密码错误，请重新输入"); // 优化：提示更友好
        }

        return Result.success(existingMerchant, "商家登录成功");
    }

    // ========== 根据ID查询商家信息 ==========
    @GetMapping("/{id}")
    public Result<Merchant> getMerchantById(@PathVariable Integer id) {
        Merchant merchant = merchantService.getById(id);
        if (merchant != null) {
            return Result.success(merchant, "商家信息查询成功");
        }
        return Result.fail("商家不存在");
    }

    // ========== 修改商家名称接口 ==========
    @PostMapping("/updateName")
    public Result<Merchant> updateMerchantName(@RequestBody Merchant merchant) {
        // 1. 校验参数
        if (merchant.getId() == null) {
            return Result.failParam("商家ID不能为空");
        }
        if (merchant.getMerchantName() == null || merchant.getMerchantName().trim().isEmpty()) {
            return Result.failParam("商家名称不能为空");
        }
        if (merchant.getMerchantName().length() > 50) {
            return Result.failParam("商家名称长度不能超过50个字符");
        }

        // 2. 查询商家
        Merchant existingMerchant = merchantService.getById(merchant.getId());
        if (existingMerchant == null) {
            return Result.failNotFound("商家不存在");
        }

        // 3. 更新商家名称
        existingMerchant.setMerchantName(merchant.getMerchantName().trim());
        boolean success = merchantService.updateById(existingMerchant);

        if (success) {
            return Result.success(existingMerchant, "商家名称修改成功");
        } else {
            return Result.fail("商家名称修改失败，请稍后重试");
        }
    }

    // ========== 修改商家联系电话接口 ==========
    @PostMapping("/updatePhone")
    public Result<Merchant> updateMerchantPhone(@RequestBody Merchant merchant) {
        // 1. 校验参数
        if (merchant.getId() == null) {
            return Result.failParam("商家ID不能为空");
        }
        if (merchant.getPhone() == null || merchant.getPhone().trim().isEmpty()) {
            return Result.failParam("联系电话不能为空");
        }

        // 2. 查询商家
        Merchant existingMerchant = merchantService.getById(merchant.getId());
        if (existingMerchant == null) {
            return Result.failNotFound("商家不存在");
        }

        // 3. 更新联系电话
        existingMerchant.setPhone(merchant.getPhone().trim());
        boolean success = merchantService.updateById(existingMerchant);

        if (success) {
            return Result.success(existingMerchant, "联系电话修改成功");
        } else {
            return Result.fail("联系电话修改失败，请稍后重试");
        }
    }

    // ========== 修改商家密码接口（和UserController格式一致） ==========
    @PostMapping("/updatePassword")
    public Result<String> updateMerchantPassword(@RequestBody Map<String, Object> paramMap) {
        // 1. 解析参数
        Integer id = (Integer) paramMap.get("id");
        String oldPassword = (String) paramMap.get("oldPassword");
        String newPassword = (String) paramMap.get("newPassword");

        // 2. 校验参数
        if (id == null) {
            return Result.failParam("商家ID不能为空");
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

        // 3. 查询商家
        Merchant existingMerchant = merchantService.getById(id);
        if (existingMerchant == null) {
            return Result.failNotFound("商家不存在");
        }

        // 4. 校验原密码
        if (!oldPassword.equals(existingMerchant.getPassword())) {
            return Result.failAuth("原密码错误");
        }

        // 5. 校验新密码与原密码是否一致
        if (oldPassword.equals(newPassword)) {
            return Result.failParam("新密码不能与原密码相同");
        }

        // 6. 更新密码
        existingMerchant.setPassword(newPassword.trim());
        boolean success = merchantService.updateById(existingMerchant);

        if (success) {
            return Result.success("", "密码修改成功");
        } else {
            return Result.fail("密码修改失败，请稍后重试");
        }
    }

    // ========== 修改商家地址接口 ==========
    @PostMapping("/updateAddress")
    public Result<Merchant> updateMerchantAddress(@RequestBody Merchant merchant) {
        // 1. 校验参数
        if (merchant.getId() == null) {
            return Result.failParam("商家ID不能为空");
        }
        if (merchant.getAddress() == null || merchant.getAddress().trim().isEmpty()) {
            return Result.failParam("商家地址不能为空");
        }

        // 2. 查询商家
        Merchant existingMerchant = merchantService.getById(merchant.getId());
        if (existingMerchant == null) {
            return Result.failNotFound("商家不存在");
        }

        // 3. 更新商家地址
        existingMerchant.setAddress(merchant.getAddress().trim());
        boolean success = merchantService.updateById(existingMerchant);

        if (success) {
            return Result.success(existingMerchant, "商家地址修改成功");
        } else {
            return Result.fail("商家地址修改失败，请稍后重试");
        }
    }

    // ========== 修改商家状态接口（禁用/启用） ==========
    @PostMapping("/updateStatus")
    public Result<Merchant> updateMerchantStatus(@RequestBody Merchant merchant) {
        // 1. 校验参数
        if (merchant.getId() == null) {
            return Result.failParam("商家ID不能为空");
        }
        if (merchant.getStatus() == null || (merchant.getStatus() != 0 && merchant.getStatus() != 1)) {
            return Result.failParam("状态值只能是0（禁用）或1（正常）");
        }

        // 2. 查询商家
        Merchant existingMerchant = merchantService.getById(merchant.getId());
        if (existingMerchant == null) {
            return Result.failNotFound("商家不存在");
        }

        // 3. 更新商家状态
        existingMerchant.setStatus(merchant.getStatus());
        boolean success = merchantService.updateById(existingMerchant);

        if (success) {
            String msg = merchant.getStatus() == 1 ? "商家账号已启用" : "商家账号已禁用";
            return Result.success(existingMerchant, msg);
        } else {
            return Result.fail("商家状态修改失败，请稍后重试");
        }
    }

    // ========== 补充：根据账号查询商家（适配Service） ==========
    public Merchant getMerchantByUsername(String username) {
        return merchantService.getOne(new QueryWrapper<Merchant>().eq("username", username));
    }
}
