package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.AdminLoginRequest;
import com.asal.ecommerce.dto.AdminLoginResponse;
import com.asal.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        System.out.println("Received admin login request for: " + request.getEmail());
        
        AdminLoginResponse response = userService.adminLogin(request);
        
        System.out.println("Login response - Success: " + response.isSuccess() + ", Message: " + response.getMessage());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
