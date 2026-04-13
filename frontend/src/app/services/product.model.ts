// ─── Response Models (from backend) ──────────────────────────────────────────

export interface ProductColorImageResponse {
  id: number;
  imageUrl: string;
  sortOrder: number;
}

export interface ProductColorResponse {
  id: number;
  colorName: string;
  colorHex: string;
  stock: number;
  images: ProductColorImageResponse[];
}

export interface ProductResponse {
  id: number;
  name: string;
  sku: string;
  description: string;
  price: number;
  oldPrice: number | null;
  status: string;
  featured: boolean;
  exclusive: boolean;
  categoryId: number;
  categoryName: string;
  subcategoryId: number | null;
  subcategoryName: string | null;
  brandId: number | null;
  brandName: string | null;
  imageUrl: string | null;
  hoverImageUrl: string | null;
  totalStock: number;
  colors: ProductColorResponse[];
  createdAt: string;
  updatedAt: string;
}

// ─── Form Models (for building FormData) ─────────────────────────────────────

export interface ColorVariantForm {
  colorName: string;
  colorHex: string;
  stock: number;
  subImages: File[];
  subImagePreviews: string[];
}

export interface ProductForm {
  name: string;
  description: string;
  price: number | null;
  oldPrice: number | null;
  status: string;
  featured: boolean;
  exclusive: boolean;
  categoryId: number | null;
  subcategoryId: number | null;
  brandId: number | null;
  primeImageFile: File | null;
  primeImagePreview: string | null;
  hoverImageFile: File | null;
  hoverImagePreview: string | null;
  colors: ColorVariantForm[];
}

// ─── Category / Subcategory / Brand (for dropdowns) ──────────────────────────

export interface CategoryOption {
  id: number;
  name: string;
  slug?: string;
  description?: string;
  imageUrl?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SubcategoryOption {
  id: number;
  name: string;
  categoryId: number;
  slug?: string;
  description?: string;
  imageUrl?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
  categoryName?: string;
}

export interface CategoryWithSubcategories {
  id: number;
  name: string;
  slug?: string;
  description?: string;
  imageUrl?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
  subcategories: SubcategoryOption[];
}

export interface BrandOption {
  id: number;
  name: string;
}

// ─── Page response wrapper (for customer paginated endpoints) ─────────────────

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}