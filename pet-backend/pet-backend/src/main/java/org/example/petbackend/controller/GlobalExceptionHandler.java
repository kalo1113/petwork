package org.example.petbackend.controller; // 包路径可根据项目结构调整，建议单独建 exception 包

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

/**
 * 全局异常处理器：统一捕获并处理项目中所有控制器层的异常
 */
@RestControllerAdvice // 标识这是一个全局异常处理类，作用于所有 @RestController 控制器
public class GlobalExceptionHandler {

    /**
     * 处理参数校验失败的异常（如 @NotBlank、@NotNull 校验不通过）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(MethodArgumentNotValidException e) {
        // 获取校验失败的具体提示信息（即实体类中 @NotBlank 等注解的 message）
        String errorMsg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return "参数错误：" + errorMsg; // 返回友好提示（如：参数错误：宠物名称不能为空）
    }

    /**
     * 处理数据库约束异常（如非空字段未传值、唯一键冲突等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityException(DataIntegrityViolationException e) {
        // 简化提示，避免暴露数据库细节
        return "数据错误：请检查输入的信息是否完整或符合规则";
    }

    /**
     * 处理参数类型不匹配异常（如前端传字符串，后端接收为数字）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return "参数类型错误：" + e.getParameter().getParameterName() + "格式不正确";
    }

    /**
     * 处理空指针异常（如查询不存在的数据时，返回 null 后调用方法）
     */
    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointerException(NullPointerException e) {
        return "操作失败：数据不存在或已被删除";
    }

}