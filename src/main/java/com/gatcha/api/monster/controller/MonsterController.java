package com.gatcha.api.monster.controller;

import com.gatcha.api.auth.service.AuthService;
import com.gatcha.api.monster.dto.ExperienceRequest;
import com.gatcha.api.monster.dto.SkillUpgradeRequest;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.service.MonsterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monsters")
public class MonsterController {

    private final MonsterService monsterService;
    private final AuthService authService;

    public MonsterController(MonsterService monsterService, AuthService authService) {
        this.monsterService = monsterService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<PlayerMonster>> getMonsters(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(monsterService.getMonstersByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerMonster> getMonster(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(monsterService.getMonsterById(id, username));
    }

    @PostMapping("/{id}/experience")
    public ResponseEntity<PlayerMonster> addExperience(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody ExperienceRequest request) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(monsterService.addExperience(id, username, request.getExperience()));
    }

    @PostMapping("/{id}/skill")
    public ResponseEntity<PlayerMonster> upgradeSkill(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody SkillUpgradeRequest request) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(monsterService.upgradeSkill(id, username, request.getSkillNum()));
    }
}