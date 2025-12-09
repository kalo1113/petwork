// UserService 接口
package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.User;
/**
 * 用户服务接口（继承MyBatis-Plus的IService，自动拥有CRUD基础方法）
 */
public interface UserService extends IService<User> {
    // 自定义业务方法示例：根据邮箱查询用户
    User getUserByEmail(String email);
}
