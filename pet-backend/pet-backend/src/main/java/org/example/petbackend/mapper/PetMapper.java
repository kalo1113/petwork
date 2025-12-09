package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.Pet;
import org.springframework.stereotype.Repository;

@Repository // 标识这是数据访问层组件
public interface PetMapper extends BaseMapper<Pet> {
    // 无需写额外方法，BaseMapper 已包含 insert（新增）、selectById（查询）等
}