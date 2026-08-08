package com.operation.survival.controller;

import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import com.operation.survival.repository.UserRepository;
import com.operation.survival.service.PlayerService;
import com.operation.survival.service.ShopService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    private final ShopService shopService;
    private final PlayerService playerService;
    private final UserRepository userRepository;

    public ShopController(ShopService shopService, PlayerService playerService, UserRepository userRepository) {
        this.shopService = shopService;
        this.playerService = playerService;
        this.userRepository = userRepository;
    }

    @GetMapping("/items")
    public List<Map<String, Object>> getItems() {
        return shopService.getItems();
    }

    @PostMapping("/buy")
    public Map<String, Object> buy(Authentication authentication, @RequestParam String itemCode) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到使用者資料"));

        Player player = playerService.getPlayer(user);
        Map<String, Object> result = shopService.buy(player, itemCode);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", result.get("message"));
        response.put("result", result);
        return response;
    }
}
