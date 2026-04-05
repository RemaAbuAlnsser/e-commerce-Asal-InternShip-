package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.AdminLoginRequest;
import com.asal.ecommerce.dto.AdminLoginResponse;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.UserRepository;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.enums.Provider;
import com.asal.ecommerce.mapper.AuthMapper;
import com.asal.ecommerce.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthMapper authMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
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
            System.out.println("User found: " + user.getEmail());
            System.out.println("User role: " + user.getRole());
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
            
            // Step 5: Verify password using BCrypt
            System.out.println("Raw password from request: " + request.getPassword());
            System.out.println("Stored password hash: " + user.getPassword());
            
            // Test what the hash should be for "admin123"
            String testHash = passwordEncoder.encode("admin123");
            System.out.println("New hash for 'admin123': " + testHash);
            System.out.println("Test hash matches stored: " + passwordEncoder.matches("admin123", user.getPassword()));
            
            System.out.println("Password match: " + passwordEncoder.matches(request.getPassword(), user.getPassword()));
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                System.out.println("Password mismatch for user: " + user.getEmail());
                return new AdminLoginResponse(false, "Invalid email or password", null);
            }
            
            // All checks passed - successful login
            System.out.println("Admin login successful for user: " + user.getEmail());
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
            Long expiresIn = jwtUtil.getExpirationTime();
            
            AdminLoginResponse.AdminData adminData = new AdminLoginResponse.AdminData(
                user.getId(),
                user.getEmail(),
                user.getName()
            );
            
            return new AdminLoginResponse(true, "Login successful", token, expiresIn, adminData);
            
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
    
    public void fixAdminPassword() {
        try {
            Optional<User> adminOpt = userRepository.findByEmail("admin@example.com");
            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();
                String correctHash = passwordEncoder.encode("admin123");
                admin.setPassword(correctHash);
                userRepository.save(admin);
                System.out.println("Admin password updated successfully");
                System.out.println("New password hash: " + correctHash);
            } else {
                System.out.println("Admin user not found");
            }
        } catch (Exception e) {
            System.err.println("Error fixing admin password: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
