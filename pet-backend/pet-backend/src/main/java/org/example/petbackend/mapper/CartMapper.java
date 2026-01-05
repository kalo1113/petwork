package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    // MyBatis Plus已提供基础CRUD，如需复杂查询可在此扩展
}
