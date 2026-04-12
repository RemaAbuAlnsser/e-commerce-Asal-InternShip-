import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  
  // Add auth token to requests if available
  let authReq = req;
  
  if (typeof window !== 'undefined' && window.localStorage) {
    const token = localStorage.getItem('adminToken');
    if (token && (req.url.includes('/admin/') || req.url.includes('/api/orders'))) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Clear invalid tokens
        if (typeof window !== 'undefined') {
          localStorage.removeItem('adminToken');
          sessionStorage.removeItem('admin');
          // Redirect to login
          router.navigate(['/admin/login']);
        }
      }
      return throwError(() => error);
    })
  );
};
