import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Settings, SiteImage, SettingsService } from '../../../services/settings.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {
  activeTab = signal<'general' | 'contact' | 'social' | 'images'>('general');

  loading = signal(false);
  submitting = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  // ── Settings form ────────────────────────────────────────────────────────
  form: Settings = {};

  // previews for image fields
  logoPreview: string | null = null;
  faviconPreview: string | null = null;
  siteImagePreview: string | null = null;

  // selected files
  logoFile: File | null = null;
  faviconFile: File | null = null;
  siteImageFile: File | null = null;

  // ── Site Images ─────────────────────────────────────────────────────────
  siteImages = signal<SiteImage[]>([]);
  siteImagesLoading = signal(false);
  siteImageUploading = signal(false);
  newSiteImageFile: File | null = null;
  newSiteImageOrder = 0;
  newSiteImagePreview: string | null = null;

  deleteModalOpen = false;
  siteImageToDelete: SiteImage | null = null;

  readonly backendUrl = environment.backendUrl;

  constructor(private settingsService: SettingsService) {}

  ngOnInit(): void {
    this.loadSettings();
    this.loadSiteImages();
  }

  setTab(tab: 'general' | 'contact' | 'social' | 'images'): void {
    this.activeTab.set(tab);
  }

  // ── Load ─────────────────────────────────────────────────────────────────
  loadSettings(): void {
    this.loading.set(true);
    this.settingsService.getSettings().subscribe({
      next: (res) => {
        this.form = { ...res };
        this.logoPreview = res.siteLogo ? `${this.backendUrl}${res.siteLogo}` : null;
        this.faviconPreview = res.siteFavicon ? `${this.backendUrl}${res.siteFavicon}` : null;
        this.siteImagePreview = res.siteImage ? `${this.backendUrl}${res.siteImage}` : null;
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  loadSiteImages(): void {
    this.siteImagesLoading.set(true);
    this.settingsService.getSiteImages().subscribe({
      next: (res) => {
        this.siteImages.set(res);
        this.siteImagesLoading.set(false);
      },
      error: () => this.siteImagesLoading.set(false)
    });
  }

  // ── File pickers ─────────────────────────────────────────────────────────
  onLogoChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.logoFile = file;
    this.logoPreview = URL.createObjectURL(file);
  }

  onFaviconChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.faviconFile = file;
    this.faviconPreview = URL.createObjectURL(file);
  }

  onSiteImageChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.siteImageFile = file;
    this.siteImagePreview = URL.createObjectURL(file);
  }

  onNewSiteImageChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.newSiteImageFile = file;
    this.newSiteImagePreview = URL.createObjectURL(file);
  }

  clearLogo(): void {
    this.logoFile = null;
    this.logoPreview = this.form.siteLogo ? `${this.backendUrl}${this.form.siteLogo}` : null;
  }

  clearFavicon(): void {
    this.faviconFile = null;
    this.faviconPreview = this.form.siteFavicon ? `${this.backendUrl}${this.form.siteFavicon}` : null;
  }

  clearSiteImage(): void {
    this.siteImageFile = null;
    this.siteImagePreview = this.form.siteImage ? `${this.backendUrl}${this.form.siteImage}` : null;
  }

  // ── Save settings ─────────────────────────────────────────────────────────
  saveSettings(): void {
    this.submitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    const fd = new FormData();

    // text fields as JSON blob
    const data = {
      siteName: this.form.siteName || '',
      siteDescription: this.form.siteDescription || '',
      contactEmail: this.form.contactEmail || '',
      contactPhone: this.form.contactPhone || '',
      address: this.form.address || '',
      facebookUrl: this.form.facebookUrl || '',
      instagramUrl: this.form.instagramUrl || '',
      whatsappUrl: this.form.whatsappUrl || ''
    };
    fd.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));

    if (this.logoFile) fd.append('logo', this.logoFile);
    if (this.faviconFile) fd.append('favicon', this.faviconFile);
    if (this.siteImageFile) fd.append('siteImage', this.siteImageFile);

    this.settingsService.updateSettings(fd).subscribe({
      next: (res) => {
        this.form = { ...res };
        this.logoFile = null;
        this.faviconFile = null;
        this.siteImageFile = null;
        this.logoPreview = res.siteLogo ? `${this.backendUrl}${res.siteLogo}` : null;
        this.faviconPreview = res.siteFavicon ? `${this.backendUrl}${res.siteFavicon}` : null;
        this.siteImagePreview = res.siteImage ? `${this.backendUrl}${res.siteImage}` : null;
        this.submitting.set(false);
        this.successMessage.set('Settings saved successfully.');
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to save settings.');
      }
    });
  }

  // ── Site Images actions ───────────────────────────────────────────────────
  uploadSiteImage(): void {
    if (!this.newSiteImageFile) return;
    this.siteImageUploading.set(true);

    this.settingsService.addSiteImage(this.newSiteImageFile, this.newSiteImageOrder).subscribe({
      next: () => {
        this.newSiteImageFile = null;
        this.newSiteImagePreview = null;
        this.newSiteImageOrder = 0;
        this.siteImageUploading.set(false);
        this.loadSiteImages();
        this.successMessage.set('Image added successfully.');
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: () => {
        this.siteImageUploading.set(false);
        this.errorMessage.set('Failed to upload image.');
      }
    });
  }

  openDeleteSiteImageModal(img: SiteImage): void {
    this.siteImageToDelete = img;
    this.deleteModalOpen = true;
  }

  closeDeleteModal(): void {
    this.siteImageToDelete = null;
    this.deleteModalOpen = false;
  }

  confirmDeleteSiteImage(): void {
    if (!this.siteImageToDelete) return;
    this.settingsService.deleteSiteImage(this.siteImageToDelete.id).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.loadSiteImages();
        this.successMessage.set('Image deleted.');
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: () => {
        this.errorMessage.set('Failed to delete image.');
      }
    });
  }

  updateOrder(img: SiteImage, event: Event): void {
    const val = Number((event.target as HTMLInputElement).value);
    this.settingsService.updateSiteImageOrder(img.id, val).subscribe({
      next: () => this.loadSiteImages()
    });
  }

  trackBySiteImageId(_: number, img: SiteImage): number {
    return img.id;
  }
}
