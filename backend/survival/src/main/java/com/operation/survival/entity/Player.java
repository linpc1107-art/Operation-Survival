package com.operation.survival.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Integer gold = 0;

    @Column(nullable = false)
    private Integer currentStage = 1;

    @Column(nullable = false)
    private Integer hp = 140;

    @Column(nullable = false)
    private Integer maxHp = 140;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Integer experience = 0;

    @Column(nullable = false)
    private Integer potionCount = 2;

    @Column(nullable = false)
    private Integer totalWins = 0;

    @Column(nullable = false)
    private Integer skillLevel = 1;

    @Column(nullable = false)
    private Integer skillCooldown = 0;

    @Column(nullable = false)
    private String difficulty = "NORMAL";

    @Column(nullable = false)
    private Integer saveVersion = 1;

    private LocalDateTime lastSavedAt;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Player() {
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
        this.gold = gold;
    }

    public Integer getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(Integer currentStage) {
        this.currentStage = currentStage;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(Integer maxHp) {
        this.maxHp = maxHp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getPotionCount() {
        return potionCount;
    }

    public void setPotionCount(Integer potionCount) {
        this.potionCount = potionCount;
    }

    public Integer getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(Integer totalWins) {
        this.totalWins = totalWins;
    }

    public Integer getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Integer skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Integer getSkillCooldown() {
        return skillCooldown;
    }

    public void setSkillCooldown(Integer skillCooldown) {
        this.skillCooldown = skillCooldown;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getSaveVersion() {
        return saveVersion;
    }

    public void setSaveVersion(Integer saveVersion) {
        this.saveVersion = saveVersion;
    }

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public void setLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}