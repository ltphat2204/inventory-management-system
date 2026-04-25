import apiClient from './client';

export interface InventoryOverview {
  variantId: number;
  sku: string;
  productName: string;
  size: string;
  color: string;
  currentQuantity: number;
  lowStockThreshold: number;
  isLowStock: boolean;
  priceVnd?: number;
}

export interface StockImportItemRequest {
  variantId: number;
  quantity: number;
  unitCostVnd: number;
  reason?: string;
}

export interface StockImportRequest {
  importNumber: string;
  supplierName?: string;
  notes?: string;
  idempotencyKey: string;
  items: StockImportItemRequest[];
}

export const inventoryApi = {
  getInventory: (params?: any) => 
    apiClient.get<{ content: InventoryOverview[], totalElements: number }>('/inventory', { params }),
  
  importStock: (data: StockImportRequest) => 
    apiClient.post('/stock-imports', data),
    
  getStockImports: (params?: any) => 
    apiClient.get('/stock-imports', { params }),
};
