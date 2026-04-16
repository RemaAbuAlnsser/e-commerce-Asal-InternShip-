import { Component, OnInit, signal, computed, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HeaderComponent } from '../landing/header/header.component';
import { ProductService } from '../../services/product.service';
import { ProductResponse, CategoryOption } from '../../services/product.model';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';

@Component({
  selector: 'app-new-arrivals',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, HeaderComponent],
  templateUrl: './new-arrivals.component.html',
  styleUrl: './new-arrivals.component.css'
})
export class NewArrivalsComponent implements OnInit {
  readonly products = signal<ProductResponse[]>([]);
  readonly categories = signal<CategoryOption[]>([]);
  readonly selectedCategory = signal<number | null>(null);
  readonly isLoading = signal(true);
  readonly filteredProducts = signal<ProductResponse[]>([]);

  private cartService     = inject(CartService);
  private wishlistService = inject(WishlistService);

  constructor(private productService: ProductService) {
    effect(() => {
      const allProducts = this.products();
      const categoryId  = this.selectedCategory();
      const safeProducts = Array.isArray(allProducts) ? allProducts : [];
      this.filteredProducts.set(
        categoryId === null ? [...safeProducts] : safeProducts.filter(p => p && p.categoryId === categoryId)
      );
    }, { allowSignalWrites: true });
  }

  ngOnInit() {
    this.loadNewArrivals();
    this.loadCategories();
  }

  private loadNewArrivals() {
    this.isLoading.set(true);
    setTimeout(() => { if (this.isLoading()) this.isLoading.set(false); }, 10000);
    this.productService.getNewArrivals(this.selectedCategory() || undefined, 0, 50).subscribe({
      next: (response) => {
        this.products.set(Array.isArray(response?.content) ? response.content : []);
        this.isLoading.set(false);
      },
      error: () => { this.isLoading.set(false); this.products.set([]); }
    });
  }

  private loadCategories() {
    this.productService.getCategories().subscribe({
      next: (cats) => this.categories.set(Array.isArray(cats) ? cats : []),
      error: () => this.categories.set([])
    });
  }

  onCategoryChange(categoryId: number | null) { this.selectedCategory.set(categoryId); }

  addToCart(product: ProductResponse, e: Event): void {
    e.stopPropagation();
    this.cartService.add({
      productId:    product.id,
      productName:  product.name,
      productImage: this.getProductImage(product),
      categoryName: product.categoryName ?? '',
      price:        product.price,
      oldPrice:     product.oldPrice ?? undefined,
      maxStock:     product.totalStock ?? 0
    });
  }

  toggleWishlist(product: ProductResponse, e: Event): void {
    e.stopPropagation();
    this.wishlistService.toggle({
      productId:    product.id,
      productName:  product.name,
      productImage: this.getProductImage(product),
      categoryName: product.categoryName ?? '',
      price:        product.price,
      oldPrice:     product.oldPrice ?? undefined,
      totalStock:   product.totalStock ?? 0
    });
  }

  isInWishlist(product: ProductResponse): boolean { return this.wishlistService.isInWishlist(product.id); }

  getProductImage(product: ProductResponse): string {
    if (!product?.imageUrl) return '/assets/images/placeholder.jpg';
    return this.productService.resolveImageUrl(product.imageUrl);
  }

  getHoverImage(product: ProductResponse): string | null {
    if (!product?.hoverImageUrl) return null;
    return this.productService.resolveImageUrl(product.hoverImageUrl);
  }

  getDiscountPercentage(product: ProductResponse): number {
    if (!product?.oldPrice || !product.price) return 0;
    return Math.round(((product.oldPrice - product.price) / product.oldPrice) * 100);
  }
}
