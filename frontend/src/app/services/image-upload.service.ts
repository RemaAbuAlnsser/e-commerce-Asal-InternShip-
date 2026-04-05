import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ImageUploadResponse {
  success: boolean;
  message: string;
  imageUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImageUploadService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    // Note: Don't set Content-Type for multipart/form-data, let browser set it
    // Authorization will be handled by the auth interceptor
    return new HttpHeaders();
  }

  uploadCategoryImage(file: File): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('image', file);
    
    return this.http.post<ImageUploadResponse>(
      `${this.apiUrl}/categories/upload-image`, 
      formData, 
      { headers: this.getHeaders() }
    );
  }

  uploadSubcategoryImage(file: File): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('image', file);
    
    return this.http.post<ImageUploadResponse>(
      `${this.apiUrl}/subcategories/upload-image`, 
      formData, 
      { headers: this.getHeaders() }
    );
  }

  deleteImage(imageUrl: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/images`, {
      headers: this.getHeaders(),
      params: { imageUrl }
    });
  }
}
