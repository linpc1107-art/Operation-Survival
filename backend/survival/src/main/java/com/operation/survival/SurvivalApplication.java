package com.operation.survival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.operation.survival.repository")
@EntityScan(basePackages = "com.operation.survival.entity")
public class SurvivalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurvivalApplication.class, args);
    }
}