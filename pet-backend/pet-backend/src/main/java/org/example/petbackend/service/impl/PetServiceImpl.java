package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.Pet;
import org.example.petbackend.mapper.PetMapper;
import org.example.petbackend.service.PetService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;

@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    @Override
    public boolean addPet(Pet pet) {
        pet.setCreateTime(LocalDateTime.now());
        pet.setUpdateTime(LocalDateTime.now());
        return baseMapper.insert(pet) > 0;
    }

    // 关键：重写父类的 Serializable 参数版 getById，而非自定义 Long 参数版
    @Override
    public Pet getById(Serializable id) {
        // 安全转换：无论传 Integer/Long，都转为 Long 后查询
        Long petId = null;
        if (id instanceof Long) {
            petId = (Long) id;
        } else if (id instanceof Integer) {
            petId = ((Integer) id).longValue(); // Integer 转 Long，避免强转报错
        }
        // 调用 baseMapper 查询，确保参数是 Long 类型
        return petId != null ? baseMapper.selectById(petId) : null;
    }
}
