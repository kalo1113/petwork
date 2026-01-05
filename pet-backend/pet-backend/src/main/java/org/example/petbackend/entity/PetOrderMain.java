package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_order_main") // 对应订单主表
public class PetOrderMain {
    @TableId // 订单ID作为主键
    private Long orderId;
    private Integer userId; // 关联用户ID
    private BigDecimal totalAmount; // 订单总金额
    private Integer orderStatus; // 订单状态（0-4）
    private String receiverName; // 收货人姓名
    private String receiverPhone; // 收货人电话
    private String receiverAddress; // 收货人地址
    private LocalDateTime createTime; // 创建时间（自动填充）
    private LocalDateTime updateTime; // 更新时间（自动填充）
}
