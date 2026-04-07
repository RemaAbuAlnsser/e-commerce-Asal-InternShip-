package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.DeliveryCityRequest;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDeliveryCityControllerTest {

    @Mock
    private DeliveryCityService deliveryCityService;

    @InjectMocks
    private DeliveryCityController adminDeliveryCityController;

    @Test
    void create_shouldReturnCreatedResponse() {
        DeliveryCityRequest request = DeliveryCityRequest.builder()
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        DeliveryCityResponse response = DeliveryCityResponse.builder()
                .id(1L)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        when(deliveryCityService.create(request)).thenReturn(response);

        ResponseEntity<DeliveryCityResponse> result = adminDeliveryCityController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Jerusalem", result.getBody().getCityName());
    }

    @Test
    void getAll_shouldReturnOkResponse() {
        List<DeliveryCityResponse> cities = List.of(
                DeliveryCityResponse.builder().id(1L).cityName("Jerusalem").deliveryPrice(new BigDecimal("15.00")).build(),
                DeliveryCityResponse.builder().id(2L).cityName("Ramallah").deliveryPrice(new BigDecimal("20.00")).build()
        );

        when(deliveryCityService.getAll()).thenReturn(cities);

        ResponseEntity<?> result = adminDeliveryCityController.getAll();

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

        ResponseEntity<DeliveryCityResponse> result = adminDeliveryCityController.getById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    void update_shouldReturnOkResponse() {
        DeliveryCityRequest request = DeliveryCityRequest.builder()
                .cityName("Ramallah")
                .deliveryPrice(new BigDecimal("20.00"))
                .build();

        DeliveryCityResponse response = DeliveryCityResponse.builder()
                .id(1L)
                .cityName("Ramallah")
                .deliveryPrice(new BigDecimal("20.00"))
                .build();

        when(deliveryCityService.update(1L, request)).thenReturn(response);

        ResponseEntity<DeliveryCityResponse> result = adminDeliveryCityController.update(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Ramallah", result.getBody().getCityName());
    }

    @Test
    void delete_shouldReturnNoContent() {
        doNothing().when(deliveryCityService).delete(1L);

        ResponseEntity<Void> result = adminDeliveryCityController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(deliveryCityService).delete(1L);
    }
}