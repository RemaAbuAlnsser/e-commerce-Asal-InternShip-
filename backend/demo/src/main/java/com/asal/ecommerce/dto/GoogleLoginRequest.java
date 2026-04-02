package com.asal.ecommerce.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class GoogleLoginRequest {
    
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Google ID is required")
    private String googleId;
}
