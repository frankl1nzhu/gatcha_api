package com.gatcha.api.player.controller;

import com.gatcha.api.auth.model.User;
import com.gatcha.api.auth.service.AuthService;
import com.gatcha.api.player.dto.ExperienceRequest;
import com.gatcha.api.player.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;
    private final AuthService authService;

    public PlayerController(PlayerService playerService, AuthService authService) {
        this.playerService = playerService;
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(playerService.getProfile(username));
    }

    @GetMapping("/monsters")
    public ResponseEntity<List<String>> getMonsters(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(playerService.getMonsters(username));
    }

    @GetMapping("/level")
    public ResponseEntity<Integer> getLevel(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(playerService.getLevel(username));
    }

    @PostMapping("/experience")
    public ResponseEntity<User> addExperience(
            @RequestHeader("Authorization") String token,
            @RequestBody ExperienceRequest request) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(playerService.addExperience(username, request.getExperience()));
    }

    @PostMapping("/levelup")
    public ResponseEntity<User> levelUp(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(playerService.levelUp(username));
    }

    @PostMapping("/monsters/{monsterId}")
    public ResponseEntity<Boolean> addMonster(
            @RequestHeader("Authorization") String token,
            @PathVariable String monsterId) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(playerService.addMonster(username, monsterId));
    }

    @DeleteMapping("/monsters/{monsterId}")
    public ResponseEntity<Boolean> removeMonster(
            @RequestHeader("Authorization") String token,
            @PathVariable String monsterId) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(playerService.removeMonster(username, monsterId));
    }
}