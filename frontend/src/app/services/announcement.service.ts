import { Injectable, signal, computed, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/* ── Model ────────────────────────────────────────────────────────────────── */
export interface Announcement {
  id: string;
  text: string;
  isActive: boolean;
  displayOrder: number;
  link?: string;
}

export interface AnnouncementConfig {
  /** true = one announcement shown at a time, false = continuous scroll */
  rotationMode: boolean;
  /** seconds each announcement stays visible in rotation mode */
  rotationInterval: number;
}

/* ── Constants ────────────────────────────────────────────────────────────── */
const STORAGE_KEY = 'ec_announcements';
const CONFIG_KEY  = 'ec_announcement_cfg';

const SEED_DATA: Announcement[] = [
  { id: '1', text: 'Free shipping on all orders over $50',              isActive: true,  displayOrder: 1 },
  { id: '2', text: '30-day hassle-free returns — no questions asked',   isActive: true,  displayOrder: 2 },
  { id: '3', text: 'Summer Sale — up to 50% off selected items',        isActive: true,  displayOrder: 3, link: '/offers' },
  { id: '4', text: 'New arrivals every week — discover the latest trends', isActive: true, displayOrder: 4 },
  { id: '5', text: 'Exclusive member discounts — join free today',      isActive: true,  displayOrder: 5 },
];

const DEFAULT_CFG: AnnouncementConfig = { rotationMode: false, rotationInterval: 5 };

/* ── Service ──────────────────────────────────────────────────────────────── */
@Injectable({ providedIn: 'root' })
export class AnnouncementService {
  /* SSR-safe browser check must be first so hydrate() can use it */
  private readonly _isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly _list   = signal<Announcement[]>(this.hydrate());
  private readonly _config = signal<AnnouncementConfig>(this.hydrateCfg());

  /* ── Public read-only signals ────────────────────────────── */
  readonly announcements = this._list.asReadonly();
  readonly config        = this._config.asReadonly();

  readonly activeAnnouncements = computed(() =>
    [...this._list()]
      .filter(a => a.isActive)
      .sort((a, b) => a.displayOrder - b.displayOrder)
  );

  readonly hasActive = computed(() => this.activeAnnouncements().length > 0);

  /* ── Hydration ───────────────────────────────────────────── */
  private hydrate(): Announcement[] {
    if (!this._isBrowser) return SEED_DATA;
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Announcement[]) : SEED_DATA;
    } catch { return SEED_DATA; }
  }

  private hydrateCfg(): AnnouncementConfig {
    if (!this._isBrowser) return DEFAULT_CFG;
    try {
      const raw = localStorage.getItem(CONFIG_KEY);
      return raw ? { ...DEFAULT_CFG, ...(JSON.parse(raw) as Partial<AnnouncementConfig>) } : DEFAULT_CFG;
    } catch { return DEFAULT_CFG; }
  }

  /* ── Persistence ─────────────────────────────────────────── */
  private persist(): void {
    if (!this._isBrowser) return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this._list()));
  }

  private persistCfg(): void {
    if (!this._isBrowser) return;
    localStorage.setItem(CONFIG_KEY, JSON.stringify(this._config()));
  }

  /* ── CRUD ────────────────────────────────────────────────── */
  add(ann: Omit<Announcement, 'id'>): Announcement {
    const item: Announcement = { ...ann, id: Date.now().toString() };
    this._list.update(l => [...l, item]);
    this.persist();
    return item;
  }

  update(id: string, patch: Partial<Omit<Announcement, 'id'>>): void {
    this._list.update(l => l.map(a => a.id === id ? { ...a, ...patch } : a));
    this.persist();
  }

  remove(id: string): void {
    this._list.update(l => l.filter(a => a.id !== id));
    this.persist();
  }

  toggleActive(id: string): void {
    this._list.update(l => l.map(a => a.id === id ? { ...a, isActive: !a.isActive } : a));
    this.persist();
  }

  /* ── Config ──────────────────────────────────────────────── */
  updateConfig(patch: Partial<AnnouncementConfig>): void {
    this._config.update(c => ({ ...c, ...patch }));
    this.persistCfg();
  }
}
