package com.operation.survival.dto;

import java.util.ArrayList;
import java.util.List;

public class BattleResponse {

    private Integer playerHp;
    private Integer monsterHp;
    private Integer damage;
    private Integer monsterDamage;
    private Boolean monsterDead;
    private Integer rewardGold;
    private String message;
    private String monsterName;
    private Integer monsterStage;
    private Integer roundCount;
    private Boolean playerAlive;
    private Integer playerLevel;
    private Integer playerExperience;
    private Integer playerMaxHp;
    private Integer playerCurrentStage;
    private Integer playerPotionCount;
    private Boolean stageAdvanced;
    private Boolean gameCompleted;
    private Boolean skillUsed;
    private Integer skillCooldown;
    private Boolean bossBattle;
    private Boolean bossSecondPhaseTriggered;
    private List<String> battleLog = new ArrayList<>();

    public Integer getPlayerHp() {
        return playerHp;
    }

    public void setPlayerHp(Integer playerHp) {
        this.playerHp = playerHp;
    }

    public Integer getMonsterHp() {
        return monsterHp;
    }

    public void setMonsterHp(Integer monsterHp) {
        this.monsterHp = monsterHp;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public Integer getMonsterDamage() {
        return monsterDamage;
    }

    public void setMonsterDamage(Integer monsterDamage) {
        this.monsterDamage = monsterDamage;
    }

    public Boolean getMonsterDead() {
        return monsterDead;
    }

    public void setMonsterDead(Boolean monsterDead) {
        this.monsterDead = monsterDead;
    }

    public Integer getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(Integer rewardGold) {
        this.rewardGold = rewardGold;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public Integer getMonsterStage() {
        return monsterStage;
    }

    public void setMonsterStage(Integer monsterStage) {
        this.monsterStage = monsterStage;
    }

    public Integer getRoundCount() {
        return roundCount;
    }

    public void setRoundCount(Integer roundCount) {
        this.roundCount = roundCount;
    }

    public Boolean getPlayerAlive() {
        return playerAlive;
    }

    public void setPlayerAlive(Boolean playerAlive) {
        this.playerAlive = playerAlive;
    }

    public Integer getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(Integer playerLevel) {
        this.playerLevel = playerLevel;
    }

    public Integer getPlayerExperience() {
        return playerExperience;
    }

    public void setPlayerExperience(Integer playerExperience) {
        this.playerExperience = playerExperience;
    }

    public Integer getPlayerMaxHp() {
        return playerMaxHp;
    }

    public void setPlayerMaxHp(Integer playerMaxHp) {
        this.playerMaxHp = playerMaxHp;
    }

    public Integer getPlayerCurrentStage() {
        return playerCurrentStage;
    }

    public void setPlayerCurrentStage(Integer playerCurrentStage) {
        this.playerCurrentStage = playerCurrentStage;
    }

    public Integer getPlayerPotionCount() {
        return playerPotionCount;
    }

    public void setPlayerPotionCount(Integer playerPotionCount) {
        this.playerPotionCount = playerPotionCount;
    }

    public Boolean getStageAdvanced() {
        return stageAdvanced;
    }

    public void setStageAdvanced(Boolean stageAdvanced) {
        this.stageAdvanced = stageAdvanced;
    }

    public Boolean getGameCompleted() {
        return gameCompleted;
    }

    public void setGameCompleted(Boolean gameCompleted) {
        this.gameCompleted = gameCompleted;
    }

    public Boolean getSkillUsed() {
        return skillUsed;
    }

    public void setSkillUsed(Boolean skillUsed) {
        this.skillUsed = skillUsed;
    }

    public Integer getSkillCooldown() {
        return skillCooldown;
    }

    public void setSkillCooldown(Integer skillCooldown) {
        this.skillCooldown = skillCooldown;
    }

    public Boolean getBossBattle() {
        return bossBattle;
    }

    public void setBossBattle(Boolean bossBattle) {
        this.bossBattle = bossBattle;
    }

    public Boolean getBossSecondPhaseTriggered() {
        return bossSecondPhaseTriggered;
    }

    public void setBossSecondPhaseTriggered(Boolean bossSecondPhaseTriggered) {
        this.bossSecondPhaseTriggered = bossSecondPhaseTriggered;
    }

    public List<String> getBattleLog() {
        return battleLog;
    }

    public void setBattleLog(List<String> battleLog) {
        this.battleLog = battleLog;
    }
}