import {
  Component, OnInit, OnDestroy, signal, inject, PLATFORM_ID
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { HeaderComponent } from '../landing/header/header.component';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';
import { ProductResponse } from '../../services/product.model';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, HeaderComponent],
  templateUrl: './search.component.html',
  styleUrl: './search.component.css'
})
export class SearchComponent implements OnInit, OnDestroy {

  private route         = inject(ActivatedRoute);
  private router        = inject(Router);
  private productService = inject(ProductService);
  private cartService   = inject(CartService);
  private wishlistService = inject(WishlistService);
  private platformId    = inject(PLATFORM_ID);
  private get isBrowser() { return isPlatformBrowser(this.platformId); }

  readonly products     = signal<ProductResponse[]>([]);
  readonly isLoading    = signal(false);
  readonly totalResults = signal(0);
  readonly query        = signal('');

  // For the in-page search bar refinement
  searchInput = '';

  private routeSub?: Subscription;

  ngOnInit(): void {
    this.routeSub = this.route.queryParamMap.subscribe(params => {
      const q = params.get('q')?.trim() ?? '';
      this.query.set(q);
      this.searchInput = q;
      if (q) this.runSearch(q);
      else   { this.products.set([]); this.totalResults.set(0); }
    });
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
  }

  private runSearch(keyword: string): void {
    this.isLoading.set(true);
    this.products.set([]);
    this.productService.search(keyword, 0, 40).subscribe({
      next: res => {
        this.products.set(res.content ?? []);
        this.totalResults.set(res.totalElements ?? 0);
        this.isLoading.set(false);
      },
      error: () => {
        this.products.set([]);
        this.totalResults.set(0);
        this.isLoading.set(false);
      }
    });
  }

  refineSearch(): void {
    const q = this.searchInput.trim();
    if (!q) return;
    this.router.navigate(['/search'], { queryParams: { q } });
  }

  // ── Product card helpers ──────────────────────────────────────────────────

  getProductImage(p: ProductResponse): string {
    if (!p?.imageUrl) return 'assets/images/placeholder.png';
    return this.productService.resolveImageUrl(p.imageUrl);
  }

  getHoverImage(p: ProductResponse): string | null {
    if (!p?.hoverImageUrl) return null;
    return this.productService.resolveImageUrl(p.hoverImageUrl);
  }

  getDiscountPercentage(p: ProductResponse): number {
    if (!p?.oldPrice || !p.price) return 0;
    return Math.round(((p.oldPrice - p.price) / p.oldPrice) * 100);
  }

  addToCart(p: ProductResponse, e: Event): void {
    e.stopPropagation();
    this.cartService.add({
      productId:    p.id,
      productName:  p.name,
      productImage: this.getProductImage(p),
      categoryName: p.categoryName ?? '',
      price:        p.price,
      oldPrice:     p.oldPrice ?? undefined,
      maxStock:     p.totalStock ?? 0
    });
  }

  toggleWishlist(p: ProductResponse, e: Event): void {
    e.stopPropagation();
    this.wishlistService.toggle({
      productId:    p.id,
      productName:  p.name,
      productImage: this.getProductImage(p),
      categoryName: p.categoryName ?? '',
      price:        p.price,
      oldPrice:     p.oldPrice ?? undefined,
      totalStock:   p.totalStock ?? 0
    });
  }

  isInWishlist(p: ProductResponse): boolean {
    return this.wishlistService.isInWishlist(p.id);
  }
}
