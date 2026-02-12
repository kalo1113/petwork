package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.PetInsurance;
import org.example.petbackend.entity.PetInsuranceMediaContent;
import org.example.petbackend.service.PetInsuranceMediaContentService;
import org.example.petbackend.service.PetInsuranceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/insurance")
public class PetInsuranceController {

    private static final Logger log = LoggerFactory.getLogger(PetInsuranceController.class);

    @Resource
    private PetInsuranceService petInsuranceService;

    @Resource
    private PetInsuranceMediaContentService petInsuranceMediaContentService;

    // 完全照搬宠物图片配置
    @Value("${upload.insurance-img-path}")
    private String insuranceImgPath;

    @Value("${upload.insurance-img-access-path:/insurance-img}")
    private String insuranceImgAccessPath;

    private final String IMG_ACCESS_PREFIX = "http://localhost:8080/insurance-img/";

    // ====================== 新增保险 ======================
    @PostMapping("/add")
    public Result<?> addInsurance(@RequestBody PetInsurance petInsurance) {
        if (petInsurance.getInsuranceName() == null || petInsurance.getInsuranceName().trim().isEmpty()) {
            return Result.fail("保险名称不能为空");
        }
        if (petInsurance.getDiscountPremium() == null) {
            return Result.fail("优惠保费不能为空");
        }
        if (petInsurance.getInsuranceNo() == null || petInsurance.getInsuranceNo().trim().isEmpty()) {
            return Result.fail("保险编号不能为空");
        }
        if (petInsurance.getPlanType() == null) {
            return Result.fail("保障方案类型不能为空（1=基础版 2=升级版 3=尊享版）");
        }
        if (petInsurance.getPetType() == null) {
            return Result.fail("适用宠物类型不能为空（1=猫咪 2=狗狗 3=通用）");
        }
        if (petInsurance.getGuaranteeCycle() == null) {
            return Result.fail("保障周期不能为空（如12=年付）");
        }

        if (petInsurance.getStatus() == null) {
            petInsurance.setStatus(1);
        }
        if (petInsurance.getPutOnShelfTime() == null) {
            petInsurance.setPutOnShelfTime(LocalDateTime.now());
        }
        petInsurance.setCreateTime(LocalDateTime.now());
        petInsurance.setUpdateTime(LocalDateTime.now());

        if (petInsurance.getTotalGuarantee() == null) {
            petInsurance.setTotalGuarantee(BigDecimal.ZERO);
        }
        if (petInsurance.getDeductible() == null) {
            petInsurance.setDeductible(BigDecimal.ZERO);
        }
        if (petInsurance.getOutpatientLimit() == null) {
            petInsurance.setOutpatientLimit(BigDecimal.ZERO);
        }
        if (petInsurance.getSurgeryLimit() == null) {
            petInsurance.setSurgeryLimit(BigDecimal.ZERO);
        }

        if (petInsurance.getInNetworkRatio() == null) {
            petInsurance.setInNetworkRatio((byte) 80);
        }
        if (petInsurance.getOutNetworkRatio() == null) {
            petInsurance.setOutNetworkRatio((byte) 50);
        }

        if (petInsurance.getWaitingPeriodAccident() == null) {
            petInsurance.setWaitingPeriodAccident((byte) 0);
        }
        if (petInsurance.getWaitingPeriodDisease() == null) {
            petInsurance.setWaitingPeriodDisease((byte) 30);
        }
        if (petInsurance.getWaitingPeriodCommon() == null) {
            petInsurance.setWaitingPeriodCommon((byte) 15);
        }

        if (petInsurance.getMonthlySubsidy() == null) {
            petInsurance.setMonthlySubsidy(BigDecimal.ZERO);
        }
        if (petInsurance.getGiftService() == null) {
            petInsurance.setGiftService("无");
        }

        boolean save = petInsuranceService.save(petInsurance);
        if (save) {
            return Result.success(petInsurance.getId(), "保险产品新增成功");
        } else {
            return Result.fail("保险产品新增失败");
        }
    }

    // ====================== 修改保险 ======================
    @PostMapping("/update")
    public Result<?> updateInsurance(@RequestBody PetInsurance petInsurance) {
        if (petInsurance.getId() == null) {
            return Result.fail("保险ID不能为空");
        }
        petInsurance.setUpdateTime(LocalDateTime.now());
        boolean update = petInsuranceService.updateById(petInsurance);
        if (update) {
            return Result.success(null, "保险产品修改成功");
        } else {
            return Result.fail("保险产品修改失败（可能产品不存在）");
        }
    }

    // ====================== 删除保险 ======================
    @PostMapping("/delete/{id}")
    public Result<?> deleteInsurance(@PathVariable Integer id) {
        petInsuranceMediaContentService.remove(
                Wrappers.<PetInsuranceMediaContent>lambdaQuery()
                        .eq(PetInsuranceMediaContent::getInsuranceId, id)
        );
        boolean remove = petInsuranceService.removeById(id);
        if (remove) {
            return Result.success(null, "保险产品及关联图片删除成功");
        } else {
            return Result.fail("保险产品删除失败");
        }
    }

    // ====================== 分页/列表 ======================
    @GetMapping("/page")
    public Result<?> getInsurancePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "-1") Integer pageSize,
            @RequestParam(required = false) String insuranceName,
            @RequestParam(required = false) String insuranceNo,
            @RequestParam(required = false) Byte petType,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<PetInsurance> wrapper = Wrappers.lambdaQuery();
        if (insuranceName != null && !insuranceName.trim().isEmpty()) {
            wrapper.like(PetInsurance::getInsuranceName, insuranceName)
                    .or()
                    .like(PetInsurance::getInsuranceNo, insuranceName);
        }
        if (insuranceNo != null && !insuranceNo.trim().isEmpty()) {
            wrapper.like(PetInsurance::getInsuranceNo, insuranceNo);
        }
        if (petType != null) {
            wrapper.eq(PetInsurance::getPetType, petType);
        }
        if (status != null) {
            wrapper.eq(PetInsurance::getStatus, status);
        }
        wrapper.orderByDesc(PetInsurance::getUpdateTime);

        if (pageSize == -1) {
            List<PetInsurance> list = petInsuranceService.list(wrapper);
            return Result.success(new HashMap<String, Object>() {{
                put("records", list);
                put("total", list.size());
                put("pageNum", 1);
                put("pageSize", list.size());
            }}, "保险产品列表查询成功");
        }

        Page<PetInsurance> page = petInsuranceService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page, "保险产品列表查询成功");
    }

    // ====================== 详情 ======================
    @GetMapping("/detail/{id}")
    public Result<?> getInsuranceDetail(@PathVariable Integer id) {
        PetInsurance insurance = petInsuranceService.getById(id);
        if (insurance == null) {
            return Result.fail("保险产品不存在");
        }

        List<PetInsuranceMediaContent> mediaList = petInsuranceMediaContentService.list(
                Wrappers.<PetInsuranceMediaContent>lambdaQuery()
                        .eq(PetInsuranceMediaContent::getInsuranceId, id)
                        .orderByAsc(PetInsuranceMediaContent::getContentType)
        );

        return Result.success(
                new HashMap<String, Object>() {{
                    put("insurance", insurance);
                    put("mediaList", mediaList);
                }},
                "保险产品详情查询成功"
        );
    }

    // ====================== 上下架 ======================
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

    // ====================== 媒体添加 ======================
    @PostMapping("/media/add")
    public Result<?> addInsuranceMedia(@RequestBody PetInsuranceMediaContent mediaContent) {
        Integer insuranceId = null;
        if (mediaContent.getInsuranceId() != null) {
            insuranceId = mediaContent.getInsuranceId().intValue();
            mediaContent.setInsuranceId(insuranceId);
        }

        if (insuranceId == null) {
            return Result.fail("关联的保险ID不能为空");
        }
        if (mediaContent.getContentType() == null) {
            return Result.fail("内容类型不能为空");
        }
        if (mediaContent.getImgPath() == null && mediaContent.getImgRemark() == null) {
            return Result.fail("图片路径和图片说明不能同时为空");
        }

        if (mediaContent.getCreateTime() == null) {
            mediaContent.setCreateTime(LocalDateTime.now());
        }

        boolean save = petInsuranceMediaContentService.save(mediaContent);
        if (save) {
            return Result.success(mediaContent.getId(), "媒体图片新增成功");
        } else {
            return Result.fail("媒体图片新增失败");
        }
    }

    // ====================== 媒体修改 ======================
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

    // ====================== 媒体删除 ======================
    @PostMapping("/media/delete/{id}")
    public Result<?> deleteInsuranceMedia(@PathVariable Integer id) {
        boolean remove = petInsuranceMediaContentService.removeById(id);
        if (remove) {
            return Result.success(null, "媒体图片删除成功");
        } else {
            return Result.fail("媒体图片删除失败");
        }
    }

    // ====================== 媒体列表 ======================
    @GetMapping("/media/list/{insuranceId}")
    public Result<?> getInsuranceMediaList(@PathVariable Integer insuranceId) {
        List<PetInsuranceMediaContent> mediaList = petInsuranceMediaContentService.list(
                Wrappers.<PetInsuranceMediaContent>lambdaQuery()
                        .eq(PetInsuranceMediaContent::getInsuranceId, insuranceId)
                        .orderByAsc(PetInsuranceMediaContent::getContentType)
        );
        return Result.success(mediaList, "媒体图片列表查询成功");
    }

    // ====================== ✅ 最终修复：保险图片上传（和宠物图片完全一致） ======================
    @PostMapping("/media/upload")
    public Result<?> uploadInsuranceMedia(
            @RequestParam(value = "insuranceId") Object insuranceIdObj,
            @RequestParam Integer contentType,
            @RequestParam(required = false) String imgRemark,
            @RequestParam MultipartFile file) {

        Integer insuranceId = null;
        try {
            if (insuranceIdObj instanceof String) {
                String idStr = ((String) insuranceIdObj).trim();
                if (!idStr.matches("^\\d+$")) {
                    return Result.fail("保险ID必须为数字");
                }
                insuranceId = Integer.parseInt(idStr);
            } else if (insuranceIdObj instanceof Long) {
                insuranceId = ((Long) insuranceIdObj).intValue();
            } else if (insuranceIdObj instanceof Integer) {
                insuranceId = (Integer) insuranceIdObj;
            } else {
                return Result.fail("保险ID类型错误，仅支持数字/数字字符串");
            }
        } catch (NumberFormatException e) {
            return Result.fail("保险ID格式错误，无法转为数字：" + e.getMessage());
        }

        if (insuranceId == null || insuranceId <= 0) {
            return Result.fail("保险ID必须为正整数");
        }
        if (contentType == null || contentType < 1 || contentType > 4) {
            return Result.fail("内容类型格式错误，仅支持1-4（1=产品特色 2=理赔案例 3=保险介绍 4=推荐图）");
        }
        if (file.isEmpty()) {
            return Result.fail("图片文件不能为空");
        }

        if (contentType == 4) {
            if (imgRemark == null || imgRemark.trim().isEmpty()) {
                return Result.fail("推荐图（contentType=4）必须填写图片说明");
            }
            if (imgRemark.length() > 200) {
                return Result.fail("图片说明长度不能超过200个字符");
            }
            imgRemark = imgRemark.trim();
        } else {
            if (imgRemark == null || imgRemark.trim().isEmpty()) {
                imgRemark = switch (contentType) {
                    case 2 -> "理赔案例图片";
                    case 3 -> "保险介绍图片";
                    case 1 -> "产品特色图";
                    default -> "其他图片";
                };
            } else {
                imgRemark = imgRemark.trim();
            }
        }

        PetInsurance insurance = petInsuranceService.getById(insuranceId);
        if (insurance == null) {
            return Result.fail("保险ID不存在，请先创建保险产品");
        }
        String insuranceNo = insurance.getInsuranceNo();
        if (insuranceNo == null || insuranceNo.trim().isEmpty()) {
            return Result.fail("该保险产品未配置编号（insuranceNo），无法上传图片");
        }
        insuranceNo = insuranceNo.trim();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("图片格式无效，必须包含后缀（如.jpg/.png）");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!suffix.startsWith(".")) {
            suffix = "." + suffix;
        }
        if (!suffix.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
            return Result.fail("仅支持jpg/jpeg/png/gif/webp格式图片");
        }

        String typeName = switch (contentType) {
            case 1 -> "产品特色";
            case 2 -> "理赔案例";
            case 3 -> "保险介绍";
            case 4 -> "推荐图";
            default -> "其他";
        };

        // 路径解析逻辑 —— 完全和宠物图片一样
        File uploadDir;
        try {
            if (insuranceImgPath.startsWith("classpath:")) {
                String classpathRelativePath = insuranceImgPath.replace("classpath:", "");
                ClassPathResource resource = new ClassPathResource(classpathRelativePath);
                String realPath = resource.getURL().getPath();
                realPath = URLDecoder.decode(realPath, "UTF-8");
                uploadDir = new File(realPath);
            } else {
                uploadDir = new File(insuranceImgPath);
            }

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            if (!uploadDir.canWrite()) {
                return Result.fail("上传失败：目录不可写");
            }
        } catch (Exception e) {
            return Result.fail("上传失败：目录解析错误");
        }

        String fileName;
        int incrementNum = 0;
        File destFile;

        do {
            if (incrementNum == 0) {
                fileName = insuranceNo + "_" + typeName + suffix;
            } else {
                fileName = insuranceNo + "_" + typeName + incrementNum + suffix;
            }
            destFile = new File(uploadDir, fileName);
            incrementNum++;
            if (incrementNum > 100) {
                return Result.fail("同类型图片超过100张，无法上传");
            }
        } while (destFile.exists());

        try {
            Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            final Integer finalInsuranceId = insuranceId;
            final String finalInsuranceNo = insuranceNo;
            final String finalFileName = fileName;

            PetInsuranceMediaContent mediaContent = new PetInsuranceMediaContent();
            mediaContent.setInsuranceId(insuranceId);
            mediaContent.setContentType(contentType);
            mediaContent.setImgPath("/insurance-img/" + fileName);
            mediaContent.setImgRemark(imgRemark);
            mediaContent.setCreateTime(LocalDateTime.now());

            boolean save = petInsuranceMediaContentService.save(mediaContent);
            if (save) {
                return Result.success(new HashMap<String, Object>() {{
                    put("mediaId", mediaContent.getId());
                    put("imgUrl", IMG_ACCESS_PREFIX + finalFileName);
                    put("insuranceId", finalInsuranceId);
                    put("insuranceNo", finalInsuranceNo);
                    put("filePath", mediaContent.getImgPath());
                }}, "图片上传成功");
            } else {
                if (destFile.exists()) destFile.delete();
                return Result.fail("图片信息保存失败");
            }
        } catch (IOException e) {
            return Result.fail("图片上传失败：" + e.getMessage());
        }
    }
    @GetMapping("/test-path")
    public String testPath() throws Exception {
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource("static/images/insurance-img/INS2026001_理赔案例1.png");
        return resource.getURL().getPath();
    }
}
