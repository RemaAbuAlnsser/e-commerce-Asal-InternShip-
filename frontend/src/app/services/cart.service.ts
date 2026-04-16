import { Injectable, signal, computed, inject, effect } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SubscriberAuthService } from './subscriber-auth.service';

export interface CartItem {
  id:           number;
  productId:    number;
  productName:  string;
  productImage: string;
  categoryName: string;
  price:        number;
  oldPrice?:    number;
  colorId?:     number;
  colorName?:   string;
  colorHex?:    string;
  quantity:     number;
  maxStock:     number;
}

const BASE = 'http://localhost:3000/api/cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private http = inject(HttpClient);
  private auth = inject(SubscriberAuthService);

  private readonly _items   = signal<CartItem[]>([]);
  private readonly _loading = signal(false);

  readonly items    = this._items.asReadonly();
  readonly loading  = this._loading.asReadonly();
  readonly count    = computed(() => this._items().reduce((s, i) => s + i.quantity, 0));
  readonly total    = computed(() => this._items().reduce((s, i) => s + i.price * i.quantity, 0));
  readonly isEmpty  = computed(() => this._items().length === 0);
  readonly isOpen   = signal(false);

  constructor() {
    // Auto-load when the subscriber logs in; clear when they log out
    effect(() => {
      if (this.auth.isLoggedIn()) {
        this.loadFromBackend();
      } else {
        this._items.set([]);
        this._loading.set(false);
        this.isOpen.set(false);
      }
    }, { allowSignalWrites: true });
  }

  open()  { this.isOpen.set(true);  }
  close() { this.isOpen.set(false); }

  // ── Add / increment ──────────────────────────────────────────────────────
  add(item: Omit<CartItem, 'id' | 'quantity'>, qty = 1): void {
    if (!this.auth.isLoggedIn()) { this.open(); return; } // drawer shows sign-in hint
    this.open();
    this.http.post<CartItem>(BASE, { ...item, quantity: qty }, { headers: this.headers() })
      .subscribe({
        next: saved => {
          const cur = this._items();
          const idx = cur.findIndex(i =>
            i.productId === saved.productId && (i.colorId ?? null) === (saved.colorId ?? null)
          );
          if (idx > -1) {
            const updated = [...cur]; updated[idx] = saved;
            this._items.set(updated);
          } else {
            this._items.set([...cur, saved]);
          }
        }
      });
  }

  // ── Update quantity ──────────────────────────────────────────────────────
  updateQty(id: number, qty: number): void {
    // Optimistic
    this._items.update(items =>
      items.map(i => i.id === id ? { ...i, quantity: Math.max(1, Math.min(qty, i.maxStock)) } : i)
    );
    this.http.put<CartItem>(`${BASE}/${id}`, { quantity: qty }, { headers: this.headers() })
      .subscribe({ next: saved => this._items.update(items => items.map(i => i.id === saved.id ? saved : i)) });
  }

  // ── Remove ───────────────────────────────────────────────────────────────
  remove(id: number): void {
    this._items.update(items => items.filter(i => i.id !== id));
    this.http.delete(`${BASE}/${id}`, { headers: this.headers() }).subscribe();
  }

  // ── Clear ────────────────────────────────────────────────────────────────
  clear(): void {
    this._items.set([]);
    this.http.delete(BASE, { headers: this.headers() }).subscribe();
  }

  // ── Internal ─────────────────────────────────────────────────────────────
  private loadFromBackend(): void {
    this._loading.set(true);
    this.http.get<CartItem[]>(BASE, { headers: this.headers() }).subscribe({
      next:  items => { this._items.set(items); this._loading.set(false); },
      error: ()    => { this._items.set([]);    this._loading.set(false); }
    });
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.token()}` });
  }
}
