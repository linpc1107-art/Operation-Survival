package com.operation.survival.repository;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    // PlayerService 使用
    Optional<Player> findByUser(User user);

    // BattleService 使用
    Optional<Player> findByUserUsername(String username);

}