package org.example.petbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.petbackend.entity.PetInsuranceMediaContent;

import java.util.List;

/**
 * 保险媒体图片 Service 接口
 */
public interface PetInsuranceMediaContentService extends IService<PetInsuranceMediaContent> {
    /**
     * 根据保险ID查询所有关联的媒体图片
     * @param insuranceId 保险ID
     * @return 媒体图片列表
     */
    List<PetInsuranceMediaContent> getMediaListByInsuranceId(Integer insuranceId);

    /**
     * 根据保险ID+内容类型查询图片
     * @param insuranceId 保险ID
     * @param contentType 内容类型 1=产品特色图 2=理赔案例图
     * @return 对应类型的图片列表
     */
    List<PetInsuranceMediaContent> getMediaListByInsuranceIdAndType(Integer insuranceId, Byte contentType);

    /**
     * 根据保险ID删除所有关联的媒体图片
     * @param insuranceId 保险ID
     * @return 是否删除成功
     */
    boolean deleteMediaByInsuranceId(Integer insuranceId);
}
