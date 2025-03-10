package com.gatcha.api.summon.controller;

import com.gatcha.api.auth.service.AuthService;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.summon.model.SummonLog;
import com.gatcha.api.summon.service.SummonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling summon-related requests
 */
@RestController
@RequestMapping("/api/summon")
public class SummonController {

    private final SummonService summonService;
    private final AuthService authService;

    public SummonController(SummonService summonService, AuthService authService) {
        this.summonService = summonService;
        this.authService = authService;
    }

    /**
     * Summon a single monster
     * 
     * @param token Authorization token
     * @return The summoned monster
     */
    @PostMapping
    public ResponseEntity<PlayerMonster> summon(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(summonService.summon(username));
    }

    /**
     * Summon multiple monsters (up to 10)
     * 
     * @param token Authorization token
     * @return List of summoned monsters
     */
    @PostMapping("/multi")
    public ResponseEntity<List<PlayerMonster>> summonMultiple(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(summonService.summonMultiple(username, 10));
    }

    /**
     * Get summon history for the current user
     * 
     * @param token Authorization token
     * @return List of summon logs
     */
    @GetMapping("/history")
    public ResponseEntity<List<SummonLog>> getSummonHistory(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(summonService.getSummonHistory(username));
    }

    /**
     * Reprocess failed summons
     * 
     * @param token Authorization token
     * @return Void
     */
    @PostMapping("/reprocess")
    public ResponseEntity<Void> reprocessFailedSummons(@RequestHeader("Authorization") String token) {
        // Validate token, but don't use the username
        authService.validateToken(token.replace("Bearer ", ""));
        summonService.reprocessFailedSummons();
        return ResponseEntity.ok().build();
    }
}