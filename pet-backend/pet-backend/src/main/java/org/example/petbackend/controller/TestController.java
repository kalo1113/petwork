package org.example.petbackend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    // 注入JdbcTemplate，直接操作数据库（Spring Boot自带）
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 测试数据库连接的接口：访问 http://localhost:8080/test/db 即可
    @GetMapping("/db")
    public String testDbConnection() {
        try {
            // 执行简单查询：查询数据库版本（MySQL特有的查询）
            String sql = "SELECT VERSION()";
            String dbVersion = jdbcTemplate.queryForObject(sql, String.class);
            return "数据库连接成功！MySQL版本：" + dbVersion;
        } catch (Exception e) {
            // 连接失败时返回错误信息
            return "数据库连接失败！原因：" + e.getMessage();
        }
    }
}