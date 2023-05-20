package org.sangonomiya.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.sangonomiya.app", "org.sangonomiya.kotlin", "org.sangonomiya.groovy"})
@MapperScan("org.sangonomiya.app.mapper")
public class DigitalCarbonBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalCarbonBackendApplication.class, args);
    }

}