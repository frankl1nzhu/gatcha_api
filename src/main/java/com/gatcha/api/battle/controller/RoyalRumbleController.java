package com.gatcha.api.battle.controller;

import com.gatcha.api.auth.service.AuthService;
import com.gatcha.api.battle.dto.RoyalRumbleResult;
import com.gatcha.api.battle.service.RoyalRumbleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * @param token   Authentication token
     * @param request Request containing monster IDs
     * @return Royal Rumble result
     */
    @PostMapping
    public ResponseEntity<RoyalRumbleResult> startRoyalRumble(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, List<String>> request) {
        try {
            String username = authService.validateToken(token.replace("Bearer ", ""));

            // Get the list of monster IDs from the request
            List<String> monsterIds = request.get("monsterIds");
            if (monsterIds == null || monsterIds.isEmpty() || monsterIds.size() < 3) {
                System.out.println("Invalid monster IDs for royal rumble: " + monsterIds);
                return ResponseEntity.badRequest().build();
            }

            System.out.println("Starting royal rumble for user: " + username + " with monsters: " + monsterIds);

            // Call the service to start the Royal Rumble, passing the username and monster
            // ID list
            RoyalRumbleResult result = royalRumbleService.startRoyalRumble(username, monsterIds);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error starting royal rumble: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    /**
     * Get all Royal Rumble history
     * 
     * @param token Authentication token
     * @return List of Royal Rumble results
     */
    @GetMapping
    public ResponseEntity<List<RoyalRumbleResult>> getAllRumbles(
            @RequestHeader("Authorization") String token) {
        try {
            // Validate token
            String username = authService.validateToken(token.replace("Bearer ", ""));
            if (username == null) {
                System.out.println("Invalid token for /royal-rumble endpoint");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            System.out.println("Getting all royal rumbles for user: " + username);

            // Get all Royal Rumble records
            List<RoyalRumbleResult> rumbles = royalRumbleService.getAllRumbles();

            // Sort by date in descending order
            rumbles.sort((a, b) -> b.getRumbleDate().compareTo(a.getRumbleDate()));

            System.out.println("Found " + rumbles.size() + " royal rumbles");

            return ResponseEntity.ok(rumbles);
        } catch (Exception e) {
            System.out.println("Error getting all royal rumbles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}