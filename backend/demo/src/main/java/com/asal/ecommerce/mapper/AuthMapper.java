package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.AdminLoginResponse;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.enums.Provider;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    
    public AdminLoginResponse toAdminLoginResponse(User user, boolean success, String message) {
        if (!success || user == null) {
            return new AdminLoginResponse(false, message, null);
        }
        
        AdminLoginResponse.AdminData adminData = new AdminLoginResponse.AdminData(
            user.getId(),
            user.getEmail(),
            user.getName()
        );
        
        return new AdminLoginResponse(true, message, adminData);
    }
    
    public User createGoogleUser(String email, String name, String googleId) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setGoogleId(googleId);
        user.setRole(Role.USER);
        user.setProvider(Provider.GOOGLE);
        user.setActive(true);
        return user;
    }
    
    public void updateGoogleUser(User user, String email, String name) {
        user.setEmail(email);
        user.setName(name);
    }
    
    public User createDefaultAdmin() {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword("admin123");
        admin.setName("Default Admin");
        admin.setRole(Role.ADMIN);
        admin.setProvider(Provider.LOCAL);
        admin.setActive(true);
        return admin;
    }
}
