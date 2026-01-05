package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.PetOrderMain;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetOrderMainMapper extends BaseMapper<PetOrderMain> {
    // MyBatis-Plus自动提供CRUD方法，无需额外编写
}
