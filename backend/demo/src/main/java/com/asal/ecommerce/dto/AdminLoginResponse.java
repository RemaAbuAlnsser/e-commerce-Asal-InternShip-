package com.asal.ecommerce.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginResponse {
    private boolean success;
    private String message;
    private AdminData admin;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminData {
        private Long id;
        private String email;
        private String name;
    }
}
