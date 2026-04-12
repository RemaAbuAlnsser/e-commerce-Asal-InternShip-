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

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private http = inject(HttpClient);

  getAll(): Observable<CustomerSummary[]> {
    return this.http.get<CustomerSummary[]>(`${environment.apiUrl}/admin/customers`);
  }
}
