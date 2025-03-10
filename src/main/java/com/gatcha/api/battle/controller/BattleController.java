package com.gatcha.api.battle.controller;

import com.gatcha.api.auth.service.AuthService;
import com.gatcha.api.battle.dto.BattleRequest;
import com.gatcha.api.battle.dto.BattleResponse;
import com.gatcha.api.battle.model.BattleLog;
import com.gatcha.api.battle.service.BattleService;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.service.MonsterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;
    private final AuthService authService;
    private final MonsterService monsterService;

    public BattleController(BattleService battleService, AuthService authService, MonsterService monsterService) {
        this.battleService = battleService;
        this.authService = authService;
        this.monsterService = monsterService;
    }

    @PostMapping
    public ResponseEntity<BattleResponse> battle(
            @RequestHeader("Authorization") String token,
            @RequestBody BattleRequest request) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        BattleLog battleLog = battleService.battle(request.getMonster1Id(), request.getMonster2Id(), username);

        // Get the winning monster information
        PlayerMonster winner = monsterService.getMonsterById(battleLog.getWinnerId(), username);

        // Get the experience gained from this battle
        int experienceGained = battleService.getExperienceGained(battleLog.getId());

        // Create a response containing battle log and winning monster information
        BattleResponse response = new BattleResponse();
        response.setBattleLog(battleLog);
        response.setWinner(winner);
        response.setExperienceGained(experienceGained);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{battleId}")
    public ResponseEntity<BattleLog> getBattle(
            @RequestHeader("Authorization") String token,
            @PathVariable String battleId) {
        // Validate token, but don't use the username
        authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(battleService.getBattleById(battleId));
    }

    @GetMapping("/monster/{monsterId}")
    public ResponseEntity<List<BattleLog>> getBattlesByMonsterId(
            @RequestHeader("Authorization") String token,
            @PathVariable String monsterId) {
        // Validate token, but don't use the username
        authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(battleService.getBattlesByMonsterId(monsterId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<BattleLog>> getAllBattles(
            @RequestHeader("Authorization") String token) {
        try {
            // 验证token
            String username = authService.validateToken(token.replace("Bearer ", ""));
            if (username == null) {
                System.out.println("Invalid token for /battles/history endpoint");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            System.out.println("Getting all battles for user: " + username);

            // 获取所有战斗记录
            List<BattleLog> battles = battleService.getAllBattles();

            // 按日期降序排序
            battles.sort((a, b) -> b.getBattleDate().compareTo(a.getBattleDate()));

            System.out.println("Found " + battles.size() + " battles");

            return ResponseEntity.ok(battles);
        } catch (Exception e) {
            System.out.println("Error getting all battles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}