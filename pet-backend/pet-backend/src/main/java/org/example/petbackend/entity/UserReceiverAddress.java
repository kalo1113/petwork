package org.example.petbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_receiver_address") // 对应收货地址表
public class UserReceiverAddress {
    @TableId(type = IdType.AUTO) // 自增主键
    private Long id;
    private Integer userId; // 关联用户表的user_id
    private String receiverName; // 收货人姓名（可与用户表不同）
    private String receiverPhone; // 收货人电话
    private String receiverProvince; // 省
    private String receiverCity; // 市
    private String receiverDistrict; // 区/县
    private String receiverDetailAddress; // 详细地址
    private Integer isDefault; // 是否默认地址：0=否 1=是
    private LocalDateTime createTime; // 创建时间（自动填充）
    private LocalDateTime updateTime; // 更新时间（自动填充）
}
