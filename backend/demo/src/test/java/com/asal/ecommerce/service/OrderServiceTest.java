package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.OrderItemRequest;
import com.asal.ecommerce.dto.OrderItemResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductColorRepository productColorRepository;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order stubOrder;
    private Product product;
    private ProductColor color;
    private OrderRequest request;
    private OrderItemRequest itemRequest;
    private OrderResponse orderResponse;
    private OrderItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Sneakers");
        product.setPrice(new BigDecimal("100.00"));

        color = new ProductColor();
        color.setId(1L);
        color.setProduct(product);
        color.setColorName("Red");

        stubOrder = new Order();
        stubOrder.setId(1L);
        stubOrder.setItems(new ArrayList<>());
        stubOrder.setShippingCost(new BigDecimal("15.00"));
        stubOrder.setStatus("pending");
        stubOrder.setCustomerName("Alice");
        stubOrder.setCustomerPhone("0123456789");
        stubOrder.setCustomerCity("Algiers");
        stubOrder.setCustomerAddress("123 Main St");
        stubOrder.setShippingMethod("express");
        stubOrder.setPaymentMethod("cash");
        stubOrder.setCreatedAt(LocalDateTime.now());
        stubOrder.setUpdatedAt(LocalDateTime.now());

        itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setProductColorId(1L);
        itemRequest.setQuantity(2);

        request = new OrderRequest();
        request.setCustomerName("Alice");
        request.setCustomerPhone("0123456789");
        request.setCustomerCity("Algiers");
        request.setCustomerAddress("123 Main St");
        request.setShippingMethod("express");
        request.setShippingCost(new BigDecimal("15.00"));
        request.setPaymentMethod("cash");
        request.setItems(new ArrayList<>(List.of(itemRequest)));

        itemResponse = new OrderItemResponse();
        itemResponse.setId(1L);
        itemResponse.setProductId(1L);
        itemResponse.setProductColorId(1L);
        itemResponse.setProductName("Sneakers");
        itemResponse.setProductPrice(new BigDecimal("100.00"));
        itemResponse.setColorName("Red");
        itemResponse.setQuantity(2);
        itemResponse.setSubtotal(new BigDecimal("200.00"));

        orderResponse = new OrderResponse();
        orderResponse.setId(1L);
        orderResponse.setCustomerName("Alice");
        orderResponse.setStatus("pending");
        orderResponse.setItems(new ArrayList<>());
    }

    // =========================================================================
    // createOrder
    // =========================================================================

    @Test
    void createOrder_guestCheckout_success() {
        // Given
        request.setUserId(null);
        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productColorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);
        when(orderMapper.toItemResponse(any(OrderItem.class))).thenReturn(itemResponse);

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertNotNull(result);
        verify(userRepository, never()).findById(any());
        verify(orderRepository).save(stubOrder);
    }

    @Test
    void createOrder_withUser_loadsAndAttachesUser() {
        // Given
        User user = new User();
        user.setId(10L);
        request.setUserId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(orderMapper.toEntity(request, user)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productColorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);
        when(orderMapper.toItemResponse(any(OrderItem.class))).thenReturn(itemResponse);

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(10L);
    }

    @Test
    void createOrder_userNotFound_throwsEntityNotFoundException() {
        // Given
        request.setUserId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.createOrder(request));
        assertEquals("User not found with id: 99", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_productNotFound_throwsEntityNotFoundException() {
        // Given
        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.createOrder(request));
        assertEquals("Product not found with id: 1", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_colorNotFound_throwsEntityNotFoundException() {
        // Given
        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productColorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.createOrder(request));
        assertEquals("ProductColor not found with id: 1", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_colorDoesNotBelongToProduct_throwsIllegalArgumentException() {
        // Given – color belongs to a different product
        Product otherProduct = new Product();
        otherProduct.setId(99L);
        color.setProduct(otherProduct);

        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productColorRepository.findById(1L)).thenReturn(Optional.of(color));

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("does not belong to product"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_subtotalAndTotalComputedFromItems() {
        // Given – price 100.00 × qty 2 = subtotal 200.00, shipping 15.00, total 215.00
        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productColorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);
        when(orderMapper.toItemResponse(any(OrderItem.class))).thenReturn(itemResponse);

        // When
        orderService.createOrder(request);

        // Then – values must be set on the entity before save
        assertEquals(new BigDecimal("200.00"), stubOrder.getSubtotal());
        assertEquals(new BigDecimal("215.00"), stubOrder.getTotal());
    }

    @Test
    void createOrder_withoutColor_snapshotsNullColorName() {
        // Given
        itemRequest.setProductColorId(null);

        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);
        when(orderMapper.toItemResponse(any(OrderItem.class))).thenReturn(itemResponse);

        // When
        orderService.createOrder(request);

        // Then
        assertEquals(1, stubOrder.getItems().size());
        assertNull(stubOrder.getItems().get(0).getColorName());
        verify(productColorRepository, never()).findById(any());
    }

    @Test
    void createOrder_snapshotsProductNamePriceAndColorName() {
        // Given
        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productColorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);
        when(orderMapper.toItemResponse(any(OrderItem.class))).thenReturn(itemResponse);

        // When
        orderService.createOrder(request);
        OrderItem saved = stubOrder.getItems().get(0);

        // Then
        assertEquals("Sneakers", saved.getProductName());
        assertEquals(new BigDecimal("100.00"), saved.getProductPrice());
        assertEquals("Red", saved.getColorName());
        assertEquals(2, saved.getQuantity());
        assertEquals(new BigDecimal("200.00"), saved.getSubtotal());
    }

    @Test
    void createOrder_itemHasOrderReference() {
        // Given
        when(orderMapper.toEntity(request, null)).thenReturn(stubOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productColorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);
        when(orderMapper.toItemResponse(any(OrderItem.class))).thenReturn(itemResponse);

        // When
        orderService.createOrder(request);

        // Then – item.order must be set (no orphaned items)
        assertSame(stubOrder, stubOrder.getItems().get(0).getOrder());
    }

    // =========================================================================
    // getAllOrders
    // =========================================================================

    @Test
    void getAllOrders_returnsAllOrdersSortedByDate() {
        // Given
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(stubOrder));
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);

        // When
        List<OrderResponse> result = orderService.getAllOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllOrders_emptyRepository_returnsEmptyList() {
        // Given
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        // When
        List<OrderResponse> result = orderService.getAllOrders();

        // Then
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // getOrderById
    // =========================================================================

    @Test
    void getOrderById_found_returnsOrderResponse() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(stubOrder));
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.getOrderById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderById_notFound_throwsEntityNotFoundException() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.getOrderById(99L));
        assertEquals("Order not found with id: 99", ex.getMessage());
    }

    // =========================================================================
    // getOrdersByStatus
    // =========================================================================

    @Test
    void getOrdersByStatus_returnsMatchingOrders() {
        // Given
        when(orderRepository.findByStatusOrderByCreatedAtDesc("pending")).thenReturn(List.of(stubOrder));
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);

        // When
        List<OrderResponse> result = orderService.getOrdersByStatus("pending");

        // Then
        assertEquals(1, result.size());
        verify(orderRepository).findByStatusOrderByCreatedAtDesc("pending");
    }

    @Test
    void getOrdersByStatus_noMatch_returnsEmptyList() {
        // Given
        when(orderRepository.findByStatusOrderByCreatedAtDesc("shipped")).thenReturn(Collections.emptyList());

        // When
        List<OrderResponse> result = orderService.getOrdersByStatus("shipped");

        // Then
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // updateOrder
    // =========================================================================

    @Test
    void updateOrder_success_returnsUpdatedResponse() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(stubOrder));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.updateOrder(1L, request);

        // Then
        assertNotNull(result);
        verify(orderMapper).updateEntity(stubOrder, request, null);
        verify(orderRepository).save(stubOrder);
    }

    @Test
    void updateOrder_notFound_throwsEntityNotFoundException() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrder(99L, request));
        assertEquals("Order not found with id: 99", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    // =========================================================================
    // updateOrderStatus
    // =========================================================================

    @Test
    void updateOrderStatus_success_setsStatusAndSaves() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(stubOrder));
        when(orderRepository.save(stubOrder)).thenReturn(stubOrder);
        when(orderMapper.toResponse(stubOrder)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.updateOrderStatus(1L, "shipped");

        // Then
        assertNotNull(result);
        assertEquals("shipped", stubOrder.getStatus());
        verify(orderRepository).save(stubOrder);
    }

    @Test
    void updateOrderStatus_notFound_throwsEntityNotFoundException() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrderStatus(99L, "shipped"));
        assertEquals("Order not found with id: 99", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    // =========================================================================
    // deleteOrder
    // =========================================================================

    @Test
    void deleteOrder_success_callsRepositoryDelete() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(stubOrder));

        // When
        orderService.deleteOrder(1L);

        // Then
        verify(orderRepository).delete(stubOrder);
    }

    @Test
    void deleteOrder_notFound_throwsEntityNotFoundException() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteOrder(99L));
        assertEquals("Order not found with id: 99", ex.getMessage());
        verify(orderRepository, never()).delete(any());
    }
}
