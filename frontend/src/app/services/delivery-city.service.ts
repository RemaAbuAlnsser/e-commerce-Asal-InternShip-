import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DeliveryCity {
  id: number;
  cityName: string;
  deliveryPrice: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface DeliveryCityRequest {
  cityName: string;
  deliveryPrice: number;
}

@Injectable({
  providedIn: 'root'
})
export class DeliveryCityService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  private adminUrl = `${this.apiUrl}/admin/delivery-cities`;
  private customerUrl = `${this.apiUrl}/customer/delivery-cities`;

  getAllAdmin(): Observable<DeliveryCity[]> {
    return this.http.get<DeliveryCity[]>(this.adminUrl);
  }

  getByIdAdmin(id: number): Observable<DeliveryCity> {
    return this.http.get<DeliveryCity>(`${this.adminUrl}/${id}`);
  }

  create(city: DeliveryCityRequest): Observable<DeliveryCity> {
    return this.http.post<DeliveryCity>(this.adminUrl, city);
  }

  update(id: number, city: DeliveryCityRequest): Observable<DeliveryCity> {
    return this.http.put<DeliveryCity>(`${this.adminUrl}/${id}`, city);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/${id}`);
  }

  getAllCustomer(): Observable<DeliveryCity[]> {
    return this.http.get<DeliveryCity[]>(this.customerUrl);
  }

  getByIdCustomer(id: number): Observable<DeliveryCity> {
    return this.http.get<DeliveryCity>(`${this.customerUrl}/${id}`);
  }
}
