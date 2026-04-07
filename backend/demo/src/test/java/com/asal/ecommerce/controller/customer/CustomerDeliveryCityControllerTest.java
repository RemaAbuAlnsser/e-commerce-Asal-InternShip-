package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.DeliveryCityResponse;
import com.asal.ecommerce.service.DeliveryCityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDeliveryCityControllerTest {

    @Mock
    private DeliveryCityService deliveryCityService;

    @InjectMocks
    private DeliveryCityController customerDeliveryCityController;

    @Test
    void getAll_shouldReturnOkResponse() {
        List<DeliveryCityResponse> cities = List.of(
                DeliveryCityResponse.builder().id(1L).cityName("Jerusalem").deliveryPrice(new BigDecimal("15.00")).build(),
                DeliveryCityResponse.builder().id(2L).cityName("Ramallah").deliveryPrice(new BigDecimal("20.00")).build()
        );

        when(deliveryCityService.getAll()).thenReturn(cities);

        ResponseEntity<?> result = customerDeliveryCityController.getAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void getById_shouldReturnOkResponse() {
        DeliveryCityResponse response = DeliveryCityResponse.builder()
                .id(1L)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        when(deliveryCityService.getById(1L)).thenReturn(response);

        ResponseEntity<DeliveryCityResponse> result = customerDeliveryCityController.getById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Jerusalem", result.getBody().getCityName());
    }
}