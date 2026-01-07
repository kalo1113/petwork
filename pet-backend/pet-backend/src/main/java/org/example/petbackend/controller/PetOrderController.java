package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.PetOrderMain;
import org.example.petbackend.entity.PetOrderItem;
import org.example.petbackend.entity.Product;
import org.example.petbackend.service.PetOrderMainService;
import org.example.petbackend.service.PetOrderItemService;
import org.example.petbackend.service.ProductService;
import org.example.petbackend.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/order")
public class PetOrderController {

    @Resource
    private PetOrderMainService orderMainService;
    @Resource
    private PetOrderItemService orderItemService;
    @Resource
    private UserService userService;
    @Resource
    private ProductService productService;

    // 1. 创建订单（无需修改）
    @PostMapping("/create")
    public Result<?> createOrder(
            @RequestParam Integer userId,
            @RequestBody List<PetOrderItem> itemList,
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String receiverAddress) {

        if (userService.getById(userId) == null) {
            return Result.fail("用户不存在，无法创建订单");
        }

        if (itemList == null || itemList.isEmpty()) {
            return Result.fail("请选择要购买的商品");
        }

        long orderId = System.currentTimeMillis() + new Random().nextInt(1000);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PetOrderItem item : itemList) {
            item.setOrderId(orderId);
            totalAmount = totalAmount.add(item.getItemAmount());
        }

        PetOrderMain orderMain = new PetOrderMain();
        orderMain.setOrderId(orderId);
        orderMain.setUserId(userId);
        orderMain.setTotalAmount(totalAmount);
        orderMain.setOrderStatus(1);
        orderMain.setReceiverName(receiverName);
        orderMain.setReceiverPhone(receiverPhone);
        orderMain.setReceiverAddress(receiverAddress);

        boolean saveMain = orderMainService.save(orderMain);
        if (!saveMain) {
            return Result.fail("订单创建失败，请稍后重试");
        }

        boolean saveItems = orderItemService.saveBatch(itemList);
        if (!saveItems) {
            orderMainService.removeById(orderId);
            return Result.fail("订单商品明细创建失败");
        }

        return Result.success(orderId, "订单创建成功，订单ID：" + orderId);
    }

    // 2. 查询用户的订单列表（核心修复：添加productDescription + 调试日志）
    @GetMapping("/list")
    public Result<?> getOrderList(@RequestParam Integer userId) {
        if (userService.getById(userId) == null) {
            return Result.fail("用户不存在");
        }

        List<PetOrderMain> orderMainList = orderMainService.lambdaQuery()
                .eq(PetOrderMain::getUserId, userId)
                .orderByDesc(PetOrderMain::getCreateTime)
                .list();

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (PetOrderMain order : orderMainList) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", order.getOrderId());
            orderMap.put("userId", order.getUserId());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("orderStatus", order.getOrderStatus());
            orderMap.put("receiverName", order.getReceiverName());
            orderMap.put("receiverPhone", order.getReceiverPhone());
            orderMap.put("receiverAddress", order.getReceiverAddress());
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("updateTime", order.getUpdateTime());

            List<PetOrderItem> rawItemList = orderItemService.lambdaQuery()
                    .eq(PetOrderItem::getOrderId, order.getOrderId())
                    .list();

            List<Map<String, Object>> itemMapList = new ArrayList<>();
            for (PetOrderItem item : rawItemList) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("productCount", item.getProductCount());
                itemMap.put("productPrice", item.getProductPrice());
                itemMap.put("itemAmount", item.getItemAmount());

                // 关联查询商品信息
                Product product = productService.getProductById(item.getProductId());
                if (product != null) {
                    // 新增：调试日志（关键）
                    System.out.println("【列表接口】商品ID：" + item.getProductId() +
                            "，标题：" + product.getTitle() +
                            "，介绍：" + product.getDescription());

                    itemMap.put("productTitle", product.getTitle());
                    itemMap.put("productImgPath", product.getImgPath());
                    itemMap.put("productNowPrice", product.getNowPrice());
                    itemMap.put("productOldPrice", product.getOldPrice());
                    // 核心修复：添加商品介绍字段 + 空值兜底
                    itemMap.put("productDescription", Optional.ofNullable(product.getDescription()).orElse("暂无介绍"));
                } else {
                    // 商品不存在时的兜底
                    itemMap.put("productTitle", "商品已下架");
                    itemMap.put("productImgPath", "");
                    itemMap.put("productNowPrice", "0.00");
                    itemMap.put("productOldPrice", "0.00");
                    itemMap.put("productDescription", "商品已下架，暂无介绍");
                }
                itemMapList.add(itemMap);
            }

            orderMap.put("itemList", itemMapList);
            resultList.add(orderMap);
        }

        return Result.success(resultList, "订单列表查询成功");
    }

    // 3. 查询订单详情（优化：添加空值兜底）
    @GetMapping("/detail")
    public Result<?> getOrderDetail(@RequestParam Long orderId) {
        PetOrderMain orderMain = orderMainService.getById(orderId);
        if (orderMain == null) {
            return Result.fail("订单不存在");
        }

        List<PetOrderItem> rawItemList = orderItemService.lambdaQuery()
                .eq(PetOrderItem::getOrderId, orderId)
                .list();

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (PetOrderItem item : rawItemList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("orderId", item.getOrderId());
            itemMap.put("productId", item.getProductId());
            itemMap.put("productPrice", item.getProductPrice());
            itemMap.put("productCount", item.getProductCount());
            itemMap.put("itemAmount", item.getItemAmount());

            Product product = productService.getProductById(item.getProductId());
            if (product != null) {
                System.out.println("【详情接口】商品ID：" + item.getProductId() +
                        "，标题：" + product.getTitle() +
                        "，介绍：" + product.getDescription());

                itemMap.put("productTitle", product.getTitle());
                itemMap.put("productImgPath", product.getImgPath());
                itemMap.put("productNowPrice", product.getNowPrice());
                itemMap.put("productOldPrice", product.getOldPrice());
                // 优化：空值兜底
                itemMap.put("productDescription", Optional.ofNullable(product.getDescription()).orElse("暂无介绍"));
            } else {
                itemMap.put("productTitle", "商品已下架");
                itemMap.put("productImgPath", "");
                itemMap.put("productNowPrice", "0.00");
                itemMap.put("productOldPrice", "0.00");
                itemMap.put("productDescription", "商品已下架，暂无介绍");
            }
            itemList.add(itemMap);
        }

        Map<String, Object> orderDetail = new HashMap<>();
        orderDetail.put("orderMain", orderMain);
        orderDetail.put("itemList", itemList);

        return Result.success(orderDetail, "订单详情查询成功");
    }

    // 4. 更新订单状态（无需修改）
    @PostMapping("/updateStatus")
    public Result<?> updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam Integer status) {

        if (status < 0 || status > 4) {
            return Result.fail("订单状态不合法（0=待付款 1=待发货 2=待收货 3=已完成 4=已取消）");
        }

        PetOrderMain orderMain = orderMainService.getById(orderId);
        if (orderMain == null) {
            return Result.fail("订单不存在");
        }

        orderMain.setOrderStatus(status);
        boolean updateSuccess = orderMainService.updateById(orderMain);

        if (!updateSuccess) {
            return Result.fail("订单状态更新失败");
        }

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

    // 5. 新增：删除订单（修复后）
    @PostMapping("/delete")
    public Result<?> deleteOrder(@RequestParam Long orderId) {
        // 1. 删除订单明细表（使用MyBatis-Plus的Wrappers构造条件）
        boolean deleteItems = orderItemService.remove(
                Wrappers.<PetOrderItem>lambdaQuery().eq(PetOrderItem::getOrderId, orderId)
        );

        // 2. 删除订单主表
        boolean deleteMain = orderMainService.removeById(orderId);

        if (deleteItems && deleteMain) {
            return Result.success(null, "订单删除成功");
        } else {
            return Result.fail("订单删除失败");
        }
    }
}
