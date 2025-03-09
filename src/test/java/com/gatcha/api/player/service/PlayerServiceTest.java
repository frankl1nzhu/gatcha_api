package com.gatcha.api.player.service;

import com.gatcha.api.auth.model.User;
import com.gatcha.api.auth.repository.UserRepository;
import com.gatcha.api.player.service.impl.PlayerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId("user1");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setLevel(1);
        testUser.setExperience(0);
        testUser.setMaxExperience(100);
        testUser.setMonsters(new ArrayList<>(Arrays.asList("monster1", "monster2")));
    }

    @Test
    void getProfile() {
        // Prepare
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Execute
        User result = playerService.getProfile("testuser");

        // Verify
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getProfileNotFound() {
        // Prepare
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> playerService.getProfile("nonexistent"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void getMonsters() {
        // Prepare
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Execute
        List<String> result = playerService.getMonsters("testuser");

        // Verify
        assertEquals(2, result.size());
        assertTrue(result.contains("monster1"));
        assertTrue(result.contains("monster2"));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getLevel() {
        // Prepare
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Execute
        int result = playerService.getLevel("testuser");

        // Verify
        assertEquals(1, result);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void addExperience() {
        // Prepare
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        User result = playerService.addExperience("testuser", 50);

        // Verify
        assertEquals(50, result.getExperience());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void levelUp() {
        // Prepare
        testUser.setExperience(100); // Enough to level up
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = (User) invocation.getArgument(0);
            savedUser.setLevel(2);
            savedUser.setExperience(0);
            savedUser.setMaxExperience(110); // 10% increase
            return savedUser;
        });

        // Execute
        User result = playerService.levelUp("testuser");

        // Verify
        assertEquals(2, result.getLevel());
        assertEquals(0, result.getExperience());
        assertEquals(110, result.getMaxExperience());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void levelUpNotEnoughExperience() {
        // Prepare
        testUser.setExperience(50); // Not enough to level up
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> playerService.levelUp("testuser"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addMonster() {
        // Prepare
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        boolean result = playerService.addMonster("testuser", "monster3");

        // Verify
        assertTrue(result);
        assertEquals(3, testUser.getMonsters().size());
        assertTrue(testUser.getMonsters().contains("monster3"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void addMonsterTooMany() {
        // Prepare
        // Mock User.canAddMonster() method to return false
        User spyUser = spy(testUser);
        when(spyUser.canAddMonster()).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(spyUser));

        // Execute
        boolean result = playerService.addMonster("testuser", "monster3");

        // Verify
        assertFalse(result);
        assertEquals(2, spyUser.getMonsters().size());
        assertFalse(spyUser.getMonsters().contains("monster3"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeMonster() {
        // Prepare
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        boolean result = playerService.removeMonster("testuser", "monster1");

        // Verify
        assertTrue(result);
        assertEquals(1, testUser.getMonsters().size());
        assertFalse(testUser.getMonsters().contains("monster1"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void removeMonsterNotFound() {
        // Prepare
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Execute
        boolean result = playerService.removeMonster("testuser", "nonexistent");

        // Verify
        assertFalse(result);
        assertEquals(2, testUser.getMonsters().size());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }
}