package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.GoogleLoginRequest;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer AuthController")
class CustomerAuthControllerTest {

    @Mock UserService userService;

    @InjectMocks
    AuthController authController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    private User sampleUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("user@example.com");
        sampleUser.setName("John Doe");
        sampleUser.setGoogleId("google123");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private GoogleLoginRequest validGoogleLoginRequest() {
        GoogleLoginRequest req = new GoogleLoginRequest();
        req.setEmail("user@example.com");
        req.setName("John Doe");
        req.setGoogleId("google123");
        return req;
    }

    // =========================================================================
    // POST /api/auth/google-login
    // =========================================================================

    @Nested
    @DisplayName("POST /api/auth/google-login")
    class GoogleLogin {

        @Test
        @DisplayName("returns 200 with user data on successful Google login")
        void googleLogin_success_returns200() throws Exception {
            given(userService.createOrUpdateGoogleUser(anyString(), anyString(), anyString()))
                    .willReturn(sampleUser);

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleLoginRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Google login successful"))
                    .andExpect(jsonPath("$.user.id").value(1))
                    .andExpect(jsonPath("$.user.email").value("user@example.com"))
                    .andExpect(jsonPath("$.user.name").value("John Doe"));
        }

        @Test
        @DisplayName("returns 400 when service throws exception")
        void googleLogin_serviceException_returns400() throws Exception {
            given(userService.createOrUpdateGoogleUser(anyString(), anyString(), anyString()))
                    .willThrow(new RuntimeException("Database connection error"));

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleLoginRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Google login failed: Database connection error"))
                    .andExpect(jsonPath("$.user").isEmpty());
        }

        @Test
        @DisplayName("returns 400 when email is blank")
        void googleLogin_blankEmail_returns400() throws Exception {
            GoogleLoginRequest req = validGoogleLoginRequest();
            req.setEmail("");

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void googleLogin_blankName_returns400() throws Exception {
            GoogleLoginRequest req = validGoogleLoginRequest();
            req.setName("");

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when googleId is blank")
        void googleLogin_blankGoogleId_returns400() throws Exception {
            GoogleLoginRequest req = validGoogleLoginRequest();
            req.setGoogleId("");

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when email format is invalid")
        void googleLogin_invalidEmailFormat_returns400() throws Exception {
            GoogleLoginRequest req = validGoogleLoginRequest();
            req.setEmail("invalid-email");

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("creates new user when user doesn't exist")
        void googleLogin_newUser_createsUser() throws Exception {
            User newUser = new User();
            newUser.setId(2L);
            newUser.setEmail("newuser@example.com");
            newUser.setName("New User");
            newUser.setGoogleId("google456");

            GoogleLoginRequest req = new GoogleLoginRequest();
            req.setEmail("newuser@example.com");
            req.setName("New User");
            req.setGoogleId("google456");

            given(userService.createOrUpdateGoogleUser("newuser@example.com", "New User", "google456"))
                    .willReturn(newUser);

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.id").value(2))
                    .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.user.name").value("New User"));
        }

        @Test
        @DisplayName("updates existing user when user already exists")
        void googleLogin_existingUser_updatesUser() throws Exception {
            // Simulate updating an existing user's name
            sampleUser.setName("John Doe Updated");
            
            given(userService.createOrUpdateGoogleUser("user@example.com", "John Doe Updated", "google123"))
                    .willReturn(sampleUser);

            GoogleLoginRequest req = new GoogleLoginRequest();
            req.setEmail("user@example.com");
            req.setName("John Doe Updated");
            req.setGoogleId("google123");

            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.name").value("John Doe Updated"));
        }

        @Test
        @DisplayName("returns 400 when request body is missing")
        void googleLogin_missingBody_returns400() throws Exception {
            mockMvc.perform(post("/api/auth/google-login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

    }
}
