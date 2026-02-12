package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 保险媒体图片实体类
 * 严格匹配 pet_insurance_media_content 数据表结构
 */
@Data
@TableName("pet_insurance_media_content")
public class PetInsuranceMediaContent {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联保险ID（pet_insurance.id）
     */
    @TableField("insurance_id")
    private Integer insuranceId;

    /**
     * 内容类型 1=产品特色图片 2=理赔案例图片 3=产品简介 4=产品推荐图
     */
    @TableField("content_type")
    private Integer contentType;

    /**
     * 图片路径（产品特色图/理赔案例图）
     */
    @TableField("img_path")
    private String imgPath;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 图片说明（仅content_type=1时有效）
     */
    @TableField("img_remark")
    private String imgRemark;
}
