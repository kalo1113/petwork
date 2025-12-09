package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.Pet;

public interface PetService extends IService<Pet> {
    // 继承 IService，自带新增、查询等业务方法
    boolean addPet(Pet pet); // 自定义新增宠物方法（也可直接用自带的 save 方法，这里为了清晰单独定义）


}