package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.Pet;
import org.example.petbackend.mapper.PetMapper;
import org.example.petbackend.service.PetService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;

@Service // 标识这是业务层组件
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    @Override
    public boolean addPet(Pet pet) {
        // 补充默认值：创建时间和更新时间
        pet.setCreateTime(LocalDateTime.now());
        pet.setUpdateTime(LocalDateTime.now());
        // 调用 Mapper 的 insert 方法新增数据（MyBatis-Plus 自带）
        return baseMapper.insert(pet) > 0; // 插入成功返回 true，失败返回 false
    }
//    Serializable 是 Integer 的父接口（Integer 实现了 Serializable），参数类型兼容
    @Override
    public Pet getById(Serializable petId) {
        Integer id = (Integer) petId;
        Pet pet = baseMapper.selectById(id);
        if (pet == null) {
            throw new NullPointerException("宠物不存在"); // 主动抛异常，让全局处理器捕获
        }
        return pet;
    }
}