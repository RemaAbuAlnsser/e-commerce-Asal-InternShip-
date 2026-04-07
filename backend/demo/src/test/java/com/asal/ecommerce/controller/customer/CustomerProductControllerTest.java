// package com.asal.ecommerce.controller.customer;

// import com.asal.ecommerce.dto.ProductResponse;
// import com.asal.ecommerce.exception.GlobalExceptionHandler;
// import com.asal.ecommerce.service.ProductService;
// import jakarta.persistence.EntityNotFoundException;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// import java.math.BigDecimal;
// import java.util.List;

// import static org.hamcrest.Matchers.hasSize;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.BDDMockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @ExtendWith(MockitoExtension.class)
// @DisplayName("Customer ProductController")
// class CustomerProductControllerTest {

//     @Mock ProductService productService;

//     @InjectMocks
//     ProductController productController;

//     MockMvc mockMvc;

//     private ProductResponse activeProduct;

//     @BeforeEach
//     void setUp() {
//         mockMvc = MockMvcBuilders
//                 .standaloneSetup(productController)
//                 .setControllerAdvice(new GlobalExceptionHandler())
//                 .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
//                 .build();

//         activeProduct = new ProductResponse();
//         activeProduct.setId(1L);
//         activeProduct.setName("Galaxy S24");
//         activeProduct.setSku("SAM-S24-001");
//         activeProduct.setPrice(new BigDecimal("999.99"));
//         activeProduct.setStock(50);
//         activeProduct.setStatus("active");
//         activeProduct.setCategoryId(1L);
//         activeProduct.setCategoryName("Electronics");
//     }

//     // =========================================================================
//     // GET /api/products/{id}
//     // =========================================================================

//     @Nested
//     @DisplayName("GET /api/products/{id}")
//     class GetById {

//         @Test
//         @DisplayName("returns 200 with product when found")
//         void getById_found_returns200() throws Exception {
//             given(productService.getById(1L)).willReturn(activeProduct);

//             mockMvc.perform(get("/api/products/1"))
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.id").value(1))
//                     .andExpect(jsonPath("$.name").value("Galaxy S24"))
//                     .andExpect(jsonPath("$.status").value("active"));
//         }

//         @Test
//         @DisplayName("returns 404 when product not found")
//         void getById_notFound_returns404() throws Exception {
//             given(productService.getById(99L))
//                     .willThrow(new EntityNotFoundException("Product not found with id: 99"));

//             mockMvc.perform(get("/api/products/99"))
//                     .andExpect(status().isNotFound());
//         }
//     }

//     // =========================================================================
//     // GET /api/products/sku/{sku}
//     // =========================================================================

//     @Nested
//     @DisplayName("GET /api/products/sku/{sku}")
//     class GetBySku {

//         @Test
//         @DisplayName("returns 200 with product when SKU matches")
//         void getBySku_found_returns200() throws Exception {
//             given(productService.getBySku("SAM-S24-001")).willReturn(activeProduct);

//             mockMvc.perform(get("/api/products/sku/SAM-S24-001"))
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.sku").value("SAM-S24-001"));
//         }

//         @Test
//         @DisplayName("returns 404 when SKU not found")
//         void getBySku_notFound_returns404() throws Exception {
//             given(productService.getBySku("UNKNOWN"))
//                     .willThrow(new EntityNotFoundException("Product not found with SKU: UNKNOWN"));

//             mockMvc.perform(get("/api/products/sku/UNKNOWN"))
//                     .andExpect(status().isNotFound());
//         }
//     }

//     // =========================================================================
//     // GET /api/products  — always hardcodes status=active
//     // =========================================================================

//     @Nested
//     @DisplayName("GET /api/products")
//     class GetAll {

//         @Test
//         @DisplayName("always passes status=active to service — never exposes inactive products")
//         void getAll_alwaysFiltersActiveStatus() throws Exception {
//             Pageable pageable = PageRequest.of(0, 20);
//             Page<ProductResponse> page = new PageImpl<>(List.of(activeProduct), pageable, 1);
//             given(productService.getAll(isNull(), isNull(), isNull(), eq("active"), isNull(), isNull(), any(Pageable.class)))
//                     .willReturn(page);

//             mockMvc.perform(get("/api/products"))
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.content", hasSize(1)));

//             then(productService).should()
//                     .getAll(isNull(), isNull(), isNull(), eq("active"), isNull(), isNull(), any(Pageable.class));
//         }

//         @Test
//         @DisplayName("passes optional filter params correctly")
//         void getAll_withOptionalFilters_passesParams() throws Exception {
//             Pageable pageable = PageRequest.of(0, 20);
//             Page<ProductResponse> page = new PageImpl<>(List.of(activeProduct), pageable, 1);
//             given(productService.getAll(eq(1L), eq(2L), eq(3L), eq("active"), eq(true), eq(false), any(Pageable.class)))
//                     .willReturn(page);

//             mockMvc.perform(get("/api/products")
//                             .param("categoryId",    "1")
//                             .param("subcategoryId", "2")
//                             .param("brandId",       "3")
//                             .param("isFeatured",    "true")
//                             .param("isExclusive",   "false"))
//                     .andExpect(status().isOk());

//             then(productService).should()
//                     .getAll(eq(1L), eq(2L), eq(3L), eq("active"), eq(true), eq(false), any(Pageable.class));
//         }

//         @Test
//         @DisplayName("status param is not exposed — service always receives active")
//         void getAll_statusParamNotExposed() throws Exception {
//             Pageable pageable = PageRequest.of(0, 20);
//             Page<ProductResponse> page = new PageImpl<>(List.of(activeProduct), pageable, 1);
//             given(productService.getAll(any(), any(), any(), eq("active"), any(), any(), any(Pageable.class)))
//                     .willReturn(page);

//             // Even if a client manually appends ?status=inactive, it has no effect
//             mockMvc.perform(get("/api/products").param("status", "inactive"))
//                     .andExpect(status().isOk());

//             then(productService).should()
//                     .getAll(any(), any(), any(), eq("active"), any(), any(), any(Pageable.class));
//         }

//         @Test
//         @DisplayName("returns empty page when no active products exist")
//         void getAll_noActiveProducts_returnsEmptyPage() throws Exception {
//             Pageable pageable = PageRequest.of(0, 20);
//             Page<ProductResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
//             given(productService.getAll(any(), any(), any(), eq("active"), any(), any(), any(Pageable.class)))
//                     .willReturn(emptyPage);

//             mockMvc.perform(get("/api/products"))
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.content", hasSize(0)));
//         }
//     }

//     // =========================================================================
//     // GET /api/products/search
//     // =========================================================================

//     @Nested
//     @DisplayName("GET /api/products/search")
//     class Search {

//         @Test
//         @DisplayName("returns 200 with matching products")
//         void search_match_returns200() throws Exception {
//             Pageable pageable = PageRequest.of(0, 20);
//             Page<ProductResponse> page = new PageImpl<>(List.of(activeProduct), pageable, 1);
//             given(productService.search(eq("galaxy"), any(Pageable.class))).willReturn(page);

//             mockMvc.perform(get("/api/products/search").param("keyword", "galaxy"))
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.content", hasSize(1)))
//                     .andExpect(jsonPath("$.content[0].name").value("Galaxy S24"));
//         }

//         @Test
//         @DisplayName("returns empty page when keyword matches nothing")
//         void search_noMatch_returnsEmpty() throws Exception {
//             Pageable pageable = PageRequest.of(0, 20);
//             Page<ProductResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
//             given(productService.search(eq("nokia"), any(Pageable.class))).willReturn(emptyPage);

//             mockMvc.perform(get("/api/products/search").param("keyword", "nokia"))
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.content", hasSize(0)));
//         }
//     }
// }