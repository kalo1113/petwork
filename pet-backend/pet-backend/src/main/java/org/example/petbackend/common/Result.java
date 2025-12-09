package org.example.petbackend.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200=成功，400=参数错误，401=认证失败，404=资源不存在，409=冲突，500=服务器错误
    private String msg;   // 提示信息（成功/错误描述）
    private T data;       // 数据体（成功返回数据，失败返回null）

    // ========== 成功响应 ==========
    // 1. 成功（带数据+自定义提示）
    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 2. 成功（带数据+默认提示）
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    // 3. 成功（无数据+自定义提示）
    public static Result<Void> success(String msg) {
        return success(null, msg);
    }

    // 4. 成功（无数据+默认提示）
    public static Result<Void> success() {
        return success(null, "操作成功");
    }

    // ========== 失败响应 ==========
    // 1. 失败（自定义错误码+自定义提示）【核心：用于区分不同错误类型】
    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null); // 失败时数据为空
        return result;
    }

    // 2. 失败（默认错误码500+自定义提示）【兼容原有逻辑】
    public static <T> Result<T> fail(String msg) {
        return fail(500, msg);
    }

    // 3. 失败（无泛型场景，兼容原有failVoid）
    public static Result<Void> failVoid(String msg) {
        return fail(500, msg);
    }

    // ========== 快捷失败方法（可选，简化Controller调用） ==========
    // 参数错误（400）
    public static <T> Result<T> failParam(String msg) {
        return fail(400, msg);
    }

    // 认证失败（401）
    public static <T> Result<T> failAuth(String msg) {
        return fail(401, msg);
    }

    // 资源不存在（404）
    public static <T> Result<T> failNotFound(String msg) {
        return fail(404, msg);
    }

    // 冲突（如邮箱已注册，409）
    public static <T> Result<T> failConflict(String msg) {
        return fail(409, msg);
    }
}
