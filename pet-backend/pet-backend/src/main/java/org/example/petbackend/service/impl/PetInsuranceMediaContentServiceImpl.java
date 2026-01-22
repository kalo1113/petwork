package org.example.petbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.petbackend.entity.PetInsuranceMediaContent;
import org.example.petbackend.mapper.PetInsuranceMediaContentMapper;
import org.example.petbackend.service.PetInsuranceMediaContentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 保险媒体图片 Service 实现类
 */
@Service
public class PetInsuranceMediaContentServiceImpl extends ServiceImpl<PetInsuranceMediaContentMapper, PetInsuranceMediaContent> implements PetInsuranceMediaContentService {

    @Override
    public List<PetInsuranceMediaContent> getMediaListByInsuranceId(Integer insuranceId) {
        // 构造查询条件：仅按保险ID筛选
        LambdaQueryWrapper<PetInsuranceMediaContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsuranceMediaContent::getInsuranceId, insuranceId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<PetInsuranceMediaContent> getMediaListByInsuranceIdAndType(Integer insuranceId, Byte contentType) {
        // 构造查询条件：保险ID + 内容类型
        LambdaQueryWrapper<PetInsuranceMediaContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsuranceMediaContent::getInsuranceId, insuranceId)
                .eq(PetInsuranceMediaContent::getContentType, contentType);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteMediaByInsuranceId(Integer insuranceId) {
        // 构造删除条件：按保险ID删除
        LambdaQueryWrapper<PetInsuranceMediaContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInsuranceMediaContent::getInsuranceId, insuranceId);
        // 删除行数>0则表示成功
        return baseMapper.delete(queryWrapper) > 0;
    }
}
