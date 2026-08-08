package com.operation.survival.controller;

import com.operation.survival.dto.UserLoginDto;
import com.operation.survival.dto.UserRegisterDto;
import com.operation.survival.model.User;
import com.operation.survival.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UserRegisterDto registerDto) {
        User registeredUser = userService.register(registerDto);
        registeredUser.setPassword(null);

        return ResponseEntity.ok(Map.of(
            "message", "註冊成功",
            "user", registeredUser
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @Valid @RequestBody UserLoginDto loginDto) {
        String token = userService.login(loginDto);
        return ResponseEntity.ok(Map.of(
            "message", "登入成功",
            "token", token
        ));
    }
}