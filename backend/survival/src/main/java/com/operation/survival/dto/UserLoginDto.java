package com.operation.survival.dto;

import jakarta.validation.constraints.NotBlank;

public class UserLoginDto {

    @NotBlank(message = "帳號不能為空")
    private String username;

    @NotBlank(message = "密碼不能為空")
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