import { Injectable, signal, computed, inject } from '@angular/core';
import { CartService } from './cart.service';
import { WishlistService } from './wishlist.service';

export interface SubscriberSession {
  name: string;
  email: string;
}

const STORAGE_KEY = 'subscriber_session';

@Injectable({ providedIn: 'root' })
export class SubscriberAuthService {
  private cartService     = inject(CartService);
  private wishlistService = inject(WishlistService);

  private readonly _session = signal<SubscriberSession | null>(this.load());

  readonly session    = this._session.asReadonly();
  readonly isLoggedIn = computed(() => this._session() !== null);
  readonly name       = computed(() => this._session()?.name ?? null);
  readonly email      = computed(() => this._session()?.email ?? null);

  /** Called after successful email verification or magic-link sign-in */
  login(name: string, email: string): void {
    const session: SubscriberSession = { name, email };
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(session)); } catch {}
    this._session.set(session);
    // Load this user's saved cart and wishlist
    this.cartService.setUserKey(email);
    this.wishlistService.setUserKey(email);
  }

  logout(): void {
    // Wipe cart and wishlist, remove from localStorage, reset to guest
    this.cartService.clearAndReset();
    this.wishlistService.clearAndReset();
    this._session.set(null);
    try { localStorage.removeItem(STORAGE_KEY); } catch {}
  }

  private load(): SubscriberSession | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }
}
