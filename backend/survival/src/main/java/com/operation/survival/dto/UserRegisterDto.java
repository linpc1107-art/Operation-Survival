package com.operation.survival.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegisterDto {

    @NotBlank(message = "帳號不能為空")
    @Size(min = 4, max = 20, message = "帳號長度必須在 4 到 20 個字元之間")
    private String username;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, message = "密碼長度至少需要 6 個字元")
    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}