package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.Merchant;

/**
 * 商家服务接口（继承MyBatis-Plus的IService，自动拥有CRUD基础方法）
 */
public interface MerchantService extends IService<Merchant> {
    // 自定义业务方法：根据登录账号查询商家（毕设核心业务方法）
    Merchant getMerchantByUsername(String username);

    // 根据手机号查询商家
    Merchant getMerchantByPhone(String phone);

    // 校验手机号是否已注册
    boolean isPhoneRegistered(String phone);

    // 校验账号是否已存在
    boolean isUsernameExist(String username);
}
