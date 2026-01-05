package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cart") // 对应数据库的cart表
public class Cart {
    @TableId(type = IdType.AUTO)
    private Integer cartId; // 购物车项ID（自增主键）

    private Integer userId; // 关联用户ID

    private Integer productId; // 关联商品ID

    private Integer productCount; // 商品数量

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间（自动填充）

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间（自动填充）

}
