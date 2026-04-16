import { Injectable, signal, computed } from '@angular/core';

export interface CartItem {
  key: string;          // unique: productId + colorId
  productId: number;
  productName: string;
  productImage: string;
  categoryName: string;
  price: number;
  oldPrice?: number;
  colorId?: number;
  colorName?: string;
  colorHex?: string;
  quantity: number;
  maxStock: number;
}

const SESSION_KEY = 'subscriber_session';
const GUEST_KEY   = 'shop_cart_guest';

function resolveKey(): string {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (raw) {
      const session = JSON.parse(raw);
      if (session?.email) return `shop_cart_${session.email}`;
    }
  } catch {}
  return GUEST_KEY;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private userKey = resolveKey();

  private readonly _items = signal<CartItem[]>(this.load());

  readonly items   = this._items.asReadonly();
  readonly count   = computed(() => this._items().reduce((s, i) => s + i.quantity, 0));
  readonly total   = computed(() => this._items().reduce((s, i) => s + i.price * i.quantity, 0));
  readonly isEmpty = computed(() => this._items().length === 0);

  // ── open/close state ────────────────────────────────────────────────────
  readonly isOpen = signal(false);
  open()  { this.isOpen.set(true); }
  close() { this.isOpen.set(false); }

  // ── Switch to a logged-in user's cart ───────────────────────────────────
  setUserKey(email: string): void {
    this.userKey = `shop_cart_${email}`;
    this._items.set(this.load());
  }

  // ── Clear cart and switch back to guest (called on logout) ──────────────
  clearAndReset(): void {
    try { localStorage.removeItem(this.userKey); } catch {}
    this.userKey = GUEST_KEY;
    this._items.set([]);
    this.close();
  }

  // ── mutations ────────────────────────────────────────────────────────────

  add(item: Omit<CartItem, 'key' | 'quantity'>, qty = 1): void {
    const key = `${item.productId}_${item.colorId ?? 0}`;
    const cur  = this._items();
    const idx  = cur.findIndex(i => i.key === key);

    if (idx > -1) {
      const updated = [...cur];
      updated[idx] = {
        ...updated[idx],
        quantity: Math.min(updated[idx].quantity + qty, updated[idx].maxStock)
      };
      this._items.set(updated);
    } else {
      this._items.set([...cur, { ...item, key, quantity: qty }]);
    }
    this.save();
    this.open();
  }

  updateQty(key: string, qty: number): void {
    this._items.update(items =>
      items.map(i => i.key === key ? { ...i, quantity: Math.max(1, Math.min(qty, i.maxStock)) } : i)
    );
    this.save();
  }

  remove(key: string): void {
    this._items.update(items => items.filter(i => i.key !== key));
    this.save();
  }

  clear(): void {
    this._items.set([]);
    this.save();
  }

  // ── persistence ──────────────────────────────────────────────────────────

  private save(): void {
    try { localStorage.setItem(this.userKey, JSON.stringify(this._items())); } catch {}
  }

  private load(): CartItem[] {
    try {
      const raw = localStorage.getItem(this.userKey);
      return raw ? JSON.parse(raw) : [];
    } catch { return []; }
  }
}
