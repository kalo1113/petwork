package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.UserInsuranceBenefit;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户保险权益Service接口
 */
public interface UserInsuranceBenefitService extends IService<UserInsuranceBenefit> {

    /**
     * 根据保险订单ID查询权益记录
     * @param insuranceOrderId 保险订单ID
     * @return 权益记录
     */
    UserInsuranceBenefit getBenefitByInsuranceOrderId(Long insuranceOrderId);

    /**
     * 根据用户ID查询所有权益记录
     * @param userId 用户ID
     * @return 权益列表
     */
    List<UserInsuranceBenefit> getBenefitListByUserId(Long userId);

    /**
     * 根据宠物ID查询权益记录
     * @param petId 宠物ID
     * @return 权益列表
     */
    List<UserInsuranceBenefit> getBenefitListByPetId(Long petId);

    /**
     * 更新剩余保额
     * @param benefitId 权益ID
     * @param amount 剩余保额
     * @return 是否更新成功
     */
    boolean updateRemainingInsuranceAmount(Long benefitId, BigDecimal amount);

    /**
     * 更新月消费补贴余额
     * @param benefitId 权益ID
     * @param balance 补贴余额
     * @return 是否更新成功
     */
    boolean updateMonthlySubsidyBalance(Long benefitId, BigDecimal balance);

    /**
     * 更新剩余赠送服务
     * @param benefitId 权益ID
     * @param serviceText 赠送服务文本
     * @return 是否更新成功
     */
    boolean updateFreeServiceRemaining(Long benefitId, String serviceText);

    /**
     * 根据订单信息自动创建权益（核心：从保险主表拉取数据）
     * @param orderId 保险订单ID
     * @param userId 用户ID
     * @param petId 宠物ID
     * @param insuranceId 保险产品ID
     * @return 创建的权益对象
     */
    UserInsuranceBenefit createBenefitByOrder(Long orderId, Long userId, Long petId, Long insuranceId);
}
