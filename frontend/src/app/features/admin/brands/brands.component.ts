import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BrandService, Brand, BrandCreateRequest, BrandUpdateRequest, PageResponse } from '../../../services/brand.service';
import { ImageUploadService } from '../../../services/image-upload.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-brands',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './brands.component.html',
  styleUrl: './brands.component.css'
})
export class BrandsComponent implements OnInit {
  brands: Brand[] = [];
  selectedBrand: Brand | null = null;
  
  showBrandModal = false;
  showDeleteModal = false;
  
  isEditingBrand = false;
  
  brandForm: FormGroup;
  
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  totalBrands = 0;
  
  loading = false;
  submitting = false;
  
  searchTerm = '';
  
  itemToDelete: Brand | null = null;

  brandLogoFile: File | null = null;
  brandLogoPreview: string | null = null;

  constructor(
    private brandService: BrandService,
    private imageUploadService: ImageUploadService,
    private fb: FormBuilder
  ) {
    this.brandForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      logoUrl: [''],
      isActive: [true]
    });
  }

  ngOnInit() {
    this.loadBrands();
    this.loadCount();
  }
  
  loadCount() {
    this.brandService.getBrandCount().subscribe({
      next: (count) => {
        this.totalBrands = count;
      },
      error: (error) => {
        console.error('Error loading brand count:', error);
      }
    });
  }

  loadBrands() {
    this.loading = true;
    this.brandService.getAllBrands(this.currentPage, this.pageSize).subscribe({
      next: (response: PageResponse<Brand>) => {
        this.brands = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading brands:', error);
        this.loading = false;
        if (error.status === 401) {
          if (typeof window !== 'undefined') {
            window.location.href = '/admin/login';
          }
        }
      }
    });
  }

  openBrandModal(brand?: Brand) {
    this.isEditingBrand = !!brand;
    this.selectedBrand = brand || null;
    
    if (brand) {
      this.brandForm.patchValue({
        name: brand.name,
        logoUrl: brand.logoUrl || '',
        isActive: brand.isActive
      });
      
      if (brand.logoUrl) {
        this.brandLogoPreview = this.getImageUrl(brand.logoUrl);
        this.brandLogoFile = null;
      } else {
        this.brandLogoPreview = null;
        this.brandLogoFile = null;
      }
    } else {
      this.brandForm.reset();
      this.brandForm.patchValue({ isActive: true });
      this.brandLogoPreview = null;
      this.brandLogoFile = null;
    }
    
    this.showBrandModal = true;
  }

  saveBrandForm() {
    if (this.brandForm.valid) {
      this.submitting = true;
      const formData = this.brandForm.value;
      
      if (this.isEditingBrand && this.selectedBrand) {
        this.brandService.updateBrand(this.selectedBrand.id, formData).subscribe({
          next: () => {
            this.loadBrands();
            this.loadCount();
            this.closeBrandModal();
            this.submitting = false;
          },
          error: (error) => {
            console.error('Error updating brand:', error);
            this.submitting = false;
          }
        });
      } else {
        this.brandService.createBrand(formData).subscribe({
          next: () => {
            this.loadBrands();
            this.loadCount();
            this.closeBrandModal();
            this.submitting = false;
          },
          error: (error) => {
            console.error('Error creating brand:', error);
            this.submitting = false;
          }
        });
      }
    }
  }

  closeBrandModal() {
    this.showBrandModal = false;
    this.brandForm.reset();
    this.selectedBrand = null;
    this.isEditingBrand = false;
    this.brandLogoFile = null;
    this.brandLogoPreview = null;
  }

  confirmDelete(brand: Brand) {
    this.itemToDelete = brand;
    this.showDeleteModal = true;
  }

  executeDelete() {
    if (!this.itemToDelete) return;
    
    this.submitting = true;
    this.brandService.deleteBrand(this.itemToDelete.id).subscribe({
      next: () => {
        this.loadBrands();
        this.loadCount();
        this.closeDeleteModal();
        this.submitting = false;
      },
      error: (error) => {
        console.error('Error deleting brand:', error);
        this.submitting = false;
      }
    });
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.itemToDelete = null;
  }

  toggleBrandStatus(brand: Brand) {
    this.brandService.updateBrandStatus(brand.id, !brand.isActive).subscribe({
      next: () => {
        this.loadBrands();
      },
      error: (error) => {
        console.error('Error updating brand status:', error);
      }
    });
  }

  goToPage(page: number) {
    this.currentPage = page;
    this.loadBrands();
  }

  onSearch() {
    if (this.searchTerm.trim()) {
      this.brandService.searchBrands(this.searchTerm, this.currentPage, this.pageSize).subscribe({
        next: (response: PageResponse<Brand>) => {
          this.brands = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
        },
        error: (error) => {
          console.error('Error searching brands:', error);
        }
      });
    } else {
      this.loadBrands();
    }
  }

  clearSearch() {
    this.searchTerm = '';
    this.onSearch();
  }

  onBrandLogoSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
      }

      if (!file.type.startsWith('image/')) {
        alert('Please select a valid image file');
        return;
      }

      this.brandLogoFile = file;
      
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.brandLogoPreview = e.target.result;
      };
      reader.readAsDataURL(file);
      
      this.imageUploadService.uploadBrandLogo(file).subscribe({
        next: (response) => {
          if (response.success && response.imageUrl) {
            this.brandForm.patchValue({
              logoUrl: response.imageUrl
            });
          }
        },
        error: (error) => {
          console.error('Error uploading brand logo:', error);
          alert('Failed to upload logo. Please try again.');
          this.brandLogoFile = null;
          this.brandLogoPreview = null;
        }
      });
    }
  }

  removeBrandLogo() {
    this.brandLogoFile = null;
    this.brandLogoPreview = null;
    this.brandForm.patchValue({
      logoUrl: ''
    });
  }

  getImageUrl(imageUrl: string): string {
    if (!imageUrl) return '';
    
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      return imageUrl;
    }
    
    if (imageUrl.startsWith('/')) {
      return `${environment.backendUrl}${imageUrl}`;
    }
    
    return `${environment.backendUrl}/uploads/brands/${imageUrl}`;
  }
}
