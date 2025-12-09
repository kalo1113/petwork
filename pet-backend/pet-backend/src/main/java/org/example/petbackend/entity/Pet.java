package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data // Lombok注解，自动生成getter/setter方法
public class Pet {
    @TableId(type = IdType.AUTO)
    private Integer petId; // 主键，自增

    @NotNull(message = "用户ID不能为空")
    private Integer userId; // 关联用户ID，非空

    @NotBlank(message = "宠物名称不能为空")
    private String petName; // 宠物名称，非空

    @NotBlank(message = "出生日期不能为空")
    private String petBirthday; // 直接存储 "yyyy-MM-dd" 格式字符串

    @NotBlank(message = "宠物种类不能为空")
    @Pattern(regexp = "^(猫|狗)$", message = "宠物种类只能是“猫”或“狗”")
    private String petType; // 宠物种类，非空（猫/狗）

    @NotBlank(message = "宠物性别不能为空")
    @Pattern(regexp = "^(公|母)$", message = "宠物性别只能是“公”或“母”")
    private String petGender; // 宠物性别，非空（公/母）

    @NotBlank(message = "绝育状态不能为空")
    @Pattern(regexp = "^(是|否)$", message = "绝育状态只能是“是”或“否”")
    private String isSterilized; // 是否绝育，非空（是/否）

    @NotBlank(message = "正脸照不能为空")
    private String petFacePhoto; // 正脸照URL，非空

    @NotBlank(message = "全身照不能为空")
    private String petBodyPhoto; // 全身照URL，非空

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 插入时自动填充

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 插入/更新时自动填充
}
