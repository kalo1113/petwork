package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.UserInsuranceBenefit;
import org.example.petbackend.entity.PetInsuranceOrder; // 引入保险订单实体
import org.example.petbackend.service.UserInsuranceBenefitService;
import org.example.petbackend.service.PetInsuranceOrderService; // 引入保险订单Service
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户保险权益控制器
 * 负责UserInsuranceBenefit表的CRUD操作（适配文本格式存储赠送服务）
 */
@RestController
@RequestMapping("/insurance/benefit")
public class UserInsuranceBenefitController {

    @Resource
    private UserInsuranceBenefitService userInsuranceBenefitService;

    @Resource
    private PetInsuranceOrderService insuranceOrderService; // 新增：注入保险订单Service

    /**
     * 新增用户保险权益记录（手动创建，兼容原有逻辑）
     * @param benefit 权益信息
     * @return 新增结果
     */
    @PostMapping("/create")
    public Result<?> createInsuranceBenefit(@RequestBody UserInsuranceBenefit benefit) {
        // 基础参数校验
        if (benefit.getUserId() == null) {
            return Result.fail("用户ID不能为空");
        }
        if (benefit.getInsuranceOrderId() == null) {
            return Result.fail("保险订单ID不能为空");
        }
        if (benefit.getInsuranceId() == null) {
            return Result.fail("保险产品ID不能为空");
        }
        if (benefit.getPetId() == null) {
            return Result.fail("宠物ID不能为空");
        }
        if (benefit.getInsuranceExpireTime() == null) {
            return Result.fail("保险到期时间不能为空");
        }

        // ========== 核心新增：校验订单状态是否为已生效（orderStatus=1） ==========
        PetInsuranceOrder insuranceOrder = insuranceOrderService.getById(benefit.getInsuranceOrderId());
        if (insuranceOrder == null) {
            return Result.fail("保险订单不存在");
        }
        // 仅允许已生效的订单创建权益记录（orderStatus=1）
        if (insuranceOrder.getOrderStatus() != 1) {
            return Result.fail("仅支持已生效的保险订单创建权益记录（当前订单状态：" +
                    getOrderStatusDesc(insuranceOrder.getOrderStatus()) + "）");
        }

        // 补充默认值（赠送服务改为文本格式，默认空字符串）
        if (benefit.getRemainingInsuranceAmount() == null) {
            benefit.setRemainingInsuranceAmount(BigDecimal.ZERO);
        }
        if (benefit.getAccidentWaitingDays() == null) {
            benefit.setAccidentWaitingDays(0);
        }
        if (benefit.getCongenitalDiseaseWaitingDays() == null) {
            benefit.setCongenitalDiseaseWaitingDays(0);
        }
        if (benefit.getGeneralDiseaseWaitingDays() == null) {
            benefit.setGeneralDiseaseWaitingDays(0);
        }
        if (benefit.getMonthlySubsidyBalance() == null) {
            benefit.setMonthlySubsidyBalance(BigDecimal.ZERO);
        }
        if (benefit.getFreeServiceRemaining() == null) {
            benefit.setFreeServiceRemaining(""); // 改为空文本，而非JSON
        }
        benefit.setIsDeleted(0); // 默认未删除
        benefit.setCreateTime(LocalDateTime.now());
        benefit.setUpdateTime(LocalDateTime.now());

        // 校验订单ID唯一性
        UserInsuranceBenefit existBenefit = userInsuranceBenefitService.getBenefitByInsuranceOrderId(benefit.getInsuranceOrderId());
        if (existBenefit != null) {
            return Result.fail("该保险订单已存在权益记录，无需重复创建");
        }

        boolean save = userInsuranceBenefitService.save(benefit);
        if (save) {
            return Result.success(benefit.getId(), "保险权益记录创建成功");
        } else {
            return Result.fail("保险权益记录创建失败");
        }
    }

    /**
     * 【推荐】根据订单信息自动创建权益（从保险主表拉取数据，保证一致性）
     * @param orderId 保险订单ID
     * @param userId 用户ID
     * @param petId 宠物ID
     * @param insuranceId 保险产品ID
     * @return 权益创建结果
     */
    @PostMapping("/createByOrder")
    public Result<?> createBenefitByOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam Long petId,
            @RequestParam Long insuranceId) {
        // ========== 核心新增：校验订单状态是否为已生效（orderStatus=1） ==========
        PetInsuranceOrder insuranceOrder = insuranceOrderService.getById(orderId);
        if (insuranceOrder == null) {
            return Result.fail("保险订单不存在");
        }
        if (insuranceOrder.getOrderStatus() != 1) {
            return Result.fail("仅支持已生效的保险订单创建权益记录（当前订单状态：" +
                    getOrderStatusDesc(insuranceOrder.getOrderStatus()) + "）");
        }

        try {
            UserInsuranceBenefit benefit = userInsuranceBenefitService.createBenefitByOrder(orderId, userId, petId, insuranceId);
            return Result.success(benefit.getId(), "权益创建成功，数据与保险产品一致");
        } catch (Exception e) {
            return Result.fail("权益创建失败：" + e.getMessage());
        }
    }

    /**
     * 辅助方法：获取订单状态描述
     * @param orderStatus 订单状态值
     * @return 状态描述
     */
    private String getOrderStatusDesc(Integer orderStatus) {
        switch (orderStatus) {
            case 0:
                return "已支付";
            case 1:
                return "已生效";
            case 2:
                return "被驳回/已取消";
            default:
                return "未知状态(" + orderStatus + ")";
        }
    }

    // ========== 以下原有接口保持不变 ==========
    /**
     * 根据保险订单ID查询用户保险权益
     * @param insuranceOrderId 保险订单ID
     * @return 权益信息
     */
    @GetMapping("/order/{insuranceOrderId}")
    public Result<?> getBenefitByOrderId(@PathVariable Long insuranceOrderId) {
        UserInsuranceBenefit benefit = userInsuranceBenefitService.getBenefitByInsuranceOrderId(insuranceOrderId);
        if (benefit == null) {
            return Result.fail("该保险订单暂无权益记录");
        }
        return Result.success(benefit, "权益记录查询成功");
    }

    /**
     * 根据用户ID查询所有保险权益
     * @param userId 用户ID
     * @return 权益列表
     */
    @GetMapping("/user/{userId}")
    public Result<?> getBenefitListByUserId(@PathVariable Long userId) {
        List<UserInsuranceBenefit> benefitList = userInsuranceBenefitService.getBenefitListByUserId(userId);
        return Result.success(benefitList, "用户权益列表查询成功");
    }

    /**
     * 根据宠物ID查询权益记录
     * @param petId 宠物ID
     * @return 权益列表
     */
    @GetMapping("/pet/{petId}")
    public Result<?> getBenefitListByPetId(@PathVariable Long petId) {
        List<UserInsuranceBenefit> benefitList = userInsuranceBenefitService.getBenefitListByPetId(petId);
        return Result.success(benefitList, "宠物权益列表查询成功");
    }

    /**
     * 更新剩余保额（理赔后扣减）
     * @param benefitId 权益ID
     * @param amount 剩余保额
     * @return 更新结果
     */
    @PostMapping("/updateAmount")
    public Result<?> updateRemainingInsuranceAmount(
            @RequestParam Long benefitId,
            @RequestParam BigDecimal amount) {
        try {
            boolean update = userInsuranceBenefitService.updateRemainingInsuranceAmount(benefitId, amount);
            if (update) {
                return Result.success(null, "剩余保额更新成功");
            } else {
                return Result.fail("剩余保额更新失败（权益记录不存在）");
            }
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 更新月消费补贴余额（使用/发放后更新）
     * @param benefitId 权益ID
     * @param balance 补贴余额
     * @return 更新结果
     */
    @PostMapping("/updateSubsidy")
    public Result<?> updateMonthlySubsidyBalance(
            @RequestParam Long benefitId,
            @RequestParam BigDecimal balance) {
        try {
            boolean update = userInsuranceBenefitService.updateMonthlySubsidyBalance(benefitId, balance);
            if (update) {
                return Result.success(null, "补贴余额更新成功");
            } else {
                return Result.fail("补贴余额更新失败（权益记录不存在）");
            }
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 更新剩余赠送服务（使用后扣减次数）
     * @param benefitId 权益ID
     * @param serviceText 赠送服务文本（如：2次免费驱虫+1次基础体检）
     * @return 更新结果
     */
    @PostMapping("/updateService")
    public Result<?> updateFreeServiceRemaining(
            @RequestParam Long benefitId,
            @RequestParam String serviceText) {
        // 移除JSON校验，仅做非空基础校验（可选）
        if (serviceText == null) {
            return Result.fail("赠送服务内容不能为空");
        }

        boolean update = userInsuranceBenefitService.updateFreeServiceRemaining(benefitId, serviceText);
        if (update) {
            return Result.success(null, "剩余赠送服务更新成功");
        } else {
            return Result.fail("赠送服务更新失败（权益记录不存在）");
        }
    }

    /**
     * 分页查询权益记录（管理员后台使用）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param userId 用户ID（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<?> getBenefitPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId) {

        LambdaQueryWrapper<UserInsuranceBenefit> wrapper = Wrappers.lambdaQuery();
        if (userId != null) {
            wrapper.eq(UserInsuranceBenefit::getUserId, userId);
        }
        wrapper.eq(UserInsuranceBenefit::getIsDeleted, 0)
                .orderByDesc(UserInsuranceBenefit::getUpdateTime);

        Page<UserInsuranceBenefit> page = userInsuranceBenefitService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page, "权益记录分页查询成功");
    }

    /**
     * 逻辑删除权益记录
     * @param id 权益ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    public Result<?> deleteBenefit(@PathVariable Long id) {
        UserInsuranceBenefit benefit = new UserInsuranceBenefit();
        benefit.setId(id);
        benefit.setIsDeleted(1);
        benefit.setUpdateTime(LocalDateTime.now());

        boolean update = userInsuranceBenefitService.updateById(benefit);
        if (update) {
            return Result.success(null, "权益记录删除成功");
        } else {
            return Result.fail("权益记录删除失败（记录不存在）");
        }
    }
}
