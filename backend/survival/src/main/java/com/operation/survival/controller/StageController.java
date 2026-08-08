package com.operation.survival.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stage")
public class StageController {

    @GetMapping("/list")
    public List<Map<String, Object>> getStages() {
        return List.of(
                stage(1, "廢墟外圍", "新手訓練區", 1, false),
                stage(2, "補給走廊", "中階怪物與資源點", 2, false),
                stage(3, "污染研究室", "高攻怪物集中", 4, false),
                stage(4, "防衛工廠", "菁英守衛區", 6, false),
                stage(5, "深淵核心", "最終首領戰", 8, true)
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getStageDetail(@PathVariable Integer id) {
        if (id < 1 || id > 5) {
            throw new RuntimeException("關卡不存在，請輸入 1 到 5");
        }

        return getStages().stream()
                .filter(stage -> id.equals(stage.get("id")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("關卡不存在"));
    }

    private Map<String, Object> stage(int id, String name, String description, int recommendedLevel, boolean bossStage) {
        Map<String, Object> stage = new LinkedHashMap<>();
        stage.put("id", id);
        stage.put("name", name);
        stage.put("description", description);
        stage.put("recommendedLevel", recommendedLevel);
        stage.put("bossStage", bossStage);
        return stage;
    }
}
