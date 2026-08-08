package com.operation.survival.service;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import com.operation.survival.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PlayerService {

    private static final int MAX_STAGE = 5;
    private static final int CURRENT_SAVE_VERSION = 4;
    private static final int BASE_HP = 140;
    private static final int STARTER_GOLD = 260;

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // 建立玩家資料
    public Player createPlayer(User user, String nickname) {
        return playerRepository.findByUser(user)
                .orElseGet(() -> {
                    Player player = new Player();
                    player.setUser(user);
                    player.setNickname(nickname);
                    player.setGold(STARTER_GOLD);
                    player.setCurrentStage(1);
                    player.setHp(BASE_HP);
                    player.setMaxHp(BASE_HP);
                    player.setLevel(1);
                    player.setExperience(0);
                    player.setPotionCount(2);
                    player.setTotalWins(0);
                    player.setSkillLevel(1);
                    player.setSkillCooldown(0);
                    player.setDifficulty("NORMAL");
                    player.setSaveVersion(CURRENT_SAVE_VERSION);
                    return playerRepository.save(player);
                });
    }

    // 查詢玩家
    public Player getPlayer(User user) {
        Player player = playerRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("找不到玩家資料"));

        boolean changed = false;

        if (player.getGold() == null) {
            player.setGold(0);
            changed = true;
        }
        if (player.getCurrentStage() == null || player.getCurrentStage() < 1) {
            player.setCurrentStage(1);
            changed = true;
        }
        if (player.getCurrentStage() > MAX_STAGE) {
            player.setCurrentStage(MAX_STAGE);
            changed = true;
        }
        if (player.getHp() == null || player.getHp() < 0) {
            player.setHp(BASE_HP);
            changed = true;
        }
        if (player.getMaxHp() == null || player.getMaxHp() <= 0) {
            player.setMaxHp(BASE_HP);
            changed = true;
        }
        if (player.getMaxHp() < BASE_HP) {
            player.setMaxHp(BASE_HP);
            changed = true;
        }
        if (player.getLevel() == null || player.getLevel() <= 0) {
            player.setLevel(1);
            changed = true;
        }
        if (player.getExperience() == null || player.getExperience() < 0) {
            player.setExperience(0);
            changed = true;
        }
        if (player.getPotionCount() == null || player.getPotionCount() < 0) {
            player.setPotionCount(0);
            changed = true;
        }
        if (player.getTotalWins() == null || player.getTotalWins() < 0) {
            player.setTotalWins(0);
            changed = true;
        }
        if (player.getSkillLevel() == null || player.getSkillLevel() <= 0) {
            player.setSkillLevel(1);
            changed = true;
        }
        if (player.getSkillCooldown() == null || player.getSkillCooldown() < 0) {
            player.setSkillCooldown(0);
            changed = true;
        }
        if (player.getDifficulty() == null || player.getDifficulty().isBlank()) {
            player.setDifficulty("NORMAL");
            changed = true;
        }
        String normalizedDifficulty = normalizeDifficulty(player.getDifficulty());
        if (!normalizedDifficulty.equals(player.getDifficulty())) {
            player.setDifficulty(normalizedDifficulty);
            changed = true;
        }
        if (player.getSaveVersion() == null || player.getSaveVersion() <= 0) {
            player.setSaveVersion(1);
            changed = true;
        }

        if (player.getSaveVersion() < CURRENT_SAVE_VERSION) {
            migratePlayerSave(player);
            changed = true;
        }

        if (changed) {
            return playerRepository.save(player);
        }

        return player;
    }

    public Player healPlayer(Player player) {
        int healCost = 55;
        if (player.getGold() < healCost) {
            throw new RuntimeException("金幣不足，回復需要 " + healCost + " 金幣");
        }

        player.setGold(player.getGold() - healCost);
        player.setHp(player.getMaxHp());
        return playerRepository.save(player);
    }

    public Player updateDifficulty(Player player, String difficulty) {
        player.setDifficulty(normalizeDifficulty(difficulty));
        return playerRepository.save(player);
    }

    public String normalizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "NORMAL";
        }

        return switch (difficulty.trim().toUpperCase()) {
            case "CASUAL", "NORMAL", "HARDCORE" -> difficulty.trim().toUpperCase();
            default -> throw new RuntimeException("無效的難易度，請使用 CASUAL、NORMAL 或 HARDCORE");
        };
    }

    public Player advanceStage(Player player) {
        if (player.getCurrentStage() >= MAX_STAGE) {
            throw new RuntimeException("你已抵達最終關卡，請挑戰魔王完成通關");
        }

        int requiredLevel = Math.max(1, player.getCurrentStage());
        int travelCost = 45 + player.getCurrentStage() * 15;

        if (player.getLevel() < requiredLevel) {
            throw new RuntimeException("等級不足，前往下一關需要等級 " + requiredLevel);
        }

        if (player.getGold() < travelCost) {
            throw new RuntimeException("金幣不足，前往下一關需要 " + travelCost + " 金幣");
        }

        player.setGold(player.getGold() - travelCost);
        player.setCurrentStage(player.getCurrentStage() + 1);
        player.setHp(player.getMaxHp());
        return playerRepository.save(player);
    }

    public Player buyPotion(Player player) {
        int potionCost = 60;
        if (player.getGold() < potionCost) {
            throw new RuntimeException("金幣不足，購買藥水需要 " + potionCost + " 金幣");
        }

        player.setGold(player.getGold() - potionCost);
        player.setPotionCount(player.getPotionCount() + 1);
        return playerRepository.save(player);
    }

    public Player usePotion(Player player) {
        if (player.getPotionCount() <= 0) {
            throw new RuntimeException("沒有藥水可使用，請先購買");
        }

        if (player.getHp() >= player.getMaxHp()) {
            throw new RuntimeException("目前 HP 已滿，不需要使用藥水");
        }

        int healAmount = Math.max(30, (int) Math.floor(player.getMaxHp() * 0.4));
        player.setPotionCount(player.getPotionCount() - 1);
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + healAmount));
        return playerRepository.save(player);
    }

    public Player upgradeSkill(Player player) {
        int currentSkillLevel = player.getSkillLevel();
        if (currentSkillLevel >= 5) {
            throw new RuntimeException("技能已達最高等級");
        }

        int requiredLevel = currentSkillLevel * 2;
        int upgradeCost = 150 + currentSkillLevel * 120;

        if (player.getLevel() < requiredLevel) {
            throw new RuntimeException("等級不足，升級技能需要等級 " + requiredLevel);
        }

        if (player.getGold() < upgradeCost) {
            throw new RuntimeException("金幣不足，升級技能需要 " + upgradeCost + " 金幣");
        }

        player.setGold(player.getGold() - upgradeCost);
        player.setSkillLevel(currentSkillLevel + 1);
        return playerRepository.save(player);
    }

    public Player applyBattleReward(Player player, int goldGain, int experienceGain) {
        player.setGold(player.getGold() + goldGain);
        player.setExperience(player.getExperience() + experienceGain);
        player.setTotalWins(player.getTotalWins() + 1);
        boolean leveledUp = false;

        while (player.getExperience() >= player.getLevel() * 100) {
            player.setExperience(player.getExperience() - player.getLevel() * 100);
            player.setLevel(player.getLevel() + 1);
            player.setMaxHp(player.getMaxHp() + 30);
            leveledUp = true;
        }

        int victoryRecovery = 18 + player.getCurrentStage() * 4;
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + victoryRecovery));

        if (leveledUp) {
            player.setHp(player.getMaxHp());
        }

        if (player.getHp() > player.getMaxHp()) {
            player.setHp(player.getMaxHp());
        }

        if (player.getCurrentStage() > MAX_STAGE) {
            player.setCurrentStage(MAX_STAGE);
        }

        return playerRepository.save(player);
    }

    public Player saveProgress(Player player) {
        player.setLastSavedAt(LocalDateTime.now());
        return playerRepository.save(player);
    }

    public Map<String, Object> buildProgressSnapshot(Player player) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("nickname", player.getNickname());
        snapshot.put("level", player.getLevel());
        snapshot.put("currentStage", player.getCurrentStage());
        snapshot.put("gold", player.getGold());
        snapshot.put("hp", player.getHp());
        snapshot.put("maxHp", player.getMaxHp());
        snapshot.put("experience", player.getExperience());
        snapshot.put("potionCount", player.getPotionCount());
        snapshot.put("totalWins", player.getTotalWins());
        snapshot.put("skillLevel", player.getSkillLevel());
        snapshot.put("skillCooldown", player.getSkillCooldown());
        snapshot.put("difficulty", player.getDifficulty());
        snapshot.put("saveVersion", player.getSaveVersion());
        snapshot.put("lastSavedAt", player.getLastSavedAt());
        return snapshot;
    }

    public Player endGameAndReset(Player player) {
        player.setGold(STARTER_GOLD);
        player.setCurrentStage(1);
        player.setHp(BASE_HP);
        player.setMaxHp(BASE_HP);
        player.setLevel(1);
        player.setExperience(0);
        player.setPotionCount(2);
        player.setTotalWins(0);
        player.setSkillLevel(1);
        player.setSkillCooldown(0);
        player.setDifficulty("NORMAL");
        player.setSaveVersion(CURRENT_SAVE_VERSION);
        player.setLastSavedAt(null);
        return playerRepository.save(player);
    }

    private void migratePlayerSave(Player player) {
        if (player.getSkillLevel() == null || player.getSkillLevel() <= 0) {
            player.setSkillLevel(1);
        }
        if (player.getSkillCooldown() == null || player.getSkillCooldown() < 0) {
            player.setSkillCooldown(0);
        }
        if (player.getPotionCount() == null || player.getPotionCount() < 0) {
            player.setPotionCount(0);
        }
        if (player.getHp() == null || player.getHp() < 0) {
            player.setHp(BASE_HP);
        }
        if (player.getMaxHp() == null || player.getMaxHp() <= 0 || player.getMaxHp() < BASE_HP) {
            player.setMaxHp(BASE_HP);
        }
        if (player.getHp() > player.getMaxHp()) {
            player.setHp(player.getMaxHp());
        }
        if (player.getDifficulty() == null || player.getDifficulty().isBlank()) {
            player.setDifficulty("NORMAL");
        } else {
            player.setDifficulty(normalizeDifficulty(player.getDifficulty()));
        }
        if (player.getCurrentStage() == 1 && player.getLevel() == 1 && player.getTotalWins() == 0
                && (player.getGold() == null || player.getGold() < STARTER_GOLD)) {
            player.setGold(STARTER_GOLD);
        }
        player.setSaveVersion(CURRENT_SAVE_VERSION);
    }
} 