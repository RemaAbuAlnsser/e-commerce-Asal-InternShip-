package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.BrandResponse;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.BrandService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer BrandController")
class CustomerBrandControllerTest {

    @Mock BrandService brandService;

    @InjectMocks
    BrandController brandController;

    MockMvc mockMvc;

    private BrandResponse activeBrand;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(brandController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        activeBrand = new BrandResponse();
        activeBrand.setId(1L);
        activeBrand.setName("Nike");
        activeBrand.setLogoUrl("/uploads/brands/nike-logo.png");
        activeBrand.setIsActive(true);
        activeBrand.setCreatedAt(LocalDateTime.now());
    }

    // =========================================================================
    // GET /api/brands/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/brands/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with active brand when found")
        void getById_found_returns200() throws Exception {
            given(brandService.getActiveBrandById(1L)).willReturn(activeBrand);

            mockMvc.perform(get("/api/brands/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Nike"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("returns 404 when brand not found")
        void getById_notFound_returns404() throws Exception {
            given(brandService.getActiveBrandById(99L))
                    .willThrow(new EntityNotFoundException("Brand not found with id: 99"));

            mockMvc.perform(get("/api/brands/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 when brand exists but is inactive")
        void getById_inactive_returns404() throws Exception {
            given(brandService.getActiveBrandById(2L))
                    .willThrow(new EntityNotFoundException("Active brand not found with id: 2"));

            mockMvc.perform(get("/api/brands/2"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/brands — only returns active brands
    // =========================================================================

    @Nested
    @DisplayName("GET /api/brands")
    class GetAll {

        @Test
        @DisplayName("returns 200 with active brands using default params")
        void getAll_defaults_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BrandResponse> page = new PageImpl<>(List.of(activeBrand), pageable, 1);
            given(brandService.getActiveBrands(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/brands"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Nike"))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));
        }

        @Test
        @DisplayName("passes custom pagination params to service")
        void getAll_customParams_passesParams() throws Exception {
            Pageable pageable = PageRequest.of(1, 5);
            Page<BrandResponse> page = new PageImpl<>(List.of(activeBrand), pageable, 1);
            given(brandService.getActiveBrands(1, 5, "id", "desc")).willReturn(page);

            mockMvc.perform(get("/api/brands")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sortBy", "id")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            then(brandService).should().getActiveBrands(1, 5, "id", "desc");
        }

        @Test
        @DisplayName("returns empty page when no active brands exist")
        void getAll_noActiveBrands_returnsEmptyPage() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BrandResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(brandService.getActiveBrands(0, 10, "name", "asc")).willReturn(emptyPage);

            mockMvc.perform(get("/api/brands"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("only returns active brands — inactive brands are filtered out")
        void getAll_onlyActiveBrands() throws Exception {
            // This test verifies that the customer endpoint only calls getActiveBrands
            // which should filter out inactive brands at the service level
            Pageable pageable = PageRequest.of(0, 10);
            Page<BrandResponse> page = new PageImpl<>(List.of(activeBrand), pageable, 1);
            given(brandService.getActiveBrands(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/brands"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));

            // Verify that only getActiveBrands is called, not getAllBrands
            then(brandService).should().getActiveBrands(0, 10, "name", "asc");
            then(brandService).should(never()).getAllBrands(anyInt(), anyInt(), anyString(), anyString());
        }

        @Test
        @DisplayName("handles various sorting options correctly")
        void getAll_differentSorting_passesCorrectly() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BrandResponse> page = new PageImpl<>(List.of(activeBrand), pageable, 1);
            
            // Test sorting by name ascending (default)
            given(brandService.getActiveBrands(0, 10, "name", "asc")).willReturn(page);
            mockMvc.perform(get("/api/brands"))
                    .andExpect(status().isOk());

            // Test sorting by name descending
            given(brandService.getActiveBrands(0, 10, "name", "desc")).willReturn(page);
            mockMvc.perform(get("/api/brands")
                            .param("sortBy", "name")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            // Test sorting by id
            given(brandService.getActiveBrands(0, 10, "id", "asc")).willReturn(page);
            mockMvc.perform(get("/api/brands")
                            .param("sortBy", "id")
                            .param("direction", "asc"))
                    .andExpect(status().isOk());

            then(brandService).should().getActiveBrands(0, 10, "name", "asc");
            then(brandService).should().getActiveBrands(0, 10, "name", "desc");
            then(brandService).should().getActiveBrands(0, 10, "id", "asc");
        }
    }
}
