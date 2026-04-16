package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.CartItem;
import com.asal.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByUser(User user);
    Optional<CartItem> findByUserAndProductIdAndColorId(User user, Long productId, Integer colorId);
    void deleteAllByUser(User user);
}
