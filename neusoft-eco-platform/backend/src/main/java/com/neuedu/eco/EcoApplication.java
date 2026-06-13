package com.neuedu.eco;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.neuedu.eco.mapper")
public class EcoApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcoApplication.class, args);
    }
}
