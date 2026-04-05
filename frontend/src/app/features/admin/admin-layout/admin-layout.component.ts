import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent implements OnInit {
  isSidebarOpen = false;
  isMobile = false;
  currentRoute = '';
  private isBrowser: boolean;

  menuItems = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/admin/dashboard',
      active: false
    },
    {
      label: 'Categories',
      icon: 'category',
      route: '/admin/categories',
      active: false
    },
    {
      label: 'Brands',
      icon: 'brand',
      route: '/admin/brands',
      active: false
    },
    {
      label: 'Products',
      icon: 'inventory',
      route: '/admin/products',
      active: false
    },
    {
      label: 'Orders',
      icon: 'shopping_cart',
      route: '/admin/orders',
      active: false
    },
    {
      label: 'Delivery',
      icon: 'local_shipping',
      route: '/admin/delivery',
      active: false
    },
    {
      label: 'Subscribers',
      icon: 'people',
      route: '/admin/subscribers',
      active: false
    }
  ];

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit() {
    this.checkScreenSize();
    // Set initial sidebar state
    this.isSidebarOpen = !this.isMobile;
    this.updateActiveRoute();
    
    // Listen for route changes
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateActiveRoute();
        if (this.isMobile) {
          this.isSidebarOpen = false;
        }
      });

    // Listen for window resize (only in browser)
    if (this.isBrowser) {
      window.addEventListener('resize', () => {
        this.checkScreenSize();
      });
    }
  }

  checkScreenSize() {
    if (this.isBrowser) {
      this.isMobile = window.innerWidth < 768;
    } else {
      // Default to desktop on server
      this.isMobile = false;
    }
    // Don't automatically set sidebar state on desktop
    // Let user control it via toggle button
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  updateActiveRoute() {
    this.currentRoute = this.router.url;
    this.menuItems.forEach(item => {
      item.active = this.currentRoute === item.route;
    });
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }

  logout() {
    if (this.isBrowser) {
      sessionStorage.removeItem('admin');
    }
    this.router.navigate(['/admin/login']);
  }

  getAdminName(): string {
    if (this.isBrowser) {
      const adminData = sessionStorage.getItem('admin');
      if (adminData) {
        const admin = JSON.parse(adminData);
        return admin.name || 'Admin';
      }
    }
    return 'Admin';
  }

  getAdminEmail(): string {
    if (this.isBrowser) {
      const adminData = sessionStorage.getItem('admin');
      if (adminData) {
        const admin = JSON.parse(adminData);
        return admin.email || 'admin@example.com';
      }
    }
    return 'admin@example.com';
  }

  getCurrentPageName(): string {
    const route = this.currentRoute;
    if (route.includes('/dashboard')) return 'Dashboard';
    if (route.includes('/categories')) return 'Categories';
    if (route.includes('/brands')) return 'Brands';
    if (route.includes('/products')) return 'Products';
    if (route.includes('/orders')) return 'Orders';
    if (route.includes('/delivery')) return 'Delivery';
    if (route.includes('/subscribers')) return 'Subscribers';
    return 'Dashboard';
  }
}
