package org.example.petbackend.controller;

import org.example.petbackend.common.Result; // 你的统一返回类
import org.example.petbackend.entity.UserReceiverAddress;
import org.example.petbackend.service.UserReceiverAddressService;
import org.example.petbackend.service.UserService; // 用于校验用户存在
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user/address")
public class UserReceiverAddressController {

    @Resource
    private UserReceiverAddressService addressService;
    @Resource
    private UserService userService;

    // 1. 添加收货地址
    @PostMapping("/add")
    public Result<?> addAddress(@RequestBody UserReceiverAddress address) {
        // 校验用户是否存在
        if (userService.getById(address.getUserId()) == null) {
            return Result.fail("用户不存在，无法添加地址");
        }

        // 若设为默认地址，先把该用户其他地址改为非默认
        if (address.getIsDefault() == 1) {
            addressService.lambdaUpdate()
                    .eq(UserReceiverAddress::getUserId, address.getUserId())
                    .set(UserReceiverAddress::getIsDefault, 0)
                    .update();
        }

        // 插入地址
        boolean success = addressService.save(address);
        return success ? Result.success("地址添加成功") : Result.fail("地址添加失败");
    }

    // 2. 获取用户的所有收货地址
    @GetMapping("/list")
    public Result<?> getAddressList(@RequestParam Integer userId) {
        // 1. 校验用户存在性
        if (userService.getById(userId) == null) {
            return Result.fail("用户不存在");
        }
        // 2. 查询地址（无数据时返回空数组）
        List<UserReceiverAddress> addressList = addressService.getAddressListByUserId(userId);
        // 3. 无论是否有数据，都返回成功状态
        return Result.success(addressList, addressList.isEmpty() ? "暂无收货地址" : "地址列表查询成功");
    }

    // 3. 获取用户的默认收货地址
    @GetMapping("/default")
    public Result<?> getDefaultAddress(@RequestParam Integer userId) {
        UserReceiverAddress defaultAddress = addressService.getDefaultAddressByUserId(userId);
        return defaultAddress != null ? Result.success(defaultAddress) : Result.fail("该用户暂无默认地址");
    }

    // 4. 修改收货地址
    @PutMapping("/update")
    public Result<?> updateAddress(@RequestBody UserReceiverAddress address) {
        // 校验地址是否属于该用户
        UserReceiverAddress oldAddress = addressService.getById(address.getId());
        if (oldAddress == null || !oldAddress.getUserId().equals(address.getUserId())) {
            return Result.fail("地址不存在或无权修改");
        }

        // 若设为默认地址，同步修改其他地址
        if (address.getIsDefault() == 1) {
            addressService.lambdaUpdate()
                    .eq(UserReceiverAddress::getUserId, address.getUserId())
                    .set(UserReceiverAddress::getIsDefault, 0)
                    .update();
        }

        boolean success = addressService.updateById(address);
        return success ? Result.success("地址修改成功") : Result.fail("地址修改失败");
    }

    // 5. 删除收货地址
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteAddress(@PathVariable Long id, @RequestParam Integer userId) {
        // 校验地址是否属于该用户
        UserReceiverAddress address = addressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail("地址不存在或无权删除");
        }

        boolean success = addressService.removeById(id);
        return success ? Result.success("地址删除成功") : Result.fail("地址删除失败");
    }
}
