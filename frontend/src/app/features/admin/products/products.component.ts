import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService, Product, ProductCreateRequest, ProductUpdateRequest, PageResponse } from '../../../services/product.service';
import { CategoryService, Category, Subcategory } from '../../../services/category.service';
import { BrandService, Brand } from '../../../services/brand.service';
import { ImageUploadService } from '../../../services/image-upload.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];
  subcategories: Subcategory[] = [];
  brands: Brand[] = [];
  selectedProduct: Product | null = null;
  
  // Modal states
  showProductModal = false;
  showDeleteModal = false;
  showImageModal = false;
  
  // Edit mode
  isEditingProduct = false;
  
  // Forms
  productForm: FormGroup;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  totalProducts = 0;
  
  // Loading states
  loading = false;
  submitting = false;
  
  // Search and filters
  searchTerm = '';
  selectedCategoryId: number | null = null;
  selectedSubcategoryId: number | null = null;
  selectedBrandId: number | null = null;
  selectedStatus = '';
  showFeaturedOnly = false;
  showExclusiveOnly = false;
  
  // Delete confirmation
  itemToDelete: Product | null = null;

  // Image upload
  productImageFile: File | null = null;
  productImagePreview: string | null = null;
  hoverImageFile: File | null = null;
  hoverImagePreview: string | null = null;

  // Product statuses
  productStatuses = [
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
    { value: 'draft', label: 'Draft' },
    { value: 'discontinued', label: 'Discontinued' }
  ];

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private brandService: BrandService,
    private imageUploadService: ImageUploadService,
    private fb: FormBuilder
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      sku: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      oldPrice: [0, [Validators.min(0)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      status: ['active', Validators.required],
      isFeatured: [false],
      isExclusive: [false],
      categoryId: [null, Validators.required],
      subcategoryId: [null],
      brandId: [null, Validators.required],
      imageUrl: [''],
      hoverImageUrl: ['']
    });
  }

  ngOnInit() {
    this.loadProducts();
    this.loadCategories();
    this.loadBrands();
    this.loadProductCount();
  }

  loadProducts() {
    this.loading = true;
    
    if (this.searchTerm.trim()) {
      this.productService.searchProducts(
        this.searchTerm,
        this.currentPage,
        this.pageSize,
        'name',
        'asc'
      ).subscribe({
        next: (response) => {
          console.log('Products loaded:', response);
          this.products = response.content;
          console.log('First product image URLs:', {
            imageUrl: this.products[0]?.imageUrl,
            hoverImageUrl: this.products[0]?.hoverImageUrl
          });
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading products:', error);
          this.loading = false;
        }
      });
    } else {
      this.productService.getAllProducts(
        this.currentPage,
        this.pageSize,
        'name',
        'asc',
        this.selectedCategoryId || undefined,
        this.selectedSubcategoryId || undefined,
        this.selectedBrandId || undefined,
        this.selectedStatus || undefined,
        this.showFeaturedOnly || undefined,
        this.showExclusiveOnly || undefined
      ).subscribe({
        next: (response) => {
          console.log('Products loaded from backend:', response);
          this.products = response.content;
          if (this.products.length > 0) {
            console.log('First product image URLs:', {
              imageUrl: this.products[0]?.imageUrl,
              hoverImageUrl: this.products[0]?.hoverImageUrl
            });
          }
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading products:', error);
          this.loading = false;
        }
      });
    }
  }

  loadCategories() {
    this.categoryService.getAllCategories(0, 100).subscribe({
      next: (response) => {
        this.categories = response.content;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  loadBrands() {
    this.brandService.getAllBrands(0, 100).subscribe({
      next: (response) => {
        this.brands = response.content;
      },
      error: (error) => {
        console.error('Error loading brands:', error);
      }
    });
  }

  loadProductCount() {
    this.productService.getProductCount().subscribe({
      next: (count) => {
        this.totalProducts = count;
      },
      error: (error) => {
        console.error('Error loading product count:', error);
      }
    });
  }

  onCategoryChange(categoryId: number | null | undefined) {
    this.selectedSubcategoryId = null;
    this.productForm.patchValue({ subcategoryId: null });
    
    if (categoryId) {
      this.categoryService.getSubcategoriesByCategory(categoryId, 0, 100).subscribe({
        next: (response) => {
          this.subcategories = response.content;
        },
        error: (error) => {
          console.error('Error loading subcategories:', error);
        }
      });
    } else {
      this.subcategories = [];
    }
    
    // Trigger filter update when category changes in filter section
    if (this.selectedCategoryId !== undefined) {
      this.onFilter();
    }
  }

  onSearch() {
    this.currentPage = 0;
    this.loadProducts();
  }

  onFilter() {
    this.currentPage = 0;
    this.loadProducts();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedCategoryId = null;
    this.selectedSubcategoryId = null;
    this.selectedBrandId = null;
    this.selectedStatus = '';
    this.showFeaturedOnly = false;
    this.showExclusiveOnly = false;
    this.currentPage = 0;
    this.loadProducts();
  }

  openProductModal() {
    this.selectedProduct = null;
    this.isEditingProduct = false;
    this.productForm.reset({
      status: 'active',
      isFeatured: false,
      isExclusive: false,
      price: 0,
      oldPrice: 0,
      stock: 0
    });
    this.productImagePreview = null;
    this.hoverImagePreview = null;
    this.showProductModal = true;
  }

  editProduct(product: Product) {
    this.selectedProduct = product;
    this.isEditingProduct = true;
    this.productForm.patchValue(product);
    this.productImagePreview = product.imageUrl || null;
    this.hoverImagePreview = product.hoverImageUrl || null;
    
    // Load subcategories for the selected category
    if (product.categoryId) {
      this.onCategoryChange(product.categoryId);
    }
    
    this.showProductModal = true;
  }

  closeProductModal() {
    this.showProductModal = false;
    this.selectedProduct = null;
    this.isEditingProduct = false;
    this.productForm.reset();
    this.productImagePreview = null;
    this.hoverImagePreview = null;
  }

  onSubmit() {
    if (this.productForm.valid) {
      this.submitting = true;
      const formData = this.productForm.value;
      
      // Save product first, then upload images
      this.saveProductThenUploadImages(formData);
    }
  }

  private saveProductThenUploadImages(formData: any) {
    if (this.isEditingProduct && this.selectedProduct) {
      // For updates, just save the product
      this.productService.updateProduct(this.selectedProduct.id, formData).subscribe({
        next: (response) => {
          // Upload images after update if any
          this.uploadImagesForProduct(response.id);
        },
        error: (error) => {
          console.error('Error updating product:', error);
          this.submitting = false;
        }
      });
    } else {
      // For new products, create first then upload images
      this.productService.createProduct(formData).subscribe({
        next: (response) => {
          // Upload images after creation
          this.uploadImagesForProduct(response.id);
        },
        error: (error) => {
          console.error('Error creating product:', error);
          this.submitting = false;
        }
      });
    }
  }

  private uploadImagesForProduct(productId: number) {
    console.log('Starting image upload for product ID:', productId);
    console.log('Main image file:', this.productImageFile ? this.productImageFile.name : 'Not selected');
    console.log('Hover image file:', this.hoverImageFile ? this.hoverImageFile.name : 'Not selected');
    
    // Upload images sequentially instead of parallel to avoid conflicts
    this.uploadImagesSequentially(productId);
  }

  private async uploadImagesSequentially(productId: number) {
    try {
      // Upload main image first
      if (this.productImageFile) {
        console.log('Uploading main image first...');
        await this.uploadMainImage(productId, this.productImageFile);
        console.log('Main image upload completed');
      }

      // Then upload hover image
      if (this.hoverImageFile) {
        console.log('Uploading hover image second...');
        await this.uploadHoverImage(productId, this.hoverImageFile);
        console.log('Hover image upload completed');
      }

      console.log('All images uploaded successfully');
      this.finishProductSave();
    } catch (error) {
      console.error('Error in sequential image upload:', error);
      this.finishProductSave();
    }
  }

  private uploadMainImage(productId: number, file: File): Promise<void> {
    console.log('Uploading main image for product:', productId, 'File:', file.name);
    return new Promise((resolve, reject) => {
      this.imageUploadService.uploadProductImage(productId, file).subscribe({
        next: (response) => {
          console.log('Main image upload response:', response);
          console.log('Main image uploaded successfully:', response.imageUrl);
          resolve();
        },
        error: (error: any) => {
          console.error('Error uploading main image:', error);
          console.error('Error details:', error.error);
          resolve(); // Don't fail the whole process for image upload errors
        }
      });
    });
  }

  private uploadHoverImage(productId: number, file: File): Promise<void> {
    console.log('Uploading hover image for product:', productId, 'File:', file.name);
    return new Promise((resolve, reject) => {
      this.imageUploadService.uploadProductHoverImage(productId, file).subscribe({
        next: (response) => {
          console.log('Hover image upload response:', response);
          console.log('Hover image uploaded successfully:', response.hoverImageUrl);
          resolve();
        },
        error: (error: any) => {
          console.error('Error uploading hover image:', error);
          console.error('Error details:', error.error);
          resolve(); // Don't fail the whole process for image upload errors
        }
      });
    });
  }

  private finishProductSave() {
    this.loadProducts();
    this.loadProductCount();
    this.closeProductModal();
    this.submitting = false;
  }

  confirmDelete(product: Product) {
    this.itemToDelete = product;
    this.showDeleteModal = true;
  }

  deleteProduct() {
    if (this.itemToDelete) {
      this.productService.deleteProduct(this.itemToDelete.id).subscribe({
        next: () => {
          this.loadProducts();
          this.loadProductCount();
          this.showDeleteModal = false;
          this.itemToDelete = null;
        },
        error: (error) => {
          console.error('Error deleting product:', error);
        }
      });
    }
  }

  toggleProductStatus(product: Product) {
    const newStatus = product.status === 'active' ? 'inactive' : 'active';
    this.productService.updateProductStatus(product.id, newStatus).subscribe({
      next: (updatedProduct) => {
        const index = this.products.findIndex(p => p.id === product.id);
        if (index !== -1) {
          this.products[index] = updatedProduct;
        }
      },
      error: (error) => {
        console.error('Error updating product status:', error);
      }
    });
  }

  onImageSelect(event: any, type: 'main' | 'hover') {
    const file = event.target.files[0];
    console.log('Image selected - Type:', type, 'File:', file ? file.name : 'No file');
    
    if (file) {
      if (type === 'main') {
        this.productImageFile = file;
        console.log('Main image file stored:', this.productImageFile?.name);
        const reader = new FileReader();
        reader.onload = (e) => {
          this.productImagePreview = e.target?.result as string;
          console.log('Main image preview loaded');
        };
        reader.readAsDataURL(file);
      } else {
        this.hoverImageFile = file;
        console.log('Hover image file stored:', this.hoverImageFile?.name);
        const reader = new FileReader();
        reader.onload = (e) => {
          this.hoverImagePreview = e.target?.result as string;
          console.log('Hover image preview loaded');
        };
        reader.readAsDataURL(file);
      }
    }
  }

  uploadImage(productId: number, file: File, type: 'main' | 'hover') {
    this.productService.uploadProductImage(productId, file).subscribe({
      next: (response) => {
        if (response.success && response.imageUrl) {
          if (type === 'main') {
            this.productForm.patchValue({ imageUrl: response.imageUrl });
          } else {
            this.productForm.patchValue({ hoverImageUrl: response.imageUrl });
          }
        }
      },
      error: (error) => {
        console.error('Error uploading image:', error);
      }
    });
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadProducts();
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadProducts();
    }
  }

  goToPage(page: number) {
    this.currentPage = page;
    this.loadProducts();
  }

  getImageUrl(url: string | undefined): string {
    console.log('Getting image URL for:', url);
    if (!url || url === 'null' || url === '') {
      console.log('No valid URL provided, returning empty string');
      return '';
    }
    if (url.startsWith('http')) {
      console.log('Full URL detected:', url);
      return url;
    }
    // Ensure URL starts with /
    const cleanUrl = url.startsWith('/') ? url : `/${url}`;
    const fullUrl = `${environment.backendUrl}${cleanUrl}`;
    console.log('Constructed URL:', fullUrl);
    return fullUrl;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'active': return 'status-active';
      case 'inactive': return 'status-inactive';
      case 'draft': return 'status-draft';
      case 'discontinued': return 'status-discontinued';
      default: return 'status-inactive';
    }
  }

  onImageError(event: any) {
    // Hide broken image and show placeholder
    event.target.style.display = 'none';
  }
}
