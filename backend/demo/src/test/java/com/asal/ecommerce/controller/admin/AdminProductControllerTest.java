package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.exception.GlobalExceptionHandler;
import com.asal.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin ProductController — Mockito MockMvc Tests")
class ProductControllerTest {

    // ── Pure Mockito — no Spring context, no Security filter chain ─────────────
    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc     mockMvc;
    private ObjectMapper objectMapper;

    // ── Shared fixtures ────────────────────────────────────────────────────────
    private ProductResponse           sampleProduct;
    private ProductColorResponse      sampleColor;
    private ProductColorImageResponse sampleColorImage;

    @BeforeEach
    void setUp() {
        // Standalone MockMvc — no security, no filter chain, but with exception handler
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        sampleColorImage = new ProductColorImageResponse();
        sampleColorImage.setId(100L);
        sampleColorImage.setImageUrl("/uploads/products/colors/black1.jpg");
        sampleColorImage.setSortOrder(0);

        sampleColor = new ProductColorResponse();
        sampleColor.setId(10L);
        sampleColor.setColorName("Black");
        sampleColor.setColorHex("#000000");
        sampleColor.setStock(50);
        sampleColor.setImages(List.of(sampleColorImage));

        sampleProduct = new ProductResponse();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setSku("TESTPR-ABC123");
        sampleProduct.setDescription("A test product description");
        sampleProduct.setPrice(new BigDecimal("99.99"));
        sampleProduct.setOldPrice(new BigDecimal("129.99"));
        sampleProduct.setStatus("active");
        sampleProduct.setFeatured(false);
        sampleProduct.setExclusive(false);
        sampleProduct.setCategoryId(1L);
        sampleProduct.setCategoryName("Electronics");
        sampleProduct.setSubcategoryId(2L);
        sampleProduct.setSubcategoryName("Phones");
        sampleProduct.setBrandId(3L);
        sampleProduct.setBrandName("Samsung");
        sampleProduct.setImageUrl("/uploads/products/prime.jpg");
        sampleProduct.setHoverImageUrl("/uploads/products/hover.jpg");
        sampleProduct.setTotalStock(50);
        sampleProduct.setColors(List.of(sampleColor));
    }

    // =========================================================================
    // POST /api/admin/products
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/products")
    class CreateProductTests {

        @Test
        @DisplayName("should return 200 with full product body")
        void createProduct_validRequest_returns200WithBody() throws Exception {
            when(productService.createProduct(
                    any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(sampleProduct);

            mockMvc.perform(multipart("/api/admin/products")
                    .file(mockImage("primeImage", "prime.jpg"))
                    .file(mockImage("hoverImage", "hover.jpg"))
                    .param("name",        "Test Product")
                    .param("price",       "99.99")
                    .param("oldPrice",    "129.99")
                    .param("categoryId",  "1")
                    .param("subcategoryId","2")
                    .param("brandId",     "3")
                    .param("status",      "active")
                    .param("featured",    "false")
                    .param("exclusive",   "false")
                    .param("colorNames",  "Black")
                    .param("colorHexes",  "#000000")
                    .param("colorStocks", "50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TESTPR-ABC123"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.oldPrice").value(129.99))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.featured").value(false))
                .andExpect(jsonPath("$.exclusive").value(false))
                .andExpect(jsonPath("$.categoryName").value("Electronics"))
                .andExpect(jsonPath("$.subcategoryName").value("Phones"))
                .andExpect(jsonPath("$.brandName").value("Samsung"))
                .andExpect(jsonPath("$.imageUrl").value("/uploads/products/prime.jpg"))
                .andExpect(jsonPath("$.hoverImageUrl").value("/uploads/products/hover.jpg"))
                .andExpect(jsonPath("$.totalStock").value(50))
                .andExpect(jsonPath("$.colors.length()").value(1))
                .andExpect(jsonPath("$.colors[0].colorName").value("Black"))
                .andExpect(jsonPath("$.colors[0].colorHex").value("#000000"))
                .andExpect(jsonPath("$.colors[0].stock").value(50))
                .andExpect(jsonPath("$.colors[0].images[0].imageUrl")
                        .value("/uploads/products/colors/black1.jpg"));

            verify(productService, times(1)).createProduct(
                    any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("should return 200 with empty colors when none provided")
        void createProduct_noColors_returns200WithEmptyColors() throws Exception {
            ProductResponse noColors = buildResponse(r -> {
                r.setColors(List.of());
                r.setTotalStock(0);
            });
            when(productService.createProduct(
                    any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(noColors);

            mockMvc.perform(multipart("/api/admin/products")
                    .param("name",      "Simple Product")
                    .param("price",     "49.99")
                    .param("categoryId","1")
                    .param("status",    "active")
                    .param("featured",  "false")
                    .param("exclusive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colors").isArray())
                .andExpect(jsonPath("$.colors.length()").value(0))
                .andExpect(jsonPath("$.totalStock").value(0));
        }

        @Test
        @DisplayName("should return 200 with featured=true")
        void createProduct_featured_returnsProductWithFeaturedTrue() throws Exception {
            ProductResponse featured = buildResponse(r -> {
                r.setFeatured(true);
                r.setColors(List.of());
            });
            when(productService.createProduct(
                    any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(featured);

            mockMvc.perform(multipart("/api/admin/products")
                    .param("name",      "Featured Product")
                    .param("price",     "99.99")
                    .param("categoryId","1")
                    .param("status",    "active")
                    .param("featured",  "true")
                    .param("exclusive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featured").value(true));
        }

        @Test
        @DisplayName("should return 200 with multiple colors and correct totalStock")
        void createProduct_multipleColors_correctTotalStock() throws Exception {
            ProductColorResponse red = new ProductColorResponse();
            red.setId(20L); red.setColorName("Red");
            red.setColorHex("#FF0000"); red.setStock(30);
            red.setImages(List.of());

            ProductResponse twoColors = buildResponse(r -> {
                r.setColors(List.of(sampleColor, red)); // 50 + 30 = 80
                r.setTotalStock(80);
            });
            when(productService.createProduct(
                    any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(twoColors);

            mockMvc.perform(multipart("/api/admin/products")
                    .param("name",        "Multi-Color Product")
                    .param("price",       "99.99")
                    .param("categoryId",  "1")
                    .param("status",      "active")
                    .param("featured",    "false")
                    .param("exclusive",   "false")
                    .param("colorNames",  "Black")
                    .param("colorNames",  "Red")
                    .param("colorHexes",  "#000000")
                    .param("colorHexes",  "#FF0000")
                    .param("colorStocks", "50")
                    .param("colorStocks", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colors.length()").value(2))
                .andExpect(jsonPath("$.totalStock").value(80));
        }

        @Test
        @DisplayName("should return 400 when service throws RuntimeException")
        void createProduct_serviceThrows_returns400() throws Exception {
            when(productService.createProduct(
                    any(), any(), any(), any(), any(), any(), any()
            )).thenThrow(new RuntimeException("Category not found: 99"));

            mockMvc.perform(multipart("/api/admin/products")
                    .param("name",      "Bad Product")
                    .param("price",     "10.00")
                    .param("categoryId","99")
                    .param("status",    "active")
                    .param("featured",  "false")
                    .param("exclusive", "false"))
                .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /api/admin/products
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/products")
    class GetAllProductsTests {

        @Test
        @DisplayName("should return 200 with list of products")
        void getAllProducts_returns200WithList() throws Exception {
            when(productService.getAllProducts()).thenReturn(List.of(sampleProduct));

            mockMvc.perform(get("/api/admin/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].sku").value("TESTPR-ABC123"))
                .andExpect(jsonPath("$[0].totalStock").value(50))
                .andExpect(jsonPath("$[0].colors[0].colorName").value("Black"))
                .andExpect(jsonPath("$[0].colors[0].images[0].imageUrl")
                        .value("/uploads/products/colors/black1.jpg"));

            verify(productService).getAllProducts();
        }

        @Test
        @DisplayName("should return 200 with empty array when no products")
        void getAllProducts_empty_returns200WithEmptyArray() throws Exception {
            when(productService.getAllProducts()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("should return 200 with multiple products")
        void getAllProducts_multipleProducts_returnsAll() throws Exception {
            ProductResponse second = buildResponse(r -> {
                r.setId(2L);
                r.setName("Second Product");
                r.setSku("SEC-123456");
                r.setPrice(new BigDecimal("49.99"));
                r.setColors(List.of());
                r.setTotalStock(0);
            });
            when(productService.getAllProducts()).thenReturn(List.of(sampleProduct, second));

            mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[1].name").value("Second Product"));
        }
    }

    // =========================================================================
    // GET /api/admin/products/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/admin/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("should return 200 with full product detail")
        void getProduct_found_returns200() throws Exception {
            when(productService.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(get("/api/admin/products/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TESTPR-ABC123"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.categoryName").value("Electronics"))
                .andExpect(jsonPath("$.subcategoryName").value("Phones"))
                .andExpect(jsonPath("$.brandName").value("Samsung"))
                .andExpect(jsonPath("$.imageUrl").value("/uploads/products/prime.jpg"))
                .andExpect(jsonPath("$.hoverImageUrl").value("/uploads/products/hover.jpg"))
                .andExpect(jsonPath("$.totalStock").value(50))
                .andExpect(jsonPath("$.colors.length()").value(1));

            verify(productService).getProductById(1L);
        }

        @Test
        @DisplayName("should call service with the correct id from path")
        void getProduct_callsServiceWithCorrectId() throws Exception {
            when(productService.getProductById(5L)).thenReturn(sampleProduct);

            mockMvc.perform(get("/api/admin/products/5"));

            verify(productService).getProductById(5L);
            verify(productService, never()).getProductById(1L);
        }

        @Test
        @DisplayName("should return 400 when product not found")
        void getProduct_notFound_returns400() throws Exception {
            when(productService.getProductById(99L))
                    .thenThrow(new RuntimeException("Product not found: 99"));

            mockMvc.perform(get("/api/admin/products/99"))
                .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // PUT /api/admin/products/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/admin/products/{id}")
    class UpdateProductTests {

        @Test
        @DisplayName("should return 200 with updated product fields")
        void updateProduct_validRequest_returns200() throws Exception {
            ProductResponse updated = buildResponse(r -> {
                r.setName("Updated Name");
                r.setPrice(new BigDecimal("149.99"));
                r.setStatus("inactive");
                r.setColors(List.of(sampleColor));
                r.setTotalStock(50);
            });
            when(productService.updateProduct(eq(1L), any(), any(), any()))
                    .thenReturn(updated);

            mockMvc.perform(multipart("/api/admin/products/1")
                    .param("name",      "Updated Name")
                    .param("price",     "149.99")
                    .param("status",    "inactive")
                    .param("featured",  "false")
                    .param("exclusive", "false")
                    .with(req -> { req.setMethod("PUT"); return req; }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.status").value("inactive"));

            verify(productService).updateProduct(eq(1L), any(), any(), any());
        }

        @Test
        @DisplayName("should return 200 with new imageUrl when primeImage uploaded")
        void updateProduct_withNewPrimeImage_returns200() throws Exception {
            ProductResponse withNewImage = buildResponse(r -> {
                r.setImageUrl("/uploads/products/new-prime.jpg");
                r.setColors(List.of());
                r.setTotalStock(0);
            });
            when(productService.updateProduct(eq(1L), any(), any(), any()))
                    .thenReturn(withNewImage);

            mockMvc.perform(multipart("/api/admin/products/1")
                    .file(mockImage("primeImage", "new-prime.jpg"))
                    .param("featured",  "false")
                    .param("exclusive", "false")
                    .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/uploads/products/new-prime.jpg"));
        }

        @Test
        @DisplayName("should return 400 when product to update not found")
        void updateProduct_notFound_returns400() throws Exception {
            when(productService.updateProduct(eq(99L), any(), any(), any()))
                    .thenThrow(new RuntimeException("Product not found: 99"));

            mockMvc.perform(multipart("/api/admin/products/99")
                    .param("name",      "X")
                    .param("featured",  "false")
                    .param("exclusive", "false")
                    .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // POST /api/admin/products/{id}/colors
    // =========================================================================

    @Nested
    @DisplayName("POST /api/admin/products/{id}/colors")
    class AddColorTests {

        @Test
        @DisplayName("should return 200 with updated product including new color")
        void addColor_validRequest_returns200() throws Exception {
            ProductColorResponse newColor = new ProductColorResponse();
            newColor.setId(20L); newColor.setColorName("Red");
            newColor.setColorHex("#FF0000"); newColor.setStock(25);
            newColor.setImages(List.of());

            ProductResponse withNewColor = buildResponse(r -> {
                r.setColors(List.of(sampleColor, newColor));
                r.setTotalStock(75);
            });
            when(productService.addColorToProduct(eq(1L), eq("Red"), eq("#FF0000"), eq(25), any()))
                    .thenReturn(withNewColor);

            mockMvc.perform(multipart("/api/admin/products/1/colors")
                    .param("colorName", "Red")
                    .param("colorHex",  "#FF0000")
                    .param("stock",     "25"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colors.length()").value(2))
                .andExpect(jsonPath("$.totalStock").value(75))
                .andExpect(jsonPath("$.colors[1].colorName").value("Red"))
                .andExpect(jsonPath("$.colors[1].stock").value(25));

            verify(productService).addColorToProduct(eq(1L), eq("Red"), eq("#FF0000"), eq(25), any());
        }

        @Test
        @DisplayName("should return 200 with sub-images in color")
        void addColor_withSubImages_imagesSavedInColor() throws Exception {
            ProductColorImageResponse img = new ProductColorImageResponse();
            img.setId(200L);
            img.setImageUrl("/uploads/products/colors/blue1.jpg");
            img.setSortOrder(0);

            ProductColorResponse blue = new ProductColorResponse();
            blue.setId(20L); blue.setColorName("Blue");
            blue.setColorHex("#0000FF"); blue.setStock(10);
            blue.setImages(List.of(img));

            ProductResponse withBlue = buildResponse(r -> {
                r.setColors(List.of(blue));
                r.setTotalStock(10);
            });
            when(productService.addColorToProduct(eq(1L), any(), any(), anyInt(), any()))
                    .thenReturn(withBlue);

            mockMvc.perform(multipart("/api/admin/products/1/colors")
                    .file(mockImage("images", "blue1.jpg"))
                    .param("colorName", "Blue")
                    .param("colorHex",  "#0000FF")
                    .param("stock",     "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colors[0].images.length()").value(1))
                .andExpect(jsonPath("$.colors[0].images[0].imageUrl")
                        .value("/uploads/products/colors/blue1.jpg"));
        }

        @Test
        @DisplayName("should return 400 when product not found")
        void addColor_productNotFound_returns400() throws Exception {
            when(productService.addColorToProduct(eq(99L), any(), any(), anyInt(), any()))
                    .thenThrow(new RuntimeException("Product not found: 99"));

            mockMvc.perform(multipart("/api/admin/products/99/colors")
                    .param("colorName", "Green")
                    .param("colorHex",  "#00FF00")
                    .param("stock",     "5"))
                .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // PATCH /api/admin/products/{id}/colors/{colorId}/stock
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/admin/products/{id}/colors/{colorId}/stock")
    class UpdateColorStockTests {

        @Test
        @DisplayName("should return 200 with updated stock and totalStock")
        void updateColorStock_validRequest_returns200() throws Exception {
            ProductColorResponse updatedColor = new ProductColorResponse();
            updatedColor.setId(10L); updatedColor.setColorName("Black");
            updatedColor.setStock(100); updatedColor.setImages(List.of());

            ProductResponse updated = buildResponse(r -> {
                r.setColors(List.of(updatedColor));
                r.setTotalStock(100);
            });
            when(productService.updateColorStock(1L, 10L, 100)).thenReturn(updated);

            mockMvc.perform(patch("/api/admin/products/1/colors/10/stock")
                    .param("stock", "100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(100))
                .andExpect(jsonPath("$.colors[0].stock").value(100));

            verify(productService).updateColorStock(1L, 10L, 100);
        }

        @Test
        @DisplayName("should return 200 when stock set to zero")
        void updateColorStock_toZero_returns200() throws Exception {
            ProductColorResponse zeroColor = new ProductColorResponse();
            zeroColor.setId(10L); zeroColor.setStock(0); zeroColor.setImages(List.of());

            ProductResponse updated = buildResponse(r -> {
                r.setColors(List.of(zeroColor));
                r.setTotalStock(0);
            });
            when(productService.updateColorStock(1L, 10L, 0)).thenReturn(updated);

            mockMvc.perform(patch("/api/admin/products/1/colors/10/stock")
                    .param("stock", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(0))
                .andExpect(jsonPath("$.colors[0].stock").value(0));
        }

        @Test
        @DisplayName("should return 400 when color not found")
        void updateColorStock_colorNotFound_returns400() throws Exception {
            when(productService.updateColorStock(1L, 99L, 50))
                    .thenThrow(new RuntimeException("Color not found: 99"));

            mockMvc.perform(patch("/api/admin/products/1/colors/99/stock")
                    .param("stock", "50"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when color does not belong to product")
        void updateColorStock_wrongProduct_returns400() throws Exception {
            when(productService.updateColorStock(2L, 10L, 50))
                    .thenThrow(new RuntimeException("Color does not belong to this product"));

            mockMvc.perform(patch("/api/admin/products/2/colors/10/stock")
                    .param("stock", "50"))
                .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // DELETE /api/admin/products/{id}/colors/{colorId}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/admin/products/{id}/colors/{colorId}")
    class DeleteColorTests {

        @Test
        @DisplayName("should return 200 with empty colors after delete")
        void deleteColor_validRequest_returns200() throws Exception {
            ProductResponse withoutColor = buildResponse(r -> {
                r.setColors(List.of());
                r.setTotalStock(0);
            });
            when(productService.deleteColor(1L, 10L)).thenReturn(withoutColor);

            mockMvc.perform(delete("/api/admin/products/1/colors/10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colors").isArray())
                .andExpect(jsonPath("$.colors.length()").value(0))
                .andExpect(jsonPath("$.totalStock").value(0));

            verify(productService).deleteColor(1L, 10L);
        }

        @Test
        @DisplayName("should call deleteColor with correct ids from path")
        void deleteColor_callsServiceWithCorrectIds() throws Exception {
            when(productService.deleteColor(3L, 7L)).thenReturn(sampleProduct);

            mockMvc.perform(delete("/api/admin/products/3/colors/7"));

            verify(productService).deleteColor(3L, 7L);
            verify(productService, never()).deleteColor(1L, 10L);
        }

        @Test
        @DisplayName("should return 400 when color not found")
        void deleteColor_notFound_returns400() throws Exception {
            when(productService.deleteColor(1L, 99L))
                    .thenThrow(new RuntimeException("Color not found: 99"));

            mockMvc.perform(delete("/api/admin/products/1/colors/99"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when color does not belong to product")
        void deleteColor_wrongProduct_returns400() throws Exception {
            when(productService.deleteColor(2L, 10L))
                    .thenThrow(new RuntimeException("Color does not belong to this product"));

            mockMvc.perform(delete("/api/admin/products/2/colors/10"))
                .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // DELETE /api/admin/products/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/admin/products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("should return 204 No Content on successful delete")
        void deleteProduct_validId_returns204() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc.perform(delete("/api/admin/products/1"))
                .andDo(print())
                .andExpect(status().isNoContent());

            verify(productService).deleteProduct(1L);
        }

        @Test
        @DisplayName("should call deleteProduct with correct id from path")
        void deleteProduct_callsServiceWithCorrectId() throws Exception {
            doNothing().when(productService).deleteProduct(5L);

            mockMvc.perform(delete("/api/admin/products/5"));

            verify(productService).deleteProduct(5L);
            verify(productService, never()).deleteProduct(1L);
        }

        @Test
        @DisplayName("should return 400 when product not found")
        void deleteProduct_notFound_returns400() throws Exception {
            doThrow(new RuntimeException("Product not found: 99"))
                    .when(productService).deleteProduct(99L);

            mockMvc.perform(delete("/api/admin/products/99"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should call service only once even for products with colors and images")
        void deleteProduct_withColors_callsServiceOnce() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc.perform(delete("/api/admin/products/1"))
                .andExpect(status().isNoContent());

            // cascade deletion is handled inside service — controller calls it once
            verify(productService, times(1)).deleteProduct(1L);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private MockMultipartFile mockImage(String paramName, String filename) {
        return new MockMultipartFile(
                paramName, filename, "image/jpeg", "fake-image-content".getBytes()
        );
    }

    private ProductResponse buildResponse(Consumer<ProductResponse> customizer) {
        ProductResponse r = new ProductResponse();
        r.setId(1L);
        r.setName("Test Product");
        r.setSku("TESTPR-ABC123");
        r.setDescription("A test product description");
        r.setPrice(new BigDecimal("99.99"));
        r.setStatus("active");
        r.setFeatured(false);
        r.setExclusive(false);
        r.setCategoryId(1L);
        r.setCategoryName("Electronics");
        r.setTotalStock(50);
        r.setColors(List.of(sampleColor));
        customizer.accept(r);
        return r;
    }
}