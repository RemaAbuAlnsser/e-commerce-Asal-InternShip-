package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatusOrderByCreatedAtDesc(String status);
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt >= :from")
    BigDecimal sumRevenueSince(@Param("from") LocalDateTime from);

    // Returns [userId, orderCount, totalSpent] for every user that has at least one order
    @Query("SELECT o.user.id, COUNT(o), COALESCE(SUM(o.total), 0) FROM Order o WHERE o.user IS NOT NULL GROUP BY o.user.id")
    List<Object[]> getOrderStatsByUser();
}