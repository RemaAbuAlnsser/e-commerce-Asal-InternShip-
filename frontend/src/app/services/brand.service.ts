import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Brand {
  id: number;
  name: string;
  logoUrl?: string;
  isActive: boolean;
  createdAt?: string;
}

export interface BrandCreateRequest {
  name: string;
  logoUrl?: string;
}

export interface BrandUpdateRequest {
  name: string;
  logoUrl?: string;
  isActive: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class BrandService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  getAllBrands(page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<PageResponse<Brand>> {
    const params = { page: page.toString(), size: size.toString(), sortBy, direction };
    return this.http.get<PageResponse<Brand>>(`${this.apiUrl}/brands`, { 
      headers: this.getHeaders(),
      params 
    });
  }

  getBrandById(id: number): Observable<Brand> {
    return this.http.get<Brand>(`${this.apiUrl}/brands/${id}`, {
      headers: this.getHeaders()
    });
  }

  createBrand(brand: BrandCreateRequest): Observable<Brand> {
    return this.http.post<Brand>(`${this.apiUrl}/brands`, brand, {
      headers: this.getHeaders()
    });
  }

  updateBrand(id: number, brand: BrandUpdateRequest): Observable<Brand> {
    return this.http.put<Brand>(`${this.apiUrl}/brands/${id}`, brand, {
      headers: this.getHeaders()
    });
  }

  deleteBrand(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/brands/${id}`, {
      headers: this.getHeaders()
    });
  }

  updateBrandStatus(id: number, isActive: boolean): Observable<Brand> {
    return this.http.patch<Brand>(`${this.apiUrl}/brands/${id}/status`, null, {
      headers: this.getHeaders(),
      params: { isActive: isActive.toString() }
    });
  }

  searchBrands(name: string, page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<PageResponse<Brand>> {
    const params = { name, page: page.toString(), size: size.toString(), sortBy, direction };
    return this.http.get<PageResponse<Brand>>(`${this.apiUrl}/brands/search`, {
      headers: this.getHeaders(),
      params
    });
  }

  getBrandCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/brands/count`, {
      headers: this.getHeaders()
    });
  }
}
