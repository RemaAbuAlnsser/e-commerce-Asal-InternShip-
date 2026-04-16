package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.CartItemRequest;
import com.asal.ecommerce.dto.CartItemResponse;
import com.asal.ecommerce.model.CartItem;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.CartItemRepository;
import com.asal.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartDbService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CartItemResponse> getCart(String email) {
        User user = getUser(email);
        return cartItemRepository.findAllByUser(user)
                .stream().map(CartItemResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CartItemResponse addOrUpdate(String email, CartItemRequest req) {
        User user = getUser(email);
        Optional<CartItem> existing = cartItemRepository
                .findByUserAndProductIdAndColorId(user, req.getProductId(), req.getColorId());

        CartItem item;
        if (existing.isPresent()) {
            item = existing.get();
            int newQty = Math.min(item.getQuantity() + req.getQuantity(), item.getMaxStock());
            item.setQuantity(newQty);
        } else {
            item = new CartItem();
            item.setUser(user);
            item.setProductId(req.getProductId());
            item.setProductName(req.getProductName());
            item.setProductImage(req.getProductImage());
            item.setCategoryName(req.getCategoryName());
            item.setPrice(req.getPrice());
            item.setOldPrice(req.getOldPrice());
            item.setColorId(req.getColorId());
            item.setColorName(req.getColorName());
            item.setColorHex(req.getColorHex());
            item.setQuantity(req.getQuantity() > 0 ? req.getQuantity() : 1);
            item.setMaxStock(req.getMaxStock());
        }
        return CartItemResponse.from(cartItemRepository.save(item));
    }

    @Transactional
    public CartItemResponse updateQuantity(String email, Long itemId, int qty) {
        User user = getUser(email);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        item.setQuantity(Math.max(1, Math.min(qty, item.getMaxStock())));
        return CartItemResponse.from(cartItemRepository.save(item));
    }

    @Transactional
    public void removeItem(String email, Long itemId) {
        User user = getUser(email);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(String email) {
        User user = getUser(email);
        cartItemRepository.deleteAllByUser(user);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
