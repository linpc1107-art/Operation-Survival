package com.operation.survival.service;

import com.operation.survival.entity.Player;
import com.operation.survival.repository.PlayerRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerServiceTest {

    @Test
    void healPlayerShouldRestoreHpAndSpendGold() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        PlayerService playerService = new PlayerService(playerRepository);

        Player player = new Player();
        player.setGold(200);
        player.setHp(40);
        player.setMaxHp(120);
        player.setLevel(2);

        when(playerRepository.save(player)).thenReturn(player);

        Player updated = playerService.healPlayer(player);

        assertEquals(120, updated.getHp());
        assertEquals(145, updated.getGold());
    }

    @Test
    void advanceStageShouldMoveToNextStageAndHealFully() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        PlayerService playerService = new PlayerService(playerRepository);

        Player player = new Player();
        player.setCurrentStage(1);
        player.setLevel(2);
        player.setGold(300);
        player.setHp(20);
        player.setMaxHp(120);

        when(playerRepository.save(player)).thenReturn(player);

        Player updated = playerService.advanceStage(player);

        assertEquals(2, updated.getCurrentStage());
        assertEquals(120, updated.getHp());
        assertEquals(240, updated.getGold());
    }
}
