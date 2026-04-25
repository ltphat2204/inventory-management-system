import apiClient from './client';

export interface InventoryOverviewResponse {
  variantId: number;
  variantSku: string;
  productId: number;
  productName: string;
  currentQuantity: number;
  lowStockThreshold: number;
  lowStock: boolean;
}

export interface SlowMovingItemResponse {
  variantId: number;
  sku: string;
  productName: string;
  currentQuantity: number;
  lastMovementAt: string;
}

export interface DashboardResponse {
  totalSkuCount: number;
  totalStockValueVnd: number;
  lowStockCount: number;
  todaysSalesTotalVnd: number;
  todaysSalesCount: number;
  lowStockItems: InventoryOverviewResponse[];
  slowMovingItems: SlowMovingItemResponse[];
}

export const dashboardApi = {
  getDashboard: (lowStockLimit = 10) =>
    apiClient.get<DashboardResponse>('/dashboard', { params: { lowStockLimit } }),
};
