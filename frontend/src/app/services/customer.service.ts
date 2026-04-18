import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CustomerSummary {
  id: number;
  name: string;
  email: string;
  provider: string;
  active: boolean;
  totalOrders: number;
  totalSpent: number;
}

export interface SubscriberSummary {
  id: number;
  name: string;
  email: string;
  verified: boolean;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private http = inject(HttpClient);

  getAll(): Observable<CustomerSummary[]> {
    return this.http.get<CustomerSummary[]>(`${environment.apiUrl}/admin/customers`);
  }

  getSubscribers(): Observable<SubscriberSummary[]> {
    return this.http.get<SubscriberSummary[]>(`${environment.apiUrl}/admin/subscribers`);
  }
}
