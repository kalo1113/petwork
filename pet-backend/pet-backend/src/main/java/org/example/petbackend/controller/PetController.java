package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.Pet;
import org.example.petbackend.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pet")
@CrossOrigin // 新增跨域注解（前端跨域请求必备）
public class PetController {
    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    @Autowired
    private PetService petService;
    // 新增：读取宠物图片上传路径配置
    @Value("${upload.pet-photo-path}")
    private String petPhotoPath;
    // 1. 新增宠物接口：明确返回自增的petId
    @PostMapping("/add")
    public Result<Pet> addPet(@Valid @RequestBody Pet pet) {
        log.info("接收宠物信息：{}", pet);
        pet.setPetId(null);
        boolean success = petService.save(pet);
        if (success) {
            log.info("新增宠物成功，自增ID：{}", pet.getPetId());
            return Result.success(pet, "新增宠物成功");
        } else {
            return Result.fail("新增宠物失败");
        }
    }

    // 2. 查询宠物列表（可选：建议也封装Result）
    @GetMapping("/list")
    public Result<List<Pet>> getPetList(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String petType,
            @RequestParam(required = false) String petGender,
            @RequestParam(required = false) String isSterilized
    ) {
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        if (userId != null) queryWrapper.eq("user_id", userId);
        if (petType != null) queryWrapper.eq("pet_type", petType);
        if (petGender != null) queryWrapper.eq("pet_gender", petGender);
        if (isSterilized != null) queryWrapper.eq("is_sterilized", isSterilized);
        List<Pet> petList = petService.list(queryWrapper);
        return Result.success(petList, "查询宠物列表成功");
    }

    // 3. 根据ID查询宠物详情（修复：封装Result，处理null情况）
    @GetMapping("/{petId}")
    public Result<Pet> getPetById(@PathVariable Integer petId) {
        Pet pet = petService.getById(petId);
        if (pet != null) {
            return Result.success(pet, "查询宠物详情成功");
        } else {
            return Result.fail("宠物ID不存在");
        }
    }

    // 4. 修改宠物信息（修复：返回Result，统一格式）
    @PutMapping("/update")
    public Result<Void> updatePet(@Valid @RequestBody Pet pet) {
        if (pet.getPetId() == null) {
            return Result.fail("修改失败：宠物ID不能为空");
        }
        pet.setUpdateTime(LocalDateTime.now());
        boolean success = petService.updateById(pet);
        if (success) {
            return Result.success("修改成功");
        } else {
            return Result.fail("修改失败（宠物ID不存在）");
        }
    }

    // 5. 删除宠物（修复：返回Result，统一格式）
    @DeleteMapping("/{petId}")
    public Result<Void> deletePet(@PathVariable Integer petId) {
        boolean success = petService.removeById(petId);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败（宠物ID不存在）");
        }
    }

    // 6. 宠物照片上传（修改：读取配置路径，替换硬编码）
    @PostMapping("/upload")
    public Result<String> uploadPetImg(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId,
            @RequestParam("petId") Integer petId,
            @RequestParam("photoType") String photoType
    ) {
        // 严格校验数值类型参数
        if (userId == null || userId <= 0) {
            return Result.fail("上传失败：userId必须为正整数");
        }
        if (petId == null || petId <= 0) {
            return Result.fail("上传失败：petId必须为正整数");
        }
        if (file.isEmpty()) {
            return Result.fail("上传失败：文件为空");
        }
        if (!"face".equals(photoType) && !"body".equals(photoType)) {
            return Result.fail("上传失败：照片类型仅支持face/body");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("上传失败：文件格式错误");
        }
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!fileExt.matches("\\.(jpg|jpeg|png|gif)$")) {
            return Result.fail("上传失败：仅支持jpg、jpeg、png、gif格式图片");
        }

        // 修改：使用配置的路径，替换硬编码
        File dir = new File(petPhotoPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String newFileName = String.format("%d_%d_%s_%s%s", userId, petId, photoType, uuid, fileExt);
        File dest = new File(petPhotoPath + newFileName);

        try {
            file.transferTo(dest);
            String imgUrl = "/pet-images/" + newFileName;
            log.info("图片上传成功：用户ID={}, 宠物ID={}, 类型={}, 路径={}", userId, petId, photoType, imgUrl);
            return Result.success(imgUrl, "图片上传成功");
        } catch (IOException e) {
            log.error("图片上传失败：用户ID={}, 宠物ID={}", userId, petId, e);
            return Result.fail("上传失败：" + e.getMessage());
        }
    }


    // 7. 关联宠物照片（修复：改用@RequestBody接收参数，适配前端传递方式）
    @PutMapping("/update-photo")
    public Result<Void> updatePetPhoto(@RequestBody PetPhotoDTO photoDTO) {
        Integer petId = photoDTO.getPetId();
        String photoType = photoDTO.getPhotoType();
        String imgUrl = photoDTO.getImgUrl();

        Pet pet = petService.getById(petId);
        if (pet == null) {
            return Result.fail("关联失败：宠物ID不存在");
        }

        if ("face".equals(photoType)) {
            pet.setPetFacePhoto(imgUrl);
        } else if ("body".equals(photoType)) {
            pet.setPetBodyPhoto(imgUrl);
        } else {
            return Result.fail("关联失败：照片类型仅支持face或body");
        }

        pet.setUpdateTime(LocalDateTime.now());
        boolean success = petService.updateById(pet);
        if (success) {
            return Result.success("宠物照片关联成功");
        } else {
            return Result.fail("宠物照片关联失败");
        }
    }

    // 8. 分页查询接口（可选：封装Result）
    @GetMapping("/page")
    public Result<IPage<Pet>> getPetPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String petType
    ) {
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        if (userId != null) queryWrapper.eq("user_id", userId);
        if (petType != null) queryWrapper.eq("pet_type", petType);
        IPage<Pet> petPage = petService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(petPage, "分页查询宠物成功");
    }

    // 新增DTO：接收照片关联参数（放在controller同包下）
    public static class PetPhotoDTO {
        private Integer petId;
        private String photoType;
        private String imgUrl;

        // 必须添加getter/setter（否则Spring无法解析参数）
        public Integer getPetId() {
            return petId;
        }

        public void setPetId(Integer petId) {
            this.petId = petId;
        }

        public String getPhotoType() {
            return photoType;
        }

        public void setPhotoType(String photoType) {
            this.photoType = photoType;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }
    }
}
