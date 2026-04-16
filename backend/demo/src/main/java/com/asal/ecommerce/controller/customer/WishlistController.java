package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.WishlistItemRequest;
import com.asal.ecommerce.dto.WishlistItemResponse;
import com.asal.ecommerce.service.WishlistDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistDbService wishlistDbService;

    @GetMapping
    public ResponseEntity<List<WishlistItemResponse>> getWishlist(Authentication auth) {
        return ResponseEntity.ok(wishlistDbService.getWishlist(auth.getName()));
    }

    @PostMapping("/toggle")
    public ResponseEntity<List<WishlistItemResponse>> toggle(Authentication auth,
                                                              @RequestBody WishlistItemRequest req) {
        return ResponseEntity.ok(wishlistDbService.toggle(auth.getName(), req));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeItem(Authentication auth, @PathVariable Long productId) {
        wishlistDbService.removeItem(auth.getName(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(Authentication auth) {
        wishlistDbService.clearWishlist(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
