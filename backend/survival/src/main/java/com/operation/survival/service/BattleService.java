package com.operation.survival.service;

import com.operation.survival.dto.BattleResponse;
import com.operation.survival.entity.Monster;
import com.operation.survival.entity.Player;
import com.operation.survival.entity.Weapon;
import com.operation.survival.repository.MonsterRepository;
import com.operation.survival.repository.PlayerRepository;
import com.operation.survival.repository.WeaponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BattleService {

    private static final int MAX_STAGE = 5;

    private record DifficultyProfile(double playerDamageMultiplier,
                                     double skillMultiplierBonus,
                                     double monsterHpMultiplier,
                                     double monsterDamageMultiplier,
                                     int rewardBonusGold,
                                     int rewardBonusExp,
                                     String label) {
    }

    private final PlayerRepository playerRepository;
    private final WeaponRepository weaponRepository;
    private final MonsterRepository monsterRepository;
    private final PlayerService playerService;

    public BattleService(
            PlayerRepository playerRepository,
            WeaponRepository weaponRepository,
            MonsterRepository monsterRepository,
            PlayerService playerService) {

        this.playerRepository = playerRepository;
        this.weaponRepository = weaponRepository;
        this.monsterRepository = monsterRepository;
        this.playerService = playerService;
    }

    @Transactional
    public BattleResponse attack(String username, Long monsterId) {
        return attack(username, monsterId, false);
    }

    @Transactional
    public BattleResponse attack(String username, Long monsterId, boolean useSkill) {
        Player player = playerRepository.findByUserUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("找不到玩家資料"));

        if (player.getHp() <= 0) {
            throw new RuntimeException("你已陣亡，請先使用回復或藥水再戰鬥");
        }

        Weapon weapon = weaponRepository.findFirstByPlayer(player)
                .orElseThrow(() ->
                        new RuntimeException("找不到玩家武器"));

        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() ->
                        new RuntimeException("找不到怪物"));

        if (monster.getStage() > player.getCurrentStage()) {
            throw new RuntimeException("尚未解鎖此關卡，請先完成前一關挑戰");
        }

        int cooldownBeforeBattle = player.getSkillCooldown() == null ? 0 : player.getSkillCooldown();
        if (cooldownBeforeBattle > 0) {
            player.setSkillCooldown(cooldownBeforeBattle - 1);
        }

        DifficultyProfile profile = difficultyProfile(player.getDifficulty());
        int skillLevel = player.getSkillLevel() == null ? 1 : player.getSkillLevel();

        int basePlayerDamage = Math.max(1, weapon.getAttack()
                + Math.max(0, weapon.getLevel() - 1) * 3
            + Math.max(0, player.getLevel() - 1));
        basePlayerDamage = Math.max(1, (int) Math.round(basePlayerDamage * profile.playerDamageMultiplier()));

        boolean skillUsed = false;
        int playerDamage = basePlayerDamage;

        if (useSkill) {
            if (player.getSkillCooldown() > 0) {
                throw new RuntimeException("技能冷卻中，還需 " + player.getSkillCooldown() + " 場戰鬥");
            }
            skillUsed = true;
            double multiplier = 1.6 + Math.max(0, skillLevel - 1) * 0.25 + profile.skillMultiplierBonus();
            playerDamage = Math.max(1, (int) Math.round(basePlayerDamage * multiplier));
            player.setSkillCooldown(2);
        }

        int monsterDamage = Math.max(1, monster.getAttack()
                + Math.max(0, monster.getStage() - 1));
        monsterDamage = Math.max(1, (int) Math.round(monsterDamage * profile.monsterDamageMultiplier()));

        boolean bossBattle = monster.getStage() == MAX_STAGE;
        boolean bossSecondPhaseTriggered = false;

        int remainingMonsterHp = Math.max(1, (int) Math.round(monster.getHp() * profile.monsterHpMultiplier()));
        int remainingPlayerHp = player.getHp();
        int roundCount = 0;
        List<String> battleLog = new ArrayList<>();
        battleLog.add("戰鬥開始！" + monster.getName() + " 出現於第 " + monster.getStage() + " 階段");
        battleLog.add("目前難易度：" + profile.label());
        if (skillUsed) {
            battleLog.add("你發動技能：破甲突擊！本場傷害提升");
        }
        if (bossBattle) {
            battleLog.add("最終首領戰開啟：首領可能進入二階段狂暴");
        }

        while (remainingPlayerHp > 0 && remainingMonsterHp > 0 && roundCount < 12) {
            roundCount++;
            remainingMonsterHp = Math.max(0, remainingMonsterHp - playerDamage);
            battleLog.add("第 " + roundCount + " 回合：你造成 " + playerDamage + " 點傷害，怪物剩餘 " + remainingMonsterHp + " HP");

            if (remainingMonsterHp <= 0 && bossBattle && !bossSecondPhaseTriggered && roundCount < 12) {
                bossSecondPhaseTriggered = true;
                remainingMonsterHp = Math.max(80, monster.getHp() / 2);
                monsterDamage += 5;
                battleLog.add("首領進入二階段狂暴！生命回復至 " + remainingMonsterHp + "，攻擊力提升");
            }

            if (remainingMonsterHp <= 0) {
                break;
            }

            remainingPlayerHp = Math.max(0, remainingPlayerHp - monsterDamage);
            battleLog.add("第 " + roundCount + " 回合：怪物反擊造成 " + monsterDamage + " 點傷害，你剩餘 " + remainingPlayerHp + " HP");
        }

        BattleResponse response = new BattleResponse();
        response.setDamage(playerDamage);
        response.setMonsterHp(remainingMonsterHp);
        response.setMonsterName(monster.getName());
        response.setMonsterStage(monster.getStage());
        response.setRoundCount(roundCount);
        response.setBattleLog(battleLog);
        response.setStageAdvanced(false);
        response.setGameCompleted(false);
        response.setSkillUsed(skillUsed);
        response.setBossBattle(bossBattle);
        response.setBossSecondPhaseTriggered(bossSecondPhaseTriggered);

        boolean monsterDead = remainingMonsterHp <= 0;
        response.setMonsterDead(monsterDead);

        player.setHp(remainingPlayerHp);
        player.setCurrentStage(Math.max(player.getCurrentStage(), monster.getStage()));

        if (monsterDead) {
            int rewardGold = monster.getRewardGold() + profile.rewardBonusGold() + monster.getStage() * 8;
            int rewardExp = 35 + monster.getStage() * 15 + profile.rewardBonusExp();
            playerService.applyBattleReward(player, rewardGold, rewardExp);

            boolean advanced = false;
            if (monster.getStage().equals(player.getCurrentStage())
                    && player.getCurrentStage() < MAX_STAGE) {
                player.setCurrentStage(player.getCurrentStage() + 1);
                player.setHp(player.getMaxHp());
                playerRepository.save(player);
                battleLog.add("你突破了第 " + monster.getStage() + " 關，已解鎖第 " + player.getCurrentStage() + " 關");
                advanced = true;
            }

            boolean gameCompleted = monster.getStage() == MAX_STAGE;
            if (gameCompleted) {
                battleLog.add("最終通關！你已擊敗最終關卡首領，成功完成 Operation Survival");
            }

            response.setPlayerHp(player.getHp());
            response.setMonsterDamage(0);
            response.setRewardGold(rewardGold);
            response.setPlayerLevel(player.getLevel());
            response.setPlayerExperience(player.getExperience());
            response.setPlayerMaxHp(player.getMaxHp());
            response.setPlayerCurrentStage(player.getCurrentStage());
            response.setPlayerPotionCount(player.getPotionCount());
            response.setSkillCooldown(player.getSkillCooldown());
            response.setPlayerAlive(true);
            response.setStageAdvanced(advanced);
            response.setGameCompleted(gameCompleted);
            battleLog.add("勝利！你獲得 " + rewardGold + " 金幣與 " + rewardExp + " 經驗值");
            response.setMessage(gameCompleted
                    ? "最終勝利！你完成整個生存戰役"
                    : "勝利！你擊敗了 " + monster.getName() + "，並獲得獎勵");
            response.setBattleLog(battleLog);
            return response;
        }

        playerRepository.save(player);

        response.setPlayerHp(remainingPlayerHp);
        response.setMonsterDamage(monsterDamage);
        response.setRewardGold(0);
        response.setPlayerLevel(player.getLevel());
        response.setPlayerExperience(player.getExperience());
        response.setPlayerMaxHp(player.getMaxHp());
        response.setPlayerCurrentStage(player.getCurrentStage());
        response.setPlayerPotionCount(player.getPotionCount());
        response.setSkillCooldown(player.getSkillCooldown());
        response.setPlayerAlive(false);

        if (remainingPlayerHp <= 0) {
            battleLog.add("你被擊敗了，請重新整理狀態後再挑戰");
            response.setMessage("你已陣亡，請重新整理狀態後再挑戰");
        } else {
            battleLog.add("戰鬥尚未結束，請再嘗試一次");
            response.setMessage("戰鬥尚未結束，怪物還在反擊");
        }

        response.setBattleLog(battleLog);
        return response;
    }

    private DifficultyProfile difficultyProfile(String difficulty) {
        String normalized = difficulty == null ? "NORMAL" : difficulty.trim().toUpperCase();

        return switch (normalized) {
            case "CASUAL" -> new DifficultyProfile(1.30, 0.18, 0.82, 0.62, -5, -8, "CASUAL");
            case "HARDCORE" -> new DifficultyProfile(1.0, 0.02, 1.18, 1.08, 18, 24, "HARDCORE");
            default -> new DifficultyProfile(1.14, 0.08, 0.95, 0.78, 8, 10, "NORMAL");
        };
    }
}