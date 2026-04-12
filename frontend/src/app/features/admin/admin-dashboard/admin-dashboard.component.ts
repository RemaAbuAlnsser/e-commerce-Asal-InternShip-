import { Component, OnInit, OnDestroy, AfterViewInit, signal, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { DashboardService, DashboardStats } from '../../../services/dashboard.service';
import { NotificationService } from '../../../services/notification.service';
import { Notification } from '../../../models/notification.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  stats         = signal<DashboardStats | null>(null);
  loading       = signal(true);
  error         = signal('');
  notifications = signal<Notification[]>([]);

  private notifSub!: Subscription;

  constructor(
    private dashboardService: DashboardService,
    public  notifService: NotificationService,
    private route: ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.loadStats();

    this.notifSub = this.notifService.getNotifications().subscribe(list => {
      this.notifications.set(list);
    });
  }

  ngAfterViewInit(): void {
    this.route.fragment.subscribe(fragment => {
      if (fragment === 'notifications' && isPlatformBrowser(this.platformId)) {
        setTimeout(() => {
          document.getElementById('notifications')?.scrollIntoView({ behavior: 'smooth' });
        }, 300);
      }
    });
  }

  ngOnDestroy(): void {
    this.notifSub?.unsubscribe();
  }

  markAsRead(id: number): void {
    this.notifService.markAsRead(id);
  }

  markAllAsRead(): void {
    this.notifService.markAllAsRead();
  }

  get unreadCount(): number {
    return this.notifications().filter(n => !n.isRead).length;
  }

  notifTypeClass(type: string): string {
    switch (type) {
      case 'OUT_OF_STOCK': return 'type-out';
      case 'LOW_STOCK':    return 'type-low';
      default:             return 'type-order';
    }
  }

  private loadStats(): void {
    this.loading.set(true);
    this.error.set('');
    this.dashboardService.getStats().subscribe({
      next:  (data) => { this.stats.set(data); this.loading.set(false); },
      error: ()     => { this.error.set('Failed to load dashboard statistics.'); this.loading.set(false); }
    });
  }

  formatCurrency(value: number): string {
    return '$' + (value ?? 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }
}
