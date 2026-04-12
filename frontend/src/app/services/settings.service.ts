import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Settings {
  id?: number;
  siteLogo?: string;
  siteFavicon?: string;
  siteImage?: string;
  siteName?: string;
  siteDescription?: string;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  facebookUrl?: string;
  instagramUrl?: string;
  whatsappUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SiteImage {
  id: number;
  imageUrl: string;
  displayOrder: number;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private http = inject(HttpClient);
  private adminUrl = `${environment.apiUrl}/admin/settings`;
  private publicUrl = `${environment.apiUrl}/settings`;
  private adminSiteImagesUrl = `${environment.apiUrl}/admin/site-images`;
  private publicSiteImagesUrl = `${environment.apiUrl}/site-images`;

  // ── Settings ────────────────────────────────────────────────────────────
  getSettings(): Observable<Settings> {
    return this.http.get<Settings>(this.adminUrl);
  }

  updateSettings(formData: FormData): Observable<Settings> {
    return this.http.put<Settings>(this.adminUrl, formData);
  }

  // ── Site Images ─────────────────────────────────────────────────────────
  getSiteImages(): Observable<SiteImage[]> {
    return this.http.get<SiteImage[]>(this.adminSiteImagesUrl);
  }

  addSiteImage(file: File, displayOrder: number): Observable<SiteImage> {
    const fd = new FormData();
    fd.append('image', file);
    fd.append('displayOrder', String(displayOrder));
    return this.http.post<SiteImage>(this.adminSiteImagesUrl, fd);
  }

  updateSiteImageOrder(id: number, displayOrder: number): Observable<SiteImage> {
    return this.http.patch<SiteImage>(`${this.adminSiteImagesUrl}/${id}/order`, { displayOrder });
  }

  deleteSiteImage(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminSiteImagesUrl}/${id}`);
  }
}
