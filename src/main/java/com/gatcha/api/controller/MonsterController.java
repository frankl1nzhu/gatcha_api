package com.gatcha.api.controller;

import com.gatcha.api.dto.ExperienceRequest;
import com.gatcha.api.dto.MonsterResponse;
import com.gatcha.api.dto.UpgradeSkillRequest;
import com.gatcha.api.service.MonsterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monsters")
@RequiredArgsConstructor
public class MonsterController {
    private final MonsterService monsterService;

    @GetMapping
    public ResponseEntity<List<MonsterResponse>> getPlayerMonsters(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(monsterService.getPlayerMonsters(username));
    }

    @GetMapping("/{monsterId}")
    public ResponseEntity<MonsterResponse> getMonster(
            @AuthenticationPrincipal String username,
            @PathVariable String monsterId) {
        return ResponseEntity.ok(monsterService.getMonster(username, monsterId));
    }

    @PostMapping("/{monsterId}/experience")
    public ResponseEntity<MonsterResponse> addExperience(
            @AuthenticationPrincipal String username,
            @PathVariable String monsterId,
            @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(monsterService.addExperience(username, monsterId, request.getExperience()));
    }

    @PostMapping("/{monsterId}/skills/upgrade")
    public ResponseEntity<MonsterResponse> upgradeSkill(
            @AuthenticationPrincipal String username,
            @PathVariable String monsterId,
            @Valid @RequestBody UpgradeSkillRequest request) {
        return ResponseEntity.ok(monsterService.upgradeSkill(username, monsterId, request.getSkillIndex()));
    }
}