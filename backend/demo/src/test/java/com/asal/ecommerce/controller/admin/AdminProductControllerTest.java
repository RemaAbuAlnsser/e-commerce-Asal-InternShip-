package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.ProductCreateRequest;
import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.dto.ProductUpdateRequest;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.ImageUploadService;
import com.asal.ecommerce.service.ProductService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin ProductController")
class AdminProductControllerTest {

    @Mock ProductService     productService;
    @Mock ImageUploadService imageUploadService;

    @InjectMocks
    ProductController productController;

    MockMvc          mockMvc;
    ObjectMapper     objectMapper = new ObjectMapper();

    private ProductResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = new ProductResponse();
        sampleResponse.setId(1L);
        sampleResponse.setName("Galaxy S24");
        sampleResponse.setSku("SAM-S24-001");
        sampleResponse.setPrice(new BigDecimal("999.99"));
        sampleResponse.setStock(50);
        sampleResponse.setStatus("active");
        sampleResponse.setIsFeatured(false);
        sampleResponse.setIsExclusive(false);
        sampleResponse.setCategoryId(1L);
        sampleResponse.setCategoryName("Electronics");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ProductCreateRequest validCreateRequest() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Galaxy S24");
        req.setSku("SAM-S24-001");
        req.setPrice(new BigDecimal("999.99"));
        req.setStock(50);
        req.setStatus("active");
        req.setCategoryId(1L);
        return req;
    }

    private ProductUpdateRequest validUpdateRequest() {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("Galaxy S24 Ultra");
        req.setSku("SAM-S24-001");
        req.setPrice(new BigDecimal("1199.99"));
        req.setStock(30);
        req.setStatus("active");
        req.setCategoryId(1L);
        return req;
    }

    // =========================================================================
    // POST /api/admin/products
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/products")
    class CreateProduct {

        @Test
        @DisplayName("returns 201 with body on valid request")
        void create_valid_returns201() throws Exception {
            given(productService.create(any())).willReturn(sampleResponse);

            mockMvc.perform(post("/api/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.sku").value("SAM-S24-001"))
                    .andExpect(jsonPath("$.categoryName").value("Electronics"));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            ProductCreateRequest req = validCreateRequest();
            req.setName("");

            mockMvc.perform(post("/api/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when SKU is blank")
        void create_blankSku_returns400() throws Exception {
            ProductCreateRequest req = validCreateRequest();
            req.setSku("");

            mockMvc.perform(post("/api/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when price is null")
        void create_nullPrice_returns400() throws Exception {
            ProductCreateRequest req = validCreateRequest();
            req.setPrice(null);

            mockMvc.perform(post("/api/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when price is negative")
        void create_negativePrice_returns400() throws Exception {
            ProductCreateRequest req = validCreateRequest();
            req.setPrice(new BigDecimal("-1.00"));

            mockMvc.perform(post("/api/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when stock is negative")
        void create_negativeStock_returns400() throws Exception {
            ProductCreateRequest req = validCreateRequest();
            req.setStock(-5);

            mockMvc.perform(post("/api/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when categoryId is null")
        void create_noCategoryId_returns400() throws Exception {
            ProductCreateRequest req = validCreateRequest();
            req.setCategoryId(null);

            mockMvc.perform(post("/api/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /api/admin/products/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/products/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with product when found")
        void getById_found_returns200() throws Exception {
            given(productService.getById(1L)).willReturn(sampleResponse);

            mockMvc.perform(get("/api/admin/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Galaxy S24"));
        }

        @Test
        @DisplayName("returns 404 when product not found")
        void getById_notFound_returns404() throws Exception {
            given(productService.getById(99L))
                    .willThrow(new EntityNotFoundException("Product not found with id: 99"));

            mockMvc.perform(get("/api/admin/products/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/admin/products
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/products")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paged results when no filters given")
        void getAll_noFilters_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ProductResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(productService.getAll(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                    .willReturn(page);

            mockMvc.perform(get("/api/admin/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].sku").value("SAM-S24-001"));
        }

        @Test
        @DisplayName("passes all filter params to the service")
        void getAll_withFilters_passesParams() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ProductResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(productService.getAll(eq(1L), eq(2L), eq(3L), eq("active"), eq(true), eq(false), any(Pageable.class)))
                    .willReturn(page);

            mockMvc.perform(get("/api/admin/products")
                            .param("categoryId",    "1")
                            .param("subcategoryId", "2")
                            .param("brandId",       "3")
                            .param("status",        "active")
                            .param("isFeatured",    "true")
                            .param("isExclusive",   "false"))
                    .andExpect(status().isOk());

            then(productService).should()
                    .getAll(eq(1L), eq(2L), eq(3L), eq("active"), eq(true), eq(false), any(Pageable.class));
        }
    }

    // =========================================================================
    // GET /api/admin/products/search
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/products/search")
    class Search {

        @Test
        @DisplayName("returns 200 with matching products")
        void search_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ProductResponse> page = new PageImpl<>(List.of(sampleResponse), pageable, 1);
            given(productService.search(eq("galaxy"), any(Pageable.class))).willReturn(page);

            mockMvc.perform(get("/api/admin/products/search").param("keyword", "galaxy"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }
    }

    // =========================================================================
    // PUT /api/admin/products/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/admin/products/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with updated product")
        void update_valid_returns200() throws Exception {
            sampleResponse.setName("Galaxy S24 Ultra");
            given(productService.update(eq(1L), any())).willReturn(sampleResponse);

            mockMvc.perform(put("/api/admin/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Galaxy S24 Ultra"));
        }

        @Test
        @DisplayName("returns 404 when product to update does not exist")
        void update_notFound_returns404() throws Exception {
            given(productService.update(eq(99L), any()))
                    .willThrow(new EntityNotFoundException("Product not found with id: 99"));

            mockMvc.perform(put("/api/admin/products/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 when update body fails validation")
        void update_invalidBody_returns400() throws Exception {
            ProductUpdateRequest req = validUpdateRequest();
            req.setName("");

            mockMvc.perform(put("/api/admin/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // DELETE /api/admin/products/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/admin/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("returns 204 on successful deletion")
        void delete_success_returns204() throws Exception {
            willDoNothing().given(productService).delete(1L);

            mockMvc.perform(delete("/api/admin/products/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when product does not exist")
        void delete_notFound_returns404() throws Exception {
            willThrow(new EntityNotFoundException("Product not found with id: 99"))
                    .given(productService).delete(99L);

            mockMvc.perform(delete("/api/admin/products/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // POST /api/admin/products/{id}/image
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/products/{id}/image")
    class UploadImage {

        @Test
        @DisplayName("returns 200 with imageUrl on valid upload")
        void uploadImage_valid_returns200() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "phone.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-bytes".getBytes());

            given(imageUploadService.uploadProductImage(any())).willReturn("/uploads/products/uuid.jpg");
            sampleResponse.setImageUrl("/uploads/products/uuid.jpg");
            given(productService.updateImageUrl(eq(1L), any())).willReturn(sampleResponse);

            mockMvc.perform(multipart("/api/admin/products/1/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("/uploads/products/uuid.jpg"));
        }

        @Test
        @DisplayName("returns 404 when product does not exist")
        void uploadImage_productNotFound_returns404() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "phone.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes());

            given(imageUploadService.uploadProductImage(any())).willReturn("/uploads/products/uuid.jpg");
            given(productService.updateImageUrl(eq(99L), any()))
                    .willThrow(new EntityNotFoundException("Product not found with id: 99"));

            mockMvc.perform(multipart("/api/admin/products/99/image").file(file))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // POST /api/admin/products/{id}/hover-image
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/products/{id}/hover-image")
    class UploadHoverImage {

        @Test
        @DisplayName("returns 200 with hoverImageUrl on valid upload")
        void uploadHoverImage_valid_returns200() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "hover.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes());

            given(imageUploadService.uploadProductHoverImage(any())).willReturn("/uploads/products/hover.jpg");
            sampleResponse.setHoverImageUrl("/uploads/products/hover.jpg");
            given(productService.updateHoverImageUrl(eq(1L), any())).willReturn(sampleResponse);

            mockMvc.perform(multipart("/api/admin/products/1/hover-image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hoverImageUrl").value("/uploads/products/hover.jpg"));
        }
    }

    // =========================================================================
    // DELETE /api/admin/products/{id}/image
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/admin/products/{id}/image")
    class DeleteImage {

        @Test
        @DisplayName("returns 204 on success")
        void deleteImage_returns204() throws Exception {
            willDoNothing().given(productService).deleteImageUrl(1L);

            mockMvc.perform(delete("/api/admin/products/1/image"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when product does not exist")
        void deleteImage_notFound_returns404() throws Exception {
            willThrow(new EntityNotFoundException("not found"))
                    .given(productService).deleteImageUrl(99L);

            mockMvc.perform(delete("/api/admin/products/99/image"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // DELETE /api/admin/products/{id}/hover-image
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/admin/products/{id}/hover-image")
    class DeleteHoverImage {

        @Test
        @DisplayName("returns 204 on success")
        void deleteHoverImage_returns204() throws Exception {
            willDoNothing().given(productService).deleteHoverImageUrl(1L);

            mockMvc.perform(delete("/api/admin/products/1/hover-image"))
                    .andExpect(status().isNoContent());
        }
    }
}