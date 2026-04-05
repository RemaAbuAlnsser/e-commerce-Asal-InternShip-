package com.asal.ecommerce.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminLoginResponse {
    private boolean success;
    private String message;
    private String token;
    private Long expiresIn;
    private AdminData admin;
    
    // Constructor for error responses (backward compatibility)
    public AdminLoginResponse(boolean success, String message, AdminData admin) {
        this.success = success;
        this.message = message;
        this.admin = admin;
        this.token = null;
        this.expiresIn = null;
    }
    
    // Constructor for successful login with JWT
    public AdminLoginResponse(boolean success, String message, String token, Long expiresIn, AdminData admin) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.expiresIn = expiresIn;
        this.admin = admin;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminData {
        private Long id;
        private String email;
        private String name;
    }
}
