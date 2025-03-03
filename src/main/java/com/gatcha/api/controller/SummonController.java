package com.gatcha.api.controller;

import com.gatcha.api.dto.SummonResponse;
import com.gatcha.api.service.SummonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/summons")
@RequiredArgsConstructor
public class SummonController {
    private final SummonService summonService;

    @PostMapping
    public ResponseEntity<SummonResponse> summonMonster(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(summonService.summonMonster(username));
    }

    @GetMapping
    public ResponseEntity<List<SummonResponse>> getPlayerSummons(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(summonService.getPlayerSummons(username));
    }
}