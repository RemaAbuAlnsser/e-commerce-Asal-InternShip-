package com.asal.ecommerce.service;

import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductNotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Sends a new-product email to every verified subscriber.
     * Runs asynchronously — never blocks the HTTP response.
     */
    @Async
    public void notifySubscribers(String productName, String categoryName,
                                  String price, String imageUrl, Long productId) {
        List<User> subscribers = userRepository.findAllByRoleAndEmailVerifiedTrue(Role.SUBSCRIBER);

        if (subscribers.isEmpty()) return;

        System.out.println("Sending new-product notification to " + subscribers.size() + " subscriber(s)...");

        for (User subscriber : subscribers) {
            emailService.sendNewProductNotification(
                    subscriber.getEmail(),
                    productName,
                    categoryName,
                    price,
                    imageUrl,
                    productId
            );
        }

        System.out.println("New-product notifications sent.");
    }
}
