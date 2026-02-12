package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    // 5. 新增：确认收货接口（核心补充）
    @PostMapping("/confirmReceive")
    public Result<?> confirmReceiveOrder(
            @RequestParam Long orderId,
            @RequestParam Integer userId) {
        // 1. 校验用户是否存在
        if (userService.getById(userId) == null) {
            log.warn("确认收货失败：用户不存在，userId={}", userId);
            return Result.fail("用户不存在，无法确认收货");
        }

        // 2. 校验订单是否存在
        PetOrderMain orderMain = orderMainService.getById(orderId);
        if (orderMain == null) {
            log.warn("确认收货失败：订单不存在，orderId={}", orderId);
            return Result.fail("订单不存在，无法确认收货");
        }

        // 3. 校验订单归属（防止用户确认他人订单）
        if (!orderMain.getUserId().equals(userId)) {
            log.warn("确认收货失败：订单归属不符，orderId={}, 操作人userId={}, 订单所属userId={}",
                    orderId, userId, orderMain.getUserId());
            return Result.fail("无权确认该订单收货");
        }

        // 4. 校验订单状态（仅待收货状态可确认收货）
        if (orderMain.getOrderStatus() != 2) {
            String currentStatus = switch (orderMain.getOrderStatus()) {
                case 0 -> "待付款";
                case 1 -> "待发货";
                case 3 -> "已完成";
                case 4 -> "已取消";
                default -> "未知状态";
            };
            log.warn("确认收货失败：订单状态不合法，orderId={}, 当前状态={}", orderId, currentStatus);
            return Result.fail("仅待收货状态的订单可确认收货，当前订单状态：" + currentStatus);
        }

        // 5. 更新订单状态为已完成（3）
        orderMain.setOrderStatus(3);
        boolean updateSuccess = orderMainService.updateById(orderMain);

        if (updateSuccess) {
            log.info("确认收货成功，orderId={}, userId={}", orderId, userId);
            return Result.success(null, "确认收货成功，订单状态已更新为已完成");
        } else {
            log.error("确认收货失败：订单状态更新失败，orderId={}", orderId);
            return Result.fail("确认收货失败，请稍后重试");
        }
    }

    // 6. 新增：删除订单（修复后）
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

    // ========== 优化：商家端接口 - 分页查询所有商品订单（完善订单号/用户ID查询） ==========
    @GetMapping("/merchant/page")
    public Result<?> getMerchantOrderPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer orderStatus,  // 可选：按状态筛选
            @RequestParam(required = false) Long orderId,         // 可选：按订单ID精准查询
            @RequestParam(required = false) String orderIdLike,   // 新增：按订单ID模糊查询
            @RequestParam(required = false) Integer userId,       // 可选：按用户ID精准查询
            @RequestParam(required = false) String userIdLike)    // 新增：按用户ID模糊查询
    {
        // 1. 初始化分页对象
        Page<PetOrderMain> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件（完善订单号/用户ID查询逻辑）
        LambdaQueryWrapper<PetOrderMain> queryWrapper = Wrappers.lambdaQuery();

        // 2.1 订单状态筛选（原有逻辑）
        if (orderStatus != null) {
            queryWrapper.eq(PetOrderMain::getOrderStatus, orderStatus);
        }

        // 2.2 订单ID查询（优先精准匹配，再模糊匹配）
        if (orderId != null) {
            // 精准匹配订单ID（完全相等）
            queryWrapper.eq(PetOrderMain::getOrderId, orderId);
        } else if (orderIdLike != null && !orderIdLike.trim().isEmpty()) {
            // 模糊匹配订单ID（支持输入部分订单号查询）
            // 注意：Long类型字段模糊查询需转字符串处理
            queryWrapper.apply("CAST(order_id AS CHAR) LIKE {0}", "%" + orderIdLike.trim() + "%");
        }

        // 2.3 用户ID查询（优先精准匹配，再模糊匹配）
        if (userId != null) {
            // 精准匹配用户ID（完全相等）
            queryWrapper.eq(PetOrderMain::getUserId, userId);
        } else if (userIdLike != null && !userIdLike.trim().isEmpty()) {
            // 模糊匹配用户ID（支持输入部分用户ID查询）
            queryWrapper.apply("CAST(user_id AS CHAR) LIKE {0}", "%" + userIdLike.trim() + "%");
        }

        // 按创建时间倒序，最新订单在前
        queryWrapper.orderByDesc(PetOrderMain::getCreateTime);

        // 3. 分页查询订单主表数据
        Page<PetOrderMain> orderPage = orderMainService.page(page, queryWrapper);

        // 4. 组装返回数据（关联明细表+商品图片）
        List<Map<String, Object>> orderList = new ArrayList<>();
        for (PetOrderMain orderMain : orderPage.getRecords()) {
            // 4.1 封装订单主表信息（完全匹配你的PetOrderMain实体字段）
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", orderMain.getOrderId());
            orderMap.put("userId", orderMain.getUserId());
            orderMap.put("totalAmount", orderMain.getTotalAmount());
            orderMap.put("orderStatus", orderMain.getOrderStatus());
            orderMap.put("receiverName", orderMain.getReceiverName());
            orderMap.put("receiverPhone", orderMain.getReceiverPhone());
            orderMap.put("receiverAddress", orderMain.getReceiverAddress());
            orderMap.put("createTime", orderMain.getCreateTime());
            orderMap.put("updateTime", orderMain.getUpdateTime());

            // 4.2 查询该订单的明细表数据（适配你的PetOrderItem实体）
            List<PetOrderItem> itemList = orderItemService.lambdaQuery()
                    .eq(PetOrderItem::getOrderId, orderMain.getOrderId())
                    .list();

            // 4.3 封装明细表+商品图片信息
            List<Map<String, Object>> itemMapList = new ArrayList<>();
            for (PetOrderItem item : itemList) {
                Map<String, Object> itemMap = new HashMap<>();
                // 明细表字段（完全匹配PetOrderItem实体）
                itemMap.put("id", item.getId());
                itemMap.put("orderId", item.getOrderId());
                itemMap.put("productId", item.getProductId());
                itemMap.put("productTitle", item.getProductTitle()); // 用明细表的商品名称快照
                itemMap.put("productPrice", item.getProductPrice());
                itemMap.put("productCount", item.getProductCount());
                itemMap.put("itemAmount", item.getItemAmount());

                // 4.4 关联商品表，获取图片信息
                Product product = productService.getProductById(item.getProductId());
                if (product != null) {
                    // 拼接完整的商品图片URL（复用你现有的工具方法）
                    String fullImgUrl = buildFullImgUrl(product.getImgPath());
                    itemMap.put("productImgPath", fullImgUrl);       // 商品图片完整URL
                    itemMap.put("productNowPrice", product.getNowPrice()); // 商品当前价格
                    itemMap.put("productOldPrice", product.getOldPrice()); // 商品原价
                    itemMap.put("productDescription", Optional.ofNullable(product.getDescription()).orElse("暂无介绍"));
                } else {
                    // 商品不存在时的兜底处理
                    itemMap.put("productImgPath", "");
                    itemMap.put("productNowPrice", "0.00");
                    itemMap.put("productOldPrice", "0.00");
                    itemMap.put("productDescription", "商品已下架");
                }
                itemMapList.add(itemMap);
            }

            orderMap.put("itemList", itemMapList); // 关联明细表数据
            orderList.add(orderMap);
        }

        // 5. 封装分页返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", orderList);       // 当前页订单数据
        result.put("total", orderPage.getTotal()); // 总订单数
        result.put("pageNum", pageNum);         // 当前页码
        result.put("pageSize", pageSize);       // 每页条数
        result.put("pages", orderPage.getPages()); // 总页数

        return Result.success(result, "商家端订单列表查询成功");
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
