package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.PetInsuranceClaim;
import org.example.petbackend.mapper.PetInsuranceClaimMapper;
import org.example.petbackend.service.PetInsuranceClaimService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 宠物保险理赔申请Service实现类
 * 核心：提供理赔申请的创建、查询、状态更新等核心业务逻辑
 */
@Service
public class PetInsuranceClaimServiceImpl extends ServiceImpl<PetInsuranceClaimMapper, PetInsuranceClaim>
        implements PetInsuranceClaimService {

    /**
     * 生成唯一理赔申请单号
     * 格式：CLAIM + 年月日 + 3位随机数（简化版，生产环境可优化为序号生成）
     */
    private String generateClaimNo() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.getYear() +
                String.format("%02d", now.getMonthValue()) +
                String.format("%02d", now.getDayOfMonth());
        // 生成3位随机数（生产环境建议用分布式序号生成器）
        String randomStr = String.format("%03d", (int) (Math.random() * 999));
        return "CLAIM" + dateStr + randomStr;
    }

    @Override
    public PetInsuranceClaim getClaimById(Long claimId) {
        if (claimId == null) {
            throw new IllegalArgumentException("理赔申请ID不能为空");
        }
        LambdaQueryWrapper<PetInsuranceClaim> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetInsuranceClaim::getId, claimId)
                .eq(PetInsuranceClaim::getIsDeleted, 0);
        return getOne(wrapper);
    }

    @Override
    public PetInsuranceClaim getClaimByClaimNo(String claimNo) {
        if (claimNo == null || claimNo.trim().isEmpty()) {
            throw new IllegalArgumentException("理赔申请单号不能为空");
        }
        LambdaQueryWrapper<PetInsuranceClaim> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetInsuranceClaim::getClaimNo, claimNo)
                .eq(PetInsuranceClaim::getIsDeleted, 0);
        return getOne(wrapper);
    }

    @Override
    public List<PetInsuranceClaim> getClaimListByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        LambdaQueryWrapper<PetInsuranceClaim> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetInsuranceClaim::getUserId, userId)
                .eq(PetInsuranceClaim::getIsDeleted, 0)
                .orderByDesc(PetInsuranceClaim::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<PetInsuranceClaim> getClaimListByInsuranceOrderId(Long insuranceOrderId) {
        if (insuranceOrderId == null) {
            throw new IllegalArgumentException("保险订单ID不能为空");
        }
        LambdaQueryWrapper<PetInsuranceClaim> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetInsuranceClaim::getInsuranceOrderId, insuranceOrderId)
                .eq(PetInsuranceClaim::getIsDeleted, 0)
                .orderByDesc(PetInsuranceClaim::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<PetInsuranceClaim> getClaimListByStatus(Integer claimStatus) {
        if (claimStatus == null || claimStatus < 0 || claimStatus > 4) {
            throw new IllegalArgumentException("理赔状态值不合法（0-4）");
        }
        LambdaQueryWrapper<PetInsuranceClaim> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetInsuranceClaim::getClaimStatus, claimStatus)
                .eq(PetInsuranceClaim::getIsDeleted, 0)
                .orderByDesc(PetInsuranceClaim::getCreateTime);
        return list(wrapper);
    }

    @Override
    public PetInsuranceClaim createClaim(PetInsuranceClaim claim) {
        // 1. 参数校验
        if (claim == null) {
            throw new IllegalArgumentException("理赔申请数据不能为空");
        }
        if (claim.getUserId() == null) {
            throw new IllegalArgumentException("申请人用户ID不能为空");
        }
        if (claim.getInsuranceOrderId() == null) {
            throw new IllegalArgumentException("关联保险订单ID不能为空");
        }
        // 核心字段非空校验
        if (claim.getPetType() == null || claim.getPetType().trim().isEmpty()) {
            throw new IllegalArgumentException("宠物种类不能为空");
        }
        if (claim.getPetNickname() == null || claim.getPetNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("宠物昵称不能为空");
        }
        if (claim.getContactPhone() == null || claim.getContactPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("联系电话不能为空");
        }
        if (claim.getRealName() == null || claim.getRealName().trim().isEmpty()) {
            throw new IllegalArgumentException("收款真实姓名不能为空");
        }
        if (claim.getMedicalCost() == null || claim.getMedicalCost().compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new IllegalArgumentException("就诊费用必须大于0");
        }

        // 2. 生成唯一理赔单号
        String claimNo = generateClaimNo();
        // 避免重复（生产环境建议加分布式锁）
        while (getClaimByClaimNo(claimNo) != null) {
            claimNo = generateClaimNo();
        }
        claim.setClaimNo(claimNo);

        // 3. 设置默认值
        claim.setClaimStatus(0); // 默认待审核状态
        claim.setIsDeleted(0); // 未删除
        LocalDateTime now = LocalDateTime.now();
        claim.setCreateTime(now);
        claim.setUpdateTime(now);

        // 4. 保存理赔申请
        boolean saveSuccess = save(claim);
        if (!saveSuccess) {
            throw new RuntimeException("理赔申请创建失败");
        }

        return claim;
    }

    @Override
    public boolean updateClaimStatus(Long claimId, Integer status, Long auditorId, String auditRemark) {
        // 1. 参数校验
        if (claimId == null) {
            throw new IllegalArgumentException("理赔申请ID不能为空");
        }
        if (status == null || status < 0 || status > 4) {
            throw new IllegalArgumentException("理赔状态值不合法（0-4）");
        }

        // 2. 构建更新条件
        LambdaUpdateWrapper<PetInsuranceClaim> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PetInsuranceClaim::getId, claimId)
                .eq(PetInsuranceClaim::getIsDeleted, 0);

        // 3. 构建更新内容
        PetInsuranceClaim updateClaim = new PetInsuranceClaim();
        updateClaim.setClaimStatus(status);
        updateClaim.setUpdateTime(LocalDateTime.now());

        // 审核相关字段（审核中/通过/驳回时必填）
        if (status != 0) {
            updateClaim.setAuditorId(auditorId);
            updateClaim.setAuditTime(LocalDateTime.now());
            updateClaim.setAuditRemark(auditRemark == null ? "" : auditRemark);
        }

        // 4. 执行更新
        return update(updateClaim, updateWrapper);
    }

    @Override
    public boolean deleteClaim(Long claimId) {
        if (claimId == null) {
            throw new IllegalArgumentException("理赔申请ID不能为空");
        }

        // 逻辑删除（而非物理删除）
        LambdaUpdateWrapper<PetInsuranceClaim> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PetInsuranceClaim::getId, claimId)
                .eq(PetInsuranceClaim::getIsDeleted, 0);

        PetInsuranceClaim deleteClaim = new PetInsuranceClaim();
        deleteClaim.setIsDeleted(1);
        deleteClaim.setUpdateTime(LocalDateTime.now());

        return update(deleteClaim, updateWrapper);
    }
}
