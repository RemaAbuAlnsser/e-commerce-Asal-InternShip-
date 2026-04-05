import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor() {
    // Check token validity on service initialization
    this.checkTokenValidity();
  }

  private hasValidToken(): boolean {
    if (typeof window === 'undefined' || !window.localStorage) {
      return false;
    }

    const token = localStorage.getItem('adminToken');
    if (!token) {
      return false;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      
      if (payload.exp && payload.exp > currentTime) {
        return true;
      } else {
        // Token expired, clean up
        this.clearAuthData();
        return false;
      }
    } catch (error) {
      // Invalid token format, clean up
      this.clearAuthData();
      return false;
    }
  }

  private checkTokenValidity(): void {
    const isValid = this.hasValidToken();
    this.isAuthenticatedSubject.next(isValid);
  }

  public clearAuthData(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('adminToken');
      sessionStorage.removeItem('admin');
    }
    this.isAuthenticatedSubject.next(false);
  }

  public setAuthData(token: string, admin: any): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('adminToken', token);
      sessionStorage.setItem('admin', JSON.stringify(admin));
    }
    this.isAuthenticatedSubject.next(true);
  }

  public isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  public getToken(): string | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('adminToken');
    }
    return null;
  }

  public getAdmin(): any | null {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const adminData = sessionStorage.getItem('admin');
      return adminData ? JSON.parse(adminData) : null;
    }
    return null;
  }
}
