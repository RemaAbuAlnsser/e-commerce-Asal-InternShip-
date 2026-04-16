import {
  Component,
  HostListener,
  ElementRef,
  ViewChild,
  signal,
  inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID
} from '@angular/core';
import { CommonModule, DOCUMENT, isPlatformBrowser } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SiteConfigService } from '../../../services/site-config.service';
import { AnnouncementService } from '../../../services/announcement.service';
import { ProductService } from '../../../services/product.service';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';
import { SubscriberAuthService } from '../../../services/subscriber-auth.service';
import { SubscriptionService } from '../../../services/subscription.service';
import { ProductResponse } from '../../../services/product.model';

export interface NavLink {
  label: string;
  path: string;
  exact: boolean;
  badge?: string;
}

export interface CategoryItem {
  id: number;
  label: string;
  emoji: string;
  path: string;
  imageUrl?: string;
}

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {
  /* ── Template refs ────────────────────────────── */
  @ViewChild('catTrigger')  catTrigger!: ElementRef<HTMLElement>;
  @ViewChild('catDropdown') catDropdown!: ElementRef<HTMLElement>;

  /* ── SSR-safe browser globals ────────────────── */
  private readonly doc        = inject(DOCUMENT);
  private readonly platformId = inject(PLATFORM_ID);
  private get isBrowser()     { return isPlatformBrowser(this.platformId); }

  /* ── Site config (from admin settings) ──────── */
  private siteConfig = inject(SiteConfigService);
  readonly siteName = this.siteConfig.siteName;
  readonly siteLogo = this.siteConfig.siteLogo;

  /* ── Announcement service (from admin) ──────── */
  private announcementService = inject(AnnouncementService);
  readonly activeAnnouncements = this.announcementService.activeAnnouncements;
  readonly hasActiveAnnouncements = this.announcementService.hasActive;

  /* ── Router ─────────────────────────────────── */
  private router = inject(Router);

  /* ── Product service (for categories) ──────── */
  private productService = inject(ProductService);

  /* ── UI state ────────────────────────────────── */
  readonly drawerOpen       = signal(false);
  readonly catOpen          = signal(false);
  readonly drawerCatOpen    = signal(false);
  readonly mobileSearchOpen = signal(false);

  /* plain string — required for [(ngModel)] two-way binding */
  searchQuery = '';

  /* ── Counts ──────────────────────────────────── */
  readonly cartService      = inject(CartService);
  readonly wishlistService  = inject(WishlistService);
  readonly subscriberAuth   = inject(SubscriberAuthService);
  readonly cartCount        = this.cartService.count;
  readonly wishlistCount    = this.wishlistService.count;
  readonly subscriberName   = this.subscriberAuth.name;
  readonly isSubscriberLoggedIn = this.subscriberAuth.isLoggedIn;

  readonly accountMenuOpen  = signal(false);

  /* ── Search suggestions ──────────────────────────────── */
  readonly suggestions       = signal<ProductResponse[]>([]);
  readonly suggestionsOpen   = signal(false);
  private  suggestTimer?: ReturnType<typeof setTimeout>;

  // ── Sign-in form state ────────────────────────
  private subscriptionService = inject(SubscriptionService);
  signinEmail  = '';
  readonly signinStatus = signal<'idle' | 'loading' | 'sent' | 'error'>('idle');
  readonly signinMsg    = signal('');

  openCart(): void { this.cartService.open(); }

  toggleAccountMenu(e: Event): void {
    e.stopPropagation();
    this.accountMenuOpen.update(v => !v);
    // Reset form when opening
    if (this.accountMenuOpen()) {
      this.signinStatus.set('idle');
      this.signinEmail = '';
    }
  }

  logout(): void {
    this.subscriberAuth.logout();
    this.accountMenuOpen.set(false);
  }

  requestSignIn(): void {
    const email = this.signinEmail.trim();
    if (!email) return;
    this.signinStatus.set('loading');
    this.subscriptionService.requestLogin(email).subscribe({
      next: () => {
        this.signinStatus.set('sent');
        this.signinMsg.set('Check your inbox — we sent you a sign-in link!');
      },
      error: () => {
        this.signinStatus.set('error');
        this.signinMsg.set('Something went wrong. Please try again.');
      }
    });
  }

  /* ── Navigation data ─────────────────────────── */
  readonly navLinks: NavLink[] = [
    { label: 'Home',         path: '/',             exact: true  },
    { label: 'New Arrivals', path: '/new-arrivals', exact: false, badge: 'New' },
    { label: 'Offers',       path: '/offers',       exact: false, badge: 'Sale' },
    { label: 'Contact',      path: '/contact',      exact: false }
  ];

  /* ── Categories (from database) ─────────────── */
  readonly categories = signal<CategoryItem[]>([]);

  /* ── Lifecycle ───────────────────────────────── */
  ngOnInit(): void {
    this.loadCategories();
  }

  private loadCategories(): void {
    this.productService.getCategories().subscribe({
      next: (dbCategories) => {
        const categoryItems: CategoryItem[] = dbCategories.map(cat => {
          const resolvedImageUrl = cat.imageUrl ? this.productService.resolveImageUrl(cat.imageUrl) : undefined;
          return {
            id: cat.id,
            label: cat.name,
            emoji: '🛍️', // Default emoji for all categories
            path: `/categories?category=${cat.id}`,
            imageUrl: resolvedImageUrl
          };
        });
        this.categories.set(categoryItems);
      },
      error: (error) => {
        console.error('Error loading categories for header:', error);
        // Set empty array on error
        this.categories.set([]);
      }
    });
  }

  /* ── Drawer ──────────────────────────────────── */
  openDrawer(): void {
    this.drawerOpen.set(true);
    if (this.isBrowser) this.doc.body.classList.add('no-scroll');
  }

  closeDrawer(): void {
    this.drawerOpen.set(false);
    this.drawerCatOpen.set(false);
    if (this.isBrowser) this.doc.body.classList.remove('no-scroll');
  }

  /* ── Drawer categories accordion ────────────── */
  toggleDrawerCat(): void {
    this.drawerCatOpen.update(v => !v);
  }

  /* ── Categories dropdown ─────────────────────── */
  toggleCat(e: Event): void {
    e.stopPropagation();
    this.catOpen.update(v => !v);
  }

  /* ── Mobile search ───────────────────────────── */
  toggleMobileSearch(): void {
    this.mobileSearchOpen.update(v => !v);
  }

  /* ── Search ──────────────────────────────────── */
  onSearch(): void {
    const q = this.searchQuery.trim();
    if (!q) return;
    this.closeSuggestions();
    this.mobileSearchOpen.set(false);
    this.router.navigate(['/search'], { queryParams: { q } });
  }

  onSearchInput(): void {
    const q = this.searchQuery.trim();
    clearTimeout(this.suggestTimer);
    if (!q || q.length < 2) { this.suggestions.set([]); this.suggestionsOpen.set(false); return; }
    this.suggestTimer = setTimeout(() => {
      this.productService.search(q, 0, 5).subscribe({
        next: res => {
          this.suggestions.set(res.content ?? []);
          this.suggestionsOpen.set((res.content ?? []).length > 0);
        },
        error: () => { this.suggestions.set([]); this.suggestionsOpen.set(false); }
      });
    }, 280);
  }

  goToSuggestion(p: ProductResponse): void {
    this.closeSuggestions();
    this.searchQuery = '';
    this.router.navigate(['/product', p.id]);
  }

  closeSuggestions(): void {
    this.suggestionsOpen.set(false);
    this.suggestions.set([]);
    clearTimeout(this.suggestTimer);
  }

  getSuggestionImage(p: ProductResponse): string {
    return this.productService.resolveImageUrl(p.imageUrl ?? '');
  }

  /* ── Global listeners ────────────────────────── */
  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    const t   = e.target as HTMLElement;
    const btn = this.catTrigger?.nativeElement;
    const dd  = this.catDropdown?.nativeElement;
    if (btn && dd && !btn.contains(t) && !dd.contains(t)) {
      this.catOpen.set(false);
    }
    // Close account menu if clicking outside
    if (!(e.target as HTMLElement).closest('.account-menu-wrap')) {
      this.accountMenuOpen.set(false);
    }
    // Close search suggestions if clicking outside
    if (!(e.target as HTMLElement).closest('.search-bar')) {
      this.closeSuggestions();
    }
  }

  @HostListener('document:keydown.escape')
  onEsc(): void {
    this.catOpen.set(false);
    this.closeDrawer();
    this.mobileSearchOpen.set(false);
  }

  ngOnDestroy(): void {
    if (this.isBrowser) this.doc.body.classList.remove('no-scroll');
    clearTimeout(this.suggestTimer);
  }
}
