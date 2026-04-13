import { Component, OnInit, OnDestroy, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { SettingsService, SiteImage } from '../../services/settings.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private settingsService = inject(SettingsService);
  private platformId = inject(PLATFORM_ID);
  private get isBrowser() { return isPlatformBrowser(this.platformId); }

  readonly siteImages = signal<SiteImage[]>([]);
  readonly activeIndex = signal(0);

  private slideInterval: ReturnType<typeof setInterval> | null = null;

  ngOnInit() {
    if (this.isBrowser) {
      this.loadSiteImages();
    }
  }

  ngOnDestroy() {
    this.stopAutoSlide();
  }

  private loadSiteImages() {
    this.settingsService.getPublicSiteImages().subscribe({
      next: (images) => {
        const sorted = [...images].sort((a, b) => a.displayOrder - b.displayOrder);
        this.siteImages.set(sorted);
        if (sorted.length > 1) {
          this.startAutoSlide();
        }

      },
      error: () => {
        this.siteImages.set([]);
      }
    });
  }

  private startAutoSlide() {
    if (!this.isBrowser) return;
    this.slideInterval = setInterval(() => {
      this.nextSlide();
    }, 3000);
  }

  private stopAutoSlide() {
    if (this.slideInterval) {
      clearInterval(this.slideInterval);
      this.slideInterval = null;
    }
  }

  nextSlide() {
    const images = this.siteImages();
    if (images.length === 0) return;
    this.activeIndex.set((this.activeIndex() + 1) % images.length);
  }

  prevSlide() {
    const images = this.siteImages();
    if (images.length === 0) return;
    this.activeIndex.set((this.activeIndex() - 1 + images.length) % images.length);
  }

  goToSlide(index: number) {
    this.stopAutoSlide();
    this.activeIndex.set(index);
    this.startAutoSlide();
  }

  getImageUrl(imageUrl: string): string {
    if (!imageUrl) return '';
    if (imageUrl.startsWith('http')) return imageUrl;
    return `${environment.backendUrl}${imageUrl}`;
  }

  goToAdmin() {
    this.router.navigate(['/admin']);
  }
}
