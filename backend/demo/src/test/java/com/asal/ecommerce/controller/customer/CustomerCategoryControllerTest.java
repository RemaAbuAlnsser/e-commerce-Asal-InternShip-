package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.CategoryResponse;
import com.asal.ecommerce.dto.SubcategoryResponse;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.CategoryService;
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
@DisplayName("Customer CategoryController")
class CustomerCategoryControllerTest {

    @Mock CategoryService categoryService;
    @Mock SubcategoryService subcategoryService;

    @InjectMocks
    CategoryController categoryController;

    MockMvc mockMvc;

    private CategoryResponse activeCategory;
    private SubcategoryResponse activeSubcategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        activeCategory = new CategoryResponse();
        activeCategory.setId(1L);
        activeCategory.setName("Electronics");
        activeCategory.setSlug("electronics");
        activeCategory.setIsActive(true);

        activeSubcategory = new SubcategoryResponse();
        activeSubcategory.setId(1L);
        activeSubcategory.setName("Smartphones");
        activeSubcategory.setSlug("smartphones");
        activeSubcategory.setCategoryId(1L);
        activeSubcategory.setCategoryName("Electronics");
        activeSubcategory.setIsActive(true);
    }

    // =========================================================================
    // GET /api/categories/{slug}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/categories/{slug}")
    class GetBySlug {

        @Test
        @DisplayName("returns 200 with active category when found")
        void getBySlug_found_returns200() throws Exception {
            given(categoryService.getActiveCategoryBySlug("electronics")).willReturn(activeCategory);

            mockMvc.perform(get("/api/categories/electronics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Electronics"))
                    .andExpect(jsonPath("$.slug").value("electronics"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("returns 404 when category not found")
        void getBySlug_notFound_returns404() throws Exception {
            given(categoryService.getActiveCategoryBySlug("unknown"))
                    .willThrow(new EntityNotFoundException("Category not found with slug: unknown"));

            mockMvc.perform(get("/api/categories/unknown"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 when category exists but is inactive")
        void getBySlug_inactive_returns404() throws Exception {
            given(categoryService.getActiveCategoryBySlug("inactive-category"))
                    .willThrow(new EntityNotFoundException("Active category not found with slug: inactive-category"));

            mockMvc.perform(get("/api/categories/inactive-category"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/categories — only returns active categories
    // =========================================================================

    @Nested
    @DisplayName("GET /api/categories")
    class GetAll {

        @Test
        @DisplayName("returns 200 with active categories using default params")
        void getAll_defaults_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CategoryResponse> page = new PageImpl<>(List.of(activeCategory), pageable, 1);
            given(categoryService.getActiveCategories(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Electronics"))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));
        }

        @Test
        @DisplayName("passes custom pagination params to service")
        void getAll_customParams_passesParams() throws Exception {
            Pageable pageable = PageRequest.of(1, 5);
            Page<CategoryResponse> page = new PageImpl<>(List.of(activeCategory), pageable, 1);
            given(categoryService.getActiveCategories(1, 5, "id", "desc")).willReturn(page);

            mockMvc.perform(get("/api/categories")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sortBy", "id")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            then(categoryService).should().getActiveCategories(1, 5, "id", "desc");
        }

        @Test
        @DisplayName("returns empty page when no active categories exist")
        void getAll_noActiveCategories_returnsEmptyPage() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CategoryResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(categoryService.getActiveCategories(0, 10, "name", "asc")).willReturn(emptyPage);

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("only returns active categories — inactive categories are filtered out")
        void getAll_onlyActiveCategories() throws Exception {
            // This test verifies that the customer endpoint only calls getActiveCategories
            // which should filter out inactive categories at the service level
            Pageable pageable = PageRequest.of(0, 10);
            Page<CategoryResponse> page = new PageImpl<>(List.of(activeCategory), pageable, 1);
            given(categoryService.getActiveCategories(0, 10, "name", "asc")).willReturn(page);

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));

            // Verify that only getActiveCategories is called, not getAllCategories
            then(categoryService).should().getActiveCategories(0, 10, "name", "asc");
            then(categoryService).should(never()).getAllCategories(anyInt(), anyInt(), anyString(), anyString());
        }
    }

    // =========================================================================
    // GET /api/categories/{slug}/subcategories
    // =========================================================================

    @Nested
    @DisplayName("GET /api/categories/{slug}/subcategories")
    class GetSubcategoriesBySlug {

        @Test
        @DisplayName("returns 200 with active subcategories for category")
        void getSubcategoriesBySlug_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            given(subcategoryService.getActiveSubcategoriesByCategorySlug("electronics", 0, 10, "name", "asc"))
                    .willReturn(page);

            mockMvc.perform(get("/api/categories/electronics/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Smartphones"))
                    .andExpect(jsonPath("$.content[0].categoryName").value("Electronics"))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));
        }

        @Test
        @DisplayName("passes custom pagination params to service")
        void getSubcategoriesBySlug_customParams_passesParams() throws Exception {
            Pageable pageable = PageRequest.of(1, 5);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            given(subcategoryService.getActiveSubcategoriesByCategorySlug("electronics", 1, 5, "id", "desc"))
                    .willReturn(page);

            mockMvc.perform(get("/api/categories/electronics/subcategories")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sortBy", "id")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            then(subcategoryService).should()
                    .getActiveSubcategoriesByCategorySlug("electronics", 1, 5, "id", "desc");
        }

        @Test
        @DisplayName("returns empty page when category has no active subcategories")
        void getSubcategoriesBySlug_noSubcategories_returnsEmpty() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(subcategoryService.getActiveSubcategoriesByCategorySlug("empty-category", 0, 10, "name", "asc"))
                    .willReturn(emptyPage);

            mockMvc.perform(get("/api/categories/empty-category/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("returns 404 when category slug not found")
        void getSubcategoriesBySlug_categoryNotFound_returns404() throws Exception {
            given(subcategoryService.getActiveSubcategoriesByCategorySlug("unknown", 0, 10, "name", "asc"))
                    .willThrow(new EntityNotFoundException("Category not found with slug: unknown"));

            mockMvc.perform(get("/api/categories/unknown/subcategories"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("only returns active subcategories for active category")
        void getSubcategoriesBySlug_onlyActiveSubcategories() throws Exception {
            // This test verifies that the customer endpoint only calls getActiveSubcategoriesByCategorySlug
            // which should filter out inactive subcategories at the service level
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubcategoryResponse> page = new PageImpl<>(List.of(activeSubcategory), pageable, 1);
            given(subcategoryService.getActiveSubcategoriesByCategorySlug("electronics", 0, 10, "name", "asc"))
                    .willReturn(page);

            mockMvc.perform(get("/api/categories/electronics/subcategories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));

            // Verify that only getActiveSubcategoriesByCategorySlug is called
            then(subcategoryService).should()
                    .getActiveSubcategoriesByCategorySlug("electronics", 0, 10, "name", "asc");
            then(subcategoryService).should(never())
                    .getSubcategoriesByCategory(anyLong(), anyInt(), anyInt(), anyString(), anyString());
        }
    }
}
