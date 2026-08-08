package com.operation.survival.controller;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import com.operation.survival.repository.UserRepository;
import com.operation.survival.service.PlayerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final PlayerService playerService;
    private final UserRepository userRepository;

    public ProgressController(PlayerService playerService, UserRepository userRepository) {
        this.playerService = playerService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Map<String, Object> getProgress(Authentication authentication) {
        Player player = getCurrentPlayer(authentication);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "目前進度讀取成功");
        response.put("progress", playerService.buildProgressSnapshot(player));
        return response;
    }

    @PostMapping("/save")
    public Map<String, Object> saveProgress(Authentication authentication) {
        Player player = getCurrentPlayer(authentication);
        Player saved = playerService.saveProgress(player);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "進度已儲存");
        response.put("progress", playerService.buildProgressSnapshot(saved));
        return response;
    }

    private Player getCurrentPlayer(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        return playerService.getPlayer(user);
    }
}
