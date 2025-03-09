package com.gatcha.api.auth.service;

public interface AuthService {
    String login(String username, String password);

    String validateToken(String token);
}