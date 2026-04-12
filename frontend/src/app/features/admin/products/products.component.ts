import {
  Component, OnInit, inject, signal, computed
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../services/product.service';
import { CategoryService, Category, Subcategory } from '../../../services/category.service';
import { BrandService, Brand } from '../../../services/brand.service';
import {
  ProductResponse,
  ProductColorResponse,
  ColorVariantForm,
  ProductForm,
  CategoryOption,
  SubcategoryOption,
  BrandOption
} from '../../../services/product.model';

type ModalMode = 'create' | 'edit' | 'view';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent implements OnInit {

  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private brandService = inject(BrandService);

  // ── State ──────────────────────────────────────────────────────────────────
  products   = signal<ProductResponse[]>([]);
  categories = signal<CategoryOption[]>([]);
  subcategories = signal<SubcategoryOption[]>([]);
  brands     = signal<BrandOption[]>([]);

  loading    = signal(false);
  saving     = signal(false);
  deleting   = signal(false);
  error      = signal<string | null>(null);
  success    = signal<string | null>(null);

  // ── Modal ──────────────────────────────────────────────────────────────────
  showModal      = signal(false);
  showDeleteModal = signal(false);
  modalMode      = signal<ModalMode>('create');
  selectedProduct = signal<ProductResponse | null>(null);

  // ── Search / filter ────────────────────────────────────────────────────────
  searchTerm = signal('');

  filteredProducts = computed(() => {
    const term = this.searchTerm().toLowerCase();
    if (!term) return this.products();
    return this.products().filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.sku.toLowerCase().includes(term) ||
      p.categoryName?.toLowerCase().includes(term)
    );
  });

  // ── Form ───────────────────────────────────────────────────────────────────
  form: ProductForm = this.emptyForm();

  // Tracks the selected category as a signal so computed() re-runs reactively
  selectedCategoryId = signal<number | null>(null);

  filteredSubcategories = computed(() =>
    this.subcategories().filter(s =>
      !this.selectedCategoryId() || s.categoryId === this.selectedCategoryId()
    )
  );

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading.set(true);
    this.productService.getAllProducts().subscribe({
      next: products => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: err => {
        this.error.set('Failed to load products');
        this.loading.set(false);
      }
    });

    // Load categories
    this.categoryService.getAllCategories(0, 100).subscribe({
      next: response => {
        const categoryOptions: CategoryOption[] = response.content.map(cat => ({
          id: cat.id,
          name: cat.name
        }));
        this.categories.set(categoryOptions);
      },
      error: err => console.error('Failed to load categories:', err)
    });
    
    // Load subcategories
    this.categoryService.getAllSubcategories(0, 100).subscribe({
      next: response => {
        const subcategoryOptions: SubcategoryOption[] = response.content.map(sub => ({
          id: sub.id,
          name: sub.name,
          categoryId: sub.categoryId
        }));
        this.subcategories.set(subcategoryOptions);
      },
      error: err => console.error('Failed to load subcategories:', err)
    });
    
    // Load brands
    this.brandService.getAllBrands(0, 100).subscribe({
      next: response => {
        const brandOptions: BrandOption[] = response.content.map(brand => ({
          id: brand.id,
          name: brand.name
        }));
        this.brands.set(brandOptions);
      },
      error: err => console.error('Failed to load brands:', err)
    });
  }

  // ── Modal helpers ──────────────────────────────────────────────────────────
  openCreate(): void {
    this.form = this.emptyForm();
    this.selectedCategoryId.set(null);
    this.modalMode.set('create');
    this.showModal.set(true);
  }

  openEdit(product: ProductResponse): void {
    this.selectedProduct.set(product);
    this.form = {
      name:             product.name,
      description:      product.description ?? '',
      price:            product.price,
      oldPrice:         product.oldPrice ?? null,
      status:           product.status,
      featured:         product.featured,
      exclusive:        product.exclusive,
      categoryId:       product.categoryId,
      subcategoryId:    product.subcategoryId ?? null,
      brandId:          product.brandId ?? null,
      primeImageFile:   null,
      primeImagePreview: product.imageUrl ? this.productService.resolveImageUrl(product.imageUrl) : null,
      hoverImageFile:   null,
      hoverImagePreview: product.hoverImageUrl ? this.productService.resolveImageUrl(product.hoverImageUrl) : null,
      colors: product.colors.map(c => ({
        colorName: c.colorName,
        colorHex:  c.colorHex,
        stock:     c.stock,
        subImages: [],
        subImagePreviews: c.images.map(i => this.productService.resolveImageUrl(i.imageUrl))
      }))
    };
    this.selectedCategoryId.set(product.categoryId);
    this.modalMode.set('edit');
    this.showModal.set(true);
  }

  onCategoryChange(catId: number | null): void {
    this.form.categoryId = catId;
    this.form.subcategoryId = null;   // reset subcategory when category changes
    this.selectedCategoryId.set(catId);
  }

  openView(product: ProductResponse): void {
    this.selectedProduct.set(product);
    this.modalMode.set('view');
    this.showModal.set(true);
  }

  openDelete(product: ProductResponse): void {
    this.selectedProduct.set(product);
    this.showDeleteModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
    this.showDeleteModal.set(false);
    this.selectedProduct.set(null);
    this.error.set(null);
  }

  // ── Image pickers ──────────────────────────────────────────────────────────
  onPrimeImageChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.form.primeImageFile = file;
    this.form.primeImagePreview = URL.createObjectURL(file);
  }

  onHoverImageChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.form.hoverImageFile = file;
    this.form.hoverImagePreview = URL.createObjectURL(file);
  }

  onSubImageChange(event: Event, colorIndex: number): void {
    const files = Array.from((event.target as HTMLInputElement).files ?? []);
    if (!files.length) return;
    const color = this.form.colors[colorIndex];
    files.forEach(file => {
      color.subImages.push(file);
      color.subImagePreviews.push(URL.createObjectURL(file));
    });
  }

  removeSubImage(colorIndex: number, imgIndex: number): void {
    this.form.colors[colorIndex].subImages.splice(imgIndex, 1);
    this.form.colors[colorIndex].subImagePreviews.splice(imgIndex, 1);
  }

  // ── Color management ───────────────────────────────────────────────────────
  addColor(): void {
    this.form.colors.push({
      colorName: '',
      colorHex: '#000000',
      stock: 0,
      subImages: [],
      subImagePreviews: []
    });
  }

  removeColor(index: number): void {
    this.form.colors.splice(index, 1);
  }

  get totalStock(): number {
    return this.form.colors.reduce((sum, c) => sum + (c.stock || 0), 0);
  }

  // ── Save ───────────────────────────────────────────────────────────────────
  save(): void {
    if (!this.validateForm()) return;
    this.saving.set(true);
    this.error.set(null);

    if (this.modalMode() === 'create') {
      const fd = this.productService.buildCreateFormData(
        {
          name:          this.form.name,
          description:   this.form.description,
          price:         this.form.price!,
          oldPrice:      this.form.oldPrice ?? undefined,
          status:        this.form.status,
          featured:      this.form.featured,
          exclusive:     this.form.exclusive,
          categoryId:    this.form.categoryId!,
          subcategoryId: this.form.subcategoryId ?? undefined,
          brandId:       this.form.brandId ?? undefined
        },
        this.form.primeImageFile,
        this.form.hoverImageFile,
        this.form.colors
      );

      this.productService.createProduct(fd).subscribe({
        next: product => {
          this.products.update(list => [product, ...list]);
          this.showSuccess('Product created successfully');
          this.closeModal();
          this.saving.set(false);
        },
        error: err => {
          this.error.set(err.error?.message ?? 'Failed to create product');
          this.saving.set(false);
        }
      });

    } else if (this.modalMode() === 'edit') {
      const fd = new FormData();
      if (this.form.name)        fd.append('name',        this.form.name);
      if (this.form.description) fd.append('description', this.form.description);
      if (this.form.price)       fd.append('price',       this.form.price.toString());
      if (this.form.oldPrice)    fd.append('oldPrice',    this.form.oldPrice.toString());
      fd.append('status',    this.form.status);
      fd.append('featured',  this.form.featured.toString());
      fd.append('exclusive', this.form.exclusive.toString());
      if (this.form.categoryId)    fd.append('categoryId',    this.form.categoryId.toString());
      if (this.form.subcategoryId) fd.append('subcategoryId', this.form.subcategoryId.toString());
      if (this.form.brandId)       fd.append('brandId',       this.form.brandId.toString());
      if (this.form.primeImageFile) fd.append('primeImage', this.form.primeImageFile);
      if (this.form.hoverImageFile) fd.append('hoverImage', this.form.hoverImageFile);

      // First update the basic product fields
      this.productService.updateProduct(this.selectedProduct()!.id, fd).subscribe({
        next: updated => {
          // Then update stock for each color that has changed
          this.updateColorStocks(updated);
        },
        error: err => {
          this.error.set(err.error?.message ?? 'Failed to update product');
          this.saving.set(false);
        }
      });
    }
  }

  // ── Delete ─────────────────────────────────────────────────────────────────
  confirmDelete(): void {
    const product = this.selectedProduct();
    if (!product) return;
    this.deleting.set(true);

    this.productService.deleteProduct(product.id).subscribe({
      next: () => {
        this.products.update(list => list.filter(p => p.id !== product.id));
        this.showSuccess('Product deleted successfully');
        this.closeModal();
        this.deleting.set(false);
      },
      error: err => {
        this.error.set('Failed to delete product');
        this.deleting.set(false);
      }
    });
  }

  // ── Update color stocks after product edit ────────────────────────────────
  private updateColorStocks(updatedProduct: ProductResponse): void {
    const originalProduct = this.selectedProduct()!;
    const stockUpdates: Array<{colorId: number, newStock: number}> = [];

    // Compare form colors with original product colors to find stock changes
    this.form.colors.forEach((formColor, index) => {
      const originalColor = originalProduct.colors.find(c => 
        c.colorName === formColor.colorName && c.colorHex === formColor.colorHex
      );
      
      if (originalColor && originalColor.stock !== formColor.stock) {
        stockUpdates.push({
          colorId: originalColor.id,
          newStock: formColor.stock
        });
      }
    });

    if (stockUpdates.length === 0) {
      // No stock changes, just update the product list and close modal
      this.products.update(list => list.map(p => p.id === updatedProduct.id ? updatedProduct : p));
      this.showSuccess('Product updated successfully');
      this.closeModal();
      this.saving.set(false);
      return;
    }

    // Update stocks sequentially
    this.updateStocksSequentially(updatedProduct.id, stockUpdates, 0, updatedProduct);
  }

  private updateStocksSequentially(
    productId: number, 
    stockUpdates: Array<{colorId: number, newStock: number}>,
    index: number,
    lastUpdatedProduct: ProductResponse
  ): void {
    if (index >= stockUpdates.length) {
      // All stock updates completed
      this.products.update(list => list.map(p => p.id === productId ? lastUpdatedProduct : p));
      this.showSuccess('Product and stock updated successfully');
      this.closeModal();
      this.saving.set(false);
      return;
    }

    const update = stockUpdates[index];
    this.productService.updateColorStock(productId, update.colorId, update.newStock).subscribe({
      next: updated => {
        // Continue with next stock update
        this.updateStocksSequentially(productId, stockUpdates, index + 1, updated);
      },
      error: err => {
        this.error.set(`Failed to update stock for color: ${err.error?.message ?? 'Unknown error'}`);
        this.saving.set(false);
      }
    });
  }

  // ── Quick stock update ─────────────────────────────────────────────────────
  quickUpdateStock(product: ProductResponse, color: ProductColorResponse, newStock: number): void {
    this.productService.updateColorStock(product.id, color.id, newStock).subscribe({
      next: updated => {
        this.products.update(list => list.map(p => p.id === updated.id ? updated : p));
        this.showSuccess('Stock updated');
      },
      error: () => this.error.set('Failed to update stock')
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────
  private validateForm(): boolean {
    if (!this.form.name?.trim()) {
      this.error.set('Product name is required');
      return false;
    }
    if (!this.form.price || this.form.price <= 0) {
      this.error.set('Valid price is required');
      return false;
    }
    if (!this.form.categoryId) {
      this.error.set('Category is required');
      return false;
    }
    return true;
  }

  private emptyForm(): ProductForm {
    return {
      name: '', description: '', price: null, oldPrice: null,
      status: 'active', featured: false, exclusive: false,
      categoryId: null, subcategoryId: null, brandId: null,
      primeImageFile: null, primeImagePreview: null,
      hoverImageFile: null, hoverImagePreview: null,
      colors: []
    };
  }

  private showSuccess(msg: string): void {
    this.success.set(msg);
    setTimeout(() => this.success.set(null), 3500);
  }

  resolveImage(path: string | null): string {
    return this.productService.resolveImageUrl(path);
  }

  trackByColor(_: number, c: ColorVariantForm): string { return c.colorHex + c.colorName; }
  trackById(_: number, p: ProductResponse): number { return p.id; }
}