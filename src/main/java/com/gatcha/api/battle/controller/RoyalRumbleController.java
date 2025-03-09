package com.gatcha.api.battle.controller;

import com.gatcha.api.auth.service.AuthService;
import com.gatcha.api.battle.dto.RoyalRumbleResult;
import com.gatcha.api.battle.service.RoyalRumbleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Royal Rumble Controller
 */
@RestController
@RequestMapping("/api/royal-rumble")
public class RoyalRumbleController {

    private final RoyalRumbleService royalRumbleService;
    private final AuthService authService;

    public RoyalRumbleController(RoyalRumbleService royalRumbleService, AuthService authService) {
        this.royalRumbleService = royalRumbleService;
        this.authService = authService;
    }

    /**
     * Start a Royal Rumble battle
     * 
     * @param token Authentication token
     * @return Royal Rumble result
     */
    @PostMapping
    public ResponseEntity<RoyalRumbleResult> startRoyalRumble(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(royalRumbleService.startRoyalRumble(username));
    }

    /**
     * Get experience gained from the most recent Royal Rumble
     * 
     * @param token    Authentication token
     * @param rumbleId Royal Rumble ID
     * @return Experience gained
     */
    @GetMapping("/experience/{rumbleId}")
    public ResponseEntity<Integer> getExperienceGained(
            @RequestHeader("Authorization") String token,
            @PathVariable String rumbleId) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(royalRumbleService.getExperienceGained(rumbleId));
    }
}