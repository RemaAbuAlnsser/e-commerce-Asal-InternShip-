package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.WishlistItemRequest;
import com.asal.ecommerce.dto.WishlistItemResponse;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.model.WishlistItem;
import com.asal.ecommerce.repository.UserRepository;
import com.asal.ecommerce.repository.WishlistItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishlistDbService {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private UserRepository userRepository;

    public List<WishlistItemResponse> getWishlist(String email) {
        User user = getUser(email);
        return wishlistItemRepository.findAllByUser(user)
                .stream().map(WishlistItemResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public List<WishlistItemResponse> toggle(String email, WishlistItemRequest req) {
        User user = getUser(email);
        Optional<WishlistItem> existing = wishlistItemRepository.findByUserAndProductId(user, req.getProductId());

        if (existing.isPresent()) {
            wishlistItemRepository.delete(existing.get());
        } else {
            WishlistItem item = new WishlistItem();
            item.setUser(user);
            item.setProductId(req.getProductId());
            item.setProductName(req.getProductName());
            item.setProductImage(req.getProductImage());
            item.setCategoryName(req.getCategoryName());
            item.setPrice(req.getPrice());
            item.setOldPrice(req.getOldPrice());
            item.setTotalStock(req.getTotalStock());
            wishlistItemRepository.save(item);
        }
        return getWishlist(email);
    }

    @Transactional
    public void removeItem(String email, Long productId) {
        User user = getUser(email);
        wishlistItemRepository.findByUserAndProductId(user, productId)
                .ifPresent(wishlistItemRepository::delete);
    }

    @Transactional
    public void clearWishlist(String email) {
        User user = getUser(email);
        wishlistItemRepository.deleteAllByUser(user);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
