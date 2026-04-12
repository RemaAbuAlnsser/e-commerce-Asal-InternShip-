import { Injectable, signal, inject } from '@angular/core';
import { SettingsService, Settings } from './settings.service';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SiteConfigService {
  private settingsService = inject(SettingsService);
  
  // Site configuration signals
  private _siteName = signal<string>('E-Commerce Admin');
  private _siteLogo = signal<string | null>(null);
  private _siteFavicon = signal<string | null>(null);
  private _settings = signal<Settings | null>(null);
  
  // Public readonly signals
  readonly siteName = this._siteName.asReadonly();
  readonly siteLogo = this._siteLogo.asReadonly();
  readonly siteFavicon = this._siteFavicon.asReadonly();
  readonly settings = this._settings.asReadonly();
  
  constructor() {
    this.loadSiteConfig();
  }
  
  /**
   * Load site configuration from backend
   */
  loadSiteConfig(): void {
    this.settingsService.getSettings().subscribe({
      next: (settings) => {
        this._settings.set(settings);
        this._siteName.set(settings.siteName || 'E-Commerce Admin');
        this._siteLogo.set(settings.siteLogo ? `${environment.backendUrl}${settings.siteLogo}` : null);
        this._siteFavicon.set(settings.siteFavicon ? `${environment.backendUrl}${settings.siteFavicon}` : null);
        
        // Update browser favicon
        this.updateBrowserFavicon(settings.siteFavicon || null);
      },
      error: (error) => {
        console.error('Failed to load site configuration:', error);
        // Keep default values on error
      }
    });
  }
  
  /**
   * Update site configuration (called after settings are saved)
   */
  updateSiteConfig(settings: Settings): void {
    this._settings.set(settings);
    this._siteName.set(settings.siteName || 'E-Commerce Admin');
    this._siteLogo.set(settings.siteLogo ? `${environment.backendUrl}${settings.siteLogo}` : null);
    this._siteFavicon.set(settings.siteFavicon ? `${environment.backendUrl}${settings.siteFavicon}` : null);
    
    // Update browser favicon
    this.updateBrowserFavicon(settings.siteFavicon || null);
  }
  
  /**
   * Get the full logo URL or null
   */
  getLogoUrl(): string | null {
    const logo = this._siteLogo();
    return logo;
  }
  
  /**
   * Get the site name
   */
  getSiteName(): string {
    return this._siteName();
  }
  
  /**
   * Get the favicon URL or null
   */
  getFaviconUrl(): string | null {
    return this._siteFavicon();
  }
  
  /**
   * Update the browser's favicon dynamically
   */
  private updateBrowserFavicon(faviconPath: string | null): void {
    if (typeof document === 'undefined') {
      return; // Skip if not in browser environment
    }
    
    // Remove existing favicon links
    const existingLinks = document.querySelectorAll('link[rel*="icon"]');
    existingLinks.forEach(link => link.remove());
    
    if (faviconPath) {
      // Add new favicon
      const faviconUrl = `${environment.backendUrl}${faviconPath}`;
      this.createFaviconLink(faviconUrl);
    } else {
      // Use default favicon
      this.createFaviconLink('/favicon.ico');
    }
  }
  
  /**
   * Create and append favicon link element
   */
  private createFaviconLink(href: string): void {
    const link = document.createElement('link');
    link.rel = 'icon';
    link.type = 'image/x-icon';
    link.href = href;
    document.head.appendChild(link);
    
    // Also create shortcut icon for better browser support
    const shortcutLink = document.createElement('link');
    shortcutLink.rel = 'shortcut icon';
    shortcutLink.type = 'image/x-icon';
    shortcutLink.href = href;
    document.head.appendChild(shortcutLink);
  }
}
