package com.operation.survival.service;

import com.operation.survival.dto.BattleResponse;
import com.operation.survival.entity.Monster;
import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import com.operation.survival.entity.Weapon;
import com.operation.survival.repository.MonsterRepository;
import com.operation.survival.repository.PlayerRepository;
import com.operation.survival.repository.WeaponRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BattleServiceTest {

    @Test
    void shouldLevelUpPlayerAndRecordBattleLogAfterVictory() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        WeaponRepository weaponRepository = mock(WeaponRepository.class);
        MonsterRepository monsterRepository = mock(MonsterRepository.class);
        PlayerService playerService = new PlayerService(playerRepository);

        BattleService battleService = new BattleService(
                playerRepository,
                weaponRepository,
                monsterRepository,
                playerService
        );

        User user = new User("alice", "pw");
        Player player = new Player();
        player.setUser(user);
        player.setNickname("Alice");
        player.setGold(0);
        player.setCurrentStage(3);
        player.setHp(100);
        player.setMaxHp(100);
        player.setLevel(5);
        player.setExperience(0);

        Weapon weapon = new Weapon();
        weapon.setAttack(10);
        weapon.setLevel(1);
        weapon.setPlayer(player);

        Monster monster = new Monster();
        monster.setId(7L);
        monster.setName("Boss");
        monster.setHp(8);
        monster.setAttack(4);
        monster.setRewardGold(1000);
        monster.setStage(3);

        when(playerRepository.findByUserUsername("alice")).thenReturn(Optional.of(player));
        when(weaponRepository.findFirstByPlayer(player)).thenReturn(Optional.of(weapon));
        when(monsterRepository.findById(7L)).thenReturn(Optional.of(monster));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BattleResponse response = battleService.attack("alice", 7L);

        assertTrue(response.getMonsterDead());
        assertEquals(1032, response.getRewardGold());
        assertTrue(response.getBattleLog().size() >= 2);
        assertEquals(100, response.getPlayerHp());
        assertTrue(response.getStageAdvanced());
        assertEquals(4, response.getPlayerCurrentStage());
    }

    @Test
    void shouldTriggerSecondPhaseInBossBattle() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        WeaponRepository weaponRepository = mock(WeaponRepository.class);
        MonsterRepository monsterRepository = mock(MonsterRepository.class);
        PlayerService playerService = new PlayerService(playerRepository);

        BattleService battleService = new BattleService(
                playerRepository,
                weaponRepository,
                monsterRepository,
                playerService
        );

        User user = new User("bossTester", "pw");
        Player player = new Player();
        player.setUser(user);
        player.setNickname("BossTester");
        player.setGold(0);
        player.setCurrentStage(5);
        player.setHp(1000);
        player.setMaxHp(1000);
        player.setLevel(10);
        player.setExperience(0);
        player.setPotionCount(2);
        player.setSkillLevel(1);
        player.setSkillCooldown(0);

        Weapon weapon = new Weapon();
        weapon.setAttack(500);
        weapon.setLevel(1);
        weapon.setPlayer(player);

        Monster boss = new Monster();
        boss.setId(99L);
        boss.setName("Demon Lord");
        boss.setHp(10);
        boss.setAttack(1);
        boss.setRewardGold(100);
        boss.setStage(5);

        when(playerRepository.findByUserUsername("bossTester")).thenReturn(Optional.of(player));
        when(weaponRepository.findFirstByPlayer(player)).thenReturn(Optional.of(weapon));
        when(monsterRepository.findById(99L)).thenReturn(Optional.of(boss));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BattleResponse response = battleService.attack("bossTester", 99L);

        assertTrue(response.getBossBattle());
        assertTrue(response.getBossSecondPhaseTriggered());
        assertTrue(response.getBattleLog().stream().anyMatch(line -> line.contains("二階段狂暴")));
    }
}
