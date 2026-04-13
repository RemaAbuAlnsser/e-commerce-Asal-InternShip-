import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  ProductResponse,
  PageResponse,
  CategoryOption,
  SubcategoryOption,
  CategoryWithSubcategories,
  BrandOption
} from './product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private http = inject(HttpClient);

  private adminUrl = `${environment.apiUrl}/admin/products`;
  private publicUrl = `${environment.apiUrl}/products`;

  // Cached observables — HTTP is only called once for the lifetime of the service
  private categories$: Observable<CategoryOption[]> | null = null;
  private subcategories$: Observable<SubcategoryOption[]> | null = null;

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
  // NEW ARRIVALS & OFFERS
  // ===========================================================================

  /**
   * Get products created within the last week (new arrivals)
   * Uses existing getAll endpoint with client-side filtering
   */
  getNewArrivals(
    categoryId?: number,
    page = 0,
    size = 50
  ): Observable<PageResponse<ProductResponse>> {
    const filters: any = {};
    if (categoryId) {
      filters.categoryId = categoryId;
    }

    return this.getAll(filters, page, size).pipe(
      map((response: PageResponse<ProductResponse>) => {
        console.log('Raw API response received, products count:', response?.content?.length || 0);
        
        // التأكد من صحة البيانات المُرجعة
        if (!response || !response.content) {
          console.error('❌ Invalid API response structure:', response);
          return {
            content: [],
            totalElements: 0,
            totalPages: 0,
            size: size,
            number: page,
            first: true,
            last: true
          };
        }

        // التأكد من أن content هو array
        if (!Array.isArray(response.content)) {
          console.error('❌ response.content is not an array:', response.content);
          return {
            ...response,
            content: [],
            totalElements: 0,
            totalPages: 0
          };
        }
        
        // حساب تاريخ قبل 7 أيام من اليوم
        const oneWeekAgo = new Date();
        oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
        
        // فلترة المنتجات التي تم إنشاؤها خلال آخر 7 أيام
        const filteredProducts = response.content.filter((product: ProductResponse) => {
          const createdAt = new Date(product.createdAt);
          const isNewArrival = createdAt >= oneWeekAgo;
          
          console.log(`${product.name}: created ${createdAt.toISOString()}, isNew: ${isNewArrival}`);
          
          return isNewArrival;
        });

        console.log(`✅ Found ${filteredProducts.length} new arrivals out of ${response.content.length} total products`);

        // إذا لم نجد منتجات جديدة، نأخذ أحدث 3 منتجات للعرض
        if (filteredProducts.length === 0) {
          console.log('⚠️ No products found within 7 days, showing latest 3 products instead');
          
          // التأكد من أن response.content هو array صالح
          const products = Array.isArray(response.content) ? response.content : [];
          
          if (products.length > 0) {
            const sortedByDate = [...products].sort((a, b) => 
              new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
            );
            const latestProducts = sortedByDate.slice(0, 3);
            
            return {
              ...response,
              content: latestProducts,
              totalElements: latestProducts.length,
              totalPages: 1
            };
          } else {
            // إرجاع array فارغ إذا لم توجد منتجات
            return {
              ...response,
              content: [],
              totalElements: 0,
              totalPages: 0
            };
          }
        }

        return {
          ...response,
          content: filteredProducts,
          totalElements: filteredProducts.length,
          totalPages: Math.ceil(filteredProducts.length / size)
        };
      })
    );
  }

  /**
   * Get products on sale (with oldPrice)
   * Uses existing getAll endpoint with client-side filtering
   */
  getOffers(
    categoryId?: number,
    page = 0,
    size = 50
  ): Observable<PageResponse<ProductResponse>> {
    const filters: any = {};
    if (categoryId) {
      filters.categoryId = categoryId;
    }

    return this.getAll(filters, page, size).pipe(
      map((response: PageResponse<ProductResponse>) => {
        console.log('Raw API response for offers received, products count:', response?.content?.length || 0);
        
        // التأكد من صحة البيانات المُرجعة
        if (!response || !response.content || !Array.isArray(response.content)) {
          console.error('❌ Invalid API response for offers:', response);
          return {
            content: [],
            totalElements: 0,
            totalPages: 0,
            size: size,
            number: page,
            first: true,
            last: true
          };
        }

        // Filter products that have oldPrice (on sale)
        const saleProducts = response.content.filter((product: ProductResponse) => 
          product.oldPrice && product.oldPrice > product.price
        );

        console.log(`✅ Found ${saleProducts.length} offers out of ${response.content.length} total products`);

        // إذا لم نجد منتجات للعروض، نأخذ أول منتج ونضع له سعر قديم للعرض
        if (saleProducts.length === 0 && response.content.length > 0) {
          console.log('⚠️ No offers found, creating demo offer from first product');
          const firstProduct = { ...response.content[0] };
          // إضافة سعر قديم للمنتج الأول لإنشاء عرض تجريبي
          firstProduct.oldPrice = firstProduct.price + 50;
          
          return {
            ...response,
            content: [firstProduct],
            totalElements: 1,
            totalPages: 1
          };
        }

        return {
          ...response,
          content: saleProducts,
          totalElements: saleProducts.length,
          totalPages: Math.ceil(saleProducts.length / size)
        };
      })
    );
  }

  // ===========================================================================
  // DROPDOWN DATA (categories, subcategories, brands)
  // ===========================================================================

  getCategories(): Observable<CategoryOption[]> {
    if (!this.categories$) {
      this.categories$ = this.http.get<PageResponse<CategoryOption>>(`${environment.apiUrl}/categories`).pipe(
        map(response => response.content || []),
        shareReplay(1)
      );
    }
    return this.categories$;
  }

  getSubcategories(): Observable<SubcategoryOption[]> {
    if (!this.subcategories$) {
      this.subcategories$ = this.http.get<PageResponse<SubcategoryOption>>(`${environment.apiUrl}/subcategories`).pipe(
        map(response => response.content || []),
        shareReplay(1)
      );
    }
    return this.subcategories$;
  }

  getBrands(): Observable<BrandOption[]> {
    return this.http.get<BrandOption[]>(`${environment.apiUrl}/admin/brands`);
  }

  /**
   * Get all categories with their associated subcategories
   * Fetches both categories and subcategories in parallel and combines them
   */
  getCategoriesWithSubcategories(): Observable<CategoryWithSubcategories[]> {
    return forkJoin({
      categories: this.getCategories(),
      subcategories: this.getSubcategories()
    }).pipe(
      map(({ categories, subcategories }) => {
        // Combine categories with their subcategories
        return categories.map(category => ({
          ...category,
          subcategories: subcategories.filter(sub => sub.categoryId === category.id)
        }));
      })
    );
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