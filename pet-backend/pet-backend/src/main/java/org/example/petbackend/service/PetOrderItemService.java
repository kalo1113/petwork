package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.PetOrderItem;
import java.util.List;

public interface PetOrderItemService extends IService<PetOrderItem> {
    // 批量插入商品明细
    default boolean batchAddItem(List<PetOrderItem> itemList) {
        return saveBatch(itemList);
}}
