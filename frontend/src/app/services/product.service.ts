import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Product {
  id: number;
  name: string;
  sku: string;
  description?: string;
  price: number;
  oldPrice?: number;
  stock: number;
  status: string;
  isFeatured: boolean;
  isExclusive: boolean;
  categoryId: number;
  categoryName?: string;
  subcategoryId?: number;
  subcategoryName?: string;
  brandId: number;
  brandName?: string;
  imageUrl?: string;
  hoverImageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductCreateRequest {
  name: string;
  sku: string;
  description?: string;
  price: number;
  oldPrice?: number;
  stock: number;
  status: string;
  isFeatured: boolean;
  isExclusive: boolean;
  categoryId: number;
  subcategoryId?: number;
  brandId: number;
  imageUrl?: string;
  hoverImageUrl?: string;
}

export interface ProductUpdateRequest {
  name: string;
  sku: string;
  description?: string;
  price: number;
  oldPrice?: number;
  stock: number;
  status: string;
  isFeatured: boolean;
  isExclusive: boolean;
  categoryId: number;
  subcategoryId?: number;
  brandId: number;
  imageUrl?: string;
  hoverImageUrl?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ImageUploadResponse {
  success: boolean;
  imageUrl?: string;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/admin/products`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('adminToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Get all products with pagination and filtering
  getAllProducts(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc',
    categoryId?: number,
    subcategoryId?: number,
    brandId?: number,
    status?: string,
    isFeatured?: boolean,
    isExclusive?: boolean
  ): Observable<PageResponse<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);

    if (categoryId !== undefined) params = params.set('categoryId', categoryId.toString());
    if (subcategoryId !== undefined) params = params.set('subcategoryId', subcategoryId.toString());
    if (brandId !== undefined) params = params.set('brandId', brandId.toString());
    if (status) params = params.set('status', status);
    if (isFeatured !== undefined) params = params.set('isFeatured', isFeatured.toString());
    if (isExclusive !== undefined) params = params.set('isExclusive', isExclusive.toString());

    return this.http.get<PageResponse<Product>>(`${this.apiUrl}`, {
      headers: this.getAuthHeaders(),
      params
    });
  }

  // Search products
  searchProducts(
    keyword: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Observable<PageResponse<Product>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.get<PageResponse<Product>>(`${this.apiUrl}/search`, {
      headers: this.getAuthHeaders(),
      params
    });
  }

  // Get product by ID
  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  // Create new product
  createProduct(product: ProductCreateRequest): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}`, product, {
      headers: this.getAuthHeaders()
    });
  }

  // Update product
  updateProduct(id: number, product: ProductUpdateRequest): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product, {
      headers: this.getAuthHeaders()
    });
  }

  // Delete product
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  // Update product status
  updateProductStatus(id: number, status: string): Observable<Product> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<Product>(`${this.apiUrl}/${id}/status`, null, {
      headers: this.getAuthHeaders(),
      params
    });
  }

  // Get product count
  getProductCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/count`, {
      headers: this.getAuthHeaders()
    });
  }

  // Upload product image
  uploadProductImage(productId: number, file: File): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('image', file);

    const token = localStorage.getItem('adminToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.post<ImageUploadResponse>(`${this.apiUrl}/${productId}/upload-image`, formData, {
      headers
    });
  }

  // Delete product image
  deleteProductImage(productId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}/image`, {
      headers: this.getAuthHeaders()
    });
  }
}
