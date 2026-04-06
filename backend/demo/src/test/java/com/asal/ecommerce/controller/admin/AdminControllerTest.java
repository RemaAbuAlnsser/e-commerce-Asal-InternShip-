package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.AdminLoginRequest;
import com.asal.ecommerce.dto.AdminLoginResponse;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController")
class AdminControllerTest {

    @Mock UserService userService;

    @InjectMocks
    AdminController adminController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AdminLoginRequest validLoginRequest() {
        AdminLoginRequest req = new AdminLoginRequest();
        req.setEmail("admin@example.com");
        req.setPassword("password123");
        return req;
    }

    // =========================================================================
    // POST /api/admin/login
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/login")
    class Login {

        @Test
        @DisplayName("returns 200 with token on successful login")
        void login_success_returns200() throws Exception {
            AdminLoginResponse.AdminData adminData = new AdminLoginResponse.AdminData(1L, "admin@example.com", "Admin User");
            AdminLoginResponse successResponse = new AdminLoginResponse(
                    true, 
                    "Login successful", 
                    "jwt-token-here",
                    86400L,
                    adminData
            );
            given(userService.adminLogin(any())).willReturn(successResponse);

            mockMvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.token").value("jwt-token-here"));
        }

        @Test
        @DisplayName("returns 400 on invalid credentials")
        void login_invalidCredentials_returns400() throws Exception {
            AdminLoginResponse failureResponse = new AdminLoginResponse(
                    false, 
                    "Invalid email or password", 
                    null
            );
            given(userService.adminLogin(any())).willReturn(failureResponse);

            mockMvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"))
                    .andExpect(jsonPath("$.token").isEmpty());
        }

        @Test
        @DisplayName("returns 400 when email is blank")
        void login_blankEmail_returns400() throws Exception {
            AdminLoginRequest req = validLoginRequest();
            req.setEmail("");

            mockMvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when password is blank")
        void login_blankPassword_returns400() throws Exception {
            AdminLoginRequest req = validLoginRequest();
            req.setPassword("");

            mockMvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when email format is invalid")
        void login_invalidEmailFormat_returns400() throws Exception {
            AdminLoginRequest req = validLoginRequest();
            req.setEmail("invalid-email");

            mockMvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when request body is missing")
        void login_missingBody_returns400() throws Exception {
            mockMvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }


        @Test
        @DisplayName("handles service exceptions gracefully")
        void login_serviceException_returns400() throws Exception {
            AdminLoginResponse errorResponse = new AdminLoginResponse(
                    false, 
                    "Database connection error", 
                    null
            );
            given(userService.adminLogin(any())).willReturn(errorResponse);

            mockMvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Database connection error"));
        }
    }
}
