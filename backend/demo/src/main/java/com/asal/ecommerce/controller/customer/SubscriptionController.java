package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.SubscribeRequest;
import com.asal.ecommerce.dto.SubscribeResponse;
import com.asal.ecommerce.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscribe")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscribeResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        SubscribeResponse response = subscriptionService.subscribe(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<SubscribeResponse> verify(@RequestParam String token) {
        SubscribeResponse response = subscriptionService.verify(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<SubscribeResponse> requestLogin(@RequestBody java.util.Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        SubscribeResponse response = subscriptionService.requestLogin(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login-verify")
    public ResponseEntity<SubscribeResponse> verifyLogin(@RequestParam String token) {
        SubscribeResponse response = subscriptionService.verifyLogin(token);
        return ResponseEntity.ok(response);
    }
}
