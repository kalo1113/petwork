package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.UserReceiverAddress;
import org.example.petbackend.mapper.UserReceiverAddressMapper;
import org.example.petbackend.service.UserReceiverAddressService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserReceiverAddressServiceImpl extends ServiceImpl<UserReceiverAddressMapper, UserReceiverAddress> implements UserReceiverAddressService {

    @Override
    public List<UserReceiverAddress> getAddressListByUserId(Integer userId) {
        // 查询该用户的所有地址
        return baseMapper.selectList(new LambdaQueryWrapper<UserReceiverAddress>()
                .eq(UserReceiverAddress::getUserId, userId)
                .orderByDesc(UserReceiverAddress::getIsDefault) // 默认地址排前面
        );
    }

    @Override
    public UserReceiverAddress getDefaultAddressByUserId(Integer userId) {
        // 查询该用户的默认地址
        return baseMapper.selectOne(new LambdaQueryWrapper<UserReceiverAddress>()
                .eq(UserReceiverAddress::getUserId, userId)
                .eq(UserReceiverAddress::getIsDefault, 1)
        );
    }
}
