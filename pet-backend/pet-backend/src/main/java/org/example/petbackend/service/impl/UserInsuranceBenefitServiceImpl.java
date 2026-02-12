package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.PetInsurance;
import org.example.petbackend.entity.UserInsuranceBenefit;
import org.example.petbackend.mapper.UserInsuranceBenefitMapper;
import org.example.petbackend.service.PetInsuranceService;
import org.example.petbackend.service.UserInsuranceBenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 用户保险权益Service实现类
 * 核心：权益数据从PetInsurance主表自动拉取，保证数据一致性
 */
@Service
public class UserInsuranceBenefitServiceImpl extends ServiceImpl<UserInsuranceBenefitMapper, UserInsuranceBenefit>
        implements UserInsuranceBenefitService {

    @Autowired
    private PetInsuranceService petInsuranceService;

    @Override
    public UserInsuranceBenefit getBenefitByInsuranceOrderId(Long insuranceOrderId) {
        LambdaQueryWrapper<UserInsuranceBenefit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInsuranceBenefit::getInsuranceOrderId, insuranceOrderId)
                .eq(UserInsuranceBenefit::getIsDeleted, 0);
        return getOne(wrapper);
    }

    @Override
    public List<UserInsuranceBenefit> getBenefitListByUserId(Long userId) {
        LambdaQueryWrapper<UserInsuranceBenefit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInsuranceBenefit::getUserId, userId)
                .eq(UserInsuranceBenefit::getIsDeleted, 0)
                .orderByDesc(UserInsuranceBenefit::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<UserInsuranceBenefit> getBenefitListByPetId(Long petId) {
        LambdaQueryWrapper<UserInsuranceBenefit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInsuranceBenefit::getPetId, petId)
                .eq(UserInsuranceBenefit::getIsDeleted, 0)
                .orderByDesc(UserInsuranceBenefit::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean updateRemainingInsuranceAmount(Long benefitId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("剩余保额不能为负数");
        }
        UserInsuranceBenefit benefit = new UserInsuranceBenefit();
        benefit.setId(benefitId);
        benefit.setRemainingInsuranceAmount(amount);
        benefit.setUpdateTime(LocalDateTime.now());
        return updateById(benefit);
    }

    @Override
    public boolean updateMonthlySubsidyBalance(Long benefitId, BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("补贴余额不能为负数");
        }
        UserInsuranceBenefit benefit = new UserInsuranceBenefit();
        benefit.setId(benefitId);
        benefit.setMonthlySubsidyBalance(balance);
        benefit.setUpdateTime(LocalDateTime.now());
        return updateById(benefit);
    }

    @Override
    public boolean updateFreeServiceRemaining(Long benefitId, String serviceText) {
        UserInsuranceBenefit benefit = new UserInsuranceBenefit();
        benefit.setId(benefitId);
        benefit.setFreeServiceRemaining(serviceText);
        benefit.setUpdateTime(LocalDateTime.now());
        return updateById(benefit);
    }

    /**
     * 核心方法：根据订单自动创建权益，数据从保险主表拉取
     */
    @Override
    public UserInsuranceBenefit createBenefitByOrder(Long orderId, Long userId, Long petId, Long insuranceId) {
        // 1. 参数校验
        if (orderId == null || userId == null || petId == null || insuranceId == null) {
            throw new IllegalArgumentException("创建权益的核心参数不能为空");
        }

        // 2. 校验订单是否已存在权益
        if (getBenefitByInsuranceOrderId(orderId) != null) {
            throw new RuntimeException("该订单已创建权益，无需重复创建");
        }

        // 3. 查询保险主表数据（转换Integer类型，因为PetInsurance的id是Integer）
        Integer insuranceIdInt = insuranceId.intValue();
        PetInsurance insurance = petInsuranceService.getById(insuranceIdInt);
        if (insurance == null) {
            throw new RuntimeException("保险产品不存在（ID：" + insuranceId + "）");
        }

        // 4. 构建权益对象，字段与保险主表一一映射
        UserInsuranceBenefit benefit = new UserInsuranceBenefit();
        // 基础关联字段
        benefit.setUserId(userId);
        benefit.setInsuranceOrderId(orderId);
        benefit.setInsuranceId(insuranceId);
        benefit.setPetId(petId);

        // 保险到期时间：当前时间 + 保障周期（月）
        LocalDateTime now = LocalDateTime.now();
        int guaranteeCycle = insurance.getGuaranteeCycle() == null ? 12 : insurance.getGuaranteeCycle();
        benefit.setInsuranceExpireTime(now.plus(guaranteeCycle, ChronoUnit.MONTHS));

        // 剩余保额：初始等于总保额
        benefit.setRemainingInsuranceAmount(insurance.getTotalGuarantee() == null ? BigDecimal.ZERO : insurance.getTotalGuarantee());

        // 等待期（Byte转Integer，保证类型匹配）
        benefit.setAccidentWaitingDays(insurance.getWaitingPeriodAccident() == null ? 0 : insurance.getWaitingPeriodAccident().intValue());
        benefit.setCongenitalDiseaseWaitingDays(insurance.getWaitingPeriodDisease() == null ? 0 : insurance.getWaitingPeriodDisease().intValue());
        benefit.setGeneralDiseaseWaitingDays(insurance.getWaitingPeriodCommon() == null ? 0 : insurance.getWaitingPeriodCommon().intValue());

        // 月消费补贴余额：初始等于月补贴金额
        benefit.setMonthlySubsidyBalance(insurance.getMonthlySubsidy() == null ? BigDecimal.ZERO : insurance.getMonthlySubsidy());

        // 剩余赠送服务：初始等于赠送服务内容（文本格式）
        benefit.setFreeServiceRemaining(insurance.getGiftService() == null ? "" : insurance.getGiftService());

        // 公共字段
        benefit.setIsDeleted(0);
        benefit.setCreateTime(now);
        benefit.setUpdateTime(now);

        // 5. 保存权益数据
        boolean saveSuccess = save(benefit);
        if (!saveSuccess) {
            throw new RuntimeException("保险权益创建失败");
        }

        return benefit;
    }
}
