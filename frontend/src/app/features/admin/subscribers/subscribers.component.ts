import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerService, SubscriberSummary } from '../../../services/customer.service';

@Component({
  selector: 'app-subscribers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subscribers.component.html',
  styleUrl: './subscribers.component.css'
})
export class SubscribersComponent implements OnInit {

  subscribers = signal<SubscriberSummary[]>([]);
  loading     = signal(false);
  error       = signal('');
  search      = signal('');

  currentPage = 1;
  readonly pageSize = 15;

  constructor(private customerService: CustomerService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.customerService.getSubscribers().subscribe({
      next: (data) => {
        this.subscribers.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load subscribers.');
        this.loading.set(false);
      }
    });
  }

  filtered = computed(() => {
    const term = this.search().trim().toLowerCase();
    if (!term) return this.subscribers();
    return this.subscribers().filter(s =>
      s.name.toLowerCase().includes(term) ||
      s.email.toLowerCase().includes(term)
    );
  });

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filtered().length / this.pageSize))
  );

  paginated = computed(() => {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filtered().slice(start, start + this.pageSize);
  });

  totalSubscribers  = computed(() => this.subscribers().length);
  verifiedCount     = computed(() => this.subscribers().filter(s => s.verified).length);
  pendingCount      = computed(() => this.subscribers().filter(s => !s.verified).length);

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

  trackById(_: number, s: SubscriberSummary): number {
    return s.id;
  }
}
