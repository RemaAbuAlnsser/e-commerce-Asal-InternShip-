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
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SiteConfigService } from '../../../services/site-config.service';
import { AnnouncementService } from '../../../services/announcement.service';
import { ProductService } from '../../../services/product.service';

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
  readonly cartCount     = signal(3);
  readonly wishlistCount = signal(1);

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
    if (q) console.log('Search:', q);
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
  }

  @HostListener('document:keydown.escape')
  onEsc(): void {
    this.catOpen.set(false);
    this.closeDrawer();
    this.mobileSearchOpen.set(false);
  }

  ngOnDestroy(): void {
    if (this.isBrowser) this.doc.body.classList.remove('no-scroll');
  }
}
