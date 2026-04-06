package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.SubcategoryCreateRequest;
import com.asal.ecommerce.dto.SubcategoryResponse;
import com.asal.ecommerce.dto.SubcategoryUpdateRequest;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.ImageUploadService;
import com.asal.ecommerce.service.SubcategoryService;
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
@DisplayName("Admin SubcategoryController")
class AdminSubcategoryControllerTest {

    @Mock SubcategoryService subcategoryService;
    @Mock ImageUploadService imageUploadService;

    @InjectMocks
    SubcategoryController subcategoryController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    private SubcategoryResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(subcategoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new SubcategoryResponse();
        sampleResponse.setId(1L);
        sampleResponse.setName("Smartphones");
        sampleResponse.setSlug("smartphones");
        sampleResponse.setCategoryId(1L);
        sampleResponse.setCategoryName("Electronics");
        sampleResponse.setIsActive(true);
        sampleResponse.setCreatedAt(LocalDateTime.now());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SubcategoryCreateRequest validCreateRequest() {
        SubcategoryCreateRequest req = new SubcategoryCreateRequest();
        req.setName("Smartphones");
        req.setCategoryId(1L);
        return req;
    }

    private SubcategoryUpdateRequest validUpdateRequest() {
        SubcategoryUpdateRequest req = new SubcategoryUpdateRequest();
        req.setName("Smartphones Updated");
        req.setCategoryId(1L);
        return req;
    }

    // =========================================================================
    // POST /api/admin/subcategories
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/subcategories")
    class CreateSubcategory {

        @Test
        @DisplayName("returns 201 with subcategory on valid request")
        void create_valid_returns201() throws Exception {
            given(subcategoryService.createSubcategory(any())).willReturn(sampleResponse);

            mockMvc.perform(post("/api/admin/subcategories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Smartphones"))
                    .andExpect(jsonPath("$.categoryName").value("Electronics"));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            SubcategoryCreateRequest req = validCreateRequest();
            req.setName("");

            mockMvc.perform(post("/api/admin/subcategories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when categoryId is null")
        void create_nullCategoryId_returns400() throws Exception {
            SubcategoryCreateRequest req = validCreateRequest();
            req.setCategoryId(null);

            mockMvc.perform(post("/api/admin/subcategories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /api/admin/subcategories/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/subcategories/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with subcategory when found")
        void getById_found_returns200() throws Exception {
            given(subcategoryService.getSubcategoryById(1L)).willReturn(sampleResponse);

            mockMvc.perform(get("/api/admin/subcategories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Smartphones"));
        }

        @Test
        @DisplayName("returns 404 when subcategory not found")
        void getById_notFound_returns404() throws Exception {
            given(subcategoryService.getSubcategoryById(99L))
                    .willThrow(new EntityNotFoundException("Subcategory not found with id: 99"));

            mockMvc.perform(get("/api/admin/subcategories/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/admin/subcategories
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/subcategories")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paged results using default params")
        void getAll_defaults_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(subcategoryService.getAllSubcategories(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/admin/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Smartphones"));
        }

        @Test
        @DisplayName("passes custom pagination params to service")
        void getAll_customParams_passesParams() throws Exception {
            Pageable pageable = PageRequest.of(1, 5);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(subcategoryService.getAllSubcategories(1, 5, "id", "desc")).willReturn(page);

            mockMvc.perform(get("/api/admin/subcategories")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sortBy", "id")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            then(subcategoryService).should().getAllSubcategories(1, 5, "id", "desc");
        }
    }

    // =========================================================================
    // GET /api/admin/subcategories/category/{categoryId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/subcategories/category/{categoryId}")
    class GetByCategory {

        @Test
        @DisplayName("returns 200 with subcategories for category")
        void getByCategory_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(subcategoryService.getSubcategoriesByCategory(1L, 0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/admin/subcategories/category/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].categoryId").value(1));
        }

        @Test
        @DisplayName("returns empty page when category has no subcategories")
        void getByCategory_noSubcategories_returnsEmpty() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(subcategoryService.getSubcategoriesByCategory(99L, 0, 10, "name", "asc")).willReturn(emptyPage);

            mockMvc.perform(get("/api/admin/subcategories/category/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }

    // =========================================================================
    // GET /api/admin/subcategories/search
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/subcategories/search")
    class Search {

        @Test
        @DisplayName("returns 200 with matching subcategories")
        void search_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(subcategoryService.searchSubcategories("smart", 0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/admin/subcategories/search").param("name", "smart"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Smartphones"));
        }
    }

    // =========================================================================
    // PUT /api/admin/subcategories/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/admin/subcategories/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with updated subcategory")
        void update_valid_returns200() throws Exception {
            sampleResponse.setName("Smartphones Updated");
            given(subcategoryService.updateSubcategory(eq(1L), any())).willReturn(sampleResponse);

            mockMvc.perform(put("/api/admin/subcategories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Smartphones Updated"));
        }

        @Test
        @DisplayName("returns 404 when subcategory not found")
        void update_notFound_returns404() throws Exception {
            given(subcategoryService.updateSubcategory(eq(99L), any()))
                    .willThrow(new EntityNotFoundException("Subcategory not found with id: 99"));

            mockMvc.perform(put("/api/admin/subcategories/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest())))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // DELETE /api/admin/subcategories/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/admin/subcategories/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 on successful deletion")
        void delete_success_returns204() throws Exception {
            willDoNothing().given(subcategoryService).deleteSubcategory(1L);

            mockMvc.perform(delete("/api/admin/subcategories/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when subcategory not found")
        void delete_notFound_returns404() throws Exception {
            willThrow(new EntityNotFoundException("Subcategory not found with id: 99"))
                    .given(subcategoryService).deleteSubcategory(99L);

            mockMvc.perform(delete("/api/admin/subcategories/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PATCH /api/admin/subcategories/{id}/status
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/admin/subcategories/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("returns 200 when status updated successfully")
        void updateStatus_success_returns200() throws Exception {
            sampleResponse.setIsActive(false);
            given(subcategoryService.updateSubcategoryStatus(1L, false)).willReturn(sampleResponse);

            mockMvc.perform(patch("/api/admin/subcategories/1/status").param("isActive", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }

    // =========================================================================
    // GET /api/admin/subcategories/count
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/subcategories/count")
    class GetCount {

        @Test
        @DisplayName("returns 200 with subcategory count")
        void getCount_returns200() throws Exception {
            given(subcategoryService.getSubcategoryCount()).willReturn(15L);

            mockMvc.perform(get("/api/admin/subcategories/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("15"));
        }
    }

    // =========================================================================
    // GET /api/admin/subcategories/count/category/{categoryId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/subcategories/count/category/{categoryId}")
    class GetCountByCategory {

        @Test
        @DisplayName("returns 200 with subcategory count for category")
        void getCountByCategory_returns200() throws Exception {
            given(subcategoryService.getSubcategoryCountByCategory(1L)).willReturn(5L);

            mockMvc.perform(get("/api/admin/subcategories/count/category/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("5"));
        }
    }

    // =========================================================================
    // POST /api/admin/subcategories/upload-image
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/subcategories/upload-image")
    class UploadImage {

        @Test
        @DisplayName("returns 200 with imageUrl on successful upload")
        void uploadImage_success_returns200() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "subcategory.png", MediaType.IMAGE_PNG_VALUE, "fake-image-data".getBytes());

            given(imageUploadService.uploadSubcategoryImage(any())).willReturn("/uploads/subcategories/image.png");

            mockMvc.perform(multipart("/api/admin/subcategories/upload-image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.imageUrl").value("/uploads/subcategories/image.png"));
        }

        @Test
        @DisplayName("returns 400 on invalid file")
        void uploadImage_invalidFile_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "image.txt", MediaType.TEXT_PLAIN_VALUE, "not-an-image".getBytes());

            given(imageUploadService.uploadSubcategoryImage(any()))
                    .willThrow(new IllegalArgumentException("Invalid file format"));

            mockMvc.perform(multipart("/api/admin/subcategories/upload-image").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid file format"));
        }
    }
}
