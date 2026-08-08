package com.operation.survival.controller;

import com.operation.survival.dto.BattleRequest;
import com.operation.survival.dto.BattleResponse;
import com.operation.survival.service.BattleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping("/attack")
    public ResponseEntity<BattleResponse> attack(
            @RequestBody BattleRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        BattleResponse response =
            battleService.attack(
                username,
                request.getMonsterId(),
                Boolean.TRUE.equals(request.getUseSkill())
            );

        return ResponseEntity.ok(response);
    }
}