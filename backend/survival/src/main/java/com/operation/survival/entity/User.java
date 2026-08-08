package com.operation.survival.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // 這會在 MySQL 中建立名為 users 的資料表
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 ID
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // 無參數建構子（JPA 規定必須要有）
    public User() {
    }

    // 有參數建構子（方便註冊時建立物件）
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter 和 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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