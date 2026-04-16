package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.CartItemRequest;
import com.asal.ecommerce.dto.CartItemResponse;
import com.asal.ecommerce.service.CartDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartDbService cartDbService;

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(Authentication auth) {
        return ResponseEntity.ok(cartDbService.getCart(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addItem(Authentication auth, @RequestBody CartItemRequest req) {
        return ResponseEntity.ok(cartDbService.addOrUpdate(auth.getName(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItemResponse> updateQty(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        int qty = body.getOrDefault("quantity", 1);
        return ResponseEntity.ok(cartDbService.updateQuantity(auth.getName(), id, qty));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItem(Authentication auth, @PathVariable Long id) {
        cartDbService.removeItem(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication auth) {
        cartDbService.clearCart(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
