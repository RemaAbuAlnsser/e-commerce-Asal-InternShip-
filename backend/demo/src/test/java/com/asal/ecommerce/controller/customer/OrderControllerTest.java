package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.OrderItemRequest;
import com.asal.ecommerce.dto.OrderItemResponse;
import com.asal.ecommerce.dto.OrderRequest;
import com.asal.ecommerce.dto.OrderResponse;
import com.asal.ecommerce.dto.OrderStatusUpdateRequest;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController")
class OrderControllerTest {

    @Mock OrderService orderService;
    @InjectMocks OrderController orderController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    private OrderResponse sampleResponse;
    private OrderRequest validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        OrderItemResponse itemResponse = new OrderItemResponse();
        itemResponse.setId(1L);
        itemResponse.setProductId(1L);
        itemResponse.setProductName("Sneakers");
        itemResponse.setProductPrice(new BigDecimal("100.00"));
        itemResponse.setQuantity(2);
        itemResponse.setSubtotal(new BigDecimal("200.00"));

        sampleResponse = new OrderResponse();
        sampleResponse.setId(1L);
        sampleResponse.setCustomerName("Alice");
        sampleResponse.setCustomerPhone("0123456789");
        sampleResponse.setCustomerCity("Algiers");
        sampleResponse.setCustomerAddress("123 Main St");
        sampleResponse.setShippingMethod("express");
        sampleResponse.setShippingCost(new BigDecimal("15.00"));
        sampleResponse.setPaymentMethod("cash");
        sampleResponse.setStatus("pending");
        sampleResponse.setItems(List.of(itemResponse));

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(2);

        validRequest = new OrderRequest();
        validRequest.setCustomerName("Alice");
        validRequest.setCustomerPhone("0123456789");
        validRequest.setCustomerCity("Algiers");
        validRequest.setCustomerAddress("123 Main St");
        validRequest.setShippingMethod("express");
        validRequest.setShippingCost(new BigDecimal("15.00"));
        validRequest.setPaymentMethod("cash");
        validRequest.setItems(new ArrayList<>(List.of(itemReq)));
    }

    // =========================================================================
    // POST /api/orders
    // =========================================================================

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrder {

        @Test
        @DisplayName("returns 201 with order and items on success")
        void createOrder_success_returns201() throws Exception {
            given(orderService.createOrder(any(OrderRequest.class))).willReturn(sampleResponse);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.customerName").value("Alice"))
                    .andExpect(jsonPath("$.data.status").value("pending"))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items[0].productName").value("Sneakers"))
                    .andExpect(jsonPath("$.data.items[0].subtotal").value(200.00));
        }

        @Test
        @DisplayName("returns 400 when customerName is blank")
        void createOrder_blankCustomerName_returns400() throws Exception {
            validRequest.setCustomerName("");

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when shippingMethod is blank")
        void createOrder_blankShippingMethod_returns400() throws Exception {
            validRequest.setShippingMethod("");

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when paymentMethod is blank")
        void createOrder_blankPaymentMethod_returns400() throws Exception {
            validRequest.setPaymentMethod("");

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when items list is empty")
        void createOrder_emptyItems_returns400() throws Exception {
            validRequest.setItems(new ArrayList<>());

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when request body is missing")
        void createOrder_missingBody_returns400() throws Exception {
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void createOrder_userNotFound_returns404() throws Exception {
            given(orderService.createOrder(any())).willThrow(
                    new EntityNotFoundException("User not found with id: 99"));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("User not found with id: 99"));
        }

        @Test
        @DisplayName("returns 404 when product not found")
        void createOrder_productNotFound_returns404() throws Exception {
            given(orderService.createOrder(any())).willThrow(
                    new EntityNotFoundException("Product not found with id: 1"));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("returns 400 when color does not belong to product")
        void createOrder_colorNotBelongToProduct_returns400() throws Exception {
            given(orderService.createOrder(any())).willThrow(
                    new IllegalArgumentException("Color 5 does not belong to product 1"));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Color 5 does not belong to product 1"));
        }
    }

    // =========================================================================
    // GET /api/orders
    // =========================================================================

    @Nested
    @DisplayName("GET /api/orders")
    class GetAllOrders {

        @Test
        @DisplayName("returns 200 with all orders when no status filter")
        void getAllOrders_noFilter_returns200() throws Exception {
            given(orderService.getAllOrders()).willReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Orders fetched successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        @DisplayName("returns 200 filtered by status when ?status= is provided")
        void getAllOrders_withStatusFilter_returnsFiltered() throws Exception {
            given(orderService.getOrdersByStatus("pending")).willReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/orders").param("status", "pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].status").value("pending"));
        }

        @Test
        @DisplayName("returns 200 with empty list when no orders exist")
        void getAllOrders_noOrders_returns200WithEmptyList() throws Exception {
            given(orderService.getAllOrders()).willReturn(List.of());

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // =========================================================================
    // GET /api/orders/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderById {

        @Test
        @DisplayName("returns 200 with order data when found")
        void getOrderById_found_returns200() throws Exception {
            given(orderService.getOrderById(1L)).willReturn(sampleResponse);

            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order fetched successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.customerName").value("Alice"));
        }

        @Test
        @DisplayName("returns 404 when order not found")
        void getOrderById_notFound_returns404() throws Exception {
            given(orderService.getOrderById(99L)).willThrow(
                    new EntityNotFoundException("Order not found with id: 99"));

            mockMvc.perform(get("/api/orders/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Order not found with id: 99"));
        }
    }

    // =========================================================================
    // PUT /api/orders/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/orders/{id}")
    class UpdateOrder {

        @Test
        @DisplayName("returns 200 with updated order on success")
        void updateOrder_success_returns200() throws Exception {
            given(orderService.updateOrder(eq(1L), any(OrderRequest.class))).willReturn(sampleResponse);

            mockMvc.perform(put("/api/orders/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order updated successfully"))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("returns 404 when order not found")
        void updateOrder_notFound_returns404() throws Exception {
            given(orderService.updateOrder(eq(99L), any())).willThrow(
                    new EntityNotFoundException("Order not found with id: 99"));

            mockMvc.perform(put("/api/orders/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("returns 400 when customerName is blank")
        void updateOrder_blankCustomerName_returns400() throws Exception {
            validRequest.setCustomerName("");

            mockMvc.perform(put("/api/orders/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // PATCH /api/orders/{id}/status
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("returns 200 with updated status on success")
        void updateOrderStatus_success_returns200() throws Exception {
            OrderStatusUpdateRequest statusReq = new OrderStatusUpdateRequest("shipped");
            given(orderService.updateOrderStatus(1L, "shipped")).willReturn(sampleResponse);

            mockMvc.perform(patch("/api/orders/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statusReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order status updated successfully"));
        }

        @Test
        @DisplayName("returns 400 when status field is blank")
        void updateOrderStatus_blankStatus_returns400() throws Exception {
            OrderStatusUpdateRequest statusReq = new OrderStatusUpdateRequest("");

            mockMvc.perform(patch("/api/orders/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statusReq)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when order not found")
        void updateOrderStatus_notFound_returns404() throws Exception {
            OrderStatusUpdateRequest statusReq = new OrderStatusUpdateRequest("shipped");
            given(orderService.updateOrderStatus(99L, "shipped")).willThrow(
                    new EntityNotFoundException("Order not found with id: 99"));

            mockMvc.perform(patch("/api/orders/99/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statusReq)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // =========================================================================
    // DELETE /api/orders/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/orders/{id}")
    class DeleteOrder {

        @Test
        @DisplayName("returns 200 on successful deletion")
        void deleteOrder_success_returns200() throws Exception {
            mockMvc.perform(delete("/api/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order deleted successfully"));
        }

        @Test
        @DisplayName("returns 404 when order not found")
        void deleteOrder_notFound_returns404() throws Exception {
            doThrow(new EntityNotFoundException("Order not found with id: 99"))
                    .when(orderService).deleteOrder(99L);

            mockMvc.perform(delete("/api/orders/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Order not found with id: 99"));
        }
    }
}
