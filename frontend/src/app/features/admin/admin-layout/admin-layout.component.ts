import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID, signal, HostListener } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd, RouterLink } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { DashboardService } from '../../../services/dashboard.service';
import { NotificationService } from '../../../services/notification.service';
import { Notification } from '../../../models/notification.model';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  isSidebarOpen = false;
  isMobile = false;
  currentRoute = '';
  isNotifOpen = false;
  notifications: Notification[] = [];
  unreadCount = 0;
  private isBrowser: boolean;
  private notifSub!: Subscription;

  pendingOrders = signal(0);

  menuItems = [
    { label: 'Dashboard', icon: 'dashboard', route: '/admin/dashboard', active: false },
    { label: 'Categories', icon: 'category', route: '/admin/categories', active: false },
    { label: 'Brands', icon: 'brand', route: '/admin/brands', active: false },
    { label: 'Products', icon: 'inventory', route: '/admin/products', active: false },
    { label: 'Orders', icon: 'shopping_cart', route: '/admin/orders', active: false },
    { label: 'Delivery', icon: 'local_shipping', route: '/admin/delivery', active: false },
    { label: 'Subscribers', icon: 'people', route: '/admin/subscribers', active: false },
    { label: 'Settings', icon: 'settings', route: '/admin/settings', active: false }
  ];

  constructor(
    private router: Router,
    private dashboardService: DashboardService,
    public notifService: NotificationService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit() {
    this.checkScreenSize();
    this.isSidebarOpen = !this.isMobile;
    this.updateActiveRoute();
    this.loadPendingCount();

    this.notifSub = this.notifService.getNotifications().subscribe(list => {
      this.notifications = list.slice(0, 5);
      this.unreadCount = list.filter(n => !n.isRead).length;
    });

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateActiveRoute();
        if (this.isMobile) this.isSidebarOpen = false;
        this.isNotifOpen = false;
      });

    if (this.isBrowser) {
      window.addEventListener('resize', () => this.checkScreenSize());
    }
  }

  ngOnDestroy() {
    this.notifSub?.unsubscribe();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.notif-wrapper')) {
      this.isNotifOpen = false;
    }
  }

  toggleNotifDropdown(event: MouseEvent) {
    event.stopPropagation();
    this.isNotifOpen = !this.isNotifOpen;
  }

  onNotifClick(notif: Notification) {
    this.notifService.markAsRead(notif.id);
    this.isNotifOpen = false;
    this.router.navigate([notif.route]);
  }

  checkScreenSize() {
    if (this.isBrowser) {
      this.isMobile = window.innerWidth < 768;
    } else {
      this.isMobile = false;
    }
  }

  private loadPendingCount(): void {
    this.dashboardService.getStats().subscribe({
      next: (data) => this.pendingOrders.set(data.pendingOrders),
      error: () => {}
    });
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
      localStorage.removeItem('adminToken');
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

  notifIconClass(type: string): string {
    return 'type-' + type.toLowerCase().replace('_', '-');
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
    if (route.includes('/settings')) return 'Settings';
    return 'Dashboard';
  }
}
