import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string;
  imageUrl?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Subcategory {
  id: number;
  name: string;
  slug: string;
  description: string;
  imageUrl?: string;
  isActive: boolean;
  categoryId: number;
  categoryName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CategoryCreateRequest {
  name: string;
  slug?: string;
  description: string;
  imageUrl?: string;
}

export interface CategoryUpdateRequest {
  name: string;
  slug?: string;
  description: string;
  imageUrl?: string;
  isActive?: boolean;
}

export interface SubcategoryCreateRequest {
  name: string;
  slug?: string;
  description: string;
  imageUrl?: string;
  categoryId: number;
}

export interface SubcategoryUpdateRequest {
  name: string;
  slug?: string;
  description: string;
  imageUrl?: string;
  categoryId: number;
  isActive?: boolean;
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
export class CategoryService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  // Category CRUD operations
  getAllCategories(page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<PageResponse<Category>> {
    const params = { page: page.toString(), size: size.toString(), sortBy, direction };
    return this.http.get<PageResponse<Category>>(`${this.apiUrl}/categories`, { 
      headers: this.getHeaders(),
      params 
    });
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/categories/${id}`, {
      headers: this.getHeaders()
    });
  }

  createCategory(category: CategoryCreateRequest): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/categories`, category, {
      headers: this.getHeaders()
    });
  }

  updateCategory(id: number, category: CategoryUpdateRequest): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/categories/${id}`, category, {
      headers: this.getHeaders()
    });
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`, {
      headers: this.getHeaders()
    });
  }

  updateCategoryStatus(id: number, isActive: boolean): Observable<Category> {
    return this.http.patch<Category>(`${this.apiUrl}/categories/${id}/status`, null, {
      headers: this.getHeaders(),
      params: { isActive: isActive.toString() }
    });
  }

  searchCategories(name: string, page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<PageResponse<Category>> {
    const params = { name, page: page.toString(), size: size.toString(), sortBy, direction };
    return this.http.get<PageResponse<Category>>(`${this.apiUrl}/categories/search`, {
      headers: this.getHeaders(),
      params
    });
  }

  // Subcategory CRUD operations
  getAllSubcategories(page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<PageResponse<Subcategory>> {
    const params = { page: page.toString(), size: size.toString(), sortBy, direction };
    return this.http.get<PageResponse<Subcategory>>(`${this.apiUrl}/subcategories`, {
      headers: this.getHeaders(),
      params
    });
  }

  getSubcategoriesByCategory(categoryId: number, page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<PageResponse<Subcategory>> {
    const params = { page: page.toString(), size: size.toString(), sortBy, direction };
    return this.http.get<PageResponse<Subcategory>>(`${this.apiUrl}/subcategories/category/${categoryId}`, {
      headers: this.getHeaders(),
      params
    });
  }

  getSubcategoryById(id: number): Observable<Subcategory> {
    return this.http.get<Subcategory>(`${this.apiUrl}/subcategories/${id}`, {
      headers: this.getHeaders()
    });
  }

  createSubcategory(subcategory: SubcategoryCreateRequest): Observable<Subcategory> {
    return this.http.post<Subcategory>(`${this.apiUrl}/subcategories`, subcategory, {
      headers: this.getHeaders()
    });
  }

  updateSubcategory(id: number, subcategory: SubcategoryUpdateRequest): Observable<Subcategory> {
    return this.http.put<Subcategory>(`${this.apiUrl}/subcategories/${id}`, subcategory, {
      headers: this.getHeaders()
    });
  }

  deleteSubcategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/subcategories/${id}`, {
      headers: this.getHeaders()
    });
  }

  updateSubcategoryStatus(id: number, isActive: boolean): Observable<Subcategory> {
    return this.http.patch<Subcategory>(`${this.apiUrl}/subcategories/${id}/status`, null, {
      headers: this.getHeaders(),
      params: { isActive: isActive.toString() }
    });
  }

  searchSubcategories(name: string, page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<PageResponse<Subcategory>> {
    const params = { name, page: page.toString(), size: size.toString(), sortBy, direction };
    return this.http.get<PageResponse<Subcategory>>(`${this.apiUrl}/subcategories/search`, {
      headers: this.getHeaders(),
      params
    });
  }

  // Count methods
  getCategoryCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/categories/count`, {
      headers: this.getHeaders()
    });
  }

  getSubcategoryCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/subcategories/count`, {
      headers: this.getHeaders()
    });
  }

  getSubcategoryCountByCategory(categoryId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/subcategories/count/category/${categoryId}`, {
      headers: this.getHeaders()
    });
  }
}
