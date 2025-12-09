package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId; // 用户唯一ID（自增主键）

    @NotBlank(message = "用户名不能为空")
    private String username; // 用户名（登录账号，非空）

    @NotBlank(message = "密码不能为空")
    private String password; // 用户密码（非空，需加密存储）

    private String avatarUrl; // 用户头像URL（可空）

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不合法")
    private String email; // 用户邮箱（非空，唯一）

    private String detailAddress; // 详细地址（可空）

    private Integer isDefaultAddress; // 是否默认地址（0-否，1-是）

    private BigDecimal accountBalance; // 账户余额（默认0.00）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间（自动填充）

    private LocalDateTime lastLoginTime; // 最后登录时间（可空）
}
