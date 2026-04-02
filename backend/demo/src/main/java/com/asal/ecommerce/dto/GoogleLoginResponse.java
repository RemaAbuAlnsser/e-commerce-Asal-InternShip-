package com.asal.ecommerce.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginResponse {
    private boolean success;
    private String message;
    private UserData user;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserData {
        private Long id;
        private String email;
        private String name;
    }
}
