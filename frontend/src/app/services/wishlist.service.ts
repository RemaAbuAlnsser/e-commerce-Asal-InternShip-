import { Injectable, signal, computed } from '@angular/core';

export interface WishlistItem {
  productId: number;
  productName: string;
  productImage: string;
  categoryName: string;
  price: number;
  oldPrice?: number;
  totalStock: number;
}

const SESSION_KEY = 'subscriber_session';
const GUEST_KEY   = 'shop_wishlist_guest';

function resolveKey(): string {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (raw) {
      const session = JSON.parse(raw);
      if (session?.email) return `shop_wishlist_${session.email}`;
    }
  } catch {}
  return GUEST_KEY;
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private userKey = resolveKey();

  private readonly _items = signal<WishlistItem[]>(this.load());

  readonly items   = this._items.asReadonly();
  readonly count   = computed(() => this._items().length);
  readonly isEmpty = computed(() => this._items().length === 0);

  // ── Switch to a logged-in user's wishlist ───────────────────────────────
  setUserKey(email: string): void {
    this.userKey = `shop_wishlist_${email}`;
    this._items.set(this.load());
  }

  // ── Clear wishlist and switch back to guest (called on logout) ──────────
  clearAndReset(): void {
    try { localStorage.removeItem(this.userKey); } catch {}
    this.userKey = GUEST_KEY;
    this._items.set([]);
  }

  // ── mutations ────────────────────────────────────────────────────────────

  isInWishlist(productId: number): boolean {
    return this._items().some(i => i.productId === productId);
  }

  toggle(item: WishlistItem): void {
    if (this.isInWishlist(item.productId)) {
      this._items.update(items => items.filter(i => i.productId !== item.productId));
    } else {
      this._items.update(items => [...items, item]);
    }
    this.save();
  }

  remove(productId: number): void {
    this._items.update(items => items.filter(i => i.productId !== productId));
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

  private load(): WishlistItem[] {
    try {
      const raw = localStorage.getItem(this.userKey);
      return raw ? JSON.parse(raw) : [];
    } catch { return []; }
  }
}
