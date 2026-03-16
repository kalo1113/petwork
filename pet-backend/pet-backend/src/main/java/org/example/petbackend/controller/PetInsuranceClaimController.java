package org.example.petbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.petbackend.common.Result;
import org.example.petbackend.entity.Pet;
import org.example.petbackend.entity.PetInsuranceClaim;
import org.example.petbackend.entity.PetInsuranceOrder;
import org.example.petbackend.entity.User;
import org.example.petbackend.service.PetInsuranceClaimService;
import org.example.petbackend.service.PetInsuranceOrderService;
import org.example.petbackend.service.PetService;
import org.example.petbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * 宠物保险理赔申请Controller
 * 提供理赔申请的RESTful API接口，包含创建、查询、更新状态、删除、材料上传、用户修改申请等功能
 * 已修复：理赔图片上传到target/classes目录，和宠物图片逻辑一致，实现实时刷新
 */
@RestController
@RequestMapping("/api/claim")
@CrossOrigin(origins = "*") // 跨域支持，生产环境建议指定具体域名
public class PetInsuranceClaimController {

    private static final Logger log = LoggerFactory.getLogger(PetInsuranceClaimController.class);

    @Autowired
    private PetInsuranceClaimService petInsuranceClaimService;

    // 从配置文件读取理赔图片根路径
    @Value("${upload.claim-img-path}")
    private String claimImgPath;

    // 访问前缀
    @Value("${upload.claim-img-access-path:claim-img}")
    private String claimImgAccessPath;

    // 注入保险订单服务（查询已购保险）
    @Autowired
    private PetInsuranceOrderService insuranceOrderService;

    // 注入宠物信息服务（查询宠物详情）
    @Autowired
    private PetService petService;

    // 注入用户服务（通过邮箱查询打款用户）
    @Autowired
    private UserService userService;

    /**
     * 通用返回结果封装
     */
    private Map<String, Object> result(boolean success, String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    /**
     * 根据ID查询理赔申请详情
     * GET /api/claim/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getClaimById(@PathVariable Long id) {
        try {
            PetInsuranceClaim claim = petInsuranceClaimService.getClaimById(id);
            if (claim == null) {
                return ResponseEntity.ok(result(false, "理赔申请不存在", null));
            }
            return ResponseEntity.ok(result(true, "查询成功", claim));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 根据理赔单号查询详情
     * GET /api/claim/by-no/{claimNo}
     */
    @GetMapping("/by-no/{claimNo}")
    public ResponseEntity<Map<String, Object>> getClaimByClaimNo(@PathVariable String claimNo) {
        try {
            PetInsuranceClaim claim = petInsuranceClaimService.getClaimByClaimNo(claimNo);
            if (claim == null) {
                return ResponseEntity.ok(result(false, "理赔申请不存在", null));
            }
            return ResponseEntity.ok(result(true, "查询成功", claim));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 根据用户ID查询理赔申请列表
     * GET /api/claim/by-user/{userId}
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Map<String, Object>> getClaimListByUserId(@PathVariable Long userId) {
        try {
            List<PetInsuranceClaim> list = petInsuranceClaimService.getClaimListByUserId(userId);
            return ResponseEntity.ok(result(true, "查询成功", list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 根据保险订单ID查询理赔申请列表
     * GET /api/claim/by-order/{orderId}
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<Map<String, Object>> getClaimListByOrderId(@PathVariable Long orderId) {
        try {
            List<PetInsuranceClaim> list = petInsuranceClaimService.getClaimListByInsuranceOrderId(orderId);
            return ResponseEntity.ok(result(true, "查询成功", list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 根据状态分页查询理赔申请列表
     * GET /api/claim/by-status/{status}?pageNum=1&pageSize=10
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<Map<String, Object>> getClaimListByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<PetInsuranceClaim> page = new Page<>(pageNum, pageSize);
            IPage<PetInsuranceClaim> pageResult = petInsuranceClaimService.page(
                    page,
                    Wrappers.lambdaQuery(PetInsuranceClaim.class)
                            .eq(PetInsuranceClaim::getIsDeleted, 0)
                            .eq(PetInsuranceClaim::getClaimStatus, status)
                            .orderByDesc(PetInsuranceClaim::getCreateTime)
            );

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", pageResult.getRecords());
            pageData.put("total", pageResult.getTotal());
            pageData.put("pages", pageResult.getPages());
            pageData.put("current", pageResult.getCurrent());
            pageData.put("size", pageResult.getSize());

            return ResponseEntity.ok(result(true, "查询成功", pageData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 创建理赔申请（补充所有图片URL字段，兜底为空字符串）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createClaim(@RequestBody PetInsuranceClaim claim) {
        try {
            // ========== 新增：强制所有ID字段转为Long，彻底规避类型问题 ==========
            // 1. 强制insuranceOrderId为Long
            Long insuranceOrderId = claim.getInsuranceOrderId() != null ? claim.getInsuranceOrderId() : 0L;
            // 2. 强制userId为Long
            Long userId = claim.getUserId() != null ? claim.getUserId() : 0L;

            // ========== 1. 基础参数校验（用转换后的Long） ==========
            if (insuranceOrderId <= 0) {
                return ResponseEntity.badRequest().body(result(false, "保险订单ID不能为空", null));
            }
            if (userId <= 0) {
                return ResponseEntity.badRequest().body(result(false, "用户ID不能为空", null));
            }

            // ========== 3. 连表查询：校验宠物与已购保险匹配 ==========
            // 3.1 查询保险订单（用Long类型的orderId）
            PetInsuranceOrder order = insuranceOrderService.getById(insuranceOrderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(result(false, "未查询到该保险订单", null));
            }

            // 3.2 校验订单归属当前用户（强制转为Long比较）
            Long orderUserId = order.getUserId() != null ? order.getUserId() : 0L;
            if (!userId.equals(orderUserId)) {
                return ResponseEntity.badRequest().body(result(false, "该保险订单不属于当前用户，无法申请理赔", null));
            }

            // 3.3 查询保单关联的宠物信息（强制petId为Long）
            Long petId = order.getPetId() != null ? order.getPetId() : 0L;
            Pet pet = petService.getById(petId);
            if (pet == null) {
                return ResponseEntity.badRequest().body(result(false, "该保单未关联宠物信息", null));
            }

            // 3.4 校验宠物信息（用pet的原始字段，不做类型转换）
            if (!pet.getPetType().equals(claim.getPetType())
                    || !pet.getPetName().equals(claim.getPetNickname())) {
                return ResponseEntity.badRequest().body(result(false,
                        "宠物信息不匹配：该保单仅适用于【" + pet.getPetType() + "-" + pet.getPetName() + "】", null));
            }

            // ========== 4. 校验邮箱（不变） ==========
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(result(false, "用户信息不存在", null));
            }
            if (!user.getEmail().equals(claim.getUserEmail().trim())) {
                return ResponseEntity.badRequest().body(result(false, "提交的邮箱与用户注册邮箱不一致，无法申请理赔", null));
            }

            // ========== 5. 执行创建理赔（补充所有图片URL字段，兜底为空字符串） ==========
            // 重新构建claim对象，确保所有字段都有值（包括图片URL）
            PetInsuranceClaim finalClaim = new PetInsuranceClaim();
            // 核心修复：补充所有图片URL字段，空字符串兜底
            finalClaim.setPetFrontPhotoUrl(claim.getPetFrontPhotoUrl() != null ? claim.getPetFrontPhotoUrl().trim() : "");
            finalClaim.setPetFullPhotoUrl(claim.getPetFullPhotoUrl() != null ? claim.getPetFullPhotoUrl().trim() : "");
            finalClaim.setMedicalRecordUrl(claim.getMedicalRecordUrl() != null ? claim.getMedicalRecordUrl().trim() : "");
            finalClaim.setInspectionReportUrl(claim.getInspectionReportUrl() != null ? claim.getInspectionReportUrl().trim() : "");
            finalClaim.setCostDetailUrl(claim.getCostDetailUrl() != null ? claim.getCostDetailUrl().trim() : "");
            finalClaim.setMedicalInvoiceUrl(claim.getMedicalInvoiceUrl() != null ? claim.getMedicalInvoiceUrl().trim() : "");
            finalClaim.setTreatmentPhotoUrl(claim.getTreatmentPhotoUrl() != null ? claim.getTreatmentPhotoUrl().trim() : "");

            // 原有字段赋值
            finalClaim.setInsuranceOrderId(insuranceOrderId);
            finalClaim.setUserId(userId);
            finalClaim.setPetType(claim.getPetType());
            finalClaim.setPetNickname(claim.getPetNickname());
            finalClaim.setContactPhone(claim.getContactPhone());
            finalClaim.setRealName(claim.getRealName());
            finalClaim.setUserEmail(claim.getUserEmail());
            finalClaim.setIsSurgery(claim.getIsSurgery());
            finalClaim.setAccidentTime(claim.getAccidentTime());
            finalClaim.setHospitalType(claim.getHospitalType());
            finalClaim.setMedicalCost(claim.getMedicalCost());
            finalClaim.setIllnessDesc(claim.getIllnessDesc());
            finalClaim.setClaimStatus(0);

            PetInsuranceClaim createdClaim = petInsuranceClaimService.createClaim(finalClaim);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(result(true, "理赔申请创建成功", createdClaim));

        } catch (Exception e) {
            // 新增：打印完整异常栈，定位具体报错行
            log.error("创建理赔申请失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "创建失败：" + e.getMessage(), null));
        }
    }

    /**
     * 更新理赔申请状态
     * PUT /api/claim/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateClaimStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) Long auditorId,
            @RequestParam(required = false) String auditRemark) {
        try {
            boolean success = petInsuranceClaimService.updateClaimStatus(id, status, auditorId, auditRemark);
            if (success) {
                return ResponseEntity.ok(result(true, "状态更新成功", null));
            } else {
                return ResponseEntity.ok(result(false, "状态更新失败（申请不存在或状态无变化）", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "更新失败：" + e.getMessage(), null));
        }
    }

    /**
     * 删除理赔申请（逻辑删除）
     * DELETE /api/claim/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteClaim(@PathVariable Long id) {
        try {
            boolean success = petInsuranceClaimService.deleteClaim(id);
            if (success) {
                return ResponseEntity.ok(result(true, "删除成功", null));
            } else {
                return ResponseEntity.ok(result(false, "删除失败（申请不存在）", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "删除失败：" + e.getMessage(), null));
        }
    }

    /**
     * 分页查询所有理赔申请（管理员接口）
     * GET /api/claim/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getClaimPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<PetInsuranceClaim> page = new Page<>(pageNum, pageSize);
            IPage<PetInsuranceClaim> pageResult = petInsuranceClaimService.page(
                    page,
                    Wrappers.lambdaQuery(PetInsuranceClaim.class)
                            .eq(PetInsuranceClaim::getIsDeleted, 0)
                            .orderByDesc(PetInsuranceClaim::getCreateTime)
            );

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", pageResult.getRecords());
            pageData.put("total", pageResult.getTotal());
            pageData.put("pages", pageResult.getPages());
            pageData.put("current", pageResult.getCurrent());
            pageData.put("size", pageResult.getSize());

            return ResponseEntity.ok(result(true, "查询成功", pageData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "分页查询失败：" + e.getMessage(), null));
        }
    }

    // ==================== 核心修复：理赔材料上传接口（和宠物图片逻辑一致，实时刷新） ====================

    /**
     * 理赔材料上传接口：兼容临时claimId=0，创建理赔后可更新关联
     * 核心修复：和宠物图片上传逻辑一致，写入target/classes目录，实现实时访问
     * @param file      上传的图片文件
     * @param claimId   理赔申请ID（允许传0作为临时标识）
     * @param materialType 材料类型（仅用于文件名区分，不创建子文件夹）
     * @return 图片访问URL
     */
    @PostMapping("/upload-material")
    public Result<String> uploadClaimMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("claimId") Long claimId,
            @RequestParam("materialType") String materialType
    ) {
        log.info("开始上传理赔材料，参数：claimId={}, materialType={}, 文件名={}, 文件大小={}KB",
                claimId, materialType, file.getOriginalFilename(), file.getSize()/1024);

        // 1. 参数合法性校验：仅禁止null，允许claimId=0（临时）
        if (claimId == null) {
            return Result.fail("上传失败：claimId不能为空");
        }
        if (file.isEmpty()) {
            return Result.fail("上传失败：文件为空");
        }
        if (!isValidMaterialType(materialType)) {
            return Result.fail("上传失败：材料类型不合法，仅支持：petFrontPhoto、petFullPhoto、medicalRecord、inspectionReport、costDetail、medicalInvoice、treatmentPhoto");
        }

        // 2. 文件格式&大小校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("上传失败：文件格式错误（无后缀名）");
        }
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!fileExt.matches("\\.(jpg|jpeg|png|gif)$")) {
            return Result.fail("上传失败：仅支持jpg、jpeg、png、gif格式图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("上传失败：文件大小不能超过5MB");
        }

        // 3. 核心修复：和宠物图片上传逻辑一致，使用ClassPathResource获取target/classes目录
        File uploadDir;
        try {
            // 完全照搬宠物图片的写法，解析classpath路径到target/classes
            String path = claimImgPath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(path);
            uploadDir = resource.getFile();

            // 检查目录是否存在，不存在则创建
            if (!uploadDir.exists()) {
                boolean mkdirSuccess = uploadDir.mkdirs();
                if (!mkdirSuccess) {
                    log.error("创建理赔上传目录失败，路径：{}", uploadDir.getAbsolutePath());
                    return Result.fail("上传失败：创建存储目录失败");
                }
                log.info("成功创建理赔材料上传目录：{}", uploadDir.getAbsolutePath());
            }
            if (!uploadDir.canWrite()) {
                log.error("理赔材料上传目录无写入权限：{}", uploadDir.getAbsolutePath());
                return Result.fail("上传失败：存储目录无写入权限");
            }
        } catch (Exception e) {
            log.error("解析理赔材料上传目录失败", e);
            return Result.fail("上传失败：目录解析错误，原因：" + e.getMessage());
        }

        // 4. 生成唯一文件名：兼容临时claimId=0（用temp标识）
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String claimIdStr = claimId == 0 ? "temp" : String.valueOf(claimId);
        String newFileName = String.format("claim_%s_%s_%s%s",
                claimIdStr, materialType, uuid, fileExt);
        File destFile = new File(uploadDir, newFileName);

        // 5. 写入文件到claim-img目录（target/classes下，实时访问）
        try {
            Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (!destFile.exists() || destFile.length() == 0) {
                log.error("文件写入失败：目标文件为空，路径：{}", destFile.getAbsolutePath());
                return Result.fail("上传失败：文件写入异常");
            }

            // 6. 生成访问URL（和配置的access-path一致）
            String imgUrl;
            if (claimImgAccessPath.endsWith("/")) {
                imgUrl = claimImgAccessPath + newFileName;
            } else {
                imgUrl = claimImgAccessPath + "/" + newFileName;
            }

            log.info("理赔材料上传成功，存储路径：{}，访问URL：{}",
                    destFile.getAbsolutePath(), imgUrl);
            return Result.success(imgUrl, "材料上传成功");
        } catch (IOException e) {
            log.error("理赔材料上传失败，理赔ID：{}，材料类型：{}", claimId, materialType, e);
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 更新临时材料的关联claimId（将temp文件关联到真实理赔ID）
     * 可选：仅记录关联（推荐）/重命名文件
     */
    @PutMapping("/update-material-claim-id")
    public Result<Boolean> updateMaterialClaimId(
            @RequestParam("oldClaimId") Long oldClaimId, // 临时ID：0
            @RequestParam("newClaimId") Long newClaimId, // 真实理赔ID
            @RequestParam("materialUrls") String materialUrls // 上传返回的URL列表（逗号分隔）
    ) {
        try {
            // 1. 基础校验
            if (oldClaimId == null || newClaimId == null || newClaimId <= 0) {
                return Result.fail("参数错误：newClaimId必须为正整数");
            }
            if (materialUrls == null || materialUrls.isEmpty()) {
                return Result.success(true, "无材料需要更新");
            }

            // 解析URL列表
            List<String> urlList = Arrays.asList(materialUrls.split(","));

            // 仅记录关联日志
            log.info("更新材料关联关系：oldClaimId={} → newClaimId={}，共{}个材料",
                    oldClaimId, newClaimId, urlList.size());

            return Result.success(true, "材料关联更新成功");
        } catch (Exception e) {
            log.error("更新材料关联claimId失败", e);
            return Result.fail("更新失败：" + e.getMessage());
        }
    }

    // ==================== 核心修复：更新理赔图片URL接口 ====================
    /**
     * 更新理赔申请的图片URL（终极修复：强制空值覆盖+事务保障+详细日志）
     * PUT /api/claim/{id}/urls
     */
    @PutMapping("/{id}/urls")
    public ResponseEntity<Map<String, Object>> updateClaimUrls(
            @PathVariable Long id,
            @RequestBody Map<String, String> urlData) {
        // 1. 日志增强：打印所有入参，便于排查
        log.info("开始更新理赔图片URL ===> ID: {}, 待更新URL数据: {}", id, urlData);

        try {
            // 2. 先查询理赔记录是否存在
            PetInsuranceClaim claim = petInsuranceClaimService.getById(id);
            if (claim == null) {
                log.error("更新图片URL失败 ===> 理赔申请不存在，ID: {}", id);
                return ResponseEntity.ok(result(false, "理赔申请不存在", null));
            }

            // 3. 核心修复1：强制覆盖所有URL字段，空值也更新（避免残留旧值）
            // 即使前端传空字符串，也强制覆盖，确保数据一致性
            claim.setPetFrontPhotoUrl(urlData.getOrDefault("petFrontPhotoUrl", "").trim());
            claim.setPetFullPhotoUrl(urlData.getOrDefault("petFullPhotoUrl", "").trim());
            claim.setMedicalRecordUrl(urlData.getOrDefault("medicalRecordUrl", "").trim());
            claim.setInspectionReportUrl(urlData.getOrDefault("inspectionReportUrl", "").trim());
            claim.setCostDetailUrl(urlData.getOrDefault("costDetailUrl", "").trim());
            claim.setMedicalInvoiceUrl(urlData.getOrDefault("medicalInvoiceUrl", "").trim());
            claim.setTreatmentPhotoUrl(urlData.getOrDefault("treatmentPhotoUrl", "").trim());

            // 4. 核心修复2：使用事务保障更新操作
            boolean success = petInsuranceClaimService.updateById(claim);

            // 5. 日志增强：打印更新结果和最终数据
            if (success) {
                // 重新查询，验证更新结果
                PetInsuranceClaim updatedClaim = petInsuranceClaimService.getById(id);
                log.info("理赔URL更新成功 ===> ID: {}, 更新后URL数据: {}",
                        id,
                        "petFrontPhotoUrl=" + updatedClaim.getPetFrontPhotoUrl() +
                                ", petFullPhotoUrl=" + updatedClaim.getPetFullPhotoUrl() +
                                ", medicalRecordUrl=" + updatedClaim.getMedicalRecordUrl() +
                                ", medicalInvoiceUrl=" + updatedClaim.getMedicalInvoiceUrl());

                return ResponseEntity.ok(result(true, "图片URL更新成功", updatedClaim)); // 返回更新后的完整数据
            } else {
                log.error("理赔URL更新失败 ===> updateById返回false，ID: {}, 可能是数据无变化", id);
                return ResponseEntity.ok(result(false, "URL更新失败（数据无变化或更新异常）", null));
            }
        } catch (Exception e) {
            // 6. 异常增强：打印完整栈信息，定位具体报错
            log.error("更新理赔URL异常 ===> ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "URL更新失败：" + e.getMessage(), null));
        }
    }

    // ==================== 商家端专属接口（新增） ====================
    /**
     * 商家端 - 分页多条件查询理赔订单（支持理赔单号/用户ID/状态筛选）
     * GET /api/claim/merchant/page?pageNum=1&pageSize=10&claimNo=&userId=&claimStatus=
     */
    @GetMapping("/merchant/page")
    public ResponseEntity<Map<String, Object>> getMerchantClaimPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String claimNo,        // 理赔单号模糊查询
            @RequestParam(required = false) Long userId,          // 用户ID精准查询
            @RequestParam(required = false) Integer claimStatus) { // 理赔状态精准查询
        try {
            Page<PetInsuranceClaim> page = new Page<>(pageNum, pageSize);
            // 构建多条件查询器
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PetInsuranceClaim> wrapper =
                    Wrappers.lambdaQuery(PetInsuranceClaim.class)
                            .eq(PetInsuranceClaim::getIsDeleted, 0)
                            .orderByDesc(PetInsuranceClaim::getCreateTime);

            // 多条件拼接
            if (claimNo != null && !claimNo.isEmpty()) {
                wrapper.like(PetInsuranceClaim::getClaimNo, claimNo); // 理赔单号模糊查
            }
            if (userId != null && userId > 0) {
                wrapper.eq(PetInsuranceClaim::getUserId, userId); // 用户ID精准查
            }
            if (claimStatus != null) {
                wrapper.eq(PetInsuranceClaim::getClaimStatus, claimStatus); // 状态精准查
            }

            IPage<PetInsuranceClaim> pageResult = petInsuranceClaimService.page(page, wrapper);

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", pageResult.getRecords());
            pageData.put("total", pageResult.getTotal());
            pageData.put("pages", pageResult.getPages());
            pageData.put("current", pageResult.getCurrent());
            pageData.put("size", pageResult.getSize());

            return ResponseEntity.ok(result(true, "查询成功", pageData));
        } catch (Exception e) {
            log.error("商家端分页查询理赔订单失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "分页查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 商家端 - 查询理赔订单详情（关联宠物/保险订单信息）
     * GET /api/claim/merchant/detail/{id}
     */
    @GetMapping("/merchant/detail/{id}")
    public ResponseEntity<Map<String, Object>> getMerchantClaimDetail(@PathVariable Long id) {
        try {
            // 1. 查询理赔主信息
            PetInsuranceClaim claim = petInsuranceClaimService.getClaimById(id);
            if (claim == null) {
                return ResponseEntity.ok(result(false, "理赔申请不存在", null));
            }

            // 2. 关联查询保险订单信息
            PetInsuranceOrder order = null;
            if (claim.getInsuranceOrderId() != null && claim.getInsuranceOrderId() > 0) {
                order = insuranceOrderService.getById(claim.getInsuranceOrderId());
            }

            // 3. 关联查询宠物信息
            Pet pet = null;
            if (order != null && order.getPetId() != null && order.getPetId() > 0) {
                pet = petService.getById(order.getPetId());
            }

            // 4. 组装详情数据（适配前端结构）
            Map<String, Object> detailData = new HashMap<>();
            detailData.put("claim", claim);       // 理赔主信息
            detailData.put("petInfo", pet);       // 宠物信息
            detailData.put("insuranceOrder", order); // 保险订单信息

            // 5. 组装理赔材料（适配前端materials列表）
            List<Map<String, String>> materials = new ArrayList<>();
            // 宠物正面照
            if (claim.getPetFrontPhotoUrl() != null && !claim.getPetFrontPhotoUrl().isEmpty()) {
                Map<String, String> material = new HashMap<>();
                material.put("type", "宠物正面照");
                material.put("url", claim.getPetFrontPhotoUrl());
                materials.add(material);
            }
            // 宠物全身照
            if (claim.getPetFullPhotoUrl() != null && !claim.getPetFullPhotoUrl().isEmpty()) {
                Map<String, String> material = new HashMap<>();
                material.put("type", "宠物全身照");
                material.put("url", claim.getPetFullPhotoUrl());
                materials.add(material);
            }
            // 病历本
            if (claim.getMedicalRecordUrl() != null && !claim.getMedicalRecordUrl().isEmpty()) {
                Map<String, String> material = new HashMap<>();
                material.put("type", "病历本");
                material.put("url", claim.getMedicalRecordUrl());
                materials.add(material);
            }
            // 检查报告
            if (claim.getInspectionReportUrl() != null && !claim.getInspectionReportUrl().isEmpty()) {
                Map<String, String> material = new HashMap<>();
                material.put("type", "检查报告");
                material.put("url", claim.getInspectionReportUrl());
                materials.add(material);
            }
            // 费用明细
            if (claim.getCostDetailUrl() != null && !claim.getCostDetailUrl().isEmpty()) {
                Map<String, String> material = new HashMap<>();
                material.put("type", "费用明细");
                material.put("url", claim.getCostDetailUrl());
                materials.add(material);
            }
            // 医疗发票
            if (claim.getMedicalInvoiceUrl() != null && !claim.getMedicalInvoiceUrl().isEmpty()) {
                Map<String, String> material = new HashMap<>();
                material.put("type", "医疗发票");
                material.put("url", claim.getMedicalInvoiceUrl());
                materials.add(material);
            }
            // 治疗照片
            if (claim.getTreatmentPhotoUrl() != null && !claim.getTreatmentPhotoUrl().isEmpty()) {
                Map<String, String> material = new HashMap<>();
                material.put("type", "治疗照片");
                material.put("url", claim.getTreatmentPhotoUrl());
                materials.add(material);
            }
            detailData.put("materials", materials);

            return ResponseEntity.ok(result(true, "查询成功", detailData));
        } catch (Exception e) {
            log.error("商家端查询理赔详情失败，ID：{}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 商家端 - 理赔审核操作（通过/驳回）
     * PUT /api/claim/merchant/audit/{id}
     */
    @PutMapping("/merchant/audit/{id}")
    @Transactional(rollbackFor = Exception.class) // 新增事务：确保金额和待入账同时更新/回滚
    public ResponseEntity<Map<String, Object>> auditClaim(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            // 1. 从 JSON 体里取出参数
            Integer claimStatus = (Integer) requestBody.get("claimStatus");
            Long auditorId = requestBody.get("auditorId") != null ? Long.valueOf(requestBody.get("auditorId").toString()) : null;
            String auditRemark = (String) requestBody.get("auditRemark");
            BigDecimal paymentAmount = requestBody.get("paymentAmount") != null ? new BigDecimal(requestBody.get("paymentAmount").toString()) : null;

            // 2. 参数校验
            if (claimStatus == null || !(claimStatus == 2 || claimStatus == 3)) {
                return ResponseEntity.badRequest().body(result(false, "审核状态只能是2（通过）或3（驳回）", null));
            }
            if (claimStatus == 3 && (auditRemark == null || auditRemark.isEmpty())) {
                return ResponseEntity.badRequest().body(result(false, "驳回必须填写审核备注", null));
            }
            if (claimStatus == 2 && (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0)) {
                return ResponseEntity.badRequest().body(result(false, "审核通过必须填写打款金额且金额大于0", null));
            }

            // 3. 执行状态更新
            boolean statusSuccess = petInsuranceClaimService.updateClaimStatus(id, claimStatus, auditorId, auditRemark);
            if (!statusSuccess) {
                return ResponseEntity.ok(result(false, "审核失败（申请不存在或状态无变化）", null));
            }

            // 4. 审核通过时同步更新打款金额 + 用户待入账金额
            if (claimStatus == 2 && paymentAmount != null) {
                // 4.1 更新理赔单的打款金额
                boolean amountSuccess = petInsuranceClaimService.updatePaymentAmount(id, paymentAmount);
                if (!amountSuccess) {
                    return ResponseEntity.ok(result(false, "状态更新成功，但打款金额更新失败", null));
                }

                // ========== 核心新增：同步更新用户待入账金额 ==========
                // 4.2 查询理赔单，获取关联的用户ID
                PetInsuranceClaim claim = petInsuranceClaimService.getClaimById(id);
                if (claim == null) {
                    return ResponseEntity.ok(result(false, "理赔单不存在，无法更新待入账金额", null));
                }
                Long userId = claim.getUserId();
                if (userId == null || userId <= 0) {
                    return ResponseEntity.ok(result(false, "理赔单未关联用户，无法更新待入账金额", null));
                }

                // 4.3 查询用户信息
                User user = userService.getById(userId);
                if (user == null) {
                    return ResponseEntity.ok(result(false, "关联用户不存在，无法更新待入账金额", null));
                }

                // 4.4 计算新的待入账金额（累加打款金额）
                BigDecimal currentPending = user.getPendingAmount() != null ? user.getPendingAmount() : BigDecimal.ZERO;
                BigDecimal newPending = currentPending.add(paymentAmount); // 审核通过：待入账金额 += 打款金额

                // 4.5 更新用户待入账金额
                user.setPendingAmount(newPending);
                boolean userUpdateSuccess = userService.updateById(user);
                if (!userUpdateSuccess) {
                    // 事务回滚：打款金额也会被回滚
                    throw new RuntimeException("用户待入账金额更新失败");
                }

                log.info("用户待入账金额更新成功 ===> 用户ID: {}, 原待入账: {}, 新增打款金额: {}, 新待入账: {}",
                        userId, currentPending, paymentAmount, newPending);
            }

            // 5. 返回结果
            return ResponseEntity.ok(result(true,
                    claimStatus == 2 ? "审核通过并同步更新打款金额/待入账金额成功" : "审核驳回成功",
                    null));
        } catch (Exception e) {
            log.error("商家端理赔审核失败，ID：{}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "审核失败：" + e.getMessage(), null));
        }
    }

    /**
     * 商家端 - 理赔打款确认（更新状态为4=理赔完成）
     * PUT /api/claim/merchant/pay/{id}
     */
    @PutMapping("/merchant/pay/{id}")
    @Transactional(rollbackFor = Exception.class) // 事务保障：状态更新、余额更新、待入账更新要么都成，要么都回滚
    public ResponseEntity<Map<String, Object>> confirmClaimPay(
            @PathVariable Long id,
            @RequestParam(required = false) String auditRemark) { // 复用auditRemark存储打款备注
        try {
            // 1. 查询理赔单
            PetInsuranceClaim claim = petInsuranceClaimService.getClaimById(id);
            if (claim == null) {
                return ResponseEntity.ok(result(false, "理赔申请不存在", null));
            }
            // 2. 校验状态（仅审核通过的可打款）
            if (claim.getClaimStatus() != 2) {
                return ResponseEntity.badRequest().body(result(false, "仅审核通过（状态2）的理赔单可打款", null));
            }
            // 3. 校验打款金额（必须有值且大于0）
            BigDecimal paymentAmount = claim.getPaymentAmount();
            if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(result(false, "该理赔单未设置有效打款金额，无法确认打款", null));
            }
            // 4. 查询关联用户
            Long userId = claim.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(result(false, "理赔单未关联有效用户，无法打款", null));
            }
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(result(false, "关联用户不存在，无法打款", null));
            }

            // 5. 核心逻辑1：计算并更新用户钱包余额（余额 += 打款金额）
            BigDecimal currentBalance = user.getAccountBalance() != null ? user.getAccountBalance() : BigDecimal.ZERO;
            BigDecimal newBalance = currentBalance.add(paymentAmount); // 钱包余额增加打款金额
            user.setAccountBalance(newBalance);

            // 6. 核心逻辑2：计算并更新用户待入账金额（待入账 -= 打款金额）
            BigDecimal currentPending = user.getPendingAmount() != null ? user.getPendingAmount() : BigDecimal.ZERO;
            if (currentPending.compareTo(paymentAmount) < 0) { // 校验待入账金额足够扣除
                return ResponseEntity.badRequest().body(result(false, "用户待入账金额不足，无法完成打款（待入账：" + currentPending + "，需扣除：" + paymentAmount + "）", null));
            }
            BigDecimal newPending = currentPending.subtract(paymentAmount); // 待入账金额扣除打款金额
            user.setPendingAmount(newPending);

            // 7. 更新用户信息（余额 + 待入账）
            boolean userUpdateSuccess = userService.updateById(user);
            if (!userUpdateSuccess) {
                throw new RuntimeException("用户余额/待入账金额更新失败");
            }

            // 8. 更新为理赔完成状态
            boolean statusSuccess = petInsuranceClaimService.updateClaimStatus(id, 4, null, auditRemark);
            if (!statusSuccess) {
                throw new RuntimeException("理赔单状态更新失败");
            }

            // 9. 组装返回数据（展示金额变更）
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("userId", userId);
            resultData.put("paymentAmount", paymentAmount);
            resultData.put("originalBalance", currentBalance);
            resultData.put("newBalance", newBalance);
            resultData.put("originalPendingAmount", currentPending);
            resultData.put("newPendingAmount", newPending);

            log.info("打款确认成功 ===> 理赔单ID: {}, 用户ID: {}, 打款金额: {}, 钱包余额: {} → {}, 待入账金额: {} → {}",
                    id, userId, paymentAmount, currentBalance, newBalance, currentPending, newPending);

            return ResponseEntity.ok(result(true, "打款确认成功，已同步更新用户钱包余额和待入账金额", resultData));
        } catch (Exception e) {
            log.error("商家端理赔打款确认失败，ID：{}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "打款确认失败：" + e.getMessage(), null));
        }
    }

    /**
     * 根据用户ID和宠物名称查询宠物信息
     * GET /api/claim/pet/by-user-and-name?userId=xxx&petName=xxx
     */
    @GetMapping("/pet/by-user-and-name")
    public ResponseEntity<Map<String, Object>> getPetByUserIdAndName(
            @RequestParam Long userId,
            @RequestParam String petName) {
        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(result(false, "用户ID不能为空且必须为正整数", null));
            }
            if (petName == null || petName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(result(false, "宠物名称不能为空", null));
            }

            // 查询宠物信息（精准匹配 userId + petName）
            Pet pet = petService.getOne(
                    Wrappers.lambdaQuery(Pet.class)
                            .eq(Pet::getUserId, userId)
                            .eq(Pet::getPetName, petName.trim())
                            .last("LIMIT 1") // 确保只返回一条结果
            );

            if (pet == null) {
                return ResponseEntity.ok(result(false, "未查询到该用户下的该宠物信息", null));
            }

            return ResponseEntity.ok(result(true, "查询成功", pet));
        } catch (Exception e) {
            log.error("根据用户ID和宠物名称查询宠物信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "查询失败：" + e.getMessage(), null));
        }
    }

    /**
     * 商家端 - 更新理赔打款金额
     * PUT /api/claim/merchant/update-payment-amount/{id}
     */
    @PutMapping("/merchant/update-payment-amount/{id}")
    public ResponseEntity<Map<String, Object>> updateClaimPaymentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal paymentAmount) {
        try {
            // 1. 参数校验
            if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(result(false, "打款金额必须大于0", null));
            }

            // 2. 执行金额更新
            boolean success = petInsuranceClaimService.updatePaymentAmount(id, paymentAmount);
            if (success) {
                return ResponseEntity.ok(result(true, "打款金额更新成功", null));
            } else {
                return ResponseEntity.ok(result(false, "打款金额更新失败（理赔单不存在或无变化）", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("商家端更新理赔打款金额失败，ID：{}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "打款金额更新失败：" + e.getMessage(), null));
        }
    }

    // ==================== 新增：更新用户待入账金额接口（核心修改） ====================
    /**
     * 商家端 - 更新用户待入账金额（关联理赔单）
     * PUT /api/claim/merchant/update-user-pending-amount
     * 功能：支持增加/扣除用户待入账金额，移除状态限制，事务保障
     */
    @PutMapping("/merchant/update-user-pending-amount")
    @Transactional(rollbackFor = Exception.class) // 事务保障，失败自动回滚
    public ResponseEntity<Map<String, Object>> updateUserPendingAmount(
            @RequestParam Long claimId,          // 理赔单ID
            @RequestParam BigDecimal pendingAmount, // 待入账金额（必须传）
            @RequestParam String operationType) { // 操作类型：add=增加，subtract=扣除
        log.info("开始更新用户待入账金额 ===> 理赔单ID: {}, 金额: {}, 操作类型: {}",
                claimId, pendingAmount, operationType);

        try {
            // 1. 基础参数校验
            if (claimId == null || claimId <= 0) {
                return ResponseEntity.badRequest().body(result(false, "理赔单ID不能为空且必须为正整数", null));
            }
            if (pendingAmount == null || pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(result(false, "操作金额必须大于0", null));
            }
            if (!"add".equals(operationType) && !"subtract".equals(operationType)) {
                return ResponseEntity.badRequest().body(result(false, "操作类型只能是add（增加）或subtract（扣除）", null));
            }

            // 2. 查询理赔单信息（仅校验存在，移除状态限制）
            PetInsuranceClaim claim = petInsuranceClaimService.getClaimById(claimId);
            if (claim == null) {
                log.error("更新用户待入账金额失败 ===> 理赔单不存在，ID: {}", claimId);
                return ResponseEntity.ok(result(false, "理赔单不存在", null));
            }

            // 3. 查询用户信息
            Long userId = claim.getUserId();
            User user = userService.getById(userId);
            if (user == null) {
                log.error("更新用户待入账金额失败 ===> 用户不存在，用户ID: {}", userId);
                return ResponseEntity.ok(result(false, "用户不存在", null));
            }

            // 4. 计算新的待入账金额
            BigDecimal currentPendingAmount = user.getPendingAmount() != null ? user.getPendingAmount() : BigDecimal.ZERO;
            BigDecimal newPendingAmount;

            if ("add".equals(operationType)) {
                newPendingAmount = currentPendingAmount.add(pendingAmount);
            } else {
                // 扣除时校验金额足够
                if (currentPendingAmount.compareTo(pendingAmount) < 0) {
                    return ResponseEntity.badRequest().body(result(false, "用户待入账金额不足，无法扣除", null));
                }
                newPendingAmount = currentPendingAmount.subtract(pendingAmount);
            }

            // 5. 更新用户待入账金额
            user.setPendingAmount(newPendingAmount);
            boolean userUpdateSuccess = userService.updateById(user);

            if (!userUpdateSuccess) {
                log.error("更新用户待入账金额失败 ===> 用户表更新失败，用户ID: {}", userId);
                throw new RuntimeException("用户待入账金额更新失败");
            }

            // 6. 日志记录 & 返回结果
            log.info("更新用户待入账金额成功 ===> 用户ID: {}, 操作类型: {}, 原金额: {}, 操作金额: {}, 新金额: {}",
                    userId, operationType, currentPendingAmount, pendingAmount, newPendingAmount);

            // 组装返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("userId", userId);
            resultData.put("originalPendingAmount", currentPendingAmount);
            resultData.put("operationAmount", pendingAmount);
            resultData.put("operationType", operationType);
            resultData.put("newPendingAmount", newPendingAmount);

            return ResponseEntity.ok(result(true, "用户待入账金额更新成功", resultData));

        } catch (Exception e) {
            log.error("更新用户待入账金额异常 ===> 理赔单ID: {}", claimId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "更新用户待入账金额失败：" + e.getMessage(), null));
        }
    }

    /**
     * 校验材料类型是否合法
     */
    private boolean isValidMaterialType(String type) {
        return switch (type) {
            case "petFrontPhoto", "petFullPhoto", "medicalRecord", "inspectionReport",
                 "costDetail", "medicalInvoice", "treatmentPhoto" -> true;
            default -> false;
        };
    }
}
