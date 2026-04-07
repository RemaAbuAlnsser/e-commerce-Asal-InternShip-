package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.DeliveryCityRequest;
import com.asal.ecommerce.dto.DeliveryCityResponse;
import com.asal.ecommerce.mapper.DeliveryCityMapper;
import com.asal.ecommerce.model.DeliveryCity;
import com.asal.ecommerce.repository.DeliveryCityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryCityService {

    private final DeliveryCityRepository deliveryCityRepository;
    private final DeliveryCityMapper deliveryCityMapper;

    // =============================
    // CREATE
    // =============================
    public DeliveryCityResponse create(DeliveryCityRequest request) {

        String cityName = request.getCityName().trim();

        if (deliveryCityRepository.existsByCityNameIgnoreCase(cityName)) {
            throw new RuntimeException("Delivery city already exists: " + cityName);
        }

        DeliveryCity entity = deliveryCityMapper.toEntity(request);
        entity.setCityName(cityName);

        DeliveryCity saved = deliveryCityRepository.save(entity);

        return deliveryCityMapper.toResponse(saved);
    }

    // =============================
    // GET ALL
    // =============================
    public List<DeliveryCityResponse> getAll() {
        return deliveryCityRepository.findAll()
                .stream()
                .map(deliveryCityMapper::toResponse)
                .toList();
    }

    // =============================
    // GET BY ID
    // =============================
    public DeliveryCityResponse getById(Long id) {
        DeliveryCity entity = deliveryCityRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Delivery city not found with id: " + id));

        return deliveryCityMapper.toResponse(entity);
    }

    // =============================
    // UPDATE
    // =============================
    public DeliveryCityResponse update(Long id, DeliveryCityRequest request) {

        DeliveryCity existing = deliveryCityRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Delivery city not found with id: " + id));

        String cityName = request.getCityName().trim();

        if (deliveryCityRepository.existsByCityNameIgnoreCaseAndIdNot(cityName, id)) {
            throw new RuntimeException("Another delivery city already exists with name: " + cityName);
        }

        deliveryCityMapper.updateEntityFromRequest(request, existing);
        existing.setCityName(cityName);

        DeliveryCity updated = deliveryCityRepository.save(existing);

        return deliveryCityMapper.toResponse(updated);
    }

    // =============================
    // DELETE
    // =============================
    public void delete(Long id) {
        DeliveryCity existing = deliveryCityRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Delivery city not found with id: " + id));

        deliveryCityRepository.delete(existing);
    }
}