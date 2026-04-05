import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    if (typeof window !== 'undefined' && window.localStorage) {
      const token = localStorage.getItem('adminToken');
      if (token) {
        // Check if token is expired (basic check)
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          const currentTime = Math.floor(Date.now() / 1000);
          
          if (payload.exp && payload.exp > currentTime) {
            return true;
          } else {
            // Token expired, remove it
            localStorage.removeItem('adminToken');
            sessionStorage.removeItem('admin');
          }
        } catch (error) {
          // Invalid token format, remove it
          localStorage.removeItem('adminToken');
          sessionStorage.removeItem('admin');
        }
      }
    }
    
    // No valid token, redirect to login
    this.router.navigate(['/admin/login']);
    return false;
  }
}
