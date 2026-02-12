package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.Merchant;
import org.example.petbackend.mapper.MerchantMapper;
import org.example.petbackend.service.MerchantService;
import org.springframework.stereotype.Service;

/**
 * 商家服务实现类（继承MyBatis-Plus的ServiceImpl，自动实现基础CRUD）
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Override
    public Merchant getMerchantByUsername(String username) {
        return this.getOne(new QueryWrapper<Merchant>().eq("username", username));
    }

    @Override
    public Merchant getMerchantByPhone(String phone) {
        return this.getOne(new QueryWrapper<Merchant>().eq("phone", phone));
    }

    @Override
    public boolean isPhoneRegistered(String phone) {
        return this.count(new QueryWrapper<Merchant>().eq("phone", phone)) > 0;
    }

    @Override
    public boolean isUsernameExist(String username) {
        return this.count(new QueryWrapper<Merchant>().eq("username", username)) > 0;
    }
}
