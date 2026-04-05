package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.AdminLoginRequest;
import com.asal.ecommerce.dto.AdminLoginResponse;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.UserRepository;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.enums.Provider;
import com.asal.ecommerce.mapper.AuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthMapper authMapper;
    
    public AdminLoginResponse adminLogin(AdminLoginRequest request) {
        try {
            System.out.println("Admin login attempt for email: " + request.getEmail());
            
            // Step 1: Find user by email only
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (!userOpt.isPresent()) {
                System.out.println("No user found with email: " + request.getEmail());
                return authMapper.toAdminLoginResponse(null, false, "Invalid email or password");
            }
            
            User user = userOpt.get();
            System.out.println("User found - Role: " + user.getRole() + ", Provider: " + user.getProvider() + ", Active: " + user.isActive());
            
            // Step 2: Check if user role is ADMIN
            if (user.getRole() != Role.ADMIN) {
                System.out.println("User is not an admin - Role: " + user.getRole());
                return authMapper.toAdminLoginResponse(null, false, "Access denied: Admin privileges required");
            }
            
            // Step 3: Check if provider is LOCAL
            if (user.getProvider() != Provider.LOCAL) {
                System.out.println("User provider is not LOCAL - Provider: " + user.getProvider());
                return authMapper.toAdminLoginResponse(null, false, "Invalid login method for admin account");
            }
            
            // Step 4: Check if account is active
            if (!user.isActive()) {
                System.out.println("Admin account is deactivated");
                return authMapper.toAdminLoginResponse(null, false, "Account is deactivated");
            }
            
            // Step 5: Compare password manually
            if (!request.getPassword().equals(user.getPassword())) {
                System.out.println("Password mismatch for user: " + user.getEmail());
                return authMapper.toAdminLoginResponse(null, false, "Invalid password");
            }
            
            // All checks passed - successful login
            System.out.println("Admin login successful for user: " + user.getEmail());
            return authMapper.toAdminLoginResponse(user, true, "Login successful");
            
        } catch (Exception e) {
            System.err.println("Admin login error: " + e.getMessage());
            e.printStackTrace();
            return authMapper.toAdminLoginResponse(null, false, "Login failed: " + e.getMessage());
        }
    }
    
    public User createOrUpdateGoogleUser(String email, String name, String googleId) {
        Optional<User> existingUser = userRepository.findByGoogleIdAndProvider(googleId, Provider.GOOGLE);
        
        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            authMapper.updateGoogleUser(user, email, name);
            return userRepository.save(user);
        } else {
            // Create new Google user
            User newUser = authMapper.createGoogleUser(email, name, googleId);
            return userRepository.save(newUser);
        }
    }
    
    public void createDefaultAdmin() {
        try {
            // Create default admin if none exists
            if (userRepository.countByRole(Role.ADMIN) == 0) {
                User defaultAdmin = authMapper.createDefaultAdmin();
                User savedAdmin = userRepository.save(defaultAdmin);
                System.out.println("Default admin created successfully with ID: " + savedAdmin.getId());
            } else {
                System.out.println("Admin user already exists, skipping creation");
            }
        } catch (Exception e) {
            System.err.println("Error creating default admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean isAdmin(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && user.get().getRole() == Role.ADMIN;
    }
}
