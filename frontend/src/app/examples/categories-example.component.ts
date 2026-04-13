import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../services/product.service';
import { CategoryWithSubcategories } from '../services/product.model';

@Component({
  selector: 'app-categories-example',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="categories-container">
      <h2>Categories with Subcategories</h2>
      
      @if (isLoading) {
        <div class="loading">Loading categories...</div>
      } @else if (error) {
        <div class="error">Error: {{ error }}</div>
      } @else {
        <div class="categories-grid">
          @for (category of categoriesWithSubs; track category.id) {
            <div class="category-card">
              <h3>{{ category.name }}</h3>
              <div class="subcategories">
                @if (category.subcategories.length > 0) {
                  <h4>Subcategories:</h4>
                  <ul>
                    @for (subcategory of category.subcategories; track subcategory.id) {
                      <li>{{ subcategory.name }}</li>
                    }
                  </ul>
                } @else {
                  <p class="no-subcategories">No subcategories</p>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .categories-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-top: 20px;
    }

    .category-card {
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 20px;
      background: white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .category-card h3 {
      margin: 0 0 15px 0;
      color: #333;
      border-bottom: 2px solid #4f46e5;
      padding-bottom: 10px;
    }

    .subcategories h4 {
      margin: 15px 0 10px 0;
      color: #666;
      font-size: 14px;
      text-transform: uppercase;
    }

    .subcategories ul {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .subcategories li {
      padding: 5px 0;
      border-bottom: 1px solid #eee;
      color: #555;
    }

    .subcategories li:last-child {
      border-bottom: none;
    }

    .no-subcategories {
      color: #999;
      font-style: italic;
      margin: 10px 0;
    }

    .loading, .error {
      text-align: center;
      padding: 40px;
      font-size: 18px;
    }

    .error {
      color: #dc3545;
      background: #f8d7da;
      border: 1px solid #f5c6cb;
      border-radius: 4px;
    }

    .loading {
      color: #6c757d;
    }
  `]
})
export class CategoriesExampleComponent implements OnInit {
  categoriesWithSubs: CategoryWithSubcategories[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(private productService: ProductService) {}

  ngOnInit() {
    this.loadCategoriesWithSubcategories();
  }

  private loadCategoriesWithSubcategories() {
    this.isLoading = true;
    this.error = null;

    this.productService.getCategoriesWithSubcategories().subscribe({
      next: (data) => {
        this.categoriesWithSubs = data;
        this.isLoading = false;
        console.log('Categories with subcategories loaded:', data);
      },
      error: (error) => {
        this.error = 'Failed to load categories and subcategories';
        this.isLoading = false;
        console.error('Error loading categories with subcategories:', error);
      }
    });
  }
}
