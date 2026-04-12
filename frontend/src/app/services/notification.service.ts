import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private notifications: Notification[] = [
    {
      id: 1,
      type: 'NEW_ORDER',
      title: 'New Order Received',
      message: 'Order #1042 placed by Ahmed Hassan — $129.99',
      isRead: false,
      createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000),
      route: '/admin/orders'
    },
    {
      id: 2,
      type: 'LOW_STOCK',
      title: 'Low Stock Alert',
      message: 'Nike Air Max 270 — only 3 units remaining',
      isRead: false,
      createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000),
      route: '/admin/products'
    },
    {
      id: 3,
      type: 'NEW_ORDER',
      title: 'New Order Received',
      message: 'Order #1041 placed by Sara Ali — $74.50',
      isRead: false,
      createdAt: new Date(Date.now() - 8 * 60 * 60 * 1000),
      route: '/admin/orders'
    },
    {
      id: 4,
      type: 'OUT_OF_STOCK',
      title: 'Out of Stock',
      message: 'Adidas Ultraboost 22 is now out of stock',
      isRead: true,
      createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000),
      route: '/admin/products'
    },
    {
      id: 5,
      type: 'LOW_STOCK',
      title: 'Low Stock Alert',
      message: 'Puma RS-X — only 2 units remaining',
      isRead: true,
      createdAt: new Date(Date.now() - 30 * 60 * 60 * 1000),
      route: '/admin/products'
    }
  ];

  private _notifications$ = new BehaviorSubject<Notification[]>(this.notifications);

  getNotifications() {
    return this._notifications$.asObservable();
  }

  getUnreadCount(): number {
    return this.notifications.filter(n => !n.isRead).length;
  }

  markAsRead(id: number): void {
    const n = this.notifications.find(n => n.id === id);
    if (n) {
      n.isRead = true;
      this._notifications$.next([...this.notifications]);
    }
  }

  markAllAsRead(): void {
    this.notifications.forEach(n => (n.isRead = true));
    this._notifications$.next([...this.notifications]);
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
