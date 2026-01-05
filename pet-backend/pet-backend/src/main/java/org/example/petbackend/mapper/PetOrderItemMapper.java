package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.PetOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PetOrderItemMapper extends BaseMapper<PetOrderItem> {
    // 批量插入商品明细（MyBatis-Plus默认不提供，手动写）
    @Insert("<script>" +
            "INSERT INTO pet_order_item (order_id, product_id, product_title, product_img, product_price, product_count, item_amount) " +
            "VALUES " +
            "<foreach collection='itemList' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productId}, #{item.productTitle}, #{item.productImg}, #{item.productPrice}, #{item.productCount}, #{item.itemAmount})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("itemList") List<PetOrderItem> itemList);
}
