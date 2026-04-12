import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderResponse, OrderService } from '../../../services/order.service';

const ORDER_STATUSES = ['pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled'];

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {

  orders = signal<OrderResponse[]>([]);
  loading = signal(false);
  submitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  searchTerm = signal('');
  statusFilter = signal('all');
  currentPage = 1;
  pageSize = 10;

  isDetailModalOpen = false;
  isStatusModalOpen = false;
  isDeleteModalOpen = false;

  selectedOrder: OrderResponse | null = null;
  orderToDelete: OrderResponse | null = null;
  newStatus = '';

  readonly statuses = ORDER_STATUSES;

  // ── Computed ──────────────────────────────────────────────────────────────

  filteredOrders = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const status = this.statusFilter();

    return this.orders().filter(order => {
      const matchesSearch = !term ||
        order.customerName.toLowerCase().includes(term) ||
        order.customerPhone.includes(term) ||
        String(order.id).includes(term);

      const matchesStatus = status === 'all' || order.status === status;

      return matchesSearch && matchesStatus;
    });
  });

  paginatedOrders = computed(() => {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredOrders().slice(start, start + this.pageSize);
  });

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filteredOrders().length / this.pageSize))
  );

  totalOrders = computed(() => this.orders().length);

  pendingCount = computed(() =>
    this.orders().filter(o => o.status === 'pending').length
  );

  deliveredCount = computed(() =>
    this.orders().filter(o => o.status === 'delivered').length
  );

  totalRevenue = computed(() =>
    this.orders().reduce((sum, o) => sum + Number(o.total), 0)
  );

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  // ── Data ──────────────────────────────────────────────────────────────────

  loadOrders(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.orderService.getAll().subscribe({
      next: (data) => {
        this.orders.set(data || []);
        this.loading.set(false);
        if (this.currentPage > this.totalPages()) {
          this.currentPage = 1;
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to load orders.');
      }
    });
  }

  // ── Search / Filter ───────────────────────────────────────────────────────

  onSearchChange(value: string): void {
    this.searchTerm.set(value);
    this.currentPage = 1;
  }

  clearSearch(): void {
    this.searchTerm.set('');
    this.currentPage = 1;
  }

  onStatusFilterChange(value: string): void {
    this.statusFilter.set(value);
    this.currentPage = 1;
  }

  // ── Detail modal ──────────────────────────────────────────────────────────

  openDetailModal(order: OrderResponse): void {
    this.selectedOrder = order;
    this.isDetailModalOpen = true;
  }

  closeDetailModal(): void {
    this.selectedOrder = null;
    this.isDetailModalOpen = false;
  }

  // ── Status modal ──────────────────────────────────────────────────────────

  openStatusModal(order: OrderResponse): void {
    this.selectedOrder = order;
    this.newStatus = order.status;
    this.isStatusModalOpen = true;
  }

  closeStatusModal(): void {
    this.selectedOrder = null;
    this.newStatus = '';
    this.isStatusModalOpen = false;
  }

  confirmStatusUpdate(): void {
    if (!this.selectedOrder || !this.newStatus) return;

    this.submitting.set(true);
    this.errorMessage.set('');

    this.orderService.updateStatus(this.selectedOrder.id, this.newStatus).subscribe({
      next: (updated) => {
        this.orders.update(orders =>
          orders.map(o => o.id === updated.id ? updated : o)
        );
        this.submitting.set(false);
        this.closeStatusModal();
        this.successMessage.set('Order status updated successfully.');
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to update status.');
      }
    });
  }

  // ── Delete modal ──────────────────────────────────────────────────────────

  openDeleteModal(order: OrderResponse): void {
    this.orderToDelete = order;
    this.isDeleteModalOpen = true;
  }

  closeDeleteModal(): void {
    this.orderToDelete = null;
    this.isDeleteModalOpen = false;
  }

  confirmDelete(): void {
    if (!this.orderToDelete?.id) return;

    this.submitting.set(true);
    this.errorMessage.set('');

    this.orderService.delete(this.orderToDelete.id).subscribe({
      next: () => {
        this.orders.update(orders => orders.filter(o => o.id !== this.orderToDelete!.id));
        this.submitting.set(false);
        this.closeDeleteModal();
        this.successMessage.set('Order deleted successfully.');
        setTimeout(() => this.successMessage.set(''), 3000);
        if (this.currentPage > this.totalPages()) {
          this.currentPage = Math.max(1, this.totalPages());
        }
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to delete order.');
      }
    });
  }

  // ── Pagination ────────────────────────────────────────────────────────────

  previousPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages()) this.currentPage++;
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      pending:    'badge-warning',
      confirmed:  'badge-primary',
      processing: 'badge-info',
      shipped:    'badge-shipped',
      delivered:  'badge-success',
      cancelled:  'badge-danger'
    };
    return map[status] ?? 'badge-gray';
  }

  trackByOrderId(_: number, order: OrderResponse): number {
    return order.id;
  }
}
