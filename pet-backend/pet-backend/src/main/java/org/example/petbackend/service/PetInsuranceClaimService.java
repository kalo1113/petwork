package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.PetInsuranceClaim;

import java.math.BigDecimal;
import java.util.List;

/**
 * 宠物保险理赔申请Service接口
 * 定义理赔申请的核心业务方法，包含查询、创建、状态更新、删除、打款金额更新等功能
 */
public interface PetInsuranceClaimService extends IService<PetInsuranceClaim> {

    /**
     * 根据理赔申请ID查询详情（仅查询未删除的数据）
     * @param claimId 理赔申请主键ID
     * @return 理赔申请实体
     * @throws IllegalArgumentException 当ID为空时抛出
     */
    PetInsuranceClaim getClaimById(Long claimId);

    /**
     * 根据理赔申请单号查询详情
     * @param claimNo 理赔申请单号（如：CLAIM20260213001）
     * @return 理赔申请实体
     * @throws IllegalArgumentException 当单号为空时抛出
     */
    PetInsuranceClaim getClaimByClaimNo(String claimNo);

    /**
     * 根据用户ID查询该用户的所有理赔申请列表
     * @param userId 用户ID
     * @return 理赔申请列表（按创建时间倒序）
     * @throws IllegalArgumentException 当用户ID为空时抛出
     */
    List<PetInsuranceClaim> getClaimListByUserId(Long userId);

    /**
     * 根据保险订单ID查询关联的理赔申请列表
     * @param insuranceOrderId 保险订单ID
     * @return 理赔申请列表（按创建时间倒序）
     * @throws IllegalArgumentException 当订单ID为空时抛出
     */
    List<PetInsuranceClaim> getClaimListByInsuranceOrderId(Long insuranceOrderId);

    /**
     * 根据理赔状态查询申请列表
     * @param claimStatus 理赔状态：0=待审核 1=审核中 2=审核通过 3=审核驳回 4=理赔完成
     * @return 理赔申请列表（按创建时间倒序）
     * @throws IllegalArgumentException 当状态值不合法时抛出
     */
    List<PetInsuranceClaim> getClaimListByStatus(Integer claimStatus);

    /**
     * 创建新的理赔申请（自动生成唯一理赔单号，设置默认状态）
     * @param claim 理赔申请实体（需包含核心字段：用户ID、订单ID、宠物信息、费用等）
     * @return 创建成功的理赔申请（包含生成的ID和单号）
     * @throws IllegalArgumentException 当核心参数为空或不合法时抛出
     * @throws RuntimeException 当保存失败时抛出
     */
    PetInsuranceClaim createClaim(PetInsuranceClaim claim);

    /**
     * 更新理赔申请状态
     * @param claimId 理赔申请ID
     * @param status 目标状态（0-4）
     * @param auditorId 审核人ID（非待审核状态必填）
     * @param auditRemark 审核备注（可选）
     * @return 更新结果（true=成功，false=失败）
     * @throws IllegalArgumentException 当ID或状态不合法时抛出
     */
    boolean updateClaimStatus(Long claimId, Integer status, Long auditorId, String auditRemark);

    /**
     * 逻辑删除理赔申请（设置isDeleted=1）
     * @param claimId 理赔申请ID
     * @return 删除结果（true=成功，false=失败）
     * @throws IllegalArgumentException 当ID为空时抛出
     */
    boolean deleteClaim(Long claimId);

    /**
     * 更新理赔申请的打款金额（对应实体类paymentAmount字段）
     * @param claimId 理赔申请ID
     * @param paymentAmount 打款金额（必须大于0）
     * @return 更新结果（true=成功，false=失败）
     * @throws IllegalArgumentException 当ID为空或金额不合法时抛出
     */
    boolean updatePaymentAmount(Long claimId, BigDecimal paymentAmount);

}
