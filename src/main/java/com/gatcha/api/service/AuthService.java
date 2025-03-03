package com.gatcha.api.service;

import com.gatcha.api.dto.AuthRequest;
import com.gatcha.api.dto.AuthResponse;
import com.gatcha.api.model.AuthToken;
import com.gatcha.api.model.Player;
import com.gatcha.api.model.User;
import com.gatcha.api.repository.AuthTokenRepository;
import com.gatcha.api.repository.PlayerRepository;
import com.gatcha.api.repository.UserRepository;
import com.gatcha.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadCredentialsException("Username already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setCreatedAt(System.currentTimeMillis());
        userRepository.save(user);

        // Create player profile
        Player player = new Player();
        player.setUserId(user.getId());
        player.setUsername(user.getUsername());
        player.setLevel(1);
        player.setExperience(0.0);
        player.setExperienceToNextLevel(50.0);
        player.setMaxMonsters(10);
        player.setCreatedAt(System.currentTimeMillis());
        player.setUpdatedAt(System.currentTimeMillis());
        playerRepository.save(player);

        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), jwtService.getExpirationTime());
    }

    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), jwtService.getExpirationTime());
    }

    public String validateTokenAndGetUsername(String token) {
        if (jwtService.validateToken(token)) {
            AuthToken authToken = authTokenRepository.findByToken(token)
                    .orElseThrow(() -> new BadCredentialsException("Invalid token"));

            if (authToken.isExpired()) {
                throw new BadCredentialsException("Token has expired");
            }

            // Update token expiration
            Date newExpirationDate = new Date(System.currentTimeMillis() + jwtService.getExpirationInMillis());
            authToken.setExpirationDate(newExpirationDate);
            authTokenRepository.save(authToken);

            return authToken.getUsername();
        }
        throw new BadCredentialsException("Invalid token");
    }
}