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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * 宠物保险理赔申请Controller
 * 提供理赔申请的RESTful API接口，包含创建、查询、更新状态、删除、材料上传等功能
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
            // 其他基础校验不变...

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

    // ==================== 核心修复：理赔材料上传接口 ====================

    /**
     * 理赔材料上传接口：兼容临时claimId=0，创建理赔后可更新关联
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

        // 3. 核心修改：所有文件都放到claim-img根目录，不分子文件夹
        File uploadDir;
        try {
            // 直接拼接项目真实路径，绕过ClassPathResource
            String basePath = claimImgPath.replace("classpath:", ""); // 去掉classpath:前缀
            String fullPath = "src/main/resources/" + basePath; // 仅到claim-img根目录，无子文件夹

            uploadDir = new File(fullPath);

            // 检查目录是否存在（不存在则自动创建，避免手动创建）
            if (!uploadDir.exists()) {
                boolean mkdirSuccess = uploadDir.mkdirs();
                if (!mkdirSuccess) {
                    log.error("创建上传目录失败，路径：{}", uploadDir.getAbsolutePath());
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

        // 5. 写入文件到claim-img根目录
        try {
            Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (!destFile.exists() || destFile.length() == 0) {
                log.error("文件写入失败：目标文件为空，路径：{}", destFile.getAbsolutePath());
                return Result.fail("上传失败：文件写入异常");
            }

            // 6. 生成访问URL（直接指向claim-img根目录，无子文件夹）
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
    public ResponseEntity<Map<String, Object>> auditClaim(
            @PathVariable Long id,
            @RequestParam Integer claimStatus,          // 2=审核通过，3=审核驳回（适配原实体类状态值）
            @RequestParam(required = false) Long auditorId,    // 审核人ID（可选）
            @RequestParam(required = false) String auditRemark) { // 审核备注（驳回必填）
        try {
            // 1. 参数校验
            if (claimStatus == null || !(claimStatus == 2 || claimStatus == 3)) {
                return ResponseEntity.badRequest().body(result(false, "审核状态只能是2（通过）或3（驳回）", null));
            }
            if (claimStatus == 3 && (auditRemark == null || auditRemark.isEmpty())) {
                return ResponseEntity.badRequest().body(result(false, "驳回必须填写审核备注", null));
            }

            // 2. 执行审核（复用原有状态更新接口）
            boolean success = petInsuranceClaimService.updateClaimStatus(id, claimStatus, auditorId, auditRemark);
            if (success) {
                return ResponseEntity.ok(result(true, claimStatus == 2 ? "审核通过成功" : "审核驳回成功", null));
            } else {
                return ResponseEntity.ok(result(false, "审核失败（申请不存在或状态无变化）", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(result(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("商家端理赔审核失败，ID：{}，状态：{}", id, claimStatus, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "审核失败：" + e.getMessage(), null));
        }
    }

    /**
     * 商家端 - 理赔打款确认（更新状态为4=理赔完成）
     * PUT /api/claim/merchant/pay/{id}
     */
    @PutMapping("/merchant/pay/{id}")
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

            // 3. 更新为理赔完成状态
            boolean success = petInsuranceClaimService.updateClaimStatus(id, 4, null, auditRemark);

            if (success) {
                return ResponseEntity.ok(result(true, "打款确认成功", null));
            } else {
                return ResponseEntity.ok(result(false, "打款确认失败", null));
            }
        } catch (Exception e) {
            log.error("商家端理赔打款确认失败，ID：{}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result(false, "打款确认失败：" + e.getMessage(), null));
        }
    }
// 在 PetInsuranceClaimController 类中添加以下接口方法（建议放在商家端接口区域）

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
