package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.User;
import com.asal.ecommerce.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findAllByUser(User user);
    Optional<WishlistItem> findByUserAndProductId(User user, Long productId);
    void deleteAllByUser(User user);
}
