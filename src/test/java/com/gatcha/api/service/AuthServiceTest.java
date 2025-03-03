package com.gatcha.api.service;

import com.gatcha.api.dto.AuthRequest;
import com.gatcha.api.dto.AuthResponse;
import com.gatcha.api.model.User;
import com.gatcha.api.repository.AuthTokenRepository;
import com.gatcha.api.repository.PlayerRepository;
import com.gatcha.api.repository.UserRepository;
import com.gatcha.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setActive(true);
    }

    @Test
    void register_WhenUsernameNotExists_ShouldRegisterSuccessfully() {
        when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(authRequest.getUsername())).thenReturn("token");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        AuthResponse response = authService.register(authRequest);

        assertNotNull(response);
        assertEquals("token", response.getToken());
        assertEquals(authRequest.getUsername(), response.getUsername());
        verify(userRepository).save(any(User.class));
        verify(playerRepository).save(any());
    }

    @Test
    void register_WhenUsernameExists_ShouldThrowException() {
        when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> authService.register(authRequest));
        verify(userRepository, never()).save(any());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void authenticate_WhenValidCredentials_ShouldAuthenticateSuccessfully() {
        when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(authRequest.getUsername())).thenReturn("token");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        AuthResponse response = authService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("token", response.getToken());
        assertEquals(authRequest.getUsername(), response.getUsername());
    }

    @Test
    void authenticate_WhenInvalidPassword_ShouldThrowException() {
        when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(authRequest));
    }

    @Test
    void authenticate_WhenUserDisabled_ShouldThrowException() {
        user.setActive(false);
        when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(authRequest));
    }
}