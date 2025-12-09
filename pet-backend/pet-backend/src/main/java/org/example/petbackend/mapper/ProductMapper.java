package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.Product;
import org.springframework.stereotype.Repository;

@Repository // 标识数据访问层组件（与 PetMapper 保持一致）
public interface ProductMapper extends BaseMapper<Product> {
    // 无需手动编写 findAll()、findByCategory() 等基础方法
    // BaseMapper 已自带：
    // - selectList(null) → 等价于 findAll()（查询所有）
    // - selectList(queryWrapper) → 可通过条件构造器实现按分类查询（等价于 findByCategory）
}
