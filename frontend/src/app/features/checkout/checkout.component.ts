import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { HeaderComponent } from '../landing/header/header.component';
import { CartService } from '../../services/cart.service';
import { SubscriberAuthService } from '../../services/subscriber-auth.service';
import { DeliveryCityService, DeliveryCity } from '../../services/delivery-city.service';
import { environment } from '../../../environments/environment';

interface OrderItemPayload {
  productId:      number;
  productColorId: number | null;
  quantity:       number;
}

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, HeaderComponent],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {
  private router      = inject(Router);
  private http        = inject(HttpClient);
  readonly cart       = inject(CartService);
  readonly auth       = inject(SubscriberAuthService);
  private cityService = inject(DeliveryCityService);

  // ── Form fields ───────────────────────────────────────────────────────────
  customerName    = '';
  customerPhone   = '';
  customerAddress = '';

  // ── State ─────────────────────────────────────────────────────────────────
  readonly cities       = signal<DeliveryCity[]>([]);
  readonly selectedCity = signal<DeliveryCity | null>(null);
  readonly isSubmitting = signal(false);
  readonly orderSuccess = signal(false);
  readonly orderId      = signal<number | null>(null);
  readonly errors       = signal<Record<string, string>>({});
  readonly citiesLoading = signal(true);

  // ── Computed totals ───────────────────────────────────────────────────────
  readonly subtotal     = computed(() => this.cart.total());
  readonly deliveryFee  = computed(() => this.selectedCity()?.deliveryPrice ?? 0);
  readonly grandTotal   = computed(() => this.subtotal() + this.deliveryFee());

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    // If cart is empty redirect home
    if (this.cart.isEmpty()) {
      this.router.navigate(['/']);
      return;
    }
    // Pre-fill name from subscriber session
    if (this.auth.name()) this.customerName = this.auth.name()!;
    this.loadCities();
  }

  private loadCities(): void {
    this.citiesLoading.set(true);
    this.cityService.getAllCustomer().subscribe({
      next:  cities => { this.cities.set(cities); this.citiesLoading.set(false); },
      error: ()     => this.citiesLoading.set(false)
    });
  }

  selectCity(city: DeliveryCity): void {
    this.selectedCity.set(city);
    this.clearError('city');
  }

  // ── Validation ─────────────────────────────────────────────────────────────
  private validate(): boolean {
    const errs: Record<string, string> = {};
    if (!this.customerName.trim())    errs['name']    = 'Full name is required.';
    if (!this.customerPhone.trim())   errs['phone']   = 'Phone number is required.';
    if (!this.customerAddress.trim()) errs['address'] = 'Delivery address is required.';
    if (!this.selectedCity())         errs['city']    = 'Please select a delivery city.';
    this.errors.set(errs);
    return Object.keys(errs).length === 0;
  }

  clearError(field: string): void {
    const cur = { ...this.errors() };
    delete cur[field];
    this.errors.set(cur);
  }

  // ── Submit ─────────────────────────────────────────────────────────────────
  placeOrder(): void {
    if (!this.validate() || this.isSubmitting()) return;

    const items: OrderItemPayload[] = this.cart.items().map(i => ({
      productId:      i.productId,
      productColorId: i.colorId ?? null,
      quantity:       i.quantity
    }));

    const city = this.selectedCity()!;
    const payload = {
      customerName:    this.customerName.trim(),
      customerPhone:   this.customerPhone.trim(),
      customerCity:    city.cityName,
      customerAddress: this.customerAddress.trim(),
      customerEmail:   this.auth.email() ?? null,
      shippingMethod:  'DELIVERY',
      shippingCost:    city.deliveryPrice,
      paymentMethod:   'CASH_ON_DELIVERY',
      items
    };

    this.isSubmitting.set(true);
    this.http.post<any>(`${environment.apiUrl}/orders`, payload).subscribe({
      next: (res) => {
        this.orderId.set(res?.data?.id ?? res?.id ?? null);
        this.cart.clear();
        this.orderSuccess.set(true);
        this.isSubmitting.set(false);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: () => {
        this.isSubmitting.set(false);
      }
    });
  }
}
