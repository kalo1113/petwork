package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物保险订单实体类
 * 对应表：pet_insurance_order
 */
@Data
@TableName("pet_insurance_order")
public class PetInsuranceOrder {

    /**
     * 订单ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号（如INS_20260130_0001）
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID（关联用户表）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 宠物ID（关联宠物档案表）
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 保险产品ID（关联保险产品表）
     */
    @TableField("insurance_id")
    private Long insuranceId;

    /**
     * 保险产品名称
     */
    @TableField("insurance_name")
    private String insuranceName;

    /**
     * 缴费方式：monthly(按月)/lump(全额)
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 折扣后保费（全额）
     */
    @TableField("discount_premium")
    private BigDecimal discountPremium;

    /**
     * 保障周期（月）
     */
    @TableField("guarantee_cycle")
    private Integer guaranteeCycle;

    /**
     * 月均保费
     */
    @TableField("monthly_price")
    private BigDecimal monthlyPrice;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 订单状态：0-已支付 1-已生效 2-已取消
     */
    @TableField("order_status")
    private Integer orderStatus;

    /**
     * 订单备注
     */
    @TableField("remark")
    private String remark;

    // ========== 核心修改：和PetInsurance保持一致，用createTime/updateTime ==========
    /**
     * 创建时间（对应数据库created_at）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（对应数据库updated_at）
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 分期付款手续费率（1.6%）
    public static final BigDecimal INSTALLMENT_FEE_RATE = new BigDecimal("0.016");

}
