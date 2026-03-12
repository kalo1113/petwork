package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.Pet;
import org.example.petbackend.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 宠物管理控制器
 * 提供宠物增删改查、照片上传、照片关联等接口
 */
@RestController
@RequestMapping("/pet")
public class PetController {
    // 日志组件
    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    // 注入宠物服务
    @Autowired
    private PetService petService;

    // 从配置文件读取宠物照片上传根路径（application.yml 中配置 upload.pet-photo-path）
    @Value("${upload.pet-photo-path}")
    private String petPhotoPath;

    // 注入宠物照片访问前缀（application.yml中配置的upload.pet-photo-access-path）
    @Value("${upload.pet-photo-access-path:/pet-img}")
    private String petPhotoAccessPath;

    /**
     * 1. 新增宠物接口
     * @param pet 宠物信息（含用户ID、名称、生日等，不含自增petId）
     * @return 新增后的宠物对象（含自增petId）
     */
    @PostMapping("/add")
    public Result<Pet> addPet(@Valid @RequestBody Pet pet) {
        log.info("开始新增宠物，接收参数：{}", pet);
        // 强制清空自增ID，由数据库生成
        pet.setPetId(null);
        // 手动填充创建/更新时间（若未配置自动填充插件）
        LocalDateTime now = LocalDateTime.now();
        pet.setCreateTime(now);
        pet.setUpdateTime(now);

        boolean success = petService.save(pet);
        if (success) {
            log.info("新增宠物成功，生成的宠物ID：{}", pet.getPetId());
            return Result.success(pet, "新增宠物成功");
        } else {
            log.error("新增宠物失败，参数：{}", pet);
            return Result.fail("新增宠物失败");
        }
    }

    /**
     * 2. 宠物照片上传（支持正脸照/全身照）- 修复classpath路径写入问题
     * @param file      上传的图片文件
     * @param userId    用户ID
     * @param petId     宠物ID
     * @param photoType 照片类型（face：正脸照，body：全身照）
     * @return 图片访问URL
     */
    @PostMapping("/upload")
    public Result<String> uploadPetImg(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId,
            @RequestParam("petId") Integer petId,
            @RequestParam("photoType") String photoType
    ) {
        log.info("开始上传宠物照片，参数：userId={}, petId={}, photoType={}, 文件名={}",
                userId, petId, photoType, file.getOriginalFilename());

        // 1. 参数合法性校验
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

        // 2. 文件格式校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("上传失败：文件格式错误（无后缀名）");
        }
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!fileExt.matches("\\.(jpg|jpeg|png|gif)$")) {
            return Result.fail("上传失败：仅支持jpg、jpeg、png、gif格式图片");
        }

        // 3. 解析上传目录（核心修复：处理classpath路径）
        File uploadDir;
        try {
            if (petPhotoPath.startsWith("classpath:")) {
                // 处理classpath路径，转换为实际可写入的物理路径
                String classpathRelativePath = petPhotoPath.replace("classpath:", "");
                ClassPathResource resource = new ClassPathResource(classpathRelativePath);

                // 解决Windows路径编码问题（如%20）
                String realPath = resource.getURL().getPath();
                realPath = URLDecoder.decode(realPath, "UTF-8");
                uploadDir = new File(realPath);
            } else {
                // 非classpath路径直接使用
                uploadDir = new File(petPhotoPath);
            }

            // 确保目录存在（多级目录）
            if (!uploadDir.exists()) {
                boolean mkdirSuccess = uploadDir.mkdirs();
                if (!mkdirSuccess) {
                    log.error("创建上传目录失败，路径：{}", uploadDir.getAbsolutePath());
                    return Result.fail("上传失败：创建存储目录失败");
                }
                log.info("成功创建宠物照片上传目录：{}", uploadDir.getAbsolutePath());
            }

            // 校验目录可写
            if (!uploadDir.canWrite()) {
                log.error("宠物照片上传目录无写入权限：{}", uploadDir.getAbsolutePath());
                return Result.fail("上传失败：存储目录无写入权限");
            }
        } catch (Exception e) {
            log.error("解析宠物照片上传目录失败", e);
            return Result.fail("上传失败：目录解析错误，原因：" + e.getMessage());
        }

        // 4. 生成唯一文件名（避免重复）
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String newFileName = String.format("%d_%d_%s_%s%s", userId, petId, photoType, uuid, fileExt);
        File destFile = new File(uploadDir, newFileName);

        // 5. 写入文件（核心修复：用Files.copy替代transferTo，兼容classpath路径）
        try {
            // 写入文件（覆盖已存在的同名文件）
            Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 校验文件是否真的写入成功
            if (!destFile.exists() || destFile.length() == 0) {
                log.error("文件写入失败：目标文件为空，路径：{}", destFile.getAbsolutePath());
                return Result.fail("上传失败：文件写入异常");
            }

            // 6. 生成访问URL（使用配置的访问前缀）
            String imgUrl;
            if (petPhotoAccessPath.endsWith("/")) {
                imgUrl = petPhotoAccessPath + newFileName;
            } else {
                imgUrl = petPhotoAccessPath + "/" + newFileName;
            }
            log.info("图片上传成功，存储路径：{}，访问URL：{}", destFile.getAbsolutePath(), imgUrl);
            return Result.success(imgUrl, "图片上传成功");
        } catch (IOException e) {
            log.error("图片上传失败，用户ID：{}，宠物ID：{}", userId, petId, e);
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 【新增】兼容前端的 /pet/list 接口（query传参）
     * 前端调用 /pet/list?userId=1 时，转发到原有逻辑
     */
    @GetMapping("/list")
    public Result<List<Pet>> getPetListByUserIdQuery(@RequestParam Integer userId) {
        // 直接复用原有路径参数的逻辑
        return getPetListByUserId(userId);
    }

    /**
     * 3. 【核心】根据用户ID查询所有宠物（登录用户专属）
     * 支持两种调用方式：
     * 1. 路径参数：/pet/user/{userId}
     * 2. Query参数：/pet/list?userId={userId}（兼容前端）
     * @param userId 用户ID（必传，确保只能查询自己的宠物）
     * @return 该用户的所有宠物列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Pet>> getPetListByUserId(@PathVariable Integer userId) {
        // 1. 校验用户ID合法性
        if (userId == null || userId <= 0) {
            log.warn("查询用户宠物失败：用户ID不合法，userId={}", userId);
            return Result.fail("查询失败：用户ID必须为正整数");
        }

        // 2. 构建查询条件：仅查询该用户的宠物，按更新时间倒序
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId) // 核心：只查当前用户的宠物
                .orderByDesc("update_time"); // 最新修改的宠物排在前面

        // 3. 执行查询
        List<Pet> petList = petService.list(queryWrapper);
        log.info("查询用户[{}]的宠物列表完成，共{}条记录", userId, petList.size());

        // 4. 返回结果（空列表友好提示）
        String msg = petList.isEmpty() ? "暂无宠物信息" : "查询宠物列表成功";
        return Result.success(petList, msg);
    }

    /**
     * 4. 【新增】根据宠物ID查询单个宠物信息
     * 用于编辑宠物信息时回显数据
     * @param petId 宠物ID（路径参数）
     * @return 宠物详细信息
     */
    @GetMapping("/info/{petId}")
    public Result<Pet> getPetInfoById(@PathVariable Integer petId) {
        log.info("开始查询宠物信息，宠物ID：{}", petId);

        // 1. 参数合法性校验
        if (petId == null || petId <= 0) {
            log.warn("查询宠物信息失败：宠物ID不合法，petId={}", petId);
            return Result.fail("查询失败：宠物ID必须为正整数");
        }

        // 2. 根据ID查询宠物
        Pet pet = petService.getById(petId);
        if (pet == null) {
            log.warn("查询宠物信息失败：宠物ID不存在，petId={}", petId);
            return Result.fail("查询失败：该宠物ID不存在");
        }

        // 3. 返回查询结果
        log.info("查询宠物信息成功，宠物ID：{}，信息：{}", petId, pet);
        return Result.success(pet, "查询宠物信息成功");
    }

    /**
     * 5. 【新增】更新宠物基础信息接口
     * 用于编辑宠物信息时提交修改
     * @param pet 宠物修改信息（必须包含petId）
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<Void> updatePetInfo(@Valid @RequestBody Pet pet) {
        log.info("开始更新宠物信息，接收参数：{}", pet);

        // 1. 参数合法性校验
        Integer petId = Math.toIntExact(pet.getPetId());
        if (petId == null || petId <= 0) {
            log.warn("更新宠物信息失败：宠物ID不合法，petId={}", petId);
            return Result.fail("更新失败：宠物ID必须为正整数");
        }

        // 3. 填充更新时间
        pet.setUpdateTime(LocalDateTime.now());

        // 4. 执行更新（仅更新非空字段）
        boolean success = petService.updateById(pet);
        if (success) {
            log.info("更新宠物信息成功，宠物ID：{}", petId);
            return Result.success("更新宠物信息成功");
        } else {
            log.error("更新宠物信息失败，参数：{}", pet);
            return Result.fail("更新宠物信息失败");
        }
    }

    /**
     * 6. 关联宠物照片（更新宠物的照片URL）
     * @param photoDTO 照片关联参数（petId + photoType + imgUrl）
     * @return 关联结果
     */
    @PutMapping("/update-photo")
    public Result<Void> updatePetPhoto(@RequestBody PetPhotoDTO photoDTO) {
        log.info("开始关联宠物照片，参数：{}", photoDTO);

        Integer petId = photoDTO.getPetId();
        String photoType = photoDTO.getPhotoType();
        String imgUrl = photoDTO.getImgUrl();

        // 1. 参数校验
        if (petId == null || petId <= 0) {
            return Result.fail("关联失败：宠物ID必须为正整数");
        }
        if (!"face".equals(photoType) && !"body".equals(photoType)) {
            return Result.fail("关联失败：照片类型仅支持face/body");
        }
        if (imgUrl == null || imgUrl.trim().isEmpty()) {
            return Result.fail("关联失败：图片URL不能为空");
        }

        // 2. 校验宠物是否存在
        Pet pet = petService.getById(petId);
        if (pet == null) {
            log.warn("关联宠物照片失败：宠物ID不存在，petId={}", petId);
            return Result.fail("关联失败：宠物ID不存在");
        }

        // 3. 更新照片URL
        if ("face".equals(photoType)) {
            pet.setPetFacePhoto(imgUrl);
        } else {
            pet.setPetBodyPhoto(imgUrl);
        }
        pet.setUpdateTime(LocalDateTime.now());

        boolean success = petService.updateById(pet);
        if (success) {
            log.info("关联宠物照片成功，宠物ID：{}，照片类型：{}", petId, photoType);
            return Result.success("宠物照片关联成功");
        } else {
            log.error("关联宠物照片失败，参数：{}", photoDTO);
            return Result.fail("宠物照片关联失败");
        }
    }

    /**
     * 内部DTO：接收宠物照片关联参数
     * （也可单独放到 dto 包下，此处为简化放在控制器内部）
     */
    public static class PetPhotoDTO {
        private Integer petId;       // 宠物ID
        private String photoType;   // 照片类型（face/body）
        private String imgUrl;      // 图片访问URL

        // Getter & Setter
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
