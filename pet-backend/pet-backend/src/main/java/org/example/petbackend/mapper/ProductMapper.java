package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.Product;
import org.springframework.stereotype.Repository;

@Repository // 标识数据访问层组件（与 PetMapper 保持一致）
public interface ProductMapper extends BaseMapper<Product> {
    // 无需手动编写任何方法！所有CRUD都由BaseMapper提供
    // 包括：selectById、selectList、selectCount 等
}
