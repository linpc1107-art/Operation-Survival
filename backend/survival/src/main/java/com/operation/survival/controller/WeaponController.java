package com.operation.survival.controller;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import com.operation.survival.entity.Weapon;
import com.operation.survival.repository.UserRepository;
import com.operation.survival.service.PlayerService;
import com.operation.survival.service.WeaponService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weapon")
public class WeaponController {

    private final WeaponService weaponService;
    private final PlayerService playerService;
    private final UserRepository userRepository;

    public WeaponController(
            WeaponService weaponService,
            PlayerService playerService,
            UserRepository userRepository) {

        this.weaponService = weaponService;
        this.playerService = playerService;
        this.userRepository = userRepository;
    }

    // 查詢目前登入玩家的武器
    @GetMapping("/me")
    public Map<String, Object> getCurrentWeapon(
            Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);

        Weapon weapon = weaponService.getPlayerWeapon(player);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", weapon.getId());
        response.put("name", weapon.getName());
        response.put("level", weapon.getLevel());
        response.put("attack", weapon.getAttack());
        response.put("type", weapon.getName().toUpperCase());

        return response;
    }

    // 升級目前登入玩家的武器
    @PostMapping("/upgrade")
    public Map<String, Object> upgradeWeapon(
            Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("找不到使用者"));

        Player player = playerService.getPlayer(user);

        Weapon weapon = weaponService.upgradeWeapon(player);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "武器升級成功");
        response.put("level", weapon.getLevel());
        response.put("attack", weapon.getAttack());
        response.put("gold", player.getGold());
        response.put("playerLevel", player.getLevel());
        response.put("playerMaxHp", player.getMaxHp());
        response.put("upgradeCost", 120 + weapon.getLevel() * 80 + player.getLevel() * 25);

        return response;
    }

    @PostMapping("/change")
    public Map<String, Object> changeWeapon(
            Authentication authentication,
            @org.springframework.web.bind.annotation.RequestParam String type) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("找不到使用者"));

        Player player = playerService.getPlayer(user);

        Weapon weapon = weaponService.changeWeapon(player, type);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "武器已切換");
        response.put("name", weapon.getName());
        response.put("type", weapon.getName().toUpperCase());
        response.put("level", weapon.getLevel());
        response.put("attack", weapon.getAttack());
        return response;
    }
}