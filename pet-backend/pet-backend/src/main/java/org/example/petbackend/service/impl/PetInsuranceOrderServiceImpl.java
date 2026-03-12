package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.Pet;
import org.example.petbackend.entity.PetInsurance;
import org.example.petbackend.entity.PetInsuranceOrder;
import org.example.petbackend.entity.User;
import org.example.petbackend.mapper.PetInsuranceOrderMapper;
import org.example.petbackend.service.PetInsuranceOrderService;
import org.example.petbackend.service.PetInsuranceService;
import org.example.petbackend.service.PetService;
import org.example.petbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宠物保险订单Service实现类
 * 适配Pet实体类：主键petId(Integer)、关联userId(Integer)
 * 新增：宠物类型与保险适用类型匹配校验
 * 修改：createOrder返回订单ID（移除requestId相关逻辑）
 * 新增：商家端订单管理/审核相关方法（适配现有表结构，无新增字段）
 * 修复：用户ID类型不匹配导致的宠物归属校验失败问题
 */
@Service
public class PetInsuranceOrderServiceImpl extends ServiceImpl<PetInsuranceOrderMapper, PetInsuranceOrder>
        implements PetInsuranceOrderService {

    private static final Logger log = LoggerFactory.getLogger(PetInsuranceOrderServiceImpl.class);

    // 注入用户Service和宠物Service
    @Autowired
    private UserService userService;

    @Autowired
    private PetService petService;

    // ========== 新增：注入保险主表Service ==========
    @Autowired
    private PetInsuranceService petInsuranceService;

    /**
     * 根据用户ID查询订单列表
     */
    @Override
    public List<PetInsuranceOrder> getOrdersByUserId(Long userId) {
        LambdaQueryWrapper<PetInsuranceOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsuranceOrder::getUserId, userId)
                .orderByDesc(PetInsuranceOrder::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 创建订单（修改核心：返回订单ID，而非boolean）
     * @param order 订单实体
     * @return 生成的订单ID
     */
    @Override
    public Long createOrder(PetInsuranceOrder order) {
        // 1. 校验用户ID非空 + 用户存在（兼容Long转Integer）
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Integer userId = order.getUserId().intValue();
        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在（userId：" + userId + "）");
        }

        // 2. 校验宠物ID非空 + 宠物存在（Pet主键是petId，Integer类型）
        if (order.getPetId() == null) {
            throw new IllegalArgumentException("宠物ID不能为空");
        }
        Integer petId = order.getPetId().intValue();
        Pet pet = petService.getById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("宠物不存在（petId：" + petId + "）");
        }

        // 3. 强制校验：该宠物必须归属当前用户
        // ========== 关键修改1：统一类型后再比较 ==========
        if (pet.getUserId() == null) {
            throw new IllegalArgumentException("宠物未绑定用户（petId：" + petId + "）");
        }
        // 原错误：userId(Integer) 和 pet.getUserId()(Integer) 理论上应该匹配，但加日志排查
        log.info("校验宠物归属：订单用户ID={}（类型={}），宠物所属用户ID={}（类型={}",
                userId, userId.getClass().getName(),
                pet.getUserId(), pet.getUserId().getClass().getName());
        // 修复：用int值比较，彻底避免类型问题
        if (userId.intValue() != pet.getUserId().intValue()) {
            throw new IllegalArgumentException("该宠物不属于当前用户（petId：" + petId + "，所属用户ID：" + pet.getUserId() + "，当前用户ID：" + userId + "）");
        }

        // 4. 校验保险ID非空 + 保险存在 + 宠物类型匹配
        if (order.getInsuranceId() == null) {
            throw new IllegalArgumentException("保险产品ID不能为空");
        }
        Integer insuranceId = order.getInsuranceId().intValue();
        PetInsurance insurance = petInsuranceService.getById(insuranceId);
        if (insurance == null) {
            throw new IllegalArgumentException("保险产品不存在（insuranceId：" + insuranceId + "）");
        }

        Byte insurancePetType = insurance.getPetType();
        if (insurancePetType == null) {
            throw new IllegalArgumentException("保险产品未配置适用宠物类型（insuranceId：" + insuranceId + "）");
        }

        String petTypeStr = pet.getPetType();
        Byte petTypeCode = convertPetTypeToCode(petTypeStr);
        if (petTypeCode == null) {
            throw new IllegalArgumentException("宠物类型无效（petId：" + petId + "，类型：" + petTypeStr + "）");
        }

        boolean isMatch = insurancePetType == 3 || insurancePetType.equals(petTypeCode);
        if (!isMatch) {
            String insurancePetTypeName = convertPetCodeToName(insurancePetType);
            String petTypeName = convertPetCodeToName(petTypeCode);
            throw new IllegalArgumentException(
                    "宠物类型不匹配，无法购买该保险！" +
                            "宠物类型：" + petTypeName + "，保险适用类型：" + insurancePetTypeName
            );
        }

        // 5. 自动生成订单编号
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            String orderNo = "INS_" + System.currentTimeMillis() / 1000 + "_" + (int)(Math.random() * 10000);
            order.setOrderNo(orderNo);
        }

        // 6. 自动计算价格
        BigDecimal[] prices = calculatePrice(
                order.getDiscountPremium(),
                order.getGuaranteeCycle(),
                order.getPaymentMethod()
        );
        order.setMonthlyPrice(prices[0]);
        order.setTotalAmount(prices[1]);

        // 7. 保存订单并返回ID（核心修改：MyBatis-Plus会自动回填主键到order.getId()）
        boolean saveSuccess = save(order);
        if (!saveSuccess) {
            throw new RuntimeException("订单创建失败：数据库插入失败");
        }
        return order.getId(); // 返回自增的订单ID
    }

    // ========== 新增辅助方法：类型转换 ==========
    private Byte convertPetTypeToCode(String petTypeStr) {
        if (petTypeStr == null) return null;
        switch (petTypeStr.trim()) {
            case "猫":
                return 1;
            case "狗":
                return 2;
            default:
                return null;
        }
    }

    private String convertPetCodeToName(Byte petCode) {
        if (petCode == null) return "未知";
        switch (petCode) {
            case 1:
                return "猫";
            case 2:
                return "狗";
            case 3:
                return "通用（猫/狗）";
            default:
                return "未知";
        }
    }

    /**
     * 价格计算逻辑（全额优惠，分期+1.6%手续费）
     */
    @Override
    public BigDecimal[] calculatePrice(BigDecimal discountPremium, Integer guaranteeCycle, String paymentMethod) {
        BigDecimal monthlyPrice = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (discountPremium == null) discountPremium = BigDecimal.ZERO;
        if (guaranteeCycle == null || guaranteeCycle <= 0) guaranteeCycle = 12;

        if ("lump".equals(paymentMethod)) {
            totalAmount = discountPremium;
            monthlyPrice = BigDecimal.ZERO;
        } else if ("monthly".equals(paymentMethod)) {
            BigDecimal baseMonthlyPrice = discountPremium.divide(
                    new BigDecimal(guaranteeCycle),
                    2,
                    RoundingMode.HALF_UP
            );
            monthlyPrice = baseMonthlyPrice.multiply(BigDecimal.ONE.add(new BigDecimal("0.016")))
                    .setScale(2, RoundingMode.HALF_UP);
            totalAmount = monthlyPrice;
        } else {
            throw new IllegalArgumentException("缴费方式无效（仅支持monthly-分期/lump-全额）");
        }

        return new BigDecimal[]{monthlyPrice, totalAmount};
    }

    /**
     * 更新订单状态（新增状态合法性校验）
     */
    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new IllegalArgumentException("订单状态无效（仅支持0-已支付/1-已生效/2-已取消）");
        }
        PetInsuranceOrder order = new PetInsuranceOrder();
        order.setId(orderId);
        order.setOrderStatus(status);
        return updateById(order);
    }

    // ========== 新增：实现幂等性校验方法 ==========
    @Override
    public boolean checkValidOrder(Long userId, Long petId, Long insuranceId) {
        LambdaQueryWrapper<PetInsuranceOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsuranceOrder::getUserId, userId)
                .eq(PetInsuranceOrder::getPetId, petId)
                .eq(PetInsuranceOrder::getInsuranceId, insuranceId)
                .in(PetInsuranceOrder::getOrderStatus, 0, 1);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    // ========== 实现接口中的一次性缴清保费方法 ==========
    @Override
    public boolean payRemainingPremium(Long orderId, Long userId) {
        // 1. 查询订单（校验归属和状态）
        PetInsuranceOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        // ========== 关键修改2：统一用int值比较用户ID ==========
        if (userId == null || order.getUserId() == null || userId.intValue() != order.getUserId().intValue()) {
            throw new RuntimeException("无权操作该订单");
        }
        if (order.getOrderStatus() == 2) {
            throw new RuntimeException("已取消订单无法缴费");
        }

        // 2. 计算剩余金额
        BigDecimal discountPremium = order.getDiscountPremium() == null ? BigDecimal.ZERO : order.getDiscountPremium();
        BigDecimal totalAmount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal remainingAmount = discountPremium.subtract(totalAmount);

        // 3. 校验剩余金额
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("暂无剩余待缴费用");
        }

        // 4. 一次性缴清：更新总金额为折扣后保费
        PetInsuranceOrder updateOrder = new PetInsuranceOrder();
        updateOrder.setId(orderId);
        updateOrder.setTotalAmount(discountPremium);
        updateOrder.setUpdateTime(LocalDateTime.now());

        try {
            return this.updateById(updateOrder);
        } catch (Exception e) {
            throw new RuntimeException("系统异常，缴费失败");
        }
    }

    // ====================== 新增：商家端专属方法（适配现有表结构，无新增字段） ======================
    /**
     * 商家端：分页查询所有订单（支持按状态/保险ID筛选）
     */
    @Override
    public IPage<PetInsuranceOrder> getMerchantOrderPage(Page<PetInsuranceOrder> page, Integer status, Long insuranceId) {
        LambdaQueryWrapper<PetInsuranceOrder> queryWrapper = new LambdaQueryWrapper<>();
        // 按状态筛选（可选）
        if (status != null && (status == 0 || status == 1 || status == 2)) {
            queryWrapper.eq(PetInsuranceOrder::getOrderStatus, status);
        }
        // 按保险产品ID筛选（可选）
        if (insuranceId != null && insuranceId > 0) {
            queryWrapper.eq(PetInsuranceOrder::getInsuranceId, insuranceId);
        }
        // 按创建时间倒序
        queryWrapper.orderByDesc(PetInsuranceOrder::getCreateTime);

        return baseMapper.selectPage(page, queryWrapper);
    }

    /**
     * 商家端：审核订单（仅更新状态，不新增字段）
     */
    @Override
    public boolean auditOrder(Long orderId, Integer status, String auditRemark) {
        // 1. 校验订单存在
        PetInsuranceOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在（orderId：" + orderId + "）");
        }
        // 2. 校验审核状态合法性（仅支持1-已生效/2-已取消）
        if (status != 1 && status != 2) {
            throw new IllegalArgumentException("审核状态无效（仅支持1-已生效 2-已取消）");
        }
        // 3. 校验订单当前状态（已取消订单不能重复审核）
        if (order.getOrderStatus() == 2) {
            throw new RuntimeException("该订单已取消，无法审核");
        }

        // 4. 更新订单状态 + 更新时间（不新增字段，审核备注可临时存到原有remark字段）
        PetInsuranceOrder updateOrder = new PetInsuranceOrder();
        updateOrder.setId(orderId);
        updateOrder.setOrderStatus(status);
        // 可选：将审核备注存入原有remark字段（不新增表字段）
        if (auditRemark != null && !auditRemark.isEmpty()) {
            updateOrder.setRemark(auditRemark);
        }
        updateOrder.setUpdateTime(LocalDateTime.now());

        return this.updateById(updateOrder);
    }

    /**
     * 商家端：查询订单审核记录（适配现有字段，无新增）
     */
    @Override
    public Map<String, Object> getOrderAuditRecord(Long orderId) {
        // 1. 校验订单存在
        PetInsuranceOrder order = this.getById(orderId);
        if (order == null) {
            return null;
        }

        // 2. 封装审核记录（仅用现有字段）
        Map<String, Object> auditRecord = new HashMap<>();
        auditRecord.put("orderId", orderId);
        auditRecord.put("orderNo", order.getOrderNo());
        auditRecord.put("orderStatus", order.getOrderStatus());
        auditRecord.put("orderStatusName", getOrderStatusName(order.getOrderStatus()));
        // 审核备注从原有remark字段读取（无新增字段）
        auditRecord.put("auditRemark", order.getRemark());
        auditRecord.put("updateTime", order.getUpdateTime()); // 审核时间=最后更新时间
        auditRecord.put("insuranceName", order.getInsuranceName());

        // 修正：User实体类字段是username，用getUsername()
        User user = null;
        if (order.getUserId() != null) {
            user = userService.getById(order.getUserId().intValue());
        }
        if (user != null) {
            auditRecord.put("userName", user.getUsername()); // 适配User实体的username字段
        } else {
            auditRecord.put("userName", "未知用户");
        }

        return auditRecord;
    }

    // ========== 辅助方法：订单状态转名称 ==========
    private String getOrderStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0:
                return "已支付";
            case 1:
                return "已生效";
            case 2:
                return "已取消";
            default:
                return "未知";
        }
    }


    /**
     * 商家端：根据订单ID查询订单详情（含宠物信息）
     * @param orderId 订单ID
     * @return 订单详情（含宠物信息）
     */
    @Override
    public Map<String, Object> getOrderDetailWithPet(Long orderId) {
        // 1. 校验订单ID
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID必须为正整数");
        }

        // 2. 查询订单主信息
        PetInsuranceOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在（orderId：" + orderId + "）");
        }

        // 3. 查询关联的宠物信息（适配Pet实体的Integer petId）
        Map<String, Object> petInfo = new HashMap<>();
        if (order.getPetId() != null) {
            // 订单表的petId是Long，转成Integer匹配Pet的主键类型
            Integer petId = order.getPetId().intValue();
            Pet pet = petService.getById(petId);
            if (pet != null) {
                // 封装宠物所有字段（和Pet实体一一对应）
                petInfo.put("petId", pet.getPetId());
                petInfo.put("userId", pet.getUserId());
                petInfo.put("petName", pet.getPetName());
                petInfo.put("petBirthday", pet.getPetBirthday());
                petInfo.put("petType", pet.getPetType());
                petInfo.put("petGender", pet.getPetGender());
                petInfo.put("isSterilized", pet.getIsSterilized());
                petInfo.put("petFacePhoto", pet.getPetFacePhoto());
                petInfo.put("petBodyPhoto", pet.getPetBodyPhoto());
                petInfo.put("createTime", pet.getCreateTime());
                petInfo.put("updateTime", pet.getUpdateTime());
            } else {
                petInfo.put("msg", "宠物信息不存在（petId：" + petId + "）");
            }
        } else {
            petInfo.put("msg", "该订单未关联宠物");
        }

        // 4. 封装完整详情：订单所有字段 + 宠物信息
        Map<String, Object> detail = new HashMap<>();
        detail.put("order", order); // 订单表所有字段（PetInsuranceOrder）
        detail.put("petInfo", petInfo); // 关联的宠物信息

        return detail;
    }
}
