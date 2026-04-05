import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CategoryService, Category, Subcategory, CategoryCreateRequest, CategoryUpdateRequest, SubcategoryCreateRequest, SubcategoryUpdateRequest, PageResponse } from '../../../services/category.service';
import { ImageUploadService } from '../../../services/image-upload.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.css'
})
export class CategoriesComponent implements OnInit {
  categories: Category[] = [];
  subcategories: Subcategory[] = [];
  selectedCategory: Category | null = null;
  selectedSubcategory: Subcategory | null = null;
  
  // Modal states
  showCategoryModal = false;
  showSubcategoryModal = false;
  showDeleteModal = false;
  
  // Edit modes
  isEditingCategory = false;
  isEditingSubcategory = false;
  
  // Forms
  categoryForm: FormGroup;
  subcategoryForm: FormGroup;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Separate counters for display
  totalCategories = 0;
  totalSubcategories = 0;
  
  // Loading states
  loading = false;
  submitting = false;
  
  // Search
  searchTerm = '';
  
  // Delete confirmation
  itemToDelete: { type: 'category' | 'subcategory', item: Category | Subcategory } | null = null;
  
  // View mode
  viewMode: 'categories' | 'subcategories' = 'categories';

  // Image upload properties
  categoryImageFile: File | null = null;
  categoryImagePreview: string | null = null;
  subcategoryImageFile: File | null = null;
  subcategoryImagePreview: string | null = null;

  constructor(
    private categoryService: CategoryService,
    private imageUploadService: ImageUploadService,
    private fb: FormBuilder
  ) {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
      imageUrl: [''],
      isActive: [true]
    });
    
    this.subcategoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
      imageUrl: [''],
      categoryId: ['', [Validators.required]],
      isActive: [true]
    });
  }

  ngOnInit() {
    this.loadCategories();
    this.loadCounts();
  }
  
  loadCounts() {
    this.categoryService.getCategoryCount().subscribe({
      next: (count) => {
        this.totalCategories = count;
      },
      error: (error) => {
        console.error('Error loading category count:', error);
      }
    });
    
    this.categoryService.getSubcategoryCount().subscribe({
      next: (count) => {
        this.totalSubcategories = count;
      },
      error: (error) => {
        console.error('Error loading subcategory count:', error);
      }
    });
  }

  loadCategories() {
    this.loading = true;
    this.categoryService.getAllCategories(this.currentPage, this.pageSize).subscribe({
      next: (response: PageResponse<Category>) => {
        this.categories = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.loading = false;
        if (error.status === 401) {
          // Redirect to login if unauthorized
          if (typeof window !== 'undefined') {
            window.location.href = '/admin/login';
          }
        }
      }
    });
  }

  loadSubcategories(categoryId?: number) {
    this.loading = true;
    const request = categoryId 
      ? this.categoryService.getSubcategoriesByCategory(categoryId, this.currentPage, this.pageSize)
      : this.categoryService.getAllSubcategories(this.currentPage, this.pageSize);
    
    request.subscribe({
      next: (response: PageResponse<Subcategory>) => {
        this.subcategories = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading subcategories:', error);
        this.loading = false;
        if (error.status === 401) {
          // Redirect to login if unauthorized
          if (typeof window !== 'undefined') {
            window.location.href = '/admin/login';
          }
        }
      }
    });
  }

  // Category operations
  openCategoryModal(category?: Category) {
    this.isEditingCategory = !!category;
    this.selectedCategory = category || null;
    
    if (category) {
      this.categoryForm.patchValue({
        name: category.name,
        description: category.description,
        imageUrl: category.imageUrl || '',
        isActive: category.isActive
      });
      
      // Show current image in preview if exists
      if (category.imageUrl) {
        this.categoryImagePreview = this.getImageUrl(category.imageUrl);
        this.categoryImageFile = null; // Reset file since we're showing existing image
      } else {
        this.categoryImagePreview = null;
        this.categoryImageFile = null;
      }
    } else {
      this.categoryForm.reset();
      this.categoryForm.patchValue({ isActive: true }); // Default to active for new categories
      this.categoryImagePreview = null;
      this.categoryImageFile = null;
    }
    
    this.showCategoryModal = true;
  }

  saveCategoryForm() {
    if (this.categoryForm.valid) {
      this.submitting = true;
      const formData = this.categoryForm.value;
      
      if (this.isEditingCategory && this.selectedCategory) {
        this.categoryService.updateCategory(this.selectedCategory.id, formData).subscribe({
          next: () => {
            this.loadCategories();
            this.loadCounts();
            this.closeCategoryModal();
            this.submitting = false;
          },
          error: (error) => {
            console.error('Error updating category:', error);
            this.submitting = false;
          }
        });
      } else {
        this.categoryService.createCategory(formData).subscribe({
          next: () => {
            this.loadCategories();
            this.loadCounts();
            this.closeCategoryModal();
            this.submitting = false;
          },
          error: (error) => {
            console.error('Error creating category:', error);
            this.submitting = false;
          }
        });
      }
    }
  }

  closeCategoryModal() {
    this.showCategoryModal = false;
    this.categoryForm.reset();
    this.selectedCategory = null;
    this.isEditingCategory = false;
    this.categoryImageFile = null;
    this.categoryImagePreview = null;
  }

  // Subcategory operations
  openSubcategoryModal(subcategory?: Subcategory) {
    this.isEditingSubcategory = !!subcategory;
    if (subcategory) {
      this.selectedSubcategory = subcategory;
      this.subcategoryForm.patchValue({
        name: subcategory.name,
        description: subcategory.description,
        imageUrl: subcategory.imageUrl || '',
        categoryId: subcategory.categoryId,
        isActive: subcategory.isActive
      });
      // Show current image in preview if exists
      if (subcategory.imageUrl) {
        this.subcategoryImagePreview = this.getImageUrl(subcategory.imageUrl);
        this.subcategoryImageFile = null;
      } else {
        this.subcategoryImagePreview = null;
        this.subcategoryImageFile = null;
      }
    } else {
      this.selectedSubcategory = null;
      this.subcategoryForm.reset();
      this.subcategoryForm.patchValue({ isActive: true });
      this.subcategoryImagePreview = null;
      this.subcategoryImageFile = null;
    }
    this.showSubcategoryModal = true;
  }

  saveSubcategoryForm() {
    if (this.subcategoryForm.valid) {
      this.submitting = true;
      const formData = this.subcategoryForm.value;
      
      if (this.isEditingSubcategory && this.selectedSubcategory) {
        this.categoryService.updateSubcategory(this.selectedSubcategory.id, formData).subscribe({
          next: () => {
            this.loadSubcategories(this.selectedCategory?.id);
            this.loadCounts();
            this.closeSubcategoryModal();
            this.submitting = false;
          },
          error: (error) => {
            console.error('Error updating subcategory:', error);
            this.submitting = false;
          }
        });
      } else {
        this.categoryService.createSubcategory(formData).subscribe({
          next: () => {
            this.loadSubcategories();
            this.loadCounts();
            this.closeSubcategoryModal();
            this.submitting = false;
          },
          error: (error) => {
            console.error('Error creating subcategory:', error);
            this.submitting = false;
          }
        });
      }
    }
  }

  closeSubcategoryModal() {
    this.showSubcategoryModal = false;
    this.subcategoryForm.reset();
    this.selectedSubcategory = null;
    this.isEditingSubcategory = false;
    this.subcategoryImageFile = null;
    this.subcategoryImagePreview = null;
  }

  // Delete operations
  confirmDelete(type: 'category' | 'subcategory', item: Category | Subcategory) {
    this.itemToDelete = { type, item };
    this.showDeleteModal = true;
  }

  executeDelete() {
    if (!this.itemToDelete) return;
    
    this.submitting = true;
    if (this.itemToDelete.type === 'category') {
      this.categoryService.deleteCategory(this.itemToDelete.item.id).subscribe({
        next: () => {
          this.loadCategories();
          this.loadCounts();
          this.closeDeleteModal();
          this.submitting = false;
        },
        error: (error) => {
          console.error('Error deleting category:', error);
          this.submitting = false;
        }
      });
    } else {
      this.categoryService.deleteSubcategory(this.itemToDelete.item.id).subscribe({
        next: () => {
          this.loadSubcategories();
          this.loadCounts();
          this.closeDeleteModal();
          this.submitting = false;
        },
        error: (error) => {
          console.error('Error deleting subcategory:', error);
          this.submitting = false;
        }
      });
    }
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.itemToDelete = null;
  }

  // Status toggle
  toggleCategoryStatus(category: Category) {
    this.categoryService.updateCategoryStatus(category.id, !category.isActive).subscribe({
      next: () => {
        this.loadCategories();
      },
      error: (error) => {
        console.error('Error updating category status:', error);
      }
    });
  }

  toggleSubcategoryStatus(subcategory: Subcategory) {
    this.categoryService.updateSubcategoryStatus(subcategory.id, !subcategory.isActive).subscribe({
      next: () => {
        this.loadSubcategories();
      },
      error: (error) => {
        console.error('Error updating subcategory status:', error);
      }
    });
  }

  // View mode switching
  switchToCategories() {
    this.selectedCategory = null;
    this.viewMode = 'categories';
    this.currentPage = 0;
    this.loadCategories();
    
    // Reload total subcategory count when returning to categories view
    this.categoryService.getSubcategoryCount().subscribe({
      next: (count) => {
        this.totalSubcategories = count;
      },
      error: (error) => {
        console.error('Error loading total subcategory count:', error);
      }
    });
  }

  switchToSubcategories() {
    this.selectedCategory = null;
    this.viewMode = 'subcategories';
    this.currentPage = 0;
    this.loadSubcategories();
    
    // Load total subcategory count
    this.categoryService.getSubcategoryCount().subscribe({
      next: (count) => {
        this.totalSubcategories = count;
      },
      error: (error) => {
        console.error('Error loading total subcategory count:', error);
      }
    });
  }

  viewCategorySubcategories(category: Category) {
    this.selectedCategory = category;
    this.viewMode = 'subcategories';
    this.currentPage = 0;
    this.loadSubcategories(category.id);
    
    // Load subcategory count for this specific category
    this.categoryService.getSubcategoryCountByCategory(category.id).subscribe({
      next: (count) => {
        this.totalSubcategories = count;
      },
      error: (error) => {
        console.error('Error loading subcategory count for category:', error);
      }
    });
  }

  // Pagination
  goToPage(page: number) {
    this.currentPage = page;
    if (this.viewMode === 'categories') {
      this.loadCategories();
    } else {
      this.loadSubcategories(this.selectedCategory?.id);
    }
  }

  // Search
  onSearch() {
    if (this.searchTerm.trim()) {
      if (this.viewMode === 'categories') {
        this.categoryService.searchCategories(this.searchTerm, this.currentPage, this.pageSize).subscribe({
          next: (response: PageResponse<Category>) => {
            this.categories = response.content;
            this.totalElements = response.totalElements;
            this.totalPages = response.totalPages;
          },
          error: (error) => {
            console.error('Error searching categories:', error);
          }
        });
      } else {
        this.categoryService.searchSubcategories(this.searchTerm, this.currentPage, this.pageSize).subscribe({
          next: (response: PageResponse<Subcategory>) => {
            this.subcategories = response.content;
            this.totalElements = response.totalElements;
            this.totalPages = response.totalPages;
          },
          error: (error) => {
            console.error('Error searching subcategories:', error);
          }
        });
      }
    } else {
      if (this.viewMode === 'categories') {
        this.loadCategories();
      } else {
        this.loadSubcategories(this.selectedCategory?.id);
      }
    }
  }

  clearSearch() {
    this.searchTerm = '';
    this.onSearch();
  }

  // Image upload methods
  onCategoryImageSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Validate file size (10MB max)
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
      }

      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Please select a valid image file');
        return;
      }

      this.categoryImageFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.categoryImagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
      
      // Upload image to server
      this.imageUploadService.uploadCategoryImage(file).subscribe({
        next: (response) => {
          if (response.success && response.imageUrl) {
            this.categoryForm.patchValue({
              imageUrl: response.imageUrl
            });
          }
        },
        error: (error) => {
          console.error('Error uploading category image:', error);
          alert('Failed to upload image. Please try again.');
          // Reset the image preview on error
          this.categoryImageFile = null;
          this.categoryImagePreview = null;
        }
      });
    }
  }

  onSubcategoryImageSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Validate file size (10MB max)
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
      }

      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Please select a valid image file');
        return;
      }

      this.subcategoryImageFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.subcategoryImagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
      
      // Upload image to server
      this.imageUploadService.uploadSubcategoryImage(file).subscribe({
        next: (response) => {
          if (response.success && response.imageUrl) {
            this.subcategoryForm.patchValue({
              imageUrl: response.imageUrl
            });
          }
        },
        error: (error) => {
          console.error('Error uploading subcategory image:', error);
          alert('Failed to upload image. Please try again.');
          // Reset the image preview on error
          this.subcategoryImageFile = null;
          this.subcategoryImagePreview = null;
        }
      });
    }
  }

  removeCategoryImage() {
    this.categoryImageFile = null;
    this.categoryImagePreview = null;
    this.categoryForm.patchValue({
      imageUrl: ''
    });
  }

  removeSubcategoryImage() {
    this.subcategoryImageFile = null;
    this.subcategoryImagePreview = null;
    this.subcategoryForm.patchValue({
      imageUrl: ''
    });
  }

  getImageUrl(imageUrl: string): string {
    if (!imageUrl) return '';
    
    // If it's already a full URL, return as is
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      return imageUrl;
    }
    
    // If it's a relative path starting with /, construct the full URL
    if (imageUrl.startsWith('/')) {
      return `${environment.backendUrl}${imageUrl}`;
    }
    
    // If it's just a filename, construct the full path
    return `${environment.backendUrl}/uploads/categories/${imageUrl}`;
  }
}
