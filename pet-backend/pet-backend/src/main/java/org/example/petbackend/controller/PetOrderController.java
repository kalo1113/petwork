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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/order")
public class PetOrderController {
    // 新增日志组件，方便排查问题
    private static final Logger log = LoggerFactory.getLogger(PetOrderController.class);

    @Resource
    private PetOrderMainService orderMainService;
    @Resource
    private PetOrderItemService orderItemService;
    @Resource
    private UserService userService;
    @Resource
    private ProductService productService;

    // 注入服务器域名（和头像/宠物图片保持一致，从yml配置读取）
    @Value("${server.domain:http://localhost:8080}")
    private String serverDomain;

    // 注入商品图片访问前缀（对应yml中的upload.product-img-access-path）
    @Value("${upload.product-img-access-path:/product-img}")
    private String productImgAccessPath;

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

    // 2. 查询用户的订单列表（核心修复：图片路径拼接完整URL）
    @GetMapping("/list")
    public Result<?> getOrderList(@RequestParam Integer userId) {
        if (userService.getById(userId) == null) {
            log.warn("查询订单列表失败：用户不存在，userId={}", userId);
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
                    // 调试日志
                    log.info("【订单列表】商品ID：{}，标题：{}，原始图片路径：{}",
                            item.getProductId(), product.getTitle(), product.getImgPath());

                    itemMap.put("productTitle", product.getTitle());
                    // 核心修复：拼接完整的图片访问URL
                    String fullImgUrl = buildFullImgUrl(product.getImgPath());
                    itemMap.put("productImgPath", fullImgUrl);
                    itemMap.put("productNowPrice", product.getNowPrice());
                    itemMap.put("productOldPrice", product.getOldPrice());
                    // 空值兜底
                    itemMap.put("productDescription", Optional.ofNullable(product.getDescription()).orElse("暂无介绍"));
                } else {
                    // 商品不存在时的兜底
                    log.warn("【订单列表】商品ID：{} 不存在", item.getProductId());
                    itemMap.put("productTitle", "商品已下架");
                    itemMap.put("productImgPath", ""); // 空路径避免前端报错
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

    // 3. 查询订单详情（核心修复：图片路径拼接完整URL）
    @GetMapping("/detail")
    public Result<?> getOrderDetail(@RequestParam Long orderId) {
        PetOrderMain orderMain = orderMainService.getById(orderId);
        if (orderMain == null) {
            log.warn("查询订单详情失败：订单不存在，orderId={}", orderId);
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
                log.info("【订单详情】商品ID：{}，标题：{}，原始图片路径：{}",
                        item.getProductId(), product.getTitle(), product.getImgPath());

                itemMap.put("productTitle", product.getTitle());
                // 核心修复：拼接完整的图片访问URL
                String fullImgUrl = buildFullImgUrl(product.getImgPath());
                itemMap.put("productImgPath", fullImgUrl);
                itemMap.put("productNowPrice", product.getNowPrice());
                itemMap.put("productOldPrice", product.getOldPrice());
                // 空值兜底
                itemMap.put("productDescription", Optional.ofNullable(product.getDescription()).orElse("暂无介绍"));
            } else {
                log.warn("【订单详情】商品ID：{} 不存在", item.getProductId());
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
        // 1. 删除订单明细表
        boolean deleteItems = orderItemService.remove(
                Wrappers.<PetOrderItem>lambdaQuery().eq(PetOrderItem::getOrderId, orderId)
        );

        // 2. 删除订单主表
        boolean deleteMain = orderMainService.removeById(orderId);

        if (deleteItems && deleteMain) {
            log.info("订单删除成功，orderId={}", orderId);
            return Result.success(null, "订单删除成功");
        } else {
            log.error("订单删除失败，orderId={}", orderId);
            return Result.fail("订单删除失败");
        }
    }

    /**
     * 工具方法：拼接完整的商品图片访问URL
     * 处理各种路径情况：空路径、已拼接域名、仅文件名、带前缀路径
     */
    private String buildFullImgUrl(String rawImgPath) {
        // 空路径直接返回空
        if (rawImgPath == null || rawImgPath.trim().isEmpty()) {
            return "";
        }

        // 已包含完整域名（如http://xxx），直接返回
        if (rawImgPath.startsWith("http://") || rawImgPath.startsWith("https://")) {
            return rawImgPath;
        }

        // 仅文件名（如xxx.png），拼接域名+商品图片前缀
        if (!rawImgPath.startsWith("/")) {
            return serverDomain + productImgAccessPath + "/" + rawImgPath;
        }

        // 带前缀路径（如/product-img/xxx.png），拼接域名
        return serverDomain + rawImgPath;
    }
}
