import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'admin/login',
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
    path: '',
    redirectTo: '/admin/login',
    pathMatch: 'full'
  }
];
