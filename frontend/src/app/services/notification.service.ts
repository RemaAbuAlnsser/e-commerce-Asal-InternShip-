import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { Notification } from '../models/notification.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);

  private _notifications$ = new BehaviorSubject<Notification[]>([]);
  // Tracks which IDs have been read locally (not persisted to server)
  private readIds = new Set<number>();

  getNotifications() {
    return this._notifications$.asObservable();
  }

  /** Call this once when the admin layout loads */
  loadNotifications(): void {
    this.http.get<any[]>(`${environment.apiUrl}/admin/dashboard/notifications`).subscribe({
      next: (data) => {
        const mapped: Notification[] = data.map(n => ({
          id:        n.id,
          type:      n.type,
          title:     n.title,
          message:   n.message,
          isRead:    this.readIds.has(n.id),
          createdAt: new Date(n.createdAt),
          route:     n.route
        }));
        this._notifications$.next(mapped);
      },
      error: () => {}
    });
  }

  getUnreadCount(): number {
    return this._notifications$.getValue().filter(n => !n.isRead).length;
  }

  markAsRead(id: number): void {
    this.readIds.add(id);
    const updated = this._notifications$.getValue().map(n =>
      n.id === id ? { ...n, isRead: true } : n
    );
    this._notifications$.next(updated);
  }

  markAllAsRead(): void {
    const updated = this._notifications$.getValue().map(n => {
      this.readIds.add(n.id);
      return { ...n, isRead: true };
    });
    this._notifications$.next(updated);
  }

  formatTime(date: Date): string {
    const diffMs = Date.now() - new Date(date).getTime();
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays}d ago`;
  }
}
