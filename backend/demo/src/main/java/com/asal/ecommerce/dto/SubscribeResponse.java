package com.asal.ecommerce.dto;

import lombok.Data;

@Data
public class SubscribeResponse {
    private boolean success;
    private String message;
    private String name;
    private String email;

    // Constructor for subscribe (no user data)
    public SubscribeResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Constructor for verify (includes user data)
    public SubscribeResponse(boolean success, String message, String name, String email) {
        this.success = success;
        this.message = message;
        this.name = name;
        this.email = email;
    }
}
