package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.ProductCreateRequest;
import com.asal.ecommerce.dto.ProductUpdateRequest;
import com.asal.ecommerce.dto.ProductColorImageResponse;
import com.asal.ecommerce.dto.ProductColorResponse;
import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.model.*;
import com.asal.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository           productRepo;
    private final ProductColorRepository      colorRepo;
    private final ProductColorImageRepository colorImageRepo;
    private final CategoryRepository          categoryRepo;
    private final SubcategoryRepository       subcategoryRepo;
    private final BrandRepository             brandRepo;
    private final ImageUploadService          imageUploadService;

    // =========================================================================
    // ADMIN METHODS
    // =========================================================================

    @Transactional
    public ProductResponse createProduct(
            ProductCreateRequest      req,
            MultipartFile             primeImage,
            MultipartFile             hoverImage,
            List<String>              colorNames,
            List<String>              colorHexes,
            List<Integer>             colorStocks,
            List<List<MultipartFile>> colorImages
    ) throws IOException {

        Category category = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + req.getCategoryId()));

        Subcategory subcategory = null;
        if (req.getSubcategoryId() != null) {
            subcategory = subcategoryRepo.findById(req.getSubcategoryId())
                    .orElseThrow(() -> new RuntimeException("Subcategory not found: " + req.getSubcategoryId()));
        }

        Brand brand = null;
        if (req.getBrandId() != null) {
            brand = brandRepo.findById(req.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found: " + req.getBrandId()));
        }

        String primeUrl = null;
        String hoverUrl = null;
        if (primeImage != null && !primeImage.isEmpty()) {
            primeUrl = imageUploadService.uploadProductImage(primeImage);
        }
        if (hoverImage != null && !hoverImage.isEmpty()) {
            hoverUrl = imageUploadService.uploadProductHoverImage(hoverImage);
        }

        Product product = Product.builder()
                .name(req.getName())
                .sku(generateSku(req.getName()))
                .description(req.getDescription())
                .price(req.getPrice())
                .oldPrice(req.getOldPrice())
                .status(req.getStatus() != null ? req.getStatus() : "active")
                .featured(req.isFeatured())
                .exclusive(req.isExclusive())
                .category(category)
                .subcategory(subcategory)
                .brand(brand)
                .imageUrl(primeUrl)
                .hoverImageUrl(hoverUrl)
                .build();

        if (colorNames != null && !colorNames.isEmpty()) {
            for (int i = 0; i < colorNames.size(); i++) {

                ProductColor color = ProductColor.builder()
                        .product(product)
                        .colorName(colorNames.get(i))
                        .colorHex(safeGet(colorHexes, i, "#000000"))
                        .stock(safeGetInt(colorStocks, i, 0))
                        .build();

                List<MultipartFile> imgs = (colorImages != null && colorImages.size() > i)
                        ? colorImages.get(i) : Collections.emptyList();

                int order = 0;
                for (MultipartFile img : imgs) {
                    if (img != null && !img.isEmpty()) {
                        String url = imageUploadService.uploadColorImage(img);
                        color.getImages().add(
                                ProductColorImage.builder()
                                        .productColor(color)
                                        .imageUrl(url)
                                        .sortOrder(order++)
                                        .build()
                        );
                    }
                }
                product.getColors().add(color);
            }
        }

        return mapToResponse(productRepo.save(product));
    }

    // Admin — list all (no pagination)
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Admin — get single product by id (no status check)
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return mapToResponse(
                productRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + id))
        );
    }

    @Transactional
    public ProductResponse updateProduct(
            Long                 id,
            ProductUpdateRequest  req,
            MultipartFile        primeImage,
            MultipartFile        hoverImage
    ) throws IOException {

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (req.getName()        != null) product.setName(req.getName());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getPrice()       != null) product.setPrice(req.getPrice());
        if (req.getOldPrice()    != null) product.setOldPrice(req.getOldPrice());
        if (req.getStatus()      != null) product.setStatus(req.getStatus());
        product.setFeatured(req.isFeatured());
        product.setExclusive(req.isExclusive());

        if (req.getCategoryId() != null) {
            product.setCategory(categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }
        if (req.getSubcategoryId() != null) {
            product.setSubcategory(subcategoryRepo.findById(req.getSubcategoryId())
                    .orElseThrow(() -> new RuntimeException("Subcategory not found")));
        }
        if (req.getBrandId() != null) {
            product.setBrand(brandRepo.findById(req.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found")));
        }

        if (primeImage != null && !primeImage.isEmpty()) {
            imageUploadService.deleteImage(product.getImageUrl());
            product.setImageUrl(imageUploadService.uploadProductImage(primeImage));
        }
        if (hoverImage != null && !hoverImage.isEmpty()) {
            imageUploadService.deleteImage(product.getHoverImageUrl());
            product.setHoverImageUrl(imageUploadService.uploadProductHoverImage(hoverImage));
        }

        return mapToResponse(productRepo.save(product));
    }

    @Transactional
    public ProductResponse addColorToProduct(
            Long                productId,
            String              colorName,
            String              colorHex,
            int                 stock,
            List<MultipartFile> images
    ) throws IOException {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        ProductColor color = ProductColor.builder()
                .product(product)
                .colorName(colorName)
                .colorHex(colorHex)
                .stock(stock)
                .build();

        if (images != null) {
            int order = 0;
            for (MultipartFile img : images) {
                if (img != null && !img.isEmpty()) {
                    String url = imageUploadService.uploadColorImage(img);
                    color.getImages().add(
                            ProductColorImage.builder()
                                    .productColor(color)
                                    .imageUrl(url)
                                    .sortOrder(order++)
                                    .build()
                    );
                }
            }
        }

        product.getColors().add(color);
        return mapToResponse(productRepo.save(product));
    }

    @Transactional
    public ProductResponse updateColorStock(Long productId, Long colorId, int newStock) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        ProductColor color = colorRepo.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found: " + colorId));

        if (!color.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Color does not belong to this product");
        }

        color.setStock(newStock);
        colorRepo.save(color);

        return mapToResponse(productRepo.findById(productId).orElseThrow());
    }

    @Transactional
    public ProductResponse deleteColor(Long productId, Long colorId) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        ProductColor color = colorRepo.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found: " + colorId));

        if (!color.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Color does not belong to this product");
        }

        color.getImages().forEach(img -> imageUploadService.deleteImage(img.getImageUrl()));
        product.getColors().remove(color);
        colorRepo.delete(color);

        return mapToResponse(productRepo.findById(productId).orElseThrow());
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        imageUploadService.deleteImage(product.getImageUrl());
        imageUploadService.deleteImage(product.getHoverImageUrl());
        product.getColors().forEach(color ->
                color.getImages().forEach(img -> imageUploadService.deleteImage(img.getImageUrl()))
        );

        productRepo.delete(product);
    }

    // =========================================================================
    // CUSTOMER METHODS
    // =========================================================================

    // Customer — get single active product by id
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        if (!"active".equals(product.getStatus())) {
            throw new RuntimeException("Product not available");
        }
        return mapToResponse(product);
    }

    // Customer — get single active product by SKU
    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        Product product = productRepo.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        if (!"active".equals(product.getStatus())) {
            throw new RuntimeException("Product not available");
        }
        return mapToResponse(product);
    }

    // Customer — filtered + paginated listing
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(
            Long     categoryId,
            Long     subcategoryId,
            Long     brandId,
            String   status,
            Boolean  isFeatured,
            Boolean  isExclusive,
            Pageable pageable
    ) {
        return productRepo
                .findAllWithFilters(status, categoryId, subcategoryId, brandId, isFeatured, isExclusive, pageable)
                .map(this::mapToResponse);
    }

    // Customer — keyword search
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Pageable pageable) {
        return productRepo
                .searchByKeyword(keyword, pageable)
                .map(this::mapToResponse);
    }

    // =========================================================================
    // MAPPER
    // =========================================================================

    private ProductResponse mapToResponse(Product p) {
        ProductResponse res = new ProductResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        res.setSku(p.getSku());
        res.setDescription(p.getDescription());
        res.setPrice(p.getPrice());
        res.setOldPrice(p.getOldPrice());
        res.setStatus(p.getStatus());
        res.setFeatured(p.isFeatured());
        res.setExclusive(p.isExclusive());
        res.setImageUrl(p.getImageUrl());
        res.setHoverImageUrl(p.getHoverImageUrl());
        res.setTotalStock(p.getTotalStock());
        res.setCreatedAt(p.getCreatedAt());
        res.setUpdatedAt(p.getUpdatedAt());

        if (p.getCategory() != null) {
            res.setCategoryId(p.getCategory().getId());
            res.setCategoryName(p.getCategory().getName());
        }
        if (p.getSubcategory() != null) {
            res.setSubcategoryId(p.getSubcategory().getId());
            res.setSubcategoryName(p.getSubcategory().getName());
        }
        if (p.getBrand() != null) {
            res.setBrandId(p.getBrand().getId());
            res.setBrandName(p.getBrand().getName());
        }

        List<ProductColorResponse> colorResponses = p.getColors()
                .stream()
                .map(c -> {
                    ProductColorResponse cr = new ProductColorResponse();
                    cr.setId(c.getId());
                    cr.setColorName(c.getColorName());
                    cr.setColorHex(c.getColorHex());
                    cr.setStock(c.getStock());

                    List<ProductColorImageResponse> imgList = c.getImages()
                            .stream()
                            .sorted(Comparator.comparingInt(ProductColorImage::getSortOrder))
                            .map(img -> {
                                ProductColorImageResponse ir = new ProductColorImageResponse();
                                ir.setId(img.getId());
                                ir.setImageUrl(img.getImageUrl());
                                ir.setSortOrder(img.getSortOrder());
                                return ir;
                            })
                            .collect(Collectors.toList());

                    cr.setImages(imgList);
                    return cr;
                })
                .collect(Collectors.toList());

        res.setColors(colorResponses);
        return res;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private String generateSku(String productName) {
        String base = productName.toUpperCase().replaceAll("[^A-Z0-9]", "");
        base = base.substring(0, Math.min(6, base.length()));
        return base + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private <T> T safeGet(List<T> list, int index, T defaultValue) {
        return (list != null && list.size() > index) ? list.get(index) : defaultValue;
    }

    private int safeGetInt(List<Integer> list, int index, int defaultValue) {
        return (list != null && list.size() > index && list.get(index) != null)
                ? list.get(index) : defaultValue;
    }
}