package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.OrderItemRequest;
import com.asal.ecommerce.dto.OrderRequest;
import com.asal.ecommerce.dto.OrderResponse;
import com.asal.ecommerce.mapper.OrderMapper;
import com.asal.ecommerce.model.Order;
import com.asal.ecommerce.model.OrderItem;
import com.asal.ecommerce.model.Product;
import com.asal.ecommerce.model.ProductColor;
import com.asal.ecommerce.model.User;
import com.asal.ecommerce.repository.OrderRepository;
import com.asal.ecommerce.repository.ProductColorRepository;
import com.asal.ecommerce.repository.ProductRepository;
import com.asal.ecommerce.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User user = null;

        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));
        }

        Order order = orderMapper.toEntity(request, user);

        // Build order items with product/color snapshots
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Product not found with id: " + itemReq.getProductId()));

                ProductColor color = null;
                String colorName = null;
                if (itemReq.getProductColorId() != null) {
                    color = productColorRepository.findById(itemReq.getProductColorId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "ProductColor not found with id: " + itemReq.getProductColorId()));
                    if (!color.getProduct().getId().equals(product.getId())) {
                        throw new IllegalArgumentException(
                                "Color " + color.getId() + " does not belong to product " + product.getId());
                    }
                    colorName = color.getColorName();
                }

                BigDecimal price = product.getPrice();
                int qty = itemReq.getQuantity();
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));

                OrderItem item = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .productColor(color)
                        .productName(product.getName())
                        .productPrice(price)
                        .colorName(colorName)
                        .quantity(qty)
                        .subtotal(subtotal)
                        .build();

                order.getItems().add(item);
            }
        }

        BigDecimal computedSubtotal = order.getItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(computedSubtotal);
        order.setTotal(computedSubtotal.add(order.getShippingCost()));

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));
        }

        orderMapper.updateEntity(existingOrder, request, user);
        Order updatedOrder = orderRepository.save(existingOrder);
        return toResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        orderRepository.delete(order);
    }

    // Maps order → response including items
    private OrderResponse toResponse(Order order) {
        OrderResponse response = orderMapper.toResponse(order);
        response.setItems(
                order.getItems().stream()
                        .map(orderMapper::toItemResponse)
                        .toList()
        );
        return response;
    }
}
