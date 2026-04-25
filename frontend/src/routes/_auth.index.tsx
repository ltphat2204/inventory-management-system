import { createFileRoute } from '@tanstack/react-router';
import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '@/api/dashboard';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Package, ShoppingCart, AlertTriangle, TrendingUp } from 'lucide-react';

export const Route = createFileRoute('/_auth/')({
  component: DashboardPage,
});

function DashboardPage() {
  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => dashboardApi.getDashboard().then((res) => res.data),
  });

  if (isLoading) {
    return <div className="flex h-full items-center justify-center">Đang tải dữ liệu...</div>;
  }

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Tổng quan</h1>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Tổng SKU</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{dashboard?.totalSkuCount.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">Hiện đang kinh doanh</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Doanh thu hôm nay</CardTitle>
            <ShoppingCart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(dashboard?.todaysSalesTotalVnd || 0)}</div>
            <p className="text-xs text-muted-foreground">{dashboard?.todaysSalesCount} đơn hàng mới</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Tồn kho thấp</CardTitle>
            <AlertTriangle className="h-4 w-4 text-destructive" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-destructive">{dashboard?.lowStockCount}</div>
            <p className="text-xs text-muted-foreground">Cần nhập thêm hàng ngay</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Giá trị kho</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(dashboard?.totalStockValueVnd || 0)}</div>
            <p className="text-xs text-muted-foreground">Tài sản lưu động</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>Biểu đồ doanh thu</CardTitle>
          </CardHeader>
          <CardContent className="pl-2">
            <div className="h-[200px] flex items-center justify-center text-muted-foreground italic">
              Dữ liệu biểu đồ đang được phát triển...
            </div>
          </CardContent>
        </Card>
        <Card className="col-span-3">
          <CardHeader>
            <CardTitle>Sản phẩm bán chậm (90 ngày)</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {dashboard?.slowMovingItems && dashboard.slowMovingItems.length > 0 ? (
                dashboard.slowMovingItems.map((item) => (
                  <div key={item.variantId} className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">{item.productName}</p>
                      <p className="text-xs text-muted-foreground">{item.sku}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold">{item.currentQuantity}</p>
                      <p className="text-xs text-muted-foreground">Tồn kho</p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-sm text-muted-foreground italic">
                  Không có sản phẩm bán chậm nào.
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
