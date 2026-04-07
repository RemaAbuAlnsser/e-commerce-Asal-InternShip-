import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  DeliveryCity,
  DeliveryCityRequest,
  DeliveryCityService
} from '../../../services/delivery-city.service';

@Component({
  selector: 'app-delivery',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './delivery.component.html',
  styleUrl: './delivery.component.css'
})
export class DeliveryComponent implements OnInit {
  deliveryCities = signal<DeliveryCity[]>([]);
  loading = signal(false);
  submitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  searchTerm = signal('');
  currentPage = 1;
  pageSize = 6;

  isModalOpen = false;
  isDeleteModalOpen = false;
  isEditMode = false;

  selectedCityId: number | null = null;
  cityToDelete: DeliveryCity | null = null;

  form: DeliveryCityRequest = {
    cityName: '',
    deliveryPrice: 0
  };

  formErrors = {
    cityName: '',
    deliveryPrice: ''
  };

filteredCities = computed(() => {
  const term = this.searchTerm().trim().toLowerCase();

  if (!term) return this.deliveryCities();

  return this.deliveryCities().filter(city =>
    city.cityName.toLowerCase().includes(term)
  );
});

  paginatedCities = computed(() => {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredCities().slice(start, start + this.pageSize);
  });

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filteredCities().length / this.pageSize))
  );

  totalCities = computed(() => this.deliveryCities().length);

  averagePrice = computed(() => {
    const cities = this.deliveryCities();
    if (!cities.length) return 0;

    const total = cities.reduce((sum, city) => sum + Number(city.deliveryPrice), 0);
    return +(total / cities.length).toFixed(2);
  });

  highestPrice = computed(() => {
    const cities = this.deliveryCities();
    if (!cities.length) return 0;

    return Math.max(...cities.map(city => Number(city.deliveryPrice)));
  });

  constructor(private deliveryCityService: DeliveryCityService) {}

  ngOnInit(): void {
    this.loadCities();
  }

  loadCities(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.deliveryCityService.getAllAdmin().subscribe({
      next: (res) => {
        this.deliveryCities.set(res || []);
        this.loading.set(false);

        if (this.currentPage > this.totalPages()) {
          this.currentPage = this.totalPages();
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(
          err?.error?.message ||
          err?.error ||
          'Failed to load delivery cities.'
        );
      }
    });
  }

  onSearchChange(value: string): void {
  this.searchTerm.set(value);
  this.currentPage = 1;
}

clearSearch(): void {
  this.searchTerm.set('');
  this.currentPage = 1;
}

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedCityId = null;
    this.resetForm();
    this.isModalOpen = true;
  }

  openEditModal(city: DeliveryCity): void {
    this.isEditMode = true;
    this.selectedCityId = city.id;
    this.form = {
      cityName: city.cityName,
      deliveryPrice: city.deliveryPrice
    };
    this.clearFormErrors();
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.resetForm();
  }

  openDeleteModal(city: DeliveryCity): void {
    this.cityToDelete = city;
    this.isDeleteModalOpen = true;
  }

  closeDeleteModal(): void {
    this.cityToDelete = null;
    this.isDeleteModalOpen = false;
  }

  saveCity(): void {
    if (!this.validateForm()) return;

    this.submitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const payload: DeliveryCityRequest = {
      cityName: this.form.cityName.trim(),
      deliveryPrice: Number(this.form.deliveryPrice)
    };

    const request$ = this.isEditMode && this.selectedCityId
      ? this.deliveryCityService.update(this.selectedCityId, payload)
      : this.deliveryCityService.create(payload);

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeModal();
        this.loadCities();
        this.successMessage.set(
          this.isEditMode
            ? 'Delivery city updated successfully.'
            : 'Delivery city created successfully.'
        );

        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(
          err?.error?.message ||
          err?.error ||
          'Failed to save delivery city.'
        );
      }
    });
  }

  confirmDelete(): void {
    if (!this.cityToDelete?.id) return;

    this.submitting.set(true);
    this.errorMessage.set('');

    this.deliveryCityService.delete(this.cityToDelete.id).subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeDeleteModal();
        this.loadCities();
        this.successMessage.set('Delivery city deleted successfully.');
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(
          err?.error?.message ||
          err?.error ||
          'Failed to delete delivery city.'
        );
      }
    });
  }

  validateForm(): boolean {
    this.clearFormErrors();
    let valid = true;

    if (!this.form.cityName || !this.form.cityName.trim()) {
      this.formErrors.cityName = 'City name is required';
      valid = false;
    }

    if (
      this.form.deliveryPrice === null ||
      this.form.deliveryPrice === undefined ||
      Number(this.form.deliveryPrice) < 0
    ) {
      this.formErrors.deliveryPrice = 'Delivery price must be 0 or greater';
      valid = false;
    }

    return valid;
  }

  clearFormErrors(): void {
    this.formErrors = {
      cityName: '',
      deliveryPrice: ''
    };
  }

  resetForm(): void {
    this.form = {
      cityName: '',
      deliveryPrice: 0
    };
    this.clearFormErrors();
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages()) {
      this.currentPage++;
    }
  }

  trackByCityId(index: number, city: DeliveryCity): number {
    return city.id;
  }
}
