package com.operation.survival.service;

import com.operation.survival.entity.Monster;
import com.operation.survival.repository.MonsterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonsterService {

    private final MonsterRepository monsterRepository;

    public MonsterService(MonsterRepository monsterRepository) {
        this.monsterRepository = monsterRepository;
    }

    public List<Monster> getStageMonsters(Integer stage) {
        return monsterRepository.findByStage(stage);
    }
}