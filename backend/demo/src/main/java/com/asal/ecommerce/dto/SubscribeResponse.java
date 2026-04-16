package com.asal.ecommerce.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscribeResponse {
    private boolean success;
    private String message;
    private String name;
    private String email;
    private String token;

    /** For simple success/error with no user data (e.g. subscribe request) */
    public static SubscribeResponse simple(boolean success, String message) {
        SubscribeResponse r = new SubscribeResponse();
        r.success = success;
        r.message = message;
        return r;
    }

    /** For login/verify — includes JWT so the client can authenticate */
    public static SubscribeResponse withUser(boolean success, String message,
                                             String name, String email, String token) {
        SubscribeResponse r = new SubscribeResponse();
        r.success = success;
        r.message = message;
        r.name    = name;
        r.email   = email;
        r.token   = token;
        return r;
    }
}
