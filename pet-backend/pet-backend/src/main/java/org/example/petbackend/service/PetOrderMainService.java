package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.PetOrderMain;

public interface PetOrderMainService extends IService<PetOrderMain> {
    // 继承IService，自动获得CRUD方法
}
