import { Injectable, signal, computed } from '@angular/core';

export interface SubscriberSession {
  name:  string;
  email: string;
  token: string;
}

const STORAGE_KEY = 'subscriber_session';

@Injectable({ providedIn: 'root' })
export class SubscriberAuthService {

  private readonly _session = signal<SubscriberSession | null>(this.load());

  readonly session    = this._session.asReadonly();
  readonly isLoggedIn = computed(() => this._session() !== null);
  readonly name       = computed(() => this._session()?.name  ?? null);
  readonly email      = computed(() => this._session()?.email ?? null);
  readonly token      = computed(() => this._session()?.token ?? null);

  /** Called after email verification or magic-link sign-in */
  login(name: string, email: string, token: string): void {
    const session: SubscriberSession = { name, email, token };
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(session)); } catch {}
    this._session.set(session);
  }

  logout(): void {
    this._session.set(null);
    try { localStorage.removeItem(STORAGE_KEY); } catch {}
  }

  private load(): SubscriberSession | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const s = JSON.parse(raw);
      // Validate shape — must have token to be usable
      return (s?.name && s?.email && s?.token) ? s : null;
    } catch { return null; }
  }
}
