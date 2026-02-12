package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.petbackend.entity.PetInsuranceOrder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * 宠物保险订单Mapper接口
 * 基于MyBatis-Plus BaseMapper，自带CRUD方法
 */
@Repository // 标识这是数据访问层组件
public interface PetInsuranceOrderMapper extends BaseMapper<PetInsuranceOrder> {
    /**
     * 一次性缴清剩余保费
     * @param orderId 订单ID
     * @param payAmount 本次缴纳金额
     * @param totalAmount 缴清后的总金额
     * @return 受影响行数
     */
    @Update("UPDATE pet_insurance_order " +
            "SET total_amount = #{totalAmount}, updated_at = NOW() " +
            "WHERE id = #{orderId} AND order_status IN (0, 1)")
    int payRemainingPremium(@Param("orderId") Long orderId,
                            @Param("payAmount") BigDecimal payAmount,
                            @Param("totalAmount") BigDecimal totalAmount);
}
