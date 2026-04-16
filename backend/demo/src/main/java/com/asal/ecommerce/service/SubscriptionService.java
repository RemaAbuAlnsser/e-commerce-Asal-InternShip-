package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.SubscribeResponse;
import com.asal.ecommerce.enums.Provider;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public SubscribeResponse subscribe(String email) {
        // Check if already subscribed and verified
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getRole() == Role.SUBSCRIBER && user.isEmailVerified()) {
                return new SubscribeResponse(false, "This email is already subscribed.");
            }
            if (user.getRole() == Role.SUBSCRIBER && !user.isEmailVerified()) {
                // Resend verification
                String token = UUID.randomUUID().toString();
                user.setVerificationToken(token);
                userRepository.save(user);
                emailService.sendVerificationEmail(email, token);
                return new SubscribeResponse(true, "Verification email resent. Please check your inbox.");
            }
            // Email belongs to a non-subscriber user (ADMIN / USER) — don't overwrite it
            return new SubscribeResponse(false, "This email is already registered with an account.");
        }

        // New subscriber
        String token = UUID.randomUUID().toString();

        User subscriber = new User();
        subscriber.setEmail(email);
        subscriber.setName(email.split("@")[0]); // use email prefix as display name
        subscriber.setPassword(null);
        subscriber.setRole(Role.SUBSCRIBER);
        subscriber.setProvider(Provider.LOCAL);
        subscriber.setActive(true);
        subscriber.setEmailVerified(false);
        subscriber.setVerificationToken(token);

        userRepository.save(subscriber);
        emailService.sendVerificationEmail(email, token);

        return new SubscribeResponse(true, "Almost there! Please check your inbox and verify your email.");
    }

    public SubscribeResponse requestLogin(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Don't reveal whether the email exists
            return new SubscribeResponse(true, "If that email is registered, you'll receive a sign-in link shortly.");
        }

        User user = userOpt.get();
        if (user.getRole() != Role.SUBSCRIBER || !user.isEmailVerified()) {
            return new SubscribeResponse(true, "If that email is registered, you'll receive a sign-in link shortly.");
        }

        String token = UUID.randomUUID().toString();
        user.setLoginToken(token);
        userRepository.save(user);
        emailService.sendLoginEmail(email, user.getName(), token);

        return new SubscribeResponse(true, "If that email is registered, you'll receive a sign-in link shortly.");
    }

    public SubscribeResponse verifyLogin(String token) {
        Optional<User> userOpt = userRepository.findByLoginToken(token);

        if (userOpt.isEmpty()) {
            return new SubscribeResponse(false, "Invalid or expired sign-in link.");
        }

        User user = userOpt.get();
        user.setLoginToken(null); // One-time use
        userRepository.save(user);

        return new SubscribeResponse(true, "Welcome back!", user.getName(), user.getEmail());
    }

    public SubscribeResponse verify(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return new SubscribeResponse(false, "Invalid or expired verification link.");
        }

        User user = userOpt.get();
        if (user.isEmailVerified()) {
            return new SubscribeResponse(true, "Your email is already verified.", user.getName(), user.getEmail());
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return new SubscribeResponse(true, "Email verified! You're now subscribed. 🎉", user.getName(), user.getEmail());
    }
}
