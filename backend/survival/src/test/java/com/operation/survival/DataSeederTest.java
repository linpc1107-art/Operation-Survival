package com.operation.survival;

import com.operation.survival.repository.MonsterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DataSeederTest {

    @Autowired
    private MonsterRepository monsterRepository;

    @Test
    void seededMonstersShouldCoverAllFiveStages() {
        for (int stage = 1; stage <= 5; stage++) {
            assertThat(monsterRepository.findByStage(stage))
                    .as("stage " + stage + " should have monsters")
                    .isNotEmpty();
        }
    }
}
