package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.PetOrderItem;
import org.example.petbackend.mapper.PetOrderItemMapper;
import org.example.petbackend.service.PetOrderItemService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PetOrderItemServiceImpl extends ServiceImpl<PetOrderItemMapper, PetOrderItem> implements PetOrderItemService {

    @Override
    public boolean batchAddItem(List<PetOrderItem> itemList) {
        // 调用Mapper的批量插入方法
        return baseMapper.batchInsert(itemList) > 0;
    }
}
