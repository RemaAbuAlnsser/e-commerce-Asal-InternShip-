import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HeaderComponent } from '../landing/header/header.component';
import { ProductService } from '../../services/product.service';
import { CategoryWithSubcategories, SubcategoryOption } from '../../services/product.model';

interface SubcategoryWithDetails extends SubcategoryOption {
  description?: string;
  productCount?: number;
}

interface CategoryWithDisplay extends CategoryWithSubcategories {
  description?: string;
  emoji?: string;
  productCount?: number;
  subcategoriesWithDetails?: SubcategoryWithDetails[];
}

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.css'
})
export class CategoriesComponent implements OnInit {
  readonly categories = signal<CategoryWithDisplay[]>([]);
  readonly selectedCategory = signal<CategoryWithDisplay | null>(null);
  readonly isLoading = signal(true);

  constructor(private router: Router, private productService: ProductService) {}

  ngOnInit() {
    this.loadCategories();
  }

  private loadCategories() {
    this.isLoading.set(true);
    
    // Use the new efficient method to load categories with subcategories from database
    this.productService.getCategoriesWithSubcategories()
      .subscribe({
        next: (categoriesWithSubs) => {
          console.log('Categories with subcategories loaded from database:', categoriesWithSubs);
          
          // Use database data directly without static enhancements
          const enhancedCategories: CategoryWithDisplay[] = categoriesWithSubs.map(category => {
            const enhancedSubcategories: SubcategoryWithDetails[] = category.subcategories.map(sub => ({
              ...sub,
              description: sub.description || 'Discover great products in this category',
              productCount: Math.floor(Math.random() * 50) + 5 // Mock count for now - replace with real count later
            }));

            return {
              ...category,
              description: category.description || 'Discover amazing products in this category',
              emoji: '🛍️', // Default emoji for all categories
              productCount: enhancedSubcategories.reduce((sum, sub) => sum + (sub.productCount || 0), 0),
              subcategoriesWithDetails: enhancedSubcategories,
              imageUrl: category.imageUrl ? this.productService.resolveImageUrl(category.imageUrl) : undefined
            };
          });
          
          this.categories.set(enhancedCategories);
          this.isLoading.set(false);
        },
        error: (error) => {
          console.error('Error loading categories with subcategories:', error);
          this.isLoading.set(false);
          // Set empty array on error
          this.categories.set([]);
        }
      });
  }


  selectCategory(category: CategoryWithDisplay) {
    this.selectedCategory.set(category);
  }

  goBack() {
    this.selectedCategory.set(null);
  }

  navigateToCategory(categoryId: number, subcategoryId?: number) {
    if (subcategoryId) {
      this.router.navigate(['/products'], { 
        queryParams: { category: categoryId, subcategory: subcategoryId } 
      });
    } else {
      this.router.navigate(['/products'], { 
        queryParams: { category: categoryId } 
      });
    }
  }

  getImageUrl(imageUrl?: string): string {
    return this.productService.resolveImageUrl(imageUrl || null);
  }
}
