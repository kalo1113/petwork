package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
    // 无需手动编写SQL，MyBatis-Plus 已提供通用增删改查方法
}
