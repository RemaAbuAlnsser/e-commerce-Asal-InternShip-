import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerService, CustomerSummary } from '../../../services/customer.service';

@Component({
  selector: 'app-subscribers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subscribers.component.html',
  styleUrl: './subscribers.component.css'
})
export class SubscribersComponent implements OnInit {

  customers = signal<CustomerSummary[]>([]);
  loading   = signal(false);
  error     = signal('');
  search    = signal('');

  currentPage = 1;
  readonly pageSize = 10;

  constructor(private customerService: CustomerService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.customerService.getAll().subscribe({
      next: (data) => {
        this.customers.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load customers.');
        this.loading.set(false);
      }
    });
  }

  filtered = computed(() => {
    const term = this.search().trim().toLowerCase();
    if (!term) return this.customers();
    return this.customers().filter(c =>
      c.name.toLowerCase().includes(term) ||
      c.email.toLowerCase().includes(term)
    );
  });

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filtered().length / this.pageSize))
  );

  paginated = computed(() => {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filtered().slice(start, start + this.pageSize);
  });

  // Grand total: sum of all customers' totalSpent
  grandTotal = computed(() =>
    this.customers().reduce((sum, c) => sum + (c.totalSpent ?? 0), 0)
  );

  totalCustomers = computed(() => this.customers().length);

  totalWithOrders = computed(() =>
    this.customers().filter(c => c.totalOrders > 0).length
  );

  onSearch(value: string): void {
    this.search.set(value);
    this.currentPage = 1;
  }

  clearSearch(): void {
    this.search.set('');
    this.currentPage = 1;
  }

  prevPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages()) this.currentPage++;
  }

  formatCurrency(value: number): string {
    return '$' + (value ?? 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  trackById(_: number, c: CustomerSummary): number {
    return c.id;
  }
}
