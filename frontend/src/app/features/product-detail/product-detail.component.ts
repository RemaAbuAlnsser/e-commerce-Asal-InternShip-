import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../landing/header/header.component';
import { ProductService } from '../../services/product.service';
import {
  ProductResponse,
  ProductColorResponse,
  ProductColorImageResponse
} from '../../services/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, HeaderComponent],
  templateUrl: './product-detail.component.html',
  styleUrl:    './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {

  private route          = inject(ActivatedRoute);
  private router         = inject(Router);
  private productService = inject(ProductService);

  // ── State ────────────────────────────────────────────────────────────────────
  readonly product       = signal<ProductResponse | null>(null);
  readonly isLoading     = signal(true);
  readonly notFound      = signal(false);

  readonly selectedColor = signal<ProductColorResponse | null>(null);
  readonly activeImgIdx  = signal(0);
  readonly quantity      = signal(1);

  // Related products state
  readonly relatedProducts = signal<ProductResponse[]>([]);
  readonly relatedLoading  = signal(false);

  // ── Derived ──────────────────────────────────────────────────────────────────

  /** Images for the currently selected color (falls back to main/hover images) */
  readonly galleryImages = computed<{ id: number; imageUrl: string }[]>(() => {
    const color   = this.selectedColor();
    const product = this.product();
    if (!product) return [];

    if (color && color.images?.length) {
      return [...color.images].sort((a, b) => a.sortOrder - b.sortOrder);
    }

    // No color selected or color has no images → show product-level images
    const fallback: { id: number; imageUrl: string }[] = [];
    if (product.imageUrl)      fallback.push({ id: -1, imageUrl: product.imageUrl });
    if (product.hoverImageUrl) fallback.push({ id: -2, imageUrl: product.hoverImageUrl });
    return fallback;
  });

  /** The image currently displayed large */
  readonly mainImage = computed(() => {
    const imgs = this.galleryImages();
    const idx  = this.activeImgIdx();
    return imgs[idx] ?? imgs[0] ?? null;
  });

  /** Stock for the selected color, or total stock when no color is selected */
  readonly availableStock = computed(() => {
    const color = this.selectedColor();
    return color ? color.stock : (this.product()?.totalStock ?? 0);
  });

  readonly discountPct = computed(() => {
    const p = this.product();
    if (!p?.oldPrice || !p.price) return 0;
    return Math.round(((p.oldPrice - p.price) / p.oldPrice) * 100);
  });

  // ────────────────────────────────────────────────────────────────────────────

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/']); return; }
    this.load(id);
  }

  private load(id: number) {
    this.isLoading.set(true);
    this.productService.getById(id).subscribe({
      next: (p) => {
        this.product.set(p);
        // Auto-select first color that has images, then first color, then null
        const first = p.colors?.find(c => c.images?.length) ?? p.colors?.[0] ?? null;
        this.selectedColor.set(first);
        this.activeImgIdx.set(0);
        this.quantity.set(1);
        this.isLoading.set(false);
        
        // Load related products
        this.loadRelatedProducts(p);
      },
      error: () => {
        this.notFound.set(true);
        this.isLoading.set(false);
      }
    });
  }

  private loadRelatedProducts(product: ProductResponse) {
    this.relatedLoading.set(true);
    this.productService.getRelatedProducts(
      product.id,
      product.categoryId,
      product.subcategoryId,
      4
    ).subscribe({
      next: (products) => {
        this.relatedProducts.set(products);
        this.relatedLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading related products:', err);
        this.relatedProducts.set([]);
        this.relatedLoading.set(false);
      }
    });
  }

  // ── User actions ──────────────────────────────────────────────────────────────

  pickColor(color: ProductColorResponse) {
    this.selectedColor.set(color);
    this.activeImgIdx.set(0);
    this.quantity.set(1);
  }

  pickImage(idx: number) {
    this.activeImgIdx.set(idx);
  }

  increaseQty() {
    this.quantity.update(q => Math.min(q + 1, this.availableStock()));
  }

  decreaseQty() {
    this.quantity.update(q => Math.max(q - 1, 1));
  }

  addToCart(product?: ProductResponse)    { 
    if (product) {
      /* TODO: wire to cart service */ 
      console.log('Add to cart (related)', product.id, product.name); 
    } else {
      /* TODO: wire to cart service */ 
      console.log('Add to cart', this.product()?.id, this.selectedColor()?.id, this.quantity()); 
    }
  }
  
  addToWishlist(product?: ProductResponse){ 
    if (product) {
      /* TODO: wire to wishlist service */ 
      console.log('Wishlist (related)', product.id, product.name); 
    } else {
      /* TODO: wire to wishlist service */ 
      console.log('Wishlist', this.product()?.id); 
    }
  }

  goBack() { window.history.back(); }

  navigateToProduct(productId: number) {
    console.log('Navigating to product:', productId);
    // Reset state before navigation
    this.isLoading.set(true);
    this.relatedProducts.set([]);
    
    this.router.navigate(['/product', productId]).then(() => {
      console.log('Navigation completed');
      // Force reload the component data
      window.location.reload();
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  img(url: string | null | undefined): string {
    if (!url) return 'assets/images/placeholder.png';
    return this.productService.resolveImageUrl(url);
  }

  /** Expose array of given length to @for in template */
  range(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i);
  }

  /** Expose Math object to template */
  Math = Math;
}
