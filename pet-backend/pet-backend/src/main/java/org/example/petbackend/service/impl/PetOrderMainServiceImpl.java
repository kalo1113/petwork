package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.PetOrderMain;
import org.example.petbackend.mapper.PetOrderMainMapper;
import org.example.petbackend.service.PetOrderMainService;
import org.springframework.stereotype.Service;

@Service
public class PetOrderMainServiceImpl extends ServiceImpl<PetOrderMainMapper, PetOrderMain> implements PetOrderMainService {
    // 无需额外代码，继承ServiceImpl即可
}
