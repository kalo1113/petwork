package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("pet_order_item") // 对应订单商品明细表
public class PetOrderItem {
    @TableId
    private Integer id; // 自增主键
    private Long orderId; // 关联订单ID
    private Integer productId; // 商品ID
    private String productTitle; // 商品名称
    private BigDecimal productPrice; // 商品单价（快照）
    private Integer productCount; // 购买数量
    private BigDecimal itemAmount; // 商品小计（单价×数量）
}
