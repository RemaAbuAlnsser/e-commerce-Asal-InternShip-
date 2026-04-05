package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.GoogleLoginRequest;
import com.asal.ecommerce.dto.GoogleLoginResponse;
import com.asal.ecommerce.service.UserService;
import com.asal.ecommerce.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/google-login")
    public ResponseEntity<GoogleLoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            User user = userService.createOrUpdateGoogleUser(
                request.getEmail(),
                request.getName(),
                request.getGoogleId()
            );
            
            GoogleLoginResponse.UserData userData = new GoogleLoginResponse.UserData(
                user.getId(),
                user.getEmail(),
                user.getName()
            );
            
            GoogleLoginResponse response = new GoogleLoginResponse(
                true,
                "Google login successful",
                userData
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GoogleLoginResponse response = new GoogleLoginResponse(
                false,
                "Google login failed: " + e.getMessage(),
                null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}
