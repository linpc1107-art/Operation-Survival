package com.operation.survival.repository;

import com.operation.survival.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // 1. 透過帳號尋找使用者，用於登入與註冊檢查
    Optional<User> findByUsername(String username);
    
    // 2. 檢查帳號是否存在，回傳 true 或 false（UserService 的第 27 行需要它！）
    boolean existsByUsername(String username);
}