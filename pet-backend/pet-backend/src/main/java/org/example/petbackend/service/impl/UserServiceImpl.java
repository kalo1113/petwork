// UserService 实现类
package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.User;
import org.example.petbackend.mapper.UserMapper;
import org.example.petbackend.service.UserService;
import org.springframework.stereotype.Service;
/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    // 实现“根据邮箱查询用户”的自定义方法
    @Override
    public User getUserByEmail(String email) {
        // 利用MyBatis-Plus的lambda查询
        return lambdaQuery()
                .eq(User::getEmail, email)
                .one();
    }
}
