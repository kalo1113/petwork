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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pet")
public class PetController {
    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    @Autowired
    private PetService petService;

    // 1. 新增宠物
    @PostMapping("/add")
    public Result<Pet> addPet(@Valid @RequestBody Pet pet) {
        log.info("接收宠物信息：{}", pet);
        // 补充创建/更新时间
        if (pet.getCreateTime() == null) {
            pet.setCreateTime(LocalDateTime.now());
        }
        if (pet.getUpdateTime() == null) {
            pet.setUpdateTime(LocalDateTime.now());
        }
        boolean success = petService.save(pet); // save 方法会自动将生成的 ID 填充回 pet 对象
        if (success) {
            // 2. 成功时，返回完整的 pet 对象
            return Result.success(pet, "新增宠物成功");
        } else {
            return Result.fail("新增宠物失败");
        }
    }

    // 2. 查询宠物列表（按用户ID、种类、性别、绝育状态筛选）
    @GetMapping("/list")
    public List<Pet> getPetList(
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
        return petService.list(queryWrapper);
    }

    // 3. 根据ID查询宠物详情
    @GetMapping("/{petId}")
    public Pet getPetById(@PathVariable Integer petId) {
        return petService.getById(petId);
    }

    // 4. 修改宠物信息
    @PutMapping("/update")
    public String updatePet(@Valid @RequestBody Pet pet) {
        if (pet.getPetId() == null) {
            return "修改失败：宠物ID不能为空";
        }
        pet.setUpdateTime(LocalDateTime.now());
        boolean success = petService.updateById(pet);
        return success ? "修改成功" : "修改失败（宠物ID不存在）";
    }

    // 5. 删除宠物
    @DeleteMapping("/{petId}")
    public String deletePet(@PathVariable Integer petId) {
        boolean success = petService.removeById(petId);
        return success ? "删除成功" : "删除失败（宠物ID不存在）";
    }

    // 6. 宠物照片上传（文件名含用户ID/宠物ID/照片类型）
    @PostMapping("/upload")
    public String uploadPetImg(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId,
            @RequestParam("petId") Integer petId,
            @RequestParam("photoType") String photoType // face:正脸照, body:全身照
    ) {
        if (file.isEmpty()) {
            return "上传失败：文件为空";
        }
        if (userId == null || petId == null || photoType == null || photoType.trim().isEmpty()) {
            return "上传失败：用户ID、宠物ID、照片类型不能为空";
        }
        if (!"face".equals(photoType) && !"body".equals(photoType)) {
            return "上传失败：照片类型仅支持face（正脸照）或body（全身照）";
        }

        String originalFilename = file.getOriginalFilename();
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!fileExt.matches("\\.(jpg|jpeg|png|gif)$")) {
            return "上传失败：仅支持jpg、jpeg、png、gif格式图片";
        }

        String uploadDir = "F:/毕设/新建文件夹/pet-backend/petphoto/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String newFileName = String.format("%d_%d_%s_%s%s", userId, petId, photoType, uuid, fileExt);
        File dest = new File(uploadDir + newFileName);

        try {
            file.transferTo(dest);
            String imgUrl = "/pet-images/" + newFileName;
            log.info("图片上传成功：用户ID={}, 宠物ID={}, 类型={}, 路径={}", userId, petId, photoType, imgUrl);
            return imgUrl;
        } catch (IOException e) {
            log.error("图片上传失败：用户ID={}, 宠物ID={}", userId, petId, e);
            return "上传失败：" + e.getMessage();
        }
    }

    // 7. 关联宠物照片
    @PutMapping("/update-photo")
    public String updatePetPhoto(
            @RequestParam Integer petId,
            @RequestParam String photoType,
            @RequestParam String imgUrl
    ) {
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return "关联失败：宠物ID不存在";
        }

        if ("face".equals(photoType)) {
            pet.setPetFacePhoto(imgUrl);
        } else if ("body".equals(photoType)) {
            pet.setPetBodyPhoto(imgUrl);
        } else {
            return "关联失败：照片类型仅支持face或body";
        }

        pet.setUpdateTime(LocalDateTime.now());
        boolean success = petService.updateById(pet);
        return success ? "宠物照片关联成功" : "宠物照片关联失败";
    }

    // 8. 分页查询接口
    @GetMapping("/page")
    public IPage<Pet> getPetPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String petType
    ) {
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        if (userId != null) queryWrapper.eq("user_id", userId);
        if (petType != null) queryWrapper.eq("pet_type", petType);
        return petService.page(new Page<>(pageNum, pageSize), queryWrapper);
    }
}
