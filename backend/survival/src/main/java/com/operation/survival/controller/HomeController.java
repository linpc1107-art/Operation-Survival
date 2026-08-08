package com.operation.survival.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Operation Survival Backend 啟動成功";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello Spring Boot";
    }
}