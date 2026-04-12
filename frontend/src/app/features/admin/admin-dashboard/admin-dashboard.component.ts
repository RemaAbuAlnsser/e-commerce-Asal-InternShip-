import { Component, OnInit, AfterViewInit, signal, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DashboardService, DashboardStats } from '../../../services/dashboard.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit, AfterViewInit {

  stats = signal<DashboardStats | null>(null);
  loading = signal(true);
  error = signal('');

  constructor(
    private dashboardService: DashboardService,
    private route: ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.loadStats();
  }

  ngAfterViewInit(): void {
    // Scroll to #notifications if the URL fragment is present
    this.route.fragment.subscribe(fragment => {
      if (fragment === 'notifications' && isPlatformBrowser(this.platformId)) {
        setTimeout(() => {
          document.getElementById('notifications')?.scrollIntoView({ behavior: 'smooth' });
        }, 300);
      }
    });
  }

  private loadStats(): void {
    this.loading.set(true);
    this.error.set('');

    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load dashboard statistics.');
        this.loading.set(false);
      }
    });
  }

  formatCurrency(value: number): string {
    return '$' + (value ?? 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }
}
