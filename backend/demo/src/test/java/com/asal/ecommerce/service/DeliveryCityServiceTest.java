package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.DeliveryCityRequest;
import com.asal.ecommerce.dto.DeliveryCityResponse;
import com.asal.ecommerce.mapper.DeliveryCityMapper;
import com.asal.ecommerce.model.DeliveryCity;
import com.asal.ecommerce.repository.DeliveryCityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryCityServiceTest {

    @Mock
    private DeliveryCityRepository deliveryCityRepository;

    @Mock
    private DeliveryCityMapper deliveryCityMapper;

    @InjectMocks
    private DeliveryCityService deliveryCityService;

    @Test
    void create_shouldCreateSuccessfully() {
        DeliveryCityRequest request = DeliveryCityRequest.builder()
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        DeliveryCity entity = DeliveryCity.builder()
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        DeliveryCity saved = DeliveryCity.builder()
                .id(1L)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        DeliveryCityResponse response = DeliveryCityResponse.builder()
                .id(1L)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        when(deliveryCityRepository.existsByCityNameIgnoreCase("Jerusalem")).thenReturn(false);
        when(deliveryCityMapper.toEntity(request)).thenReturn(entity);
        when(deliveryCityRepository.save(entity)).thenReturn(saved);
        when(deliveryCityMapper.toResponse(saved)).thenReturn(response);

        DeliveryCityResponse result = deliveryCityService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jerusalem", result.getCityName());

        verify(deliveryCityRepository).existsByCityNameIgnoreCase("Jerusalem");
        verify(deliveryCityMapper).toEntity(request);
        verify(deliveryCityRepository).save(entity);
        verify(deliveryCityMapper).toResponse(saved);
    }

    @Test
    void create_shouldThrowRuntimeException_whenCityExists() {
        DeliveryCityRequest request = DeliveryCityRequest.builder()
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        when(deliveryCityRepository.existsByCityNameIgnoreCase("Jerusalem")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> deliveryCityService.create(request));

        assertEquals("Delivery city already exists: Jerusalem", ex.getMessage());
        verify(deliveryCityRepository).existsByCityNameIgnoreCase("Jerusalem");
        verify(deliveryCityMapper, never()).toEntity(any());
        verify(deliveryCityRepository, never()).save(any());
    }

    @Test
    void getAll_shouldReturnAllCities() {
        DeliveryCity city1 = DeliveryCity.builder().id(1L).cityName("Jerusalem").deliveryPrice(new BigDecimal("15.00")).build();
        DeliveryCity city2 = DeliveryCity.builder().id(2L).cityName("Ramallah").deliveryPrice(new BigDecimal("20.00")).build();

        DeliveryCityResponse response1 = DeliveryCityResponse.builder().id(1L).cityName("Jerusalem").deliveryPrice(new BigDecimal("15.00")).build();
        DeliveryCityResponse response2 = DeliveryCityResponse.builder().id(2L).cityName("Ramallah").deliveryPrice(new BigDecimal("20.00")).build();

        when(deliveryCityRepository.findAll()).thenReturn(List.of(city1, city2));
        when(deliveryCityMapper.toResponse(city1)).thenReturn(response1);
        when(deliveryCityMapper.toResponse(city2)).thenReturn(response2);

        List<DeliveryCityResponse> result = deliveryCityService.getAll();

        assertEquals(2, result.size());
        assertEquals("Jerusalem", result.get(0).getCityName());
        assertEquals("Ramallah", result.get(1).getCityName());
    }

    @Test
    void getById_shouldReturnCity_whenFound() {
        DeliveryCity city = DeliveryCity.builder()
                .id(1L)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        DeliveryCityResponse response = DeliveryCityResponse.builder()
                .id(1L)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        when(deliveryCityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(deliveryCityMapper.toResponse(city)).thenReturn(response);

        DeliveryCityResponse result = deliveryCityService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jerusalem", result.getCityName());
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(deliveryCityRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> deliveryCityService.getById(1L));

        assertEquals("Delivery city not found with id: 1", ex.getMessage());
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        Long id = 1L;

        DeliveryCityRequest request = DeliveryCityRequest.builder()
                .cityName("Ramallah")
                .deliveryPrice(new BigDecimal("20.00"))
                .build();

        DeliveryCity existing = DeliveryCity.builder()
                .id(id)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        DeliveryCity updated = DeliveryCity.builder()
                .id(id)
                .cityName("Ramallah")
                .deliveryPrice(new BigDecimal("20.00"))
                .build();

        DeliveryCityResponse response = DeliveryCityResponse.builder()
                .id(id)
                .cityName("Ramallah")
                .deliveryPrice(new BigDecimal("20.00"))
                .build();

        when(deliveryCityRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deliveryCityRepository.existsByCityNameIgnoreCaseAndIdNot("Ramallah", id)).thenReturn(false);
        when(deliveryCityRepository.save(existing)).thenReturn(updated);
        when(deliveryCityMapper.toResponse(updated)).thenReturn(response);

        DeliveryCityResponse result = deliveryCityService.update(id, request);

        assertNotNull(result);
        assertEquals("Ramallah", result.getCityName());

        verify(deliveryCityMapper).updateEntityFromRequest(request, existing);
        verify(deliveryCityRepository).save(existing);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenNotFound() {
        DeliveryCityRequest request = DeliveryCityRequest.builder()
                .cityName("Ramallah")
                .deliveryPrice(new BigDecimal("20.00"))
                .build();

        when(deliveryCityRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> deliveryCityService.update(1L, request));

        assertEquals("Delivery city not found with id: 1", ex.getMessage());
    }

    @Test
    void update_shouldThrowRuntimeException_whenDuplicateNameExists() {
        Long id = 1L;

        DeliveryCityRequest request = DeliveryCityRequest.builder()
                .cityName("Ramallah")
                .deliveryPrice(new BigDecimal("20.00"))
                .build();

        DeliveryCity existing = DeliveryCity.builder()
                .id(id)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        when(deliveryCityRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deliveryCityRepository.existsByCityNameIgnoreCaseAndIdNot("Ramallah", id)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> deliveryCityService.update(id, request));

        assertEquals("Another delivery city already exists with name: Ramallah", ex.getMessage());
    }

    @Test
    void delete_shouldDeleteSuccessfully() {
        DeliveryCity city = DeliveryCity.builder()
                .id(1L)
                .cityName("Jerusalem")
                .deliveryPrice(new BigDecimal("15.00"))
                .build();

        when(deliveryCityRepository.findById(1L)).thenReturn(Optional.of(city));

        deliveryCityService.delete(1L);

        verify(deliveryCityRepository).delete(city);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(deliveryCityRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> deliveryCityService.delete(1L));

        assertEquals("Delivery city not found with id: 1", ex.getMessage());
        verify(deliveryCityRepository, never()).delete(any());
    }
}