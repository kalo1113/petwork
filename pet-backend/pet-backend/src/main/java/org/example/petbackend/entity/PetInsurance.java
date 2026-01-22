package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物保险主表实体类
 * 对应数据库表：pet_insurance
 */
@Data
@TableName("pet_insurance") // 对应宠物保险主表
public class PetInsurance {
    @TableId
    private Integer id; // 保险ID（主键），对应INT UNSIGNED

    private String insuranceName; // 保险名称（如：宠物年度医保尊享版），对应insurance_name
    private String insuranceNo; // 保险编号（唯一，如INS2026001），对应insurance_no
    private Byte planType; // 保障方案类型 1=基础版 2=升级版 3=尊享版，对应plan_type（TINYINT UNSIGNED）
    private Byte petType; // 适用宠物类型 1=猫咪 2=狗狗 3=通用，对应pet_type（TINYINT UNSIGNED）
    private Byte guaranteeCycle; // 保障周期（月） 12=年付，对应guarantee_cycle（TINYINT UNSIGNED）

    // 费用与额度
    private BigDecimal discountPremium; // 优惠保费（售价），对应discount_premium（DECIMAL(10,2)）
    private BigDecimal totalGuarantee; // 总保额（如20000元），对应total_guarantee（DECIMAL(10,2)）
    private BigDecimal deductible; // 免赔额（如0元），对应deductible（DECIMAL(10,2)）

    // 报销规则
    private BigDecimal outpatientLimit; // 门诊单次赔付上限（如1200元），对应outpatient_limit（DECIMAL(10,2)）
    private BigDecimal surgeryLimit; // 手术单次赔付上限（如2000元），对应surgery_limit（DECIMAL(10,2)）
    private Byte inNetworkRatio; // 定点医院赔付比例（%），对应in_network_ratio（TINYINT UNSIGNED）
    private Byte outNetworkRatio; // 非定点医院赔付比例（%），对应out_network_ratio（TINYINT UNSIGNED）

    // 其他规则
    private Byte waitingPeriodAccident; // 意外等待期（天），对应waiting_period_accident（TINYINT UNSIGNED）
    private Byte waitingPeriodDisease; // 先天性/遗传疾病等待期（天），对应waiting_period_disease（TINYINT UNSIGNED）
    private Byte waitingPeriodCommon; // 一般疾病等待期（天），对应waiting_period_common（TINYINT UNSIGNED）

    // 补贴与赠送
    private BigDecimal monthlySubsidy; // 月消费补贴（如200元/月），对应monthly_subsidy（DECIMAL(10,2)）
    private String giftService; // 赠送服务（如“3次免费驱虫”），对应gift_service

    // 状态与时间
    private Integer status; // 状态 1=上架 0=下架，对应status（TINYINT UNSIGNED）
    private LocalDateTime putOnShelfTime; // 上架时间，对应put_on_shelf_time（DATETIME）
    private LocalDateTime createTime; // 创建时间，对应create_time（DATETIME）
    private LocalDateTime updateTime; // 更新时间，对应update_time（DATETIME）
}
