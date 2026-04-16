import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

function isTokenValid(): boolean {
  if (typeof window === 'undefined' || !window.localStorage) return false;
  const token = localStorage.getItem('adminToken');
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp && payload.exp > Math.floor(Date.now() / 1000);
  } catch {
    return false;
  }
}

function loginGuard() {
  if (isTokenValid()) {
    inject(Router).navigate(['/admin/dashboard']);
    return false;
  }
  return true;
}

export const routes: Routes = [
  {
    path: 'admin/login',
    canActivate: [loginGuard],
    loadComponent: () => import('./features/admin/admin-login/admin-login.component').then(m => m.AdminLoginComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'categories',
        loadComponent: () => import('./features/admin/categories/categories.component').then(m => m.CategoriesComponent)
      },
      {
        path: 'brands',
        loadComponent: () => import('./features/admin/brands/brands.component').then(m => m.BrandsComponent)
      },
      {
        path: 'products',
        loadComponent: () => import('./features/admin/products/products.component').then(m => m.ProductsComponent)
      },
      {
        path: 'orders',
        loadComponent: () => import('./features/admin/orders/orders.component').then(m => m.OrdersComponent)
      },
      {
        path: 'delivery',
        loadComponent: () => import('./features/admin/delivery/delivery.component').then(m => m.DeliveryComponent)
      },
      {
        path: 'subscribers',
        loadComponent: () => import('./features/admin/subscribers/subscribers.component').then(m => m.SubscribersComponent)
      },
      {
        path: 'announcements',
        loadComponent: () => import('./features/admin/announcements/announcements.component').then(m => m.AnnouncementsComponent)
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/admin/settings/settings.component').then(m => m.SettingsComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'product/:id',
    loadComponent: () => import('./features/product-detail/product-detail.component').then(m => m.ProductDetailComponent)
  },
  {
    path: 'new-arrivals',
    loadComponent: () => import('./features/new-arrivals/new-arrivals.component').then(m => m.NewArrivalsComponent)
  },
  {
    path: 'offers',
    loadComponent: () => import('./features/offers/offers.component').then(m => m.OffersComponent)
  },
  {
    path: 'contact',
    loadComponent: () => import('./features/contact/contact.component').then(m => m.ContactComponent)
  },
  {
    path: 'categories',
    loadComponent: () => import('./features/categories/categories.component').then(m => m.CategoriesComponent)
  },
  {
    path: 'verify-email',
    loadComponent: () => import('./features/verify-email/verify-email.component').then(m => m.VerifyEmailComponent)
  },
  {
    path: 'wishlist',
    loadComponent: () => import('./features/wishlist/wishlist.component').then(m => m.WishlistComponent)
  },
  {
    path: 'subscriber-login',
    loadComponent: () => import('./features/subscriber-login/subscriber-login.component').then(m => m.SubscriberLoginComponent)
  },
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
