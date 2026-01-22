package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.PetInsurance;
import org.example.petbackend.mapper.PetInsuranceMapper;
import org.example.petbackend.service.PetInsuranceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 宠物保险套餐 Service 实现类
 */
@Service
public class PetInsuranceServiceImpl extends ServiceImpl<PetInsuranceMapper, PetInsurance> implements PetInsuranceService {

    /**
     * 根据宠物类型查询上架的保险套餐列表
     */
    @Override
    public List<PetInsurance> getPackageListByPetType(Integer petType) {
        LambdaQueryWrapper<PetInsurance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsurance::getPetType, petType) // 按宠物类型筛选
                .eq(PetInsurance::getStatus, 1); // 仅查询上架状态
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据保险编号查询套餐信息
     */
    @Override
    public PetInsurance getPackageByInsuranceNo(String insuranceNo) {
        LambdaQueryWrapper<PetInsurance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsurance::getInsuranceNo, insuranceNo);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 修改保险套餐上下架状态
     */
    @Override
    public boolean updatePackageStatus(Long id, Integer status) {
        PetInsurance petInsurance = new PetInsurance();
        petInsurance.setId(id.intValue());
        // 直接赋值，不需要转成byte
        petInsurance.setStatus(status);
        return updateById(petInsurance);
    }
}
