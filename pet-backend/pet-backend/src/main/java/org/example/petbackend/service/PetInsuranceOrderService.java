package org.example.petbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.PetInsuranceOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PetInsuranceOrderService extends IService<PetInsuranceOrder> {
    // 原boolean返回改为Long（返回订单ID）
    Long createOrder(PetInsuranceOrder order);

    List<PetInsuranceOrder> getOrdersByUserId(Long userId);

    boolean updateOrderStatus(Long orderId, Integer status);

    BigDecimal[] calculatePrice(BigDecimal discountPremium, Integer guaranteeCycle, String paymentMethod);

    boolean checkValidOrder(Long userId, Long petId, Long insuranceId);

    boolean payRemainingPremium(Long orderId, Long userId);

    // 1. 商家分页查询订单
    IPage<PetInsuranceOrder> getMerchantOrderPage(Page<PetInsuranceOrder> page, Integer status, Long insuranceId);

    // 2. 商家审核订单
    boolean auditOrder(Long orderId, Integer status, String auditRemark);

    // 3. 查询订单审核记录
    Map<String, Object> getOrderAuditRecord(Long orderId);

    /**
     * 商家端：根据订单ID查询订单详情（含宠物信息）
     * @param orderId 订单ID
     * @return 订单详情
     */
    Map<String, Object> getOrderDetailWithPet(Long orderId);
}
