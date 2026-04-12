import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface OrderItemResponse {
  id: number;
  productId: number;
  productColorId: number | null;
  productName: string;
  productPrice: number;
  colorName: string | null;
  quantity: number;
  subtotal: number;
  createdAt: string;
}

export interface OrderResponse {
  id: number;
  userId: number | null;
  customerName: string;
  customerPhone: string;
  customerCity: string;
  customerAddress: string;
  shippingMethod: string;
  shippingCost: number;
  paymentMethod: string;
  subtotal: number;
  total: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemResponse[];
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private ordersUrl = `${environment.apiUrl}/orders`;

  getAll(): Observable<OrderResponse[]> {
    return this.http
      .get<ApiResponse<OrderResponse[]>>(this.ordersUrl)
      .pipe(map(res => res.data));
  }

  getByStatus(status: string): Observable<OrderResponse[]> {
    return this.http
      .get<ApiResponse<OrderResponse[]>>(this.ordersUrl, { params: { status } })
      .pipe(map(res => res.data));
  }

  getById(id: number): Observable<OrderResponse> {
    return this.http
      .get<ApiResponse<OrderResponse>>(`${this.ordersUrl}/${id}`)
      .pipe(map(res => res.data));
  }

  updateStatus(id: number, status: string): Observable<OrderResponse> {
    return this.http
      .patch<ApiResponse<OrderResponse>>(`${this.ordersUrl}/${id}/status`, { status })
      .pipe(map(res => res.data));
  }

  delete(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.ordersUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
