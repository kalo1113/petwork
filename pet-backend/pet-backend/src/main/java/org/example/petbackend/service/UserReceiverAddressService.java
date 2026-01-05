package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.UserReceiverAddress;
import java.util.List;

public interface UserReceiverAddressService extends IService<UserReceiverAddress> {
    // 获取用户的所有收货地址
    List<UserReceiverAddress> getAddressListByUserId(Integer userId);
    // 获取用户的默认收货地址
    UserReceiverAddress getDefaultAddressByUserId(Integer userId);
}
