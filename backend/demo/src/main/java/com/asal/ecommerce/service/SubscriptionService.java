package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.SubscribeResponse;
import com.asal.ecommerce.enums.Provider;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.UserRepository;
import com.asal.ecommerce.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {

    @Autowired private UserRepository userRepository;
    @Autowired private EmailService    emailService;
    @Autowired private JwtUtil         jwtUtil;

    @Transactional
    public SubscribeResponse subscribe(String email) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getRole() == Role.SUBSCRIBER && user.isEmailVerified()) {
                return SubscribeResponse.simple(false, "This email is already subscribed.");
            }
            if (user.getRole() == Role.SUBSCRIBER && !user.isEmailVerified()) {
                String token = UUID.randomUUID().toString();
                user.setVerificationToken(token);
                userRepository.save(user);
                emailService.sendVerificationEmail(email, token);
                return SubscribeResponse.simple(true, "Verification email resent. Please check your inbox.");
            }
            return SubscribeResponse.simple(false, "This email is already registered with an account.");
        }

        String token = UUID.randomUUID().toString();
        User subscriber = new User();
        subscriber.setEmail(email);
        subscriber.setName(email.split("@")[0]);
        subscriber.setPassword(null);
        subscriber.setRole(Role.SUBSCRIBER);
        subscriber.setProvider(Provider.LOCAL);
        subscriber.setActive(true);
        subscriber.setEmailVerified(false);
        subscriber.setVerificationToken(token);
        userRepository.save(subscriber);

        emailService.sendVerificationEmail(email, token);
        return SubscribeResponse.simple(true, "Almost there! Please check your inbox and verify your email.");
    }

    public SubscribeResponse verify(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return SubscribeResponse.simple(false, "Invalid or expired verification link.");
        }

        User user = userOpt.get();
        if (user.isEmailVerified()) {
            String jwt = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
            return SubscribeResponse.withUser(true, "Your email is already verified.",
                    user.getName(), user.getEmail(), jwt);
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        String jwt = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return SubscribeResponse.withUser(true, "Email verified! You're now subscribed. 🎉",
                user.getName(), user.getEmail(), jwt);
    }

    public SubscribeResponse requestLogin(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.SUBSCRIBER || !userOpt.get().isEmailVerified()) {
            return SubscribeResponse.simple(true, "If that email is registered, you'll receive a sign-in link shortly.");
        }

        User user = userOpt.get();
        String loginToken = UUID.randomUUID().toString();
        user.setLoginToken(loginToken);
        userRepository.save(user);
        emailService.sendLoginEmail(email, user.getName(), loginToken);

        return SubscribeResponse.simple(true, "If that email is registered, you'll receive a sign-in link shortly.");
    }

    public SubscribeResponse verifyLogin(String token) {
        Optional<User> userOpt = userRepository.findByLoginToken(token);

        if (userOpt.isEmpty()) {
            return SubscribeResponse.simple(false, "Invalid or expired sign-in link.");
        }

        User user = userOpt.get();
        user.setLoginToken(null);
        userRepository.save(user);

        String jwt = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return SubscribeResponse.withUser(true, "Welcome back!", user.getName(), user.getEmail(), jwt);
    }
}
