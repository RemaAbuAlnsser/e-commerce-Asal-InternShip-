import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface StatCard {
  title: string;
  value: number | string;
  icon: string;
  color: string;
  change?: string;
  changeType?: 'positive' | 'negative' | 'neutral';
}

interface Notification {
  id: number;
  type: 'warning' | 'info' | 'success' | 'error';
  title: string;
  message: string;
  time: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  
  basicStats: StatCard[] = [];
  financialStats: StatCard[] = [];
  notifications: Notification[] = [];

  ngOnInit() {
    this.loadDashboardData();
  }

  private loadDashboardData() {
    // Basic Stats
    this.basicStats = [
      {
        title: 'Total Products',
        value: 1247,
        icon: 'box',
        color: '#3b82f6',
        change: '+12%',
        changeType: 'positive'
      },
      {
        title: 'Total Categories',
        value: 24,
        icon: 'layers',
        color: '#10b981',
        change: '+2',
        changeType: 'positive'
      },
      {
        title: 'Total Brands',
        value: 156,
        icon: 'tag',
        color: '#f59e0b',
        change: '+8',
        changeType: 'positive'
      },
      {
        title: 'Total Orders',
        value: 3892,
        icon: 'shopping-cart',
        color: '#ef4444',
        change: '+23%',
        changeType: 'positive'
      },
      {
        title: 'Total Users',
        value: 12567,
        icon: 'users',
        color: '#8b5cf6',
        change: '+156',
        changeType: 'positive'
      },
      {
        title: 'Total Subscribers',
        value: 8934,
        icon: 'mail',
        color: '#06b6d4',
        change: '+89',
        changeType: 'positive'
      }
    ];

    // Financial Stats
    this.financialStats = [
      {
        title: 'Total Sales',
        value: '$127,450',
        icon: 'dollar-sign',
        color: '#10b981',
        change: '+18.2%',
        changeType: 'positive'
      },
      {
        title: 'Today Sales',
        value: '$2,340',
        icon: 'trending-up',
        color: '#3b82f6',
        change: '+5.4%',
        changeType: 'positive'
      },
      {
        title: 'Weekly Sales',
        value: '$18,920',
        icon: 'calendar',
        color: '#f59e0b',
        change: '+12.8%',
        changeType: 'positive'
      },
      {
        title: 'Monthly Sales',
        value: '$89,340',
        icon: 'bar-chart',
        color: '#8b5cf6',
        change: '+24.1%',
        changeType: 'positive'
      }
    ];

    // Notifications
    this.notifications = [
      {
        id: 1,
        type: 'warning',
        title: 'Low Stock Alert',
        message: '15 products are running low on stock',
        time: '2 hours ago'
      },
      {
        id: 2,
        type: 'error',
        title: 'Out of Stock',
        message: '3 products are completely out of stock',
        time: '4 hours ago'
      },
      {
        id: 3,
        type: 'info',
        title: 'New Subscribers',
        message: '89 new subscribers joined this week',
        time: '1 day ago'
      },
      {
        id: 4,
        type: 'info',
        title: 'New Users',
        message: '156 new users registered this week',
        time: '1 day ago'
      },
      {
        id: 5,
        type: 'success',
        title: 'New Orders',
        message: '23 new orders received today',
        time: '3 hours ago'
      }
    ];
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'warning': return 'alert-triangle';
      case 'error': return 'alert-circle';
      case 'info': return 'info';
      case 'success': return 'check-circle';
      default: return 'bell';
    }
  }
}
