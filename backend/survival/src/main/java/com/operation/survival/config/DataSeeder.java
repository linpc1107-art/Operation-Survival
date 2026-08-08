package com.operation.survival.config;

import com.operation.survival.entity.Monster;
import com.operation.survival.repository.MonsterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedMonsters(MonsterRepository monsterRepository) {
        return args -> {
            createMonsterIfMissing(monsterRepository, "Slime", 30, 8, 10, 1);
            createMonsterIfMissing(monsterRepository, "Goblin", 45, 12, 15, 1);
            createMonsterIfMissing(monsterRepository, "Orc", 70, 18, 25, 2);
            createMonsterIfMissing(monsterRepository, "Wolf", 85, 22, 35, 2);
            createMonsterIfMissing(monsterRepository, "Witch", 110, 26, 45, 3);
            createMonsterIfMissing(monsterRepository, "Skeleton", 95, 24, 40, 3);
            createMonsterIfMissing(monsterRepository, "Troll", 140, 30, 60, 4);
            createMonsterIfMissing(monsterRepository, "Stone Golem", 180, 36, 80, 4);
            createMonsterIfMissing(monsterRepository, "Dragon", 240, 45, 130, 5);
            createMonsterIfMissing(monsterRepository, "Demon Lord", 300, 55, 180, 5);
        };
    }

    private void createMonsterIfMissing(MonsterRepository monsterRepository, String name, int hp, int attack, int rewardGold, int stage) {
        boolean exists = monsterRepository.findByStage(stage).stream()
                .anyMatch(monster -> name.equals(monster.getName()));

        if (!exists) {
            Monster monster = new Monster();
            monster.setName(name);
            monster.setHp(hp);
            monster.setAttack(attack);
            monster.setRewardGold(rewardGold);
            monster.setStage(stage);
            monsterRepository.save(monster);
        }
    }
}
