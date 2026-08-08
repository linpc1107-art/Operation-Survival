package com.operation.survival.controller;

import com.operation.survival.entity.Monster;
import com.operation.survival.service.MonsterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monster")
public class MonsterController {

    private final MonsterService monsterService;

    public MonsterController(MonsterService monsterService) {
        this.monsterService = monsterService;
    }

    @GetMapping("/stage/{stage}")
    public List<Monster> getStageMonsters(
            @PathVariable Integer stage) {

        return monsterService.getStageMonsters(stage);
    }
}