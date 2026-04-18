package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.CustomerSummaryResponse;
import com.asal.ecommerce.dto.SubscriberSummaryResponse;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.repository.OrderRepository;
import com.asal.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    @Autowired private UserRepository  userRepository;
    @Autowired private OrderRepository orderRepository;

    public List<SubscriberSummaryResponse> getAllSubscribers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.SUBSCRIBER)
                .map(u -> SubscriberSummaryResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .verified(u.isEmailVerified())
                        .active(u.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CustomerSummaryResponse> getAllCustomers() {
        // Build a map: userId → [totalOrders, totalSpent]
        Map<Long, Object[]> statsMap = new HashMap<>();
        for (Object[] row : orderRepository.getOrderStatsByUser()) {
            Long   userId     = ((Number) row[0]).longValue();
            long   orderCount = ((Number) row[1]).longValue();
            BigDecimal spent  = (BigDecimal) row[2];
            statsMap.put(userId, new Object[]{orderCount, spent});
        }

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.USER)
                .map(u -> {
                    Object[] stats = statsMap.get(u.getId());
                    long       orders = stats != null ? (long)       stats[0] : 0L;
                    BigDecimal spent  = stats != null ? (BigDecimal) stats[1] : BigDecimal.ZERO;

                    return CustomerSummaryResponse.builder()
                            .id(u.getId())
                            .name(u.getName())
                            .email(u.getEmail())
                            .provider(u.getProvider().name())
                            .active(u.isActive())
                            .totalOrders(orders)
                            .totalSpent(spent)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
