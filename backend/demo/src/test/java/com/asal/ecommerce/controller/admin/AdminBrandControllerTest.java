package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.BrandCreateRequest;
import com.asal.ecommerce.dto.BrandResponse;
import com.asal.ecommerce.dto.BrandUpdateRequest;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.BrandService;
import com.asal.ecommerce.service.ImageUploadService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin BrandController")
class AdminBrandControllerTest {

    @Mock BrandService brandService;
    @Mock ImageUploadService imageUploadService;

    @InjectMocks
    BrandController brandController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    private BrandResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(brandController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new BrandResponse();
        sampleResponse.setId(1L);
        sampleResponse.setName("Nike");
        sampleResponse.setLogoUrl("/uploads/brands/nike-logo.png");
        sampleResponse.setIsActive(true);
        sampleResponse.setCreatedAt(LocalDateTime.now());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BrandCreateRequest validCreateRequest() {
        BrandCreateRequest req = new BrandCreateRequest();
        req.setName("Nike");
        req.setLogoUrl("/uploads/brands/nike-logo.png");
        return req;
    }

    private BrandUpdateRequest validUpdateRequest() {
        BrandUpdateRequest req = new BrandUpdateRequest();
        req.setName("Nike Updated");
        req.setLogoUrl("/uploads/brands/nike-logo-updated.png");
        req.setIsActive(true);
        return req;
    }

    // =========================================================================
    // POST /api/admin/brands
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/brands")
    class CreateBrand {

        @Test
        @DisplayName("returns 201 with brand on valid request")
        void create_valid_returns201() throws Exception {
            given(brandService.createBrand(any())).willReturn(sampleResponse);

            mockMvc.perform(post("/api/admin/brands")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Nike"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            BrandCreateRequest req = validCreateRequest();
            req.setName("");

            mockMvc.perform(post("/api/admin/brands")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when name exceeds max length")
        void create_longName_returns400() throws Exception {
            BrandCreateRequest req = validCreateRequest();
            req.setName("a".repeat(192)); // exceeds 191 char limit

            mockMvc.perform(post("/api/admin/brands")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when logoUrl exceeds max length")
        void create_longLogoUrl_returns400() throws Exception {
            BrandCreateRequest req = validCreateRequest();
            req.setLogoUrl("a".repeat(501)); // exceeds 500 char limit

            mockMvc.perform(post("/api/admin/brands")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /api/admin/brands/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/brands/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with brand when found")
        void getById_found_returns200() throws Exception {
            given(brandService.getBrandById(1L)).willReturn(sampleResponse);

            mockMvc.perform(get("/api/admin/brands/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Nike"));
        }

        @Test
        @DisplayName("returns 404 when brand not found")
        void getById_notFound_returns404() throws Exception {
            given(brandService.getBrandById(99L))
                    .willThrow(new EntityNotFoundException("Brand not found with id: 99"));

            mockMvc.perform(get("/api/admin/brands/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/admin/brands
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/brands")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paged results using default params")
        void getAll_defaults_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BrandResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(brandService.getAllBrands(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/admin/brands"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Nike"));
        }

        @Test
        @DisplayName("passes custom pagination params to service")
        void getAll_customParams_passesParams() throws Exception {
            Pageable pageable = PageRequest.of(1, 5);
            Page<BrandResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(brandService.getAllBrands(1, 5, "id", "desc")).willReturn(page);

            mockMvc.perform(get("/api/admin/brands")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sortBy", "id")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            then(brandService).should().getAllBrands(1, 5, "id", "desc");
        }
    }

    // =========================================================================
    // GET /api/admin/brands/search
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/brands/search")
    class Search {

        @Test
        @DisplayName("returns 200 with matching brands")
        void search_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BrandResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(brandService.searchBrands("nike", 0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/admin/brands/search").param("name", "nike"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Nike"));
        }

        @Test
        @DisplayName("returns empty page when no matches")
        void search_noMatches_returnsEmpty() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BrandResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(brandService.searchBrands("unknown", 0, 10, "name", "asc")).willReturn(emptyPage);

            mockMvc.perform(get("/api/admin/brands/search").param("name", "unknown"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }

    // =========================================================================
    // PUT /api/admin/brands/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/admin/brands/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with updated brand")
        void update_valid_returns200() throws Exception {
            sampleResponse.setName("Nike Updated");
            given(brandService.updateBrand(eq(1L), any())).willReturn(sampleResponse);

            mockMvc.perform(put("/api/admin/brands/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Nike Updated"));
        }

        @Test
        @DisplayName("returns 404 when brand not found")
        void update_notFound_returns404() throws Exception {
            given(brandService.updateBrand(eq(99L), any()))
                    .willThrow(new EntityNotFoundException("Brand not found with id: 99"));

            mockMvc.perform(put("/api/admin/brands/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 when validation fails")
        void update_invalidData_returns400() throws Exception {
            BrandUpdateRequest req = validUpdateRequest();
            req.setName(""); // blank name

            mockMvc.perform(put("/api/admin/brands/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // DELETE /api/admin/brands/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/admin/brands/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 on successful deletion")
        void delete_success_returns204() throws Exception {
            willDoNothing().given(brandService).deleteBrand(1L);

            mockMvc.perform(delete("/api/admin/brands/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when brand not found")
        void delete_notFound_returns404() throws Exception {
            willThrow(new EntityNotFoundException("Brand not found with id: 99"))
                    .given(brandService).deleteBrand(99L);

            mockMvc.perform(delete("/api/admin/brands/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PATCH /api/admin/brands/{id}/status
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/admin/brands/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("returns 200 when status updated successfully")
        void updateStatus_success_returns200() throws Exception {
            sampleResponse.setIsActive(false);
            given(brandService.updateBrandStatus(1L, false)).willReturn(sampleResponse);

            mockMvc.perform(patch("/api/admin/brands/1/status").param("isActive", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }

        @Test
        @DisplayName("returns 404 when brand not found")
        void updateStatus_notFound_returns404() throws Exception {
            given(brandService.updateBrandStatus(99L, true))
                    .willThrow(new EntityNotFoundException("Brand not found with id: 99"));

            mockMvc.perform(patch("/api/admin/brands/99/status").param("isActive", "true"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/admin/brands/count
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/brands/count")
    class GetCount {

        @Test
        @DisplayName("returns 200 with brand count")
        void getCount_returns200() throws Exception {
            given(brandService.getBrandCount()).willReturn(5L);

            mockMvc.perform(get("/api/admin/brands/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("5"));
        }
    }

    // =========================================================================
    // POST /api/admin/brands/upload-logo
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/brands/upload-logo")
    class UploadLogo {

        @Test
        @DisplayName("returns 200 with imageUrl on successful upload")
        void uploadLogo_success_returns200() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "logo.png", MediaType.IMAGE_PNG_VALUE, "fake-image-data".getBytes());

            given(imageUploadService.uploadCategoryImage(any())).willReturn("/uploads/brands/logo.png");

            mockMvc.perform(multipart("/api/admin/brands/upload-logo").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.imageUrl").value("/uploads/brands/logo.png"));
        }

        @Test
        @DisplayName("returns 400 on invalid file")
        void uploadLogo_invalidFile_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "logo.txt", MediaType.TEXT_PLAIN_VALUE, "not-an-image".getBytes());

            given(imageUploadService.uploadCategoryImage(any()))
                    .willThrow(new IllegalArgumentException("Invalid file format"));

            mockMvc.perform(multipart("/api/admin/brands/upload-logo").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid file format"));
        }

        @Test
        @DisplayName("returns 500 on upload failure")
        void uploadLogo_uploadFails_returns500() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "logo.png", MediaType.IMAGE_PNG_VALUE, "fake-image-data".getBytes());

            given(imageUploadService.uploadCategoryImage(any()))
                    .willThrow(new RuntimeException("Storage error"));

            mockMvc.perform(multipart("/api/admin/brands/upload-logo").file(file))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to upload logo"));
        }
    }
}
