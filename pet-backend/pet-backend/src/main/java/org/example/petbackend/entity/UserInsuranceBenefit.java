package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户保险权益表实体类
 * 对应数据库表：user_insurance_benefit
 */
@Data
@TableName("user_insurance_benefit")
public class UserInsuranceBenefit {
    @TableId
    private Long id; // 主键ID（BIGINT）

    private Long userId; // 用户ID，对应user_id
    private Long insuranceOrderId; // 保险订单ID，对应insurance_order_id
    private Long insuranceId; // 保险产品ID，对应insurance_id
    private Long petId; // 宠物ID，对应pet_id

    private LocalDateTime insuranceExpireTime; // 保险到期时间，对应insurance_expire_time
    private BigDecimal remainingInsuranceAmount; // 剩余保额（元），对应remaining_insurance_amount
    private Integer accidentWaitingDays; // 意外等待期（天），对应accident_waiting_days
    private Integer congenitalDiseaseWaitingDays; // 先天性/遗传疾病等待期（天），对应congenital_disease_waiting_days
    private Integer generalDiseaseWaitingDays; // 一般疾病等待期（天），对应general_disease_waiting_days
    private BigDecimal monthlySubsidyBalance; // 月消费补贴余额（元），对应monthly_subsidy_balance
    private String freeServiceRemaining; // 剩余赠送服务（JSON格式），对应free_service_remaining

    private LocalDateTime createTime; // 创建时间，对应create_time
    private LocalDateTime updateTime; // 更新时间，对应update_time
    private Integer isDeleted; // 逻辑删除 0-未删除 1-已删除，对应is_deleted
}
