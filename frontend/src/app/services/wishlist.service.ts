import { Injectable, signal, computed, inject, effect } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SubscriberAuthService } from './subscriber-auth.service';

export interface WishlistItem {
  id?:           number;
  productId:    number;
  productName:  string;
  productImage: string;
  categoryName: string;
  price:        number;
  oldPrice?:    number;
  totalStock:   number;
}

const BASE = 'http://localhost:3000/api/wishlist';

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private http = inject(HttpClient);
  private auth = inject(SubscriberAuthService);

  private readonly _items = signal<WishlistItem[]>([]);

  readonly items   = this._items.asReadonly();
  readonly count   = computed(() => this._items().length);
  readonly isEmpty = computed(() => this._items().length === 0);

  constructor() {
    effect(() => {
      if (this.auth.isLoggedIn()) {
        this.loadFromBackend();
      } else {
        this._items.set([]);
      }
    }, { allowSignalWrites: true });
  }

  isInWishlist(productId: number): boolean {
    return this._items().some(i => i.productId === productId);
  }

  toggle(item: WishlistItem): void {
    if (!this.auth.isLoggedIn()) return;
    // Optimistic toggle
    if (this.isInWishlist(item.productId)) {
      this._items.update(items => items.filter(i => i.productId !== item.productId));
    } else {
      this._items.update(items => [...items, item]);
    }
    this.http.post<WishlistItem[]>(`${BASE}/toggle`, item, { headers: this.headers() })
      .subscribe({ next: updated => this._items.set(updated) });
  }

  remove(productId: number): void {
    this._items.update(items => items.filter(i => i.productId !== productId));
    this.http.delete(`${BASE}/${productId}`, { headers: this.headers() }).subscribe();
  }

  clear(): void {
    this._items.set([]);
    this.http.delete(BASE, { headers: this.headers() }).subscribe();
  }

  private loadFromBackend(): void {
    this.http.get<WishlistItem[]>(BASE, { headers: this.headers() }).subscribe({
      next:  items => this._items.set(items),
      error: ()    => this._items.set([])
    });
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.token()}` });
  }
}
