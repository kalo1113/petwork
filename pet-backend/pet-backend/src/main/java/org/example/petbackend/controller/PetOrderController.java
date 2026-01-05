package org.example.petbackend.controller;

import org.example.petbackend.common.Result; // 引入你的统一返回类
import org.example.petbackend.entity.PetOrderMain;
import org.example.petbackend.entity.PetOrderItem;
import org.example.petbackend.service.PetOrderMainService;
import org.example.petbackend.service.PetOrderItemService;
import org.example.petbackend.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * 订单控制器（统一风格版）
 * 适配项目Result返回、与用户表插入逻辑一致
 */
@RestController
@RequestMapping("/order")
public class PetOrderController {

    @Resource
    private PetOrderMainService orderMainService;
    @Resource
    private PetOrderItemService orderItemService;
    @Resource
    private UserService userService;

    // 1. 创建订单（统一Result返回，与用户注册逻辑一致）
    @PostMapping("/create")
    public Result<?> createOrder(
            @RequestParam Integer userId,
            @RequestBody List<PetOrderItem> itemList,
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String receiverAddress) {

        // 1. 校验用户是否存在
        if (userService.getById(userId) == null) {
            return Result.fail("用户不存在，无法创建订单");
        }

        // 2. 校验商品列表是否为空
        if (itemList == null || itemList.isEmpty()) {
            return Result.fail("请选择要购买的商品");
        }

        // 3. 生成订单ID（毕设简化版：时间戳+随机数）
        long orderId = System.currentTimeMillis() + new Random().nextInt(1000);

        // 4. 计算订单总金额 + 绑定订单ID到商品明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PetOrderItem item : itemList) {
            item.setOrderId(orderId); // 每个商品绑定订单ID
            totalAmount = totalAmount.add(item.getItemAmount()); // 累加商品小计
        }

        // 5. 构建订单主表对象（简洁赋值，与用户表一致）
        PetOrderMain orderMain = new PetOrderMain();
        orderMain.setOrderId(orderId);
        orderMain.setUserId(userId);
        orderMain.setTotalAmount(totalAmount);
        orderMain.setOrderStatus(0); // 0=待付款
        orderMain.setReceiverName(receiverName);
        orderMain.setReceiverPhone(receiverPhone);
        orderMain.setReceiverAddress(receiverAddress);

        // 6. 插入订单主表（与userService.save(user)完全一致）
        boolean saveMain = orderMainService.save(orderMain);
        if (!saveMain) {
            return Result.fail("订单创建失败，请稍后重试");
        }

        // 7. 批量插入商品明细（使用MP通用saveBatch，与主表插入风格统一）
        boolean saveItems = orderItemService.saveBatch(itemList);
        if (!saveItems) {
            // 回滚主表插入（毕设简易处理：删除已插入的主表数据）
            orderMainService.removeById(orderId);
            return Result.fail("订单商品明细创建失败");
        }

        return Result.success(orderId, "订单创建成功，订单ID：" + orderId);
    }

    // 2. 查询用户的订单列表（统一Result返回）
    @GetMapping("/list")
    public Result<?> getOrderList(@RequestParam Integer userId) {
        // 校验用户
        if (userService.getById(userId) == null) {
            return Result.fail("用户不存在");
        }

        // 查询订单列表（按创建时间倒序）
        List<PetOrderMain> orderList = orderMainService.lambdaQuery()
                .eq(PetOrderMain::getUserId, userId)
                .orderByDesc(PetOrderMain::getCreateTime)
                .list();

        return Result.success(orderList, "订单列表查询成功");
    }

    // 3. 查询订单详情（主表+商品明细，统一Result返回）
    @GetMapping("/detail")
    public Result<?> getOrderDetail(@RequestParam Long orderId) {
        // 1. 查询订单主表
        PetOrderMain orderMain = orderMainService.getById(orderId);
        if (orderMain == null) {
            return Result.fail("订单不存在");
        }

        // 2. 查询订单商品明细
        List<PetOrderItem> itemList = orderItemService.lambdaQuery()
                .eq(PetOrderItem::getOrderId, orderId)
                .list();

        // 3. 组装返回数据
        Map<String, Object> orderDetail = new HashMap<>();
        orderDetail.put("orderMain", orderMain);
        orderDetail.put("itemList", itemList);

        return Result.success(orderDetail, "订单详情查询成功");
    }

    // 4. 更新订单状态（模拟付款/发货/收货，统一Result返回）
    @PostMapping("/updateStatus")
    public Result<?> updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam Integer status) {

        // 1. 校验订单状态合法性
        if (status < 0 || status > 4) {
            return Result.fail("订单状态不合法（0=待付款 1=待发货 2=待收货 3=已完成 4=已取消）");
        }

        // 2. 查询订单
        PetOrderMain orderMain = orderMainService.getById(orderId);
        if (orderMain == null) {
            return Result.fail("订单不存在");
        }

        // 3. 更新订单状态（简洁赋值，与用户表更新逻辑一致）
        orderMain.setOrderStatus(status);
        boolean updateSuccess = orderMainService.updateById(orderMain);

        if (!updateSuccess) {
            return Result.fail("订单状态更新失败");
        }

        // 状态说明
        String statusDesc = switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "";
        };

        return Result.success(null, "订单状态已更新为：" + statusDesc);
    }
}
