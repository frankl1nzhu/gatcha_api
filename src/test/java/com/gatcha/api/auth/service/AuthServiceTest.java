package com.gatcha.api.auth.service;

import com.gatcha.api.auth.model.AuthToken;
import com.gatcha.api.auth.model.User;
import com.gatcha.api.auth.repository.AuthTokenRepository;
import com.gatcha.api.auth.repository.UserRepository;
import com.gatcha.api.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private AuthToken testToken;

    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setPassword("password");

        testToken = new AuthToken();
        testToken.setId("1");
        testToken.setUsername("testuser");
        testToken.setToken("test-token");
        testToken.updateExpiration(3600000); // Expires in 1 hour

        // Set token expiration time
        ReflectionTestUtils.setField(authService, "tokenExpirationTime", 3600000L);
    }

    @Test
    void loginSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authTokenRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String token = authService.login("testuser", "password");

        // Assert
        assertNotNull(token);
        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    void loginWithExistingToken() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authTokenRepository.findByUsername("testuser")).thenReturn(Optional.of(testToken));
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String token = authService.login("testuser", "password");

        // Assert
        assertNotNull(token);
        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    void loginFailUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login("nonexistent", "password"));
        verify(authTokenRepository, never()).save(any(AuthToken.class));
    }

    @Test
    void loginFailWrongPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login("testuser", "wrongpassword"));
        verify(authTokenRepository, never()).save(any(AuthToken.class));
    }

    @Test
    void validateTokenSuccess() {
        // Arrange
        when(authTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String username = authService.validateToken("test-token");

        // Assert
        assertEquals("testuser", username);
        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    void validateTokenFailTokenNotFound() {
        // Arrange
        when(authTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.validateToken("nonexistent-token"));
        verify(authTokenRepository, never()).save(any(AuthToken.class));
    }

    @Test
    void validateTokenFailTokenExpired() {
        // Arrange
        AuthToken expiredToken = new AuthToken();
        expiredToken.setId("2");
        expiredToken.setUsername("testuser");
        expiredToken.setToken("expired-token");
        expiredToken.updateExpiration(-1000); // Already expired

        when(authTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.validateToken("expired-token"));
        verify(authTokenRepository, never()).save(any(AuthToken.class));
    }
}