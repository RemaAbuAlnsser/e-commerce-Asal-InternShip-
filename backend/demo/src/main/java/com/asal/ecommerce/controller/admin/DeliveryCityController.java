package com.asal.ecommerce.controller.admin;


import com.asal.ecommerce.dto.DeliveryCityRequest;
import com.asal.ecommerce.dto.DeliveryCityResponse;
import com.asal.ecommerce.service.DeliveryCityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("AdminDeliveryCityController")
@RequestMapping("/api/admin/delivery-cities")
@RequiredArgsConstructor
public class DeliveryCityController {

    private final DeliveryCityService deliveryCityService;

    @PostMapping
    public ResponseEntity<DeliveryCityResponse> create(@Valid @RequestBody DeliveryCityRequest request) {
        DeliveryCityResponse response = deliveryCityService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(deliveryCityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryCityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryCityService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryCityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryCityRequest request
    ) {
        return ResponseEntity.ok(deliveryCityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryCityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}