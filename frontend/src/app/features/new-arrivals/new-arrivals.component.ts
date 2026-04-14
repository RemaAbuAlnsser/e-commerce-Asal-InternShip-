import { Component, OnInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HeaderComponent } from '../landing/header/header.component';
import { ProductService } from '../../services/product.service';
import { ProductResponse, CategoryOption } from '../../services/product.model';

@Component({
  selector: 'app-new-arrivals',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, HeaderComponent],
  templateUrl: './new-arrivals.component.html',
  styleUrl: './new-arrivals.component.css'
})
export class NewArrivalsComponent implements OnInit {
  readonly products = signal<ProductResponse[]>([]);
  readonly categories = signal<CategoryOption[]>([]);
  readonly selectedCategory = signal<number | null>(null);
  readonly isLoading = signal(true);
  readonly filteredProducts = signal<ProductResponse[]>([]);

  constructor(private productService: ProductService) {
    // Simplified effect with proper array handling
    effect(() => {
      const allProducts = this.products();
      const categoryId = this.selectedCategory();
      
      // Ensure we always work with a valid array
      const safeProducts = Array.isArray(allProducts) ? allProducts : [];
      
      let filtered: ProductResponse[];
      
      if (categoryId === null) {
        filtered = [...safeProducts];
      } else {
        filtered = safeProducts.filter(product => 
          product && product.categoryId === categoryId
        );
      }
      
      this.filteredProducts.set(filtered);
    }, { allowSignalWrites: true });
  }

  ngOnInit() {
    console.log('🚀 New Arrivals component initialized');
    this.loadNewArrivals();
    this.loadCategories();
  }

  private loadNewArrivals() {
    this.isLoading.set(true);
    console.log('🔄 Loading new arrivals...');
    
    // Add timeout to prevent infinite loading
    setTimeout(() => {
      if (this.isLoading()) {
        console.warn('⚠️ Loading timeout - forcing loading to false');
        this.isLoading.set(false);
      }
    }, 10000); // 10 second timeout
    
    this.productService.getNewArrivals(this.selectedCategory() || undefined, 0, 50)
      .subscribe({
        next: (response) => {
          console.log('✅ New arrivals loaded successfully:', response);
          
          // Ensure we always have a valid array
          const products = Array.isArray(response?.content) ? response.content : [];
          
          console.log('Setting products array:', products);
          this.products.set(products);
          this.isLoading.set(false);
        },
        error: (error) => {
          console.error('❌ Error loading new arrivals:', error);
          this.isLoading.set(false);
          this.products.set([]);
        }
      });
  }

  private loadCategories() {
    this.productService.getCategories()
      .subscribe({
        next: (categories) => {
          const safeCategories = Array.isArray(categories) ? categories : [];
          this.categories.set(safeCategories);
        },
        error: (error) => {
          console.error('Error loading categories:', error);
          this.categories.set([]);
        }
      });
  }

  onCategoryChange(categoryId: number | null) {
    console.log('Category changed to:', categoryId);
    this.selectedCategory.set(categoryId);
    // لا نحتاج لإعادة تحميل البيانات، الـ effect سيتولى الفلترة
  }

  addToCart(product: ProductResponse) {
    console.log('Added to cart:', product);
    // Implement add to cart functionality
  }

  addToWishlist(product: ProductResponse) {
    console.log('Added to wishlist:', product);
    // Implement add to wishlist functionality
  }

  getProductImage(product: ProductResponse): string {
    if (!product || !product.imageUrl) {
      return '/assets/images/placeholder.jpg'; // صورة افتراضية
    }
    return this.productService.resolveImageUrl(product.imageUrl);
  }

  getHoverImage(product: ProductResponse): string | null {
    if (!product || !product.hoverImageUrl) return null;
    return this.productService.resolveImageUrl(product.hoverImageUrl);
  }

  getDiscountPercentage(product: ProductResponse): number {
    if (!product || !product.oldPrice || !product.price) return 0;
    return Math.round(((product.oldPrice - product.price) / product.oldPrice) * 100);
  }
}
