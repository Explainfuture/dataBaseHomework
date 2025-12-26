package com.hnu.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HNU校园交流平台启动类
 */
@SpringBootApplication
@MapperScan("com.hnu.campus.mapper")
@EnableScheduling
public class HnuCampusPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(HnuCampusPlatformApplication.class, args);
    }
}

