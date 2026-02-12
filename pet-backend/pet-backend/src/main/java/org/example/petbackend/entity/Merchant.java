package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家实体类
 */
@Data
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Integer id; // 商家唯一ID（自增主键，对应数据库id）

    @NotBlank(message = "商家名称不能为空")
    private String merchantName; // 商家名称（非空，对应数据库merchant_name）

    @NotBlank(message = "登录账号不能为空")
    private String username; // 商家登录账号（非空，对应数据库username）

    @NotBlank(message = "密码不能为空")
    private String password; // 商家密码（非空，需加密存储，对应数据库password）

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不合法")
    private String phone; // 商家联系电话（非空，手机号格式校验，对应数据库phone）

    private String address; // 商家地址（可空，对应数据库address）

    private Integer status; // 商家状态（1-正常 0-禁用，对应数据库status）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间（自动填充，对应数据库create_time）

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间（插入/更新自动填充，对应数据库update_time）
}
