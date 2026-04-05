package com.asal.ecommerce.config;

import com.asal.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default admin user on application startup
        userService.createDefaultAdmin();
        
        // Fix admin password if needed
        userService.fixAdminPassword();
    }
}
