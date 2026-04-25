import apiClient from './client';

export interface Category {
  id: number;
  name: string;
  description?: string;
  productCount?: number;
}

export interface Variant {
  id: number;
  sku: string;
  size: string;
  color: string;
  designStyle?: string;
  variantPriceVnd?: number;
  barcode?: string;
  lowStockThreshold: number;
  currentStock?: number;
}

export interface Product {
  id: number;
  productCode: string;
  nameVn: string;
  nameEn?: string;
  categoryId: number;
  categoryName?: string;
  basePriceVnd: number;
  vatRate: number;
  description?: string;
  isActive: boolean;
  variants: Variant[];
}

export interface CreateProductRequest {
  productCode: string;
  nameVn: string;
  nameEn?: string;
  categoryId: number;
  basePriceVnd: number;
  vatRate: number;
  description?: string;
  variants: Omit<Variant, 'id' | 'sku' | 'currentStock'>[];
}

export const catalogApi = {
  // Categories
  getCategories: () => apiClient.get<Category[]>('/v1/categories'),
  createCategory: (data: Partial<Category>) => apiClient.post<Category>('/v1/categories', data),
  updateCategory: (id: number, data: Partial<Category>) => apiClient.put<Category>(`/v1/categories/${id}`, data),
  deleteCategory: (id: number) => apiClient.delete(`/v1/categories/${id}`),

  // Products
  getProducts: (params?: any) => apiClient.get<{ content: Product[], totalElements: number }>('/v1/products', { params }),
  getProduct: (id: number) => apiClient.get<Product>(`/v1/products/${id}`),
  createProduct: (data: CreateProductRequest) => apiClient.post<Product>('/v1/products', data),
  updateProduct: (id: number, data: Partial<CreateProductRequest>) => apiClient.put<Product>(`/v1/products/${id}`, data),
  deleteProduct: (id: number) => apiClient.delete(`/v1/products/${id}`),
};
