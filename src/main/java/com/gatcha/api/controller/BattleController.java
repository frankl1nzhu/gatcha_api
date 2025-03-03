package com.gatcha.api.controller;

import com.gatcha.api.dto.BattleRequest;
import com.gatcha.api.dto.BattleResponse;
import com.gatcha.api.service.BattleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
public class BattleController {
    private final BattleService battleService;

    @PostMapping
    public ResponseEntity<BattleResponse> startBattle(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody BattleRequest request) {
        return ResponseEntity.ok(battleService.startBattle(
                username,
                request.getPlayerMonsterId(),
                request.getOpponentMonsterId()));
    }

    @GetMapping
    public ResponseEntity<List<BattleResponse>> getPlayerBattles(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(battleService.getPlayerBattles(username));
    }

    @GetMapping("/{battleId}")
    public ResponseEntity<BattleResponse> getBattle(
            @PathVariable String battleId) {
        return ResponseEntity.ok(battleService.getBattle(battleId));
    }
}