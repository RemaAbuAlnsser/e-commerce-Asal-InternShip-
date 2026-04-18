package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.CustomerSummaryResponse;
import com.asal.ecommerce.dto.SubscriberSummaryResponse;
import com.asal.ecommerce.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerSummaryResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/subscribers")
    public ResponseEntity<List<SubscriberSummaryResponse>> getAllSubscribers() {
        return ResponseEntity.ok(customerService.getAllSubscribers());
    }
}
