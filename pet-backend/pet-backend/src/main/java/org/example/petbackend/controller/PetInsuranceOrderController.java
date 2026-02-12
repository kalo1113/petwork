package org.example.petbackend.controller;

import org.example.petbackend.entity.PetInsuranceOrder;
import org.example.petbackend.service.PetInsuranceOrderService;
import org.example.petbackend.service.UserInsuranceBenefitService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宠物保险订单控制器
 * 提供RESTful接口，前后端交互
 * 核心修改：仅订单生效（status=1）时创建权益，移除订单创建时的自动权益创建
 * 新增：商家端订单管理/审核接口（不改动客户端接口）
 */
@RestController
@RequestMapping("/api/order")
@CrossOrigin // 解决前端跨域问题
public class PetInsuranceOrderController {

    private static final Logger log = LoggerFactory.getLogger(PetInsuranceOrderController.class);

    @Autowired
    private PetInsuranceOrderService orderService;

    // 注入权益服务，仅在订单生效时调用
    @Autowired
    private UserInsuranceBenefitService benefitService;

    /**
     * 统一返回结果封装
     */
    private Map<String, Object> result(int code, String msg, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);   // 200=成功，400=参数错误，500=系统错误
        result.put("msg", msg);     // 提示信息
        result.put("data", data);   // 附加数据
        return result;
    }

    // ====================== 原有客户端接口（核心修改） ======================
    /**
     * 创建订单（核心修改：移除自动创建权益逻辑，仅创建订单）
     * POST /api/order/create
     * @param order 订单信息（JSON格式）
     * @return 标准化JSON结果（包含订单ID）
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody PetInsuranceOrder order) {
        // 逐项校验，返回具体错误原因
        if (order.getUserId() == null) {
            return result(400, "数据错误：用户ID不能为空", null);
        }
        if (order.getPetId() == null) {
            return result(400, "数据错误：宠物ID不能为空", null);
        }
        if (order.getInsuranceId() == null) {
            return result(400, "数据错误：保险产品ID不能为空", null);
        }
        if (order.getInsuranceName() == null || order.getInsuranceName().trim().isEmpty()) {
            return result(400, "数据错误：保险产品名称不能为空", null);
        }
        if (order.getPaymentMethod() == null) {
            return result(400, "数据错误：缴费方式不能为空", null);
        } else if (!"monthly".equals(order.getPaymentMethod()) && !"lump".equals(order.getPaymentMethod())) {
            return result(400, "数据错误：缴费方式无效（仅支持monthly-分期/lump-全额）", null);
        }
        if (order.getDiscountPremium() == null) {
            return result(400, "数据错误：优惠保费不能为空", null);
        } else if (order.getDiscountPremium().compareTo(BigDecimal.ZERO) <= 0) {
            return result(400, "数据错误：优惠保费不能为0或负数", null);
        }
        if (order.getGuaranteeCycle() == null) {
            return result(400, "数据错误：保障周期不能为空", null);
        } else if (order.getGuaranteeCycle() <= 0) {
            return result(400, "数据错误：保障周期必须为正整数", null);
        }

        try {
            // 1. 仅创建订单，获取订单ID（移除自动创建权益逻辑）
            Long orderId = orderService.createOrder(order);
            if (orderId == null || orderId <= 0) {
                return result(500, "订单创建失败：数据库插入失败", null);
            }

            // 2. 仅返回订单基础信息，不涉及权益
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", orderId);
            data.put("monthlyPrice", order.getMonthlyPrice());
            data.put("totalAmount", order.getTotalAmount());
            return result(200, "订单创建成功", data);
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return result(500, "订单创建失败：" + e.getMessage(), null);
        }
    }

    /**
     * 根据用户ID查询订单列表
     * GET /api/order/user/{userId}
     * @param userId 用户ID
     * @return 标准化JSON结果
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getOrdersByUserId(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return result(400, "数据错误：用户ID必须为正整数", null);
        }

        try {
            List<PetInsuranceOrder> orders = orderService.getOrdersByUserId(userId);
            return result(200, "查询成功", orders);
        } catch (Exception e) {
            log.error("查询用户订单异常, userId:{}", userId, e);
            return result(500, "查询订单列表失败：" + e.getMessage(), null);
        }
    }

    /**
     * 更新订单状态（核心修改：仅状态变为1时创建权益）
     * POST /api/order/updateStatus
     * @param orderId 订单ID
     * @param status 新状态
     * @return 标准化JSON结果
     */
    @PostMapping("/updateStatus")
    public Map<String, Object> updateOrderStatus(@RequestParam Long orderId, @RequestParam Integer status) {
        // 参数校验
        if (orderId == null || orderId <= 0) {
            return result(400, "数据错误：订单ID必须为正整数", null);
        }
        if (status == null || status < 0 || status > 2) {
            return result(400, "状态值无效（仅支持0-已支付 1-已生效 2-已取消）", null);
        }

        try {
            // 先查询原订单状态
            PetInsuranceOrder oldOrder = orderService.getById(orderId);
            if (oldOrder == null) {
                return result(404, "订单不存在", null);
            }
            Integer oldStatus = oldOrder.getOrderStatus();

            // 更新订单状态
            boolean success = orderService.updateOrderStatus(orderId, status);
            if (!success) {
                return result(500, "状态更新失败", null);
            }

            // 核心逻辑：仅当状态从非1变为1时，创建权益
            if (status == 1 && oldStatus != 1) {
                try {
                    benefitService.createBenefitByOrder(
                            orderId,
                            oldOrder.getUserId(),
                            oldOrder.getPetId(),
                            oldOrder.getInsuranceId()
                    );
                    log.info("订单{}状态变为已生效，权益创建成功", orderId);
                } catch (Exception e) {
                    log.error("订单{}状态变为已生效，但权益创建失败", orderId, e);
                    // 权益创建失败不回滚订单状态，仅记录日志并提示
                    return result(200, "订单状态更新成功，但权益创建失败：" + e.getMessage(), null);
                }
            }

            return result(200, "状态更新成功", null);
        } catch (Exception e) {
            log.error("更新订单状态异常, orderId:{}, status:{}", orderId, status, e);
            return result(500, "更新订单状态失败：" + e.getMessage(), null);
        }
    }

    /**
     * 根据订单ID查询订单详情
     * GET /api/order/{id}
     * @param id 订单ID
     * @return 标准化JSON结果
     */
    @GetMapping("/{id}")
    public Map<String, Object> getOrderById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return result(400, "数据错误：订单ID必须为正整数", null);
        }

        try {
            PetInsuranceOrder order = orderService.getById(id);
            if (order == null) {
                return result(404, "订单不存在", null);
            }
            return result(200, "查询成功", order);
        } catch (Exception e) {
            log.error("查询订单详情异常, orderId:{}", id, e);
            return result(500, "查询订单详情失败：" + e.getMessage(), null);
        }
    }

    // ========== 新增：一次性缴清剩余保费接口 ==========
    /**
     * 一次性缴清剩余保费
     * POST /api/order/payRemaining
     * @param orderId 订单ID
     * @param userId 用户ID（校验订单归属）
     * @return 标准化JSON结果
     */
    @PostMapping("/payRemaining")
    public Map<String, Object> payRemainingPremium(
            @RequestParam Long orderId,
            @RequestParam Long userId) {
        // 1. 严格的参数校验
        if (orderId == null || orderId <= 0) {
            return result(400, "数据错误：订单ID必须为正整数", null);
        }
        if (userId == null || userId <= 0) {
            return result(400, "数据错误：用户ID必须为正整数", null);
        }

        // 2. 调用Service层方法
        try {
            boolean success = orderService.payRemainingPremium(orderId, userId);
            if (success) {
                // 缴费成功：返回订单最新信息
                PetInsuranceOrder order = orderService.getById(orderId);
                Map<String, Object> data = new HashMap<>();
                data.put("orderId", orderId);
                data.put("totalAmount", order.getTotalAmount()); // 缴清后的总金额
                data.put("discountPremium", order.getDiscountPremium()); // 折扣后保费
                return result(200, "剩余保费缴清成功", data);
            } else {
                return result(500, "剩余保费缴清失败：数据库更新失败", null);
            }
        } catch (RuntimeException e) {
            log.warn("缴清剩余保费业务异常, orderId:{}, userId:{}", orderId, userId, e);
            return result(400, "缴清失败：" + e.getMessage(), null);
        } catch (Exception e) {
            log.error("缴清剩余保费系统异常, orderId:{}, userId:{}", orderId, userId, e);
            return result(500, "缴清剩余保费失败：系统异常，请联系管理员", null);
        }
    }

    // ====================== 新增：商家端专属接口 ======================
    /**
     * 商家端：分页查询所有订单（支持筛选）
     * GET /api/order/merchant/page
     * @param pageNum 页码（默认1）
     * @param pageSize 页大小（默认10）
     * @param status 订单状态（可选：0-已支付 1-已生效 2-已取消）
     * @param insuranceId 保险产品ID（可选）
     * @return 分页订单列表
     */
    @GetMapping("/merchant/page")
    public Map<String, Object> getMerchantOrderPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long insuranceId) {
        // 参数校验
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0 || pageSize > 100) pageSize = 10;

        try {
            // 构建分页条件
            Page<PetInsuranceOrder> page = new Page<>(pageNum, pageSize);
            // 构建查询条件（支持按状态/保险ID筛选）
            IPage<PetInsuranceOrder> orderPage = orderService.getMerchantOrderPage(page, status, insuranceId);

            // 封装分页返回格式（匹配前端分页组件）
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", orderPage.getRecords());
            pageData.put("total", orderPage.getTotal());
            pageData.put("pageNum", orderPage.getCurrent());
            pageData.put("pageSize", orderPage.getSize());

            return result(200, "查询成功", pageData);
        } catch (Exception e) {
            log.error("商家端分页查询订单异常", e);
            return result(500, "商家端查询订单失败：" + e.getMessage(), null);
        }
    }

    /**
     * 商家端：审核订单（生效/取消）（核心修改：审核生效时创建权益）
     * POST /api/order/merchant/audit
     * @param data 审核参数：{orderId: 1, status: 1, auditRemark: "审核通过"}
     * @return 审核结果
     */
    @PostMapping("/merchant/audit")
    public Map<String, Object> auditOrder(@RequestBody Map<String, Object> data) {
        // 参数提取与校验
        Long orderId = data.get("orderId") != null ? Long.valueOf(data.get("orderId").toString()) : null;
        Integer status = data.get("status") != null ? Integer.valueOf(data.get("status").toString()) : null;
        String auditRemark = data.get("auditRemark") != null ? data.get("auditRemark").toString() : "";

        if (orderId == null || orderId <= 0) {
            return result(400, "数据错误：订单ID必须为正整数", null);
        }
        if (status == null || (status != 1 && status != 2)) {
            return result(400, "审核状态无效（仅支持1-已生效 2-已取消）", null);
        }

        try {
            // 查询原订单状态
            PetInsuranceOrder oldOrder = orderService.getById(orderId);
            if (oldOrder == null) {
                return result(404, "订单不存在", null);
            }
            Integer oldStatus = oldOrder.getOrderStatus();

            // 调用Service审核订单（更新状态+记录审核备注）
            boolean success = orderService.auditOrder(orderId, status, auditRemark);
            if (!success) {
                return result(500, "订单审核失败：订单不存在或状态异常", null);
            }

            // 核心逻辑：审核生效（status=1）且原状态非1时，创建权益
            if (status == 1 && oldStatus != 1) {
                try {
                    benefitService.createBenefitByOrder(
                            orderId,
                            oldOrder.getUserId(),
                            oldOrder.getPetId(),
                            oldOrder.getInsuranceId()
                    );
                    log.info("商家审核订单{}生效，权益创建成功", orderId);
                } catch (Exception e) {
                    log.error("商家审核订单{}生效，但权益创建失败", orderId, e);
                    return result(200, "订单审核生效成功，但权益创建失败：" + e.getMessage(), null);
                }
            }

            String msg = status == 1 ? "订单审核生效成功" : "订单审核取消成功";
            return result(200, msg, null);
        } catch (Exception e) {
            log.error("商家审核订单异常", e);
            return result(500, "订单审核失败：" + e.getMessage(), null);
        }
    }

    /**
     * 商家端：查询订单审核记录
     * GET /api/order/merchant/audit/record/{orderId}
     * @param orderId 订单ID
     * @return 审核记录
     */
    @GetMapping("/merchant/audit/record/{orderId}")
    public Map<String, Object> getOrderAuditRecord(@PathVariable Long orderId) {
        if (orderId == null || orderId <= 0) {
            return result(400, "数据错误：订单ID必须为正整数", null);
        }

        try {
            // 实际业务中可查询订单的审核日志表
            Map<String, Object> auditRecord = orderService.getOrderAuditRecord(orderId);
            if (auditRecord == null) {
                return result(404, "该订单暂无审核记录", null);
            }
            return result(200, "查询成功", auditRecord);
        } catch (Exception e) {
            log.error("查询订单审核记录异常, orderId:{}", orderId, e);
            return result(500, "查询审核记录失败：" + e.getMessage(), null);
        }
    }

    /**
     * 商家端：根据订单ID查询订单详情（含宠物信息）
     * GET /api/order/merchant/detail/{orderId}
     * @param orderId 订单ID
     * @return 订单详情（含宠物信息）
     */
    @GetMapping("/merchant/detail/{orderId}")
    public Map<String, Object> getOrderDetailWithPet(@PathVariable Long orderId) {
        // 1. 参数校验
        if (orderId == null || orderId <= 0) {
            return result(400, "数据错误：订单ID必须为正整数", null);
        }

        // 2. 调用Service查询详情
        try {
            Map<String, Object> detail = orderService.getOrderDetailWithPet(orderId);
            return result(200, "查询成功", detail);
        } catch (RuntimeException e) {
            log.warn("查询订单详情业务异常, orderId:{}", orderId, e);
            return result(400, "查询失败：" + e.getMessage(), null);
        } catch (Exception e) {
            log.error("查询订单详情系统异常, orderId:{}", orderId, e);
            return result(500, "查询订单详情失败：系统异常", null);
        }
    }
}
