package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.LongTypeHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物保险理赔申请表实体类
 * 对应数据库表：pet_insurance_claim
 */
@Data
@TableName("pet_insurance_claim")
public class PetInsuranceClaim {
    @TableId
    private Long id; // 主键ID（BIGINT）

    private String claimNo; // 理赔申请单号，对应claim_no
    @TableField(jdbcType = JdbcType.BIGINT, typeHandler = LongTypeHandler.class)
    private Long insuranceOrderId;

    @TableField(jdbcType = JdbcType.BIGINT, typeHandler = LongTypeHandler.class)
    private Long userId;

    // 被保宠物信息
    private String petType; // 宠物种类，对应pet_type
    private String petNickname; // 宠物昵称，对应pet_nickname
    private String petFrontPhotoUrl; // 宠物正脸照URL，对应pet_front_photo_url
    private String petFullPhotoUrl; // 宠物全身照URL，对应pet_full_photo_url

    // 理赔收款信息
    private String contactPhone; // 联系电话，对应contact_phone
    private String realName; // 收款真实姓名，对应real_name
    private String userEmail; // 用户邮箱，对应user_email

    // 医疗出诊信息
    private Integer isSurgery; // 是否手术：1=是，0=否，对应is_surgery
    private LocalDateTime accidentTime; // 出险时间，对应accident_time
    private Integer hospitalType; // 就诊医院类型：1=定点医院，2=非定点医院，对应hospital_type
    private BigDecimal medicalCost; // 就诊费用（元），对应medical_cost
    private String illnessDesc; // 宠物病情概述，对应illness_desc

    // 材料上传信息
    private String medicalRecordUrl; // 就诊病历URL（多个逗号分隔），对应medical_record_url
    private String inspectionReportUrl; // 检查报告URL（多个逗号分隔），对应inspection_report_url
    private String costDetailUrl; // 医疗费用明细清单URL（多个逗号分隔），对应cost_detail_url
    private String medicalInvoiceUrl; // 医疗发票URL（多个逗号分隔），对应medical_invoice_url
    private String treatmentPhotoUrl; // 宠物治疗中照片URL（多个逗号分隔），对应treatment_photo_url

    // 流程状态字段
    private Integer claimStatus; // 理赔状态：0=待审核，1=审核中，2=审核通过，3=审核驳回，4=理赔完成，对应claim_status
    private String auditRemark; // 审核备注（驳回原因等），对应audit_remark
    @TableField(jdbcType = JdbcType.BIGINT, typeHandler = LongTypeHandler.class)
    private Long auditorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime auditTime; // 审核时间，对应audit_time

    // 通用字段
    private LocalDateTime createTime; // 创建时间，对应create_time
    private LocalDateTime updateTime; // 更新时间，对应update_time
    private Integer isDeleted; // 逻辑删除 0-未删除 1-已删除，对应is_deleted


}
