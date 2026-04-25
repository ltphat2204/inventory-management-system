import apiClient from './client';

export interface SaleItemRequest {
  variantId: number;
  quantity: number;
}

export interface SaleRequest {
  idempotencyKey: string;
  discountVnd: number;
  notes?: string;
  items: SaleItemRequest[];
}

export interface SaleResponse {
  id: number;
  invoiceNumber: string;
  totalAmountVnd: number;
  discountVnd: number;
  finalAmountVnd: number;
  createdAt: string;
}

export const salesApi = {
  createSale: (data: SaleRequest) => apiClient.post<SaleResponse>('/sales', data),
  getSales: (params?: any) => apiClient.get('/sales', { params }),
};
