package com.operation.survival.service;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.Weapon;
import com.operation.survival.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopService {

    private final PlayerRepository playerRepository;
    private final WeaponService weaponService;

    public ShopService(PlayerRepository playerRepository, WeaponService weaponService) {
        this.playerRepository = playerRepository;
        this.weaponService = weaponService;
    }

    public List<Map<String, Object>> getItems() {
        return List.of(
                buildItem("POTION", "治療藥水", 60, "+1 藥水，可在戰鬥後回復生命"),
                buildItem("FIELD_MEDKIT", "戰地醫療包", 100, "立即恢復至滿血"),
                buildItem("SHARPEN_KIT", "磨刀工具", 180, "武器攻擊力 +3（永久）"),
                buildItem("COOLANT", "技能冷卻劑", 90, "技能冷卻 -1 場（最低為 0）")
        );
    }

    @Transactional
    public Map<String, Object> buy(Player player, String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            throw new RuntimeException("請指定要購買的道具");
        }

        String normalized = itemCode.trim().toUpperCase();
        int cost = getItemCost(normalized);

        if (player.getGold() < cost) {
            throw new RuntimeException("金幣不足，購買此道具需要 " + cost + " 金幣");
        }

        player.setGold(player.getGold() - cost);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemCode", normalized);
        result.put("cost", cost);

        switch (normalized) {
            case "POTION" -> {
                player.setPotionCount(player.getPotionCount() + 1);
                playerRepository.save(player);
                result.put("message", "購買成功：獲得 1 瓶治療藥水");
            }
            case "FIELD_MEDKIT" -> {
                player.setHp(player.getMaxHp());
                playerRepository.save(player);
                result.put("message", "購買成功：你已被完整治療");
            }
            case "SHARPEN_KIT" -> {
                playerRepository.save(player);
                Weapon weapon = weaponService.increaseWeaponAttack(player, 3);
                result.put("weaponAttack", weapon.getAttack());
                result.put("weaponLevel", weapon.getLevel());
                result.put("message", "購買成功：武器攻擊力永久提升");
            }
            case "COOLANT" -> {
                int oldCooldown = player.getSkillCooldown();
                player.setSkillCooldown(Math.max(0, oldCooldown - 1));
                playerRepository.save(player);
                result.put("skillCooldown", player.getSkillCooldown());
                result.put("message", "購買成功：技能冷卻已降低");
            }
            default -> throw new RuntimeException("無效的商店道具代碼：" + itemCode);
        }

        result.put("gold", player.getGold());
        result.put("hp", player.getHp());
        result.put("maxHp", player.getMaxHp());
        result.put("potionCount", player.getPotionCount());
        result.put("skillCooldown", player.getSkillCooldown());

        return result;
    }

    private int getItemCost(String itemCode) {
        return switch (itemCode) {
            case "POTION" -> 60;
            case "FIELD_MEDKIT" -> 100;
            case "SHARPEN_KIT" -> 180;
            case "COOLANT" -> 90;
            default -> throw new RuntimeException("無效的商店道具代碼：" + itemCode);
        };
    }

    private Map<String, Object> buildItem(String code, String name, int price, String description) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("code", code);
        item.put("name", name);
        item.put("price", price);
        item.put("description", description);
        return item;
    }
}
