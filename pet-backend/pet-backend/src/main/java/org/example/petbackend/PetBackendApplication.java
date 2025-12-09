package org.example.petbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement // 注解驱动的事务管理功能
@MapperScan("org.example.petbackend.mapper") // 扫描 Mapper 接口所在包
public class PetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetBackendApplication.class, args);
    }

}
