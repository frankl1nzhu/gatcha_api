package com.gatcha.api.auth.service.impl;

import com.gatcha.api.auth.model.AuthToken;
import com.gatcha.api.auth.model.User;
import com.gatcha.api.auth.repository.AuthTokenRepository;
import com.gatcha.api.auth.repository.UserRepository;
import com.gatcha.api.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private long tokenExpirationTime;

    public AuthServiceImpl(UserRepository userRepository, AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Simplified version, not using password encryption
        if (!password.equals(user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate token: username-date(YYYY/MM/DD)-time(HH:mm:ss)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        String tokenData = username + "-" + dateFormat.format(new Date());
        String token = Base64.getEncoder().encodeToString(tokenData.getBytes());

        // Save or update token
        Optional<AuthToken> existingToken = authTokenRepository.findByUsername(username);
        AuthToken authToken;

        if (existingToken.isPresent()) {
            authToken = existingToken.get();
            authToken.setToken(token);
            authToken.updateExpiration(tokenExpirationTime);
        } else {
            authToken = new AuthToken();
            authToken.setUsername(username);
            authToken.setToken(token);
            authToken.updateExpiration(tokenExpirationTime);
        }

        authTokenRepository.save(authToken);

        return token;
    }

    @Override
    public String validateToken(String token) {
        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));

        if (authToken.isExpired()) {
            throw new BadCredentialsException("Token expired");
        }

        // Update token expiration time
        authToken.updateExpiration(tokenExpirationTime);
        authTokenRepository.save(authToken);

        return authToken.getUsername();
    }
}