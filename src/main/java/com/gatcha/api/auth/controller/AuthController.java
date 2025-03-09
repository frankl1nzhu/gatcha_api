package com.gatcha.api.auth.controller;

import com.gatcha.api.auth.dto.LoginRequest;
import com.gatcha.api.auth.dto.TokenResponse;
import com.gatcha.api.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(username);
    }
}