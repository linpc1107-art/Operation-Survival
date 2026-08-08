package com.operation.survival.repository;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeaponRepository extends JpaRepository<Weapon, Long> {

    List<Weapon> findByPlayer(Player player);

    Optional<Weapon> findFirstByPlayer(Player player);
}