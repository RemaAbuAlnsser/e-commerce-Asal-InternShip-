import {
  Component, OnInit, OnDestroy, AfterViewInit,
  signal, inject, PLATFORM_ID, ElementRef
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from './header/header.component';
import { SettingsService, SiteImage } from '../../services/settings.service';
import { SiteConfigService } from '../../services/site-config.service';
import { ProductService } from '../../services/product.service';
import { CategoryOption, ProductResponse } from '../../services/product.model';
import { SubscriptionService } from '../../services/subscription.service';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';
import { environment } from '../../../environments/environment';
import { InteractiveImageAccordionComponent } from '../../components/ui/interactive-image-accordion.component';

interface CategorySection {
  category: CategoryOption;
  products: ProductResponse[];
  isLoading: boolean;
  isLoaded: boolean;
}

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, RouterLink, InteractiveImageAccordionComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent implements OnInit, AfterViewInit, OnDestroy {

  router               = inject(Router);
  private settingsService     = inject(SettingsService);
  siteConfigService           = inject(SiteConfigService);
  private productService      = inject(ProductService);
  private subscriptionService = inject(SubscriptionService);
  private cartService         = inject(CartService);
  private wishlistService     = inject(WishlistService);
  private el                  = inject(ElementRef);
  private platformId          = inject(PLATFORM_ID);
  private get isBrowser() { return isPlatformBrowser(this.platformId); }

  // ── Newsletter subscribe ──────────────────────────────────────────────────────
  subscribeEmail  = '';
  subscribeStatus = signal<'idle' | 'loading' | 'success' | 'error'>('idle');
  subscribeMsg    = signal('');

  onSubscribe() {
    const email = this.subscribeEmail.trim();
    if (!email) return;
    this.subscribeStatus.set('loading');
    this.subscriptionService.subscribe(email).subscribe({
      next: (res) => {
        this.subscribeStatus.set(res.success ? 'success' : 'error');
        this.subscribeMsg.set(res.message);
        if (res.success) this.subscribeEmail = '';
      },
      error: () => {
        this.subscribeStatus.set('error');
        this.subscribeMsg.set('Something went wrong. Please try again.');
      }
    });
  }

  // ── Hero slider ──────────────────────────────────────────────────────────────
  readonly siteImages  = signal<SiteImage[]>([]);
  readonly activeIndex = signal(0);
  private slideInterval: ReturnType<typeof setInterval> | null = null;

  // ── Category sections ────────────────────────────────────────────────────────
  readonly categorySections = signal<CategorySection[]>([]);
  readonly skeletons = [1, 2, 3, 4];

  private sectionObserver: IntersectionObserver | null = null;

  // ────────────────────────────────────────────────────────────────────────────

  ngOnInit() {
    if (!this.isBrowser) return;
    this.loadSiteImages();
    this.loadCategories();
  }

  ngAfterViewInit() {
    // Observer will be (re-)created after categories arrive and sections render
  }

  ngOnDestroy() {
    this.stopAutoSlide();
    this.sectionObserver?.disconnect();
  }

  // ── Hero ─────────────────────────────────────────────────────────────────────

  private loadSiteImages() {
    this.settingsService.getPublicSiteImages().subscribe({
      next: (images) => {
        const sorted = [...images].sort((a, b) => a.displayOrder - b.displayOrder);
        this.siteImages.set(sorted);
        if (sorted.length > 1) this.startAutoSlide();
      },
      error: () => this.siteImages.set([])
    });
  }

  private startAutoSlide() {
    this.slideInterval = setInterval(() => this.nextSlide(), 3000);
  }

  private stopAutoSlide() {
    if (this.slideInterval) { clearInterval(this.slideInterval); this.slideInterval = null; }
  }

  nextSlide() {
    const len = this.siteImages().length;
    if (!len) return;
    this.activeIndex.set((this.activeIndex() + 1) % len);
  }

  prevSlide() {
    const len = this.siteImages().length;
    if (!len) return;
    this.activeIndex.set((this.activeIndex() - 1 + len) % len);
  }

  goToSlide(i: number) {
    this.stopAutoSlide();
    this.activeIndex.set(i);
    this.startAutoSlide();
  }

  getImageUrl(url: string): string {
    if (!url) return '';
    return url.startsWith('http') ? url : `${environment.backendUrl}${url}`;
  }

  goToAdmin() { this.router.navigate(['/admin']); }

  // ── Category sections ─────────────────────────────────────────────────────────

  private loadCategories() {
    this.productService.getCategories().subscribe({
      next: (cats) => {
        this.categorySections.set(
          cats.map(cat => ({ category: cat, products: [], isLoading: false, isLoaded: false }))
        );
        // Give Angular one tick to render the section elements, then observe them
        setTimeout(() => this.setupSectionObserver(), 60);
      },
      error: () => {}
    });
  }

  private setupSectionObserver() {
    if (!this.isBrowser || !('IntersectionObserver' in window)) {
      // Fallback: load everything immediately
      this.categorySections().forEach(s => this.loadCategoryProducts(s.category.id));
      return;
    }

    this.sectionObserver?.disconnect();

    this.sectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const id = Number(entry.target.getAttribute('data-cat-id'));
            this.loadCategoryProducts(id);
            this.sectionObserver?.unobserve(entry.target); // load once only
          }
        });
      },
      { rootMargin: '400px 0px', threshold: 0 }
      // 400 px look-ahead → products fetched well before the section is visible
    );

    const sectionEls: NodeListOf<Element> =
      this.el.nativeElement.querySelectorAll('[data-cat-id]');
    sectionEls.forEach((el: Element) => this.sectionObserver!.observe(el));
  }

  private loadCategoryProducts(categoryId: number) {
    const sections = this.categorySections();
    const idx = sections.findIndex(s => s.category.id === categoryId);
    if (idx === -1 || sections[idx].isLoaded || sections[idx].isLoading) return;

    // Mark loading
    const next = [...sections];
    next[idx] = { ...next[idx], isLoading: true };
    this.categorySections.set(next);

    this.productService.getAll({ categoryId }, 0, 8).subscribe({
      next: (res) => {
        const cur = [...this.categorySections()];
        const i   = cur.findIndex(s => s.category.id === categoryId);
        if (i !== -1) {
          cur[i] = { ...cur[i], products: res.content ?? [], isLoading: false, isLoaded: true };
          this.categorySections.set(cur);
        }
      },
      error: () => {
        const cur = [...this.categorySections()];
        const i   = cur.findIndex(s => s.category.id === categoryId);
        if (i !== -1) {
          cur[i] = { ...cur[i], isLoading: false, isLoaded: true };
          this.categorySections.set(cur);
        }
      }
    });
  }

  // ── Product card helpers ──────────────────────────────────────────────────────

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
