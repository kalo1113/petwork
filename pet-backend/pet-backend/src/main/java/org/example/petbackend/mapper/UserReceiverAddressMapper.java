package org.example.petbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.petbackend.entity.UserReceiverAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserReceiverAddressMapper extends BaseMapper<UserReceiverAddress> {
    // MyBatis-Plus自动提供CRUD方法
}
