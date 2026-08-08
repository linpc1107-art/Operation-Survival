package com.operation.survival.controller;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import com.operation.survival.repository.UserRepository;
import com.operation.survival.service.PlayerService;
import com.operation.survival.service.WeaponService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class PlayerController {

    private final PlayerService playerService;
    private final WeaponService weaponService;
    private final UserRepository userRepository;

    public PlayerController(
            PlayerService playerService,
            WeaponService weaponService,
            UserRepository userRepository) {

        this.playerService = playerService;
        this.weaponService = weaponService;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/player/test")
    public String test() {
        return "Player API 正常";
    }

    @GetMapping("/api/player/me")
    public Map<String, Object> getCurrentPlayer(
            Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", player.getId());
        response.put("username", user.getUsername());
        response.put("nickname", player.getNickname());
        response.put("gold", player.getGold());
        response.put("currentStage", player.getCurrentStage());
        response.put("hp", player.getHp());
        response.put("maxHp", player.getMaxHp());
        response.put("level", player.getLevel());
        response.put("experience", player.getExperience());
        response.put("potionCount", player.getPotionCount());
        response.put("totalWins", player.getTotalWins());
        response.put("skillLevel", player.getSkillLevel());
        response.put("skillCooldown", player.getSkillCooldown());
        response.put("difficulty", player.getDifficulty());
        response.put("saveVersion", player.getSaveVersion());
        response.put("lastSavedAt", player.getLastSavedAt());
        return response;
    }

    @PostMapping("/api/player/heal")
    public Map<String, Object> healPlayer(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        Player updated = playerService.healPlayer(player);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "HP 已回復");
        response.put("hp", updated.getHp());
        response.put("maxHp", updated.getMaxHp());
        response.put("gold", updated.getGold());
        response.put("potionCount", updated.getPotionCount());
        return response;
    }

    @PostMapping("/api/player/advance-stage")
    public Map<String, Object> advanceStage(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        Player updated = playerService.advanceStage(player);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "已進入下一關");
        response.put("currentStage", updated.getCurrentStage());
        response.put("hp", updated.getHp());
        response.put("maxHp", updated.getMaxHp());
        response.put("gold", updated.getGold());
        response.put("potionCount", updated.getPotionCount());
        return response;
    }

    @PostMapping("/api/player/buy-potion")
    public Map<String, Object> buyPotion(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        Player updated = playerService.buyPotion(player);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "購買藥水成功");
        response.put("gold", updated.getGold());
        response.put("potionCount", updated.getPotionCount());
        return response;
    }

    @PostMapping("/api/player/use-potion")
    public Map<String, Object> usePotion(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        Player updated = playerService.usePotion(player);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "使用藥水成功");
        response.put("hp", updated.getHp());
        response.put("maxHp", updated.getMaxHp());
        response.put("potionCount", updated.getPotionCount());
        return response;
    }

    @PostMapping("/api/player/upgrade-skill")
    public Map<String, Object> upgradeSkill(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        int oldSkillLevel = player.getSkillLevel();
        int upgradeCost = 150 + oldSkillLevel * 120;
        Player updated = playerService.upgradeSkill(player);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "技能升級成功");
        response.put("skillLevel", updated.getSkillLevel());
        response.put("gold", updated.getGold());
        response.put("upgradeCost", upgradeCost);
        return response;
    }

    @PostMapping("/api/player/end-game")
    public Map<String, Object> endGame(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        Player resetPlayer = playerService.endGameAndReset(player);
        weaponService.resetWeapon(resetPlayer);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "本局遊戲已結束，進度已重置");
        response.put("currentStage", resetPlayer.getCurrentStage());
        response.put("level", resetPlayer.getLevel());
        response.put("gold", resetPlayer.getGold());
        return response;
    }

    @PostMapping("/api/player/difficulty")
    public Map<String, Object> updateDifficulty(
            Authentication authentication,
            @org.springframework.web.bind.annotation.RequestParam String mode) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        Player updated = playerService.updateDifficulty(player, mode);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "難易度已更新");
        response.put("difficulty", updated.getDifficulty());
        response.put("hp", updated.getHp());
        response.put("maxHp", updated.getMaxHp());
        return response;
    }
}