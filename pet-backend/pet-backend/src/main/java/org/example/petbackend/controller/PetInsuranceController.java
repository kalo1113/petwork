package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.PetInsurance;
import org.example.petbackend.entity.PetInsuranceMediaContent;
import org.example.petbackend.service.PetInsuranceMediaContentService;
import org.example.petbackend.service.PetInsuranceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物保险控制器
 * 负责PetInsurance和PetInsuranceMediaContent两张表的CRUD操作
 */
@RestController
@RequestMapping("/insurance")
public class PetInsuranceController {

    @Resource
    private PetInsuranceService petInsuranceService;

    @Resource
    private PetInsuranceMediaContentService petInsuranceMediaContentService;

    // ====================== 宠物保险主表操作 ======================

    /**
     * 新增保险产品
     * @param petInsurance 保险产品信息
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result<?> addInsurance(@RequestBody PetInsurance petInsurance) {
        // 基础参数校验
        if (petInsurance.getInsuranceName() == null || petInsurance.getInsuranceName().trim().isEmpty()) {
            return Result.fail("保险名称不能为空");
        }
        if (petInsurance.getDiscountPremium() == null) {
            return Result.fail("优惠保费不能为空");
        }

        // 补充默认值
        if (petInsurance.getStatus() == null) {
            petInsurance.setStatus(1); // 默认上架
        }
        if (petInsurance.getPutOnShelfTime() == null) {
            petInsurance.setPutOnShelfTime(LocalDateTime.now()); // 默认当前时间上架
        }
        petInsurance.setCreateTime(LocalDateTime.now());
        petInsurance.setUpdateTime(LocalDateTime.now());

        boolean save = petInsuranceService.save(petInsurance);
        if (save) {
            return Result.success(petInsurance.getId(), "保险产品新增成功");
        } else {
            return Result.fail("保险产品新增失败");
        }
    }

    /**
     * 修改保险产品
     * @param petInsurance 保险产品信息
     * @return 修改结果
     */
    @PostMapping("/update")
    public Result<?> updateInsurance(@RequestBody PetInsurance petInsurance) {
        // 主键校验
        if (petInsurance.getId() == null) {
            return Result.fail("保险ID不能为空");
        }

        // 补充更新时间
        petInsurance.setUpdateTime(LocalDateTime.now());

        boolean update = petInsuranceService.updateById(petInsurance);
        if (update) {
            return Result.success(null, "保险产品修改成功");
        } else {
            return Result.fail("保险产品修改失败（可能产品不存在）");
        }
    }

    /**
     * 删除保险产品（级联删除关联的媒体图片）
     * @param id 保险ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    public Result<?> deleteInsurance(@PathVariable Integer id) {
        // 1. 删除关联的媒体图片
        petInsuranceMediaContentService.remove(
                Wrappers.<PetInsuranceMediaContent>lambdaQuery()
                        .eq(PetInsuranceMediaContent::getInsuranceId, id)
        );

        // 2. 删除保险主表数据
        boolean remove = petInsuranceService.removeById(id);
        if (remove) {
            return Result.success(null, "保险产品及关联图片删除成功");
        } else {
            return Result.fail("保险产品删除失败");
        }
    }

    /**
     * 分页查询保险产品列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param insuranceName 保险名称（模糊查询）
     * @param petType 适用宠物类型（1=猫咪 2=狗狗 3=通用）
     * @param status 状态（1=上架 0=下架）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<?> getInsurancePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String insuranceName,
            @RequestParam(required = false) Byte petType,
            @RequestParam(required = false) Integer status) {

        // 构建查询条件
        LambdaQueryWrapper<PetInsurance> wrapper = Wrappers.lambdaQuery();
        if (insuranceName != null && !insuranceName.trim().isEmpty()) {
            wrapper.like(PetInsurance::getInsuranceName, insuranceName);
        }
        if (petType != null) {
            wrapper.eq(PetInsurance::getPetType, petType);
        }
        if (status != null) {
            wrapper.eq(PetInsurance::getStatus, status);
        }
        // 按更新时间倒序
        wrapper.orderByDesc(PetInsurance::getUpdateTime);

        // 分页查询
        Page<PetInsurance> page = petInsuranceService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page, "保险产品列表查询成功");
    }

    /**
     * 根据ID查询保险产品详情（包含关联的媒体图片）
     * @param id 保险ID
     * @return 保险详情+媒体图片
     */
    @GetMapping("/detail/{id}")
    public Result<?> getInsuranceDetail(@PathVariable Integer id) {
        // 1. 查询主表信息
        PetInsurance insurance = petInsuranceService.getById(id);
        if (insurance == null) {
            return Result.fail("保险产品不存在");
        }

        // 2. 查询关联的媒体图片
        List<PetInsuranceMediaContent> mediaList = petInsuranceMediaContentService.list(
                Wrappers.<PetInsuranceMediaContent>lambdaQuery()
                        .eq(PetInsuranceMediaContent::getInsuranceId, id)
                        .orderByAsc(PetInsuranceMediaContent::getContentType)
        );

        // 3. 组装返回结果
        return Result.success(
                new java.util.HashMap<String, Object>() {{
                    put("insurance", insurance);
                    put("mediaList", mediaList);
                }},
                "保险产品详情查询成功"
        );
    }

    /**
     * 更新保险产品状态（上架/下架）
     * @param id 保险ID
     * @param status 状态（1=上架 0=下架）
     * @return 更新结果
     */
    @PostMapping("/updateStatus")
    public Result<?> updateInsuranceStatus(
            @RequestParam Integer id,
            @RequestParam Integer status) {

        if (status != 0 && status != 1) {
            return Result.fail("状态值只能是0（下架）或1（上架）");
        }

        PetInsurance insurance = new PetInsurance();
        insurance.setId(id);
        insurance.setStatus(status);
        insurance.setUpdateTime(LocalDateTime.now());
        // 上架时补充上架时间
        if (status == 1 && petInsuranceService.getById(id).getPutOnShelfTime() == null) {
            insurance.setPutOnShelfTime(LocalDateTime.now());
        }

        boolean update = petInsuranceService.updateById(insurance);
        if (update) {
            String statusDesc = status == 1 ? "上架" : "下架";
            return Result.success(null, "保险产品" + statusDesc + "成功");
        } else {
            return Result.fail("保险产品状态更新失败");
        }
    }

    // ====================== 保险媒体图片操作 ======================

    /**
     * 新增保险媒体图片
     * @param mediaContent 媒体图片信息
     * @return 新增结果
     */
    @PostMapping("/media/add")
    public Result<?> addInsuranceMedia(@RequestBody PetInsuranceMediaContent mediaContent) {
        // 基础校验
        if (mediaContent.getInsuranceId() == null) {
            return Result.fail("关联的保险ID不能为空");
        }
        if (mediaContent.getContentType() == null) {
            return Result.fail("内容类型不能为空");
        }
        // 图片路径或说明至少填一个（根据类型）
        if (mediaContent.getImgPath() == null && mediaContent.getImgRemark() == null) {
            return Result.fail("图片路径和图片说明不能同时为空");
        }

        // 补充默认值
        mediaContent.setCreateTime(LocalDateTime.now());

        boolean save = petInsuranceMediaContentService.save(mediaContent);
        if (save) {
            return Result.success(mediaContent.getId(), "媒体图片新增成功");
        } else {
            return Result.fail("媒体图片新增失败");
        }
    }

    /**
     * 修改保险媒体图片
     * @param mediaContent 媒体图片信息
     * @return 修改结果
     */
    @PostMapping("/media/update")
    public Result<?> updateInsuranceMedia(@RequestBody PetInsuranceMediaContent mediaContent) {
        if (mediaContent.getId() == null) {
            return Result.fail("媒体图片ID不能为空");
        }

        boolean update = petInsuranceMediaContentService.updateById(mediaContent);
        if (update) {
            return Result.success(null, "媒体图片修改成功");
        } else {
            return Result.fail("媒体图片修改失败（可能图片不存在）");
        }
    }

    /**
     * 删除保险媒体图片
     * @param id 媒体图片ID
     * @return 删除结果
     */
    @PostMapping("/media/delete/{id}")
    public Result<?> deleteInsuranceMedia(@PathVariable Integer id) {
        boolean remove = petInsuranceMediaContentService.removeById(id);
        if (remove) {
            return Result.success(null, "媒体图片删除成功");
        } else {
            return Result.fail("媒体图片删除失败");
        }
    }

    /**
     * 根据保险ID查询关联的媒体图片
     * @param insuranceId 保险ID
     * @return 媒体图片列表
     */
    @GetMapping("/media/list/{insuranceId}")
    public Result<?> getInsuranceMediaList(@PathVariable Integer insuranceId) {
        List<PetInsuranceMediaContent> mediaList = petInsuranceMediaContentService.list(
                Wrappers.<PetInsuranceMediaContent>lambdaQuery()
                        .eq(PetInsuranceMediaContent::getInsuranceId, insuranceId)
                        .orderByAsc(PetInsuranceMediaContent::getContentType)
        );
        return Result.success(mediaList, "媒体图片列表查询成功");
    }
}
