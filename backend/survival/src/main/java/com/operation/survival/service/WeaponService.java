package com.operation.survival.service;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.Weapon;
import com.operation.survival.repository.PlayerRepository;
import com.operation.survival.repository.WeaponRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeaponService {

    private static final String[] ALLOWED_WEAPON_TYPES = {"PISTOL", "BLADE", "SPEAR"};

    private final WeaponRepository weaponRepository;
    private final PlayerRepository playerRepository;

    public WeaponService(
            WeaponRepository weaponRepository,
            PlayerRepository playerRepository) {

        this.weaponRepository = weaponRepository;
        this.playerRepository = playerRepository;
    }

    // 查詢玩家目前的武器
    public Weapon getPlayerWeapon(Player player) {
        return weaponRepository.findFirstByPlayer(player)
                .orElseThrow(() ->
                        new RuntimeException("找不到玩家武器資料"));
    }

    // 升級玩家武器
    @Transactional
    public Weapon upgradeWeapon(Player player) {
        Weapon weapon = getPlayerWeapon(player);

        int upgradeCost = 120 + weapon.getLevel() * 80 + player.getLevel() * 25;

        if (player.getGold() < upgradeCost) {
            throw new RuntimeException(
                    "金幣不足，升級需要 " + upgradeCost + " 金幣");
        }

        player.setGold(player.getGold() - upgradeCost);
        playerRepository.save(player);

        weapon.setLevel(weapon.getLevel() + 1);
        weapon.setAttack(weapon.getAttack() + 9);

        if (weapon.getLevel() % 3 == 0) {
            player.setMaxHp(player.getMaxHp() + 10);
            playerRepository.save(player);
        }

        return weaponRepository.save(weapon);
    }

    @Transactional
    public Weapon increaseWeaponAttack(Player player, int attackGain) {
        Weapon weapon = getPlayerWeapon(player);
        weapon.setAttack(weapon.getAttack() + Math.max(1, attackGain));
        return weaponRepository.save(weapon);
    }

    @Transactional
    public Weapon changeWeapon(Player player, String weaponType) {
        Weapon weapon = getPlayerWeapon(player);
        String normalized = normalizeWeaponType(weaponType);
        weapon.setName(displayNameForType(normalized));
        return weaponRepository.save(weapon);
    }

    public String normalizeWeaponType(String weaponType) {
        if (weaponType == null || weaponType.isBlank()) {
            throw new RuntimeException("請指定武器類型");
        }

        String normalized = weaponType.trim().toUpperCase();
        for (String allowedWeaponType : ALLOWED_WEAPON_TYPES) {
            if (allowedWeaponType.equals(normalized)) {
                return normalized;
            }
        }

        throw new RuntimeException("無效的武器類型，請使用 PISTOL、BLADE 或 SPEAR");
    }

    public String displayNameForType(String weaponType) {
        return switch (weaponType) {
            case "PISTOL" -> "Pistol";
            case "BLADE" -> "Blade";
            case "SPEAR" -> "Spear";
            default -> throw new RuntimeException("無效的武器類型：" + weaponType);
        };
    }

    @Transactional
    public Weapon resetWeapon(Player player) {
        Weapon weapon = getPlayerWeapon(player);
        weapon.setName("Pistol");
        weapon.setLevel(1);
        weapon.setAttack(16);
        return weaponRepository.save(weapon);
    }
}