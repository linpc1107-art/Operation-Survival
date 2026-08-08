package com.operation.survival.service;

import com.operation.survival.entity.Player;
import com.operation.survival.repository.PlayerRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerResetServiceTest {

    @Test
    void endGameAndResetShouldResetPlayerCoreStats() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        PlayerService playerService = new PlayerService(playerRepository);

        Player player = new Player();
        player.setGold(999);
        player.setCurrentStage(4);
        player.setHp(12);
        player.setMaxHp(300);
        player.setLevel(8);
        player.setExperience(88);
        player.setPotionCount(9);
        player.setTotalWins(77);
        player.setSkillLevel(5);
        player.setSkillCooldown(2);

        when(playerRepository.save(player)).thenReturn(player);

        Player reset = playerService.endGameAndReset(player);

        assertEquals(260, reset.getGold());
        assertEquals(1, reset.getCurrentStage());
        assertEquals(140, reset.getHp());
        assertEquals(140, reset.getMaxHp());
        assertEquals(1, reset.getLevel());
        assertEquals(0, reset.getExperience());
        assertEquals(2, reset.getPotionCount());
        assertEquals(0, reset.getTotalWins());
        assertEquals(1, reset.getSkillLevel());
        assertEquals(0, reset.getSkillCooldown());
        assertNull(reset.getLastSavedAt());
    }
}
