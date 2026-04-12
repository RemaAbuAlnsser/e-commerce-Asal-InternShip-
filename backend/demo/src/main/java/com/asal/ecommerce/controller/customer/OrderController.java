package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.ApiResponse;
import com.asal.ecommerce.dto.OrderRequest;
import com.asal.ecommerce.dto.OrderResponse;
import com.asal.ecommerce.dto.OrderStatusUpdateRequest;
import com.asal.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status
    ) {
        List<OrderResponse> response = (status != null && !status.isBlank())
                ? orderService.getOrdersByStatus(status)
                : orderService.getAllOrders();

        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.updateOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully", null));
    }
}