package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.DeliveryCityResponse;
import com.asal.ecommerce.service.DeliveryCityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("CustomerDeliveryCityController")
@RequestMapping("/api/customer/delivery-cities")
@RequiredArgsConstructor
public class DeliveryCityController {

    private final DeliveryCityService deliveryCityService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(deliveryCityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryCityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryCityService.getById(id));
    }
}