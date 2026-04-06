package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.SubcategoryResponse;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.SubcategoryService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer SubcategoryController")
class CustomerSubcategoryControllerTest {

    @Mock SubcategoryService subcategoryService;

    @InjectMocks
    SubcategoryController subcategoryController;

    MockMvc mockMvc;

    private SubcategoryResponse activeSubcategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(subcategoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        activeSubcategory = new SubcategoryResponse();
        activeSubcategory.setId(1L);
        activeSubcategory.setName("Smartphones");
        activeSubcategory.setSlug("smartphones");
        activeSubcategory.setCategoryId(1L);
        activeSubcategory.setCategoryName("Electronics");
        activeSubcategory.setIsActive(true);
    }

    // =========================================================================
    // GET /api/subcategories/{slug}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/subcategories/{slug}")
    class GetBySlug {

        @Test
        @DisplayName("returns 200 with active subcategory when found")
        void getBySlug_found_returns200() throws Exception {
            given(subcategoryService.getActiveSubcategoryBySlug("smartphones")).willReturn(activeSubcategory);

            mockMvc.perform(get("/api/subcategories/smartphones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Smartphones"))
                    .andExpect(jsonPath("$.slug").value("smartphones"))
                    .andExpect(jsonPath("$.categoryName").value("Electronics"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("returns 404 when subcategory not found")
        void getBySlug_notFound_returns404() throws Exception {
            given(subcategoryService.getActiveSubcategoryBySlug("unknown"))
                    .willThrow(new EntityNotFoundException("Subcategory not found with slug: unknown"));

            mockMvc.perform(get("/api/subcategories/unknown"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 when subcategory exists but is inactive")
        void getBySlug_inactive_returns404() throws Exception {
            given(subcategoryService.getActiveSubcategoryBySlug("inactive-subcategory"))
                    .willThrow(new EntityNotFoundException("Active subcategory not found with slug: inactive-subcategory"));

            mockMvc.perform(get("/api/subcategories/inactive-subcategory"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 when subcategory belongs to inactive category")
        void getBySlug_inactiveCategory_returns404() throws Exception {
            given(subcategoryService.getActiveSubcategoryBySlug("subcategory-of-inactive-category"))
                    .willThrow(new EntityNotFoundException("Active subcategory not found with slug: subcategory-of-inactive-category"));

            mockMvc.perform(get("/api/subcategories/subcategory-of-inactive-category"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/subcategories — only returns active subcategories
    // =========================================================================

    @Nested
    @DisplayName("GET /api/subcategories")
    class GetAll {

        @Test
        @DisplayName("returns 200 with active subcategories using default params")
        void getAll_defaults_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            given(subcategoryService.getActiveSubcategories(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Smartphones"))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));
        }

        @Test
        @DisplayName("passes custom pagination params to service")
        void getAll_customParams_passesParams() throws Exception {
            Pageable pageable = PageRequest.of(1, 5);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            given(subcategoryService.getActiveSubcategories(1, 5, "id", "desc")).willReturn(page);

            mockMvc.perform(get("/api/subcategories")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sortBy", "id")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            then(subcategoryService).should().getActiveSubcategories(1, 5, "id", "desc");
        }

        @Test
        @DisplayName("returns empty page when no active subcategories exist")
        void getAll_noActiveSubcategories_returnsEmptyPage() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(subcategoryService.getActiveSubcategories(0, 10, "name", "asc")).willReturn(emptyPage);

            mockMvc.perform(get("/api/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("only returns active subcategories — inactive subcategories are filtered out")
        void getAll_onlyActiveSubcategories() throws Exception {
            // This test verifies that the customer endpoint only calls getActiveSubcategories
            // which should filter out inactive subcategories at the service level
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            given(subcategoryService.getActiveSubcategories(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));

            // Verify that only getActiveSubcategories is called, not getAllSubcategories
            then(subcategoryService).should().getActiveSubcategories(0, 10, "name", "asc");
            then(subcategoryService).should(never()).getAllSubcategories(anyInt(), anyInt(), anyString(), anyString());
        }

        @Test
        @DisplayName("handles various sorting options correctly")
        void getAll_differentSorting_passesCorrectly() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            
            // Test sorting by name ascending (default)
            given(subcategoryService.getActiveSubcategories(0, 10, "name", "asc")).willReturn(page);
            mockMvc.perform(get("/api/subcategories"))
                    .andExpect(status().isOk());

            // Test sorting by name descending
            given(subcategoryService.getActiveSubcategories(0, 10, "name", "desc")).willReturn(page);
            mockMvc.perform(get("/api/subcategories")
                            .param("sortBy", "name")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            // Test sorting by id
            given(subcategoryService.getActiveSubcategories(0, 10, "id", "asc")).willReturn(page);
            mockMvc.perform(get("/api/subcategories")
                            .param("sortBy", "id")
                            .param("direction", "asc"))
                    .andExpect(status().isOk());

            then(subcategoryService).should().getActiveSubcategories(0, 10, "name", "asc");
            then(subcategoryService).should().getActiveSubcategories(0, 10, "name", "desc");
            then(subcategoryService).should().getActiveSubcategories(0, 10, "id", "asc");
        }

        @Test
        @DisplayName("only returns subcategories from active categories")
        void getAll_onlyFromActiveCategories() throws Exception {
            // This test verifies that subcategories from inactive categories are not returned
            // The service should handle this filtering at the database level
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            given(subcategoryService.getActiveSubcategories(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].isActive").value(true))
                    .andExpect(jsonPath("$.content[0].categoryName").value("Electronics"));

            then(subcategoryService).should().getActiveSubcategories(0, 10, "name", "asc");
        }
    }
}
