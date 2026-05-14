package com.young;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.young.mapper") // 扫描 MyBatis 的 Mapper 接口
public class QimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QimsApplication.class, args);
        System.out.println("======= 食品质量检测系统后端启动成功 =======");
    }
}
