import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ProductResponse,
  PageResponse,
  CategoryOption,
  SubcategoryOption,
  BrandOption
} from './product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private http = inject(HttpClient);

  private adminUrl = `${environment.apiUrl}/admin/products`;
  private publicUrl = `${environment.apiUrl}/products`;

  // ===========================================================================
  // ADMIN ENDPOINTS
  // ===========================================================================

  /**
   * Create a new product with colors and images.
   * Uses multipart/form-data — Angular HttpClient handles boundary automatically.
   */
  createProduct(formData: FormData): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.adminUrl, formData);
  }

  /** Get all products (admin view, no pagination) */
  getAllProducts(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(this.adminUrl);
  }

  /** Get single product by id (admin — no status filter) */
  getProductById(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.adminUrl}/${id}`);
  }

  /**
   * Update basic product fields + landing images.
   * Only send the fields you want to change.
   */
  updateProduct(id: number, formData: FormData): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.adminUrl}/${id}`, formData);
  }

  /** Add a new color variant to an existing product */
  addColor(productId: number, formData: FormData): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(`${this.adminUrl}/${productId}/colors`, formData);
  }

  /** Update the stock of one color variant */
  updateColorStock(productId: number, colorId: number, stock: number): Observable<ProductResponse> {
    const params = new HttpParams().set('stock', stock.toString());
    return this.http.patch<ProductResponse>(
      `${this.adminUrl}/${productId}/colors/${colorId}/stock`,
      null,
      { params }
    );
  }

  /** Delete one color variant (and its sub-images) */
  deleteColor(productId: number, colorId: number): Observable<ProductResponse> {
    return this.http.delete<ProductResponse>(`${this.adminUrl}/${productId}/colors/${colorId}`);
  }

  /** Delete entire product */
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/${id}`);
  }

  // ===========================================================================
  // CUSTOMER / PUBLIC ENDPOINTS
  // ===========================================================================

  getById(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.publicUrl}/${id}`);
  }

  getBySku(sku: string): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.publicUrl}/sku/${sku}`);
  }

  getAll(
    filters: {
      categoryId?: number;
      subcategoryId?: number;
      brandId?: number;
      isFeatured?: boolean;
      isExclusive?: boolean;
    } = {},
    page = 0,
    size = 20
  ): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters.categoryId)    params = params.set('categoryId',    filters.categoryId.toString());
    if (filters.subcategoryId) params = params.set('subcategoryId', filters.subcategoryId.toString());
    if (filters.brandId)       params = params.set('brandId',       filters.brandId.toString());
    if (filters.isFeatured  != null) params = params.set('isFeatured',  filters.isFeatured.toString());
    if (filters.isExclusive != null) params = params.set('isExclusive', filters.isExclusive.toString());

    return this.http.get<PageResponse<ProductResponse>>(this.publicUrl, { params });
  }

  search(keyword: string, page = 0, size = 20): Observable<PageResponse<ProductResponse>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<ProductResponse>>(`${this.publicUrl}/search`, { params });
  }

  // ===========================================================================
  // DROPDOWN DATA (categories, subcategories, brands)
  // ===========================================================================

  getCategories(): Observable<CategoryOption[]> {
    return this.http.get<CategoryOption[]>(`${environment.apiUrl}/admin/categories`);
  }

  getSubcategories(): Observable<SubcategoryOption[]> {
    return this.http.get<SubcategoryOption[]>(`${environment.apiUrl}/admin/subcategories`);
  }

  getBrands(): Observable<BrandOption[]> {
    return this.http.get<BrandOption[]>(`${environment.apiUrl}/admin/brands`);
  }

  // ===========================================================================
  // FORM DATA BUILDER HELPERS
  // ===========================================================================

  /**
   * Build FormData for product creation.
   * colorImages[0] = sub-images for color at index 0, etc.
   */
  buildCreateFormData(
    fields: {
      name: string;
      description?: string;
      price: number;
      oldPrice?: number;
      status?: string;
      featured?: boolean;
      exclusive?: boolean;
      categoryId: number;
      subcategoryId?: number;
      brandId?: number;
    },
    primeImage: File | null,
    hoverImage: File | null,
    colors: Array<{
      colorName: string;
      colorHex: string;
      stock: number;
      subImages: File[];
    }>
  ): FormData {
    const fd = new FormData();

    fd.append('name',       fields.name);
    fd.append('price',      fields.price.toString());
    fd.append('categoryId', fields.categoryId.toString());
    fd.append('status',     fields.status ?? 'active');
    fd.append('featured',   (fields.featured ?? false).toString());
    fd.append('exclusive',  (fields.exclusive ?? false).toString());

    if (fields.description)   fd.append('description',   fields.description);
    if (fields.oldPrice)       fd.append('oldPrice',      fields.oldPrice.toString());
    if (fields.subcategoryId)  fd.append('subcategoryId', fields.subcategoryId.toString());
    if (fields.brandId)        fd.append('brandId',       fields.brandId.toString());

    if (primeImage) fd.append('primeImage', primeImage);
    if (hoverImage) fd.append('hoverImage', hoverImage);

    colors.forEach((color, i) => {
      fd.append('colorNames',  color.colorName);
      fd.append('colorHexes',  color.colorHex);
      fd.append('colorStocks', color.stock.toString());

      color.subImages.forEach(img => {
        fd.append(`colorImages[${i}]`, img);
      });
    });

    return fd;
  }

  /** Build FormData for adding a single color to an existing product */
  buildAddColorFormData(
    colorName: string,
    colorHex: string,
    stock: number,
    images: File[]
  ): FormData {
    const fd = new FormData();
    fd.append('colorName', colorName);
    fd.append('colorHex',  colorHex);
    fd.append('stock',     stock.toString());
    images.forEach(img => fd.append('images', img));
    return fd;
  }

  /** Resolve image URL to full backend URL */
  resolveImageUrl(path: string | null): string {
    if (!path) return 'assets/images/placeholder.png';
    if (path.startsWith('http')) return path;
    return `${environment.backendUrl}${path}`;
  }
}