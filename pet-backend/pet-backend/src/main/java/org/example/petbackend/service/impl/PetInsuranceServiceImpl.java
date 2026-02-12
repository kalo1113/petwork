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
 * 适配自定义的PetInsuranceService接口
 */
@Service
public class PetInsuranceServiceImpl extends ServiceImpl<PetInsuranceMapper, PetInsurance>
        implements PetInsuranceService {

    /**
     * 根据宠物类型查询上架的保险套餐列表
     * @param petType 1=猫咪 2=狗狗 3=通用
     * @return 符合条件的保险套餐列表
     */
    @Override
    public List<PetInsurance> getPackageListByPetType(Integer petType) {
        LambdaQueryWrapper<PetInsurance> queryWrapper = new LambdaQueryWrapper<>();
        // 条件1：宠物类型匹配（1/2/3）
        queryWrapper.eq(PetInsurance::getPetType, petType)
                // 条件2：仅查询上架状态（1=上架）
                .eq(PetInsurance::getStatus, 1)
                // 按保障方案类型升序、价格降序排列
                .orderByAsc(PetInsurance::getPlanType)
                .orderByDesc(PetInsurance::getDiscountPremium);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据保险编号查询套餐信息
     * @param insuranceNo 保险编号（如INS2026001）
     * @return 保险套餐信息（null=不存在）
     */
    @Override
    public PetInsurance getPackageByInsuranceNo(String insuranceNo) {
        LambdaQueryWrapper<PetInsurance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsurance::getInsuranceNo, insuranceNo);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 修改保险套餐上下架状态
     * @param id 保险套餐ID
     * @param status 1=上架 0=下架
     * @return true=修改成功，false=修改失败
     */
    @Override
    public boolean updatePackageStatus(Long id, Integer status) {
        // 校验状态合法性
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值无效（仅支持1=上架/0=下架）");
        }
        PetInsurance insurance = new PetInsurance();
        insurance.setId(id.intValue()); // 适配你的Integer类型主键
        insurance.setStatus(status);
        return updateById(insurance);
    }
}
