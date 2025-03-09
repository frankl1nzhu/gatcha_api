package com.gatcha.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatcha.api.auth.controller.AuthController;
import com.gatcha.api.auth.dto.LoginRequest;
import com.gatcha.api.auth.service.AuthService;
import com.gatcha.api.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestConfig.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @WithMockUser
    void loginSuccess() throws Exception {
        // Prepare
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(authService.login("testuser", "password")).thenReturn("test-token");

        // Execute & Verify
        mockMvc.perform(post("/api/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"test-token\"}"));
    }

    @Test
    @WithMockUser
    void validateTokenSuccess() throws Exception {
        // Prepare
        when(authService.validateToken("test-token")).thenReturn("testuser");

        // Execute & Verify
        mockMvc.perform(post("/api/auth/validate")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("testuser"));
    }

    @Test
    @WithMockUser
    void validateTokenFailNoToken() throws Exception {
        // Execute & Verify
        mockMvc.perform(post("/api/auth/validate")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void validateTokenFailInvalidToken() throws Exception {
        // Prepare
        when(authService.validateToken(anyString())).thenThrow(new BadCredentialsException("Invalid token"));

        // Execute & Verify
        mockMvc.perform(post("/api/auth/validate")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}