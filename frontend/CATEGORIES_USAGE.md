# Categories with Subcategories - Usage Guide

## Overview
The ProductService now provides methods to fetch categories and subcategories from the database tables:
- `categories` table
- `subcategories` table

## Available Methods

### 1. Individual Methods
```typescript
// Get all categories
this.productService.getCategories().subscribe(categories => {
  console.log('Categories:', categories);
});

// Get all subcategories
this.productService.getSubcategories().subscribe(subcategories => {
  console.log('Subcategories:', subcategories);
});
```

### 2. Combined Method (Recommended)
```typescript
// Get categories with their subcategories in one call
this.productService.getCategoriesWithSubcategories().subscribe(categoriesWithSubs => {
  console.log('Categories with subcategories:', categoriesWithSubs);
  
  categoriesWithSubs.forEach(category => {
    console.log(`Category: ${category.name}`);
    category.subcategories.forEach(sub => {
      console.log(`  - Subcategory: ${sub.name}`);
    });
  });
});
```

## Data Structure

### CategoryOption
```typescript
interface CategoryOption {
  id: number;
  name: string;
}
```

### SubcategoryOption
```typescript
interface SubcategoryOption {
  id: number;
  name: string;
  categoryId: number;
}
```

### CategoryWithSubcategories
```typescript
interface CategoryWithSubcategories {
  id: number;
  name: string;
  subcategories: SubcategoryOption[];
}
```

## Example Component Usage

```typescript
import { Component, OnInit } from '@angular/core';
import { ProductService } from '../services/product.service';
import { CategoryWithSubcategories } from '../services/product.model';

@Component({
  selector: 'app-my-component',
  template: `
    <div>
      @for (category of categories; track category.id) {
        <div class="category">
          <h3>{{ category.name }}</h3>
          @for (subcategory of category.subcategories; track subcategory.id) {
            <div class="subcategory">{{ subcategory.name }}</div>
          }
        </div>
      }
    </div>
  `
})
export class MyComponent implements OnInit {
  categories: CategoryWithSubcategories[] = [];

  constructor(private productService: ProductService) {}

  ngOnInit() {
    this.productService.getCategoriesWithSubcategories().subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }
}
```

## API Endpoints Used

- `GET /api/categories` - Fetches all categories from the database
- `GET /api/subcategories` - Fetches all subcategories from the database (paginated)

## Performance Benefits

The `getCategoriesWithSubcategories()` method uses RxJS `forkJoin` to fetch both categories and subcategories in parallel, making it more efficient than sequential calls.

## Error Handling

Always include error handling when using these methods:

```typescript
this.productService.getCategoriesWithSubcategories().subscribe({
  next: (data) => {
    // Handle success
    this.categories = data;
  },
  error: (error) => {
    // Handle error
    console.error('Failed to load categories:', error);
    this.categories = []; // Set fallback
  }
});
```
