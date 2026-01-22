package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.PetInsurance;

import java.util.List;

/**
 * 宠物保险套餐 Service 接口
 */
public interface PetInsuranceService extends IService<PetInsurance> {
    // 根据宠物类型查询上架的保险套餐列表（1=猫咪 2=狗狗 3=通用）
    List<PetInsurance> getPackageListByPetType(Integer petType);
    // 根据保险编号查询套餐信息
    PetInsurance getPackageByInsuranceNo(String insuranceNo);
    // 修改保险套餐上下架状态（1=上架 0=下架）
    boolean updatePackageStatus(Long id, Integer status);
}
