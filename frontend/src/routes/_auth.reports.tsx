import { createFileRoute } from '@tanstack/react-router';
import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '@/api/dashboard';
import { inventoryApi } from '@/api/inventory';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { TrendingUp, Package, DollarSign, ShoppingBag, ArrowUpRight, ArrowDownRight } from 'lucide-react';

export const Route = createFileRoute('/_auth/reports')({
  component: ReportsPage,
});

const mockChartData = [
  { name: '01/04', revenue: 4500000, sales: 12 },
  { name: '02/04', revenue: 5200000, sales: 15 },
  { name: '03/04', revenue: 3800000, sales: 10 },
  { name: '04/04', revenue: 6100000, sales: 18 },
  { name: '05/04', revenue: 7500000, sales: 22 },
  { name: '06/04', revenue: 4800000, sales: 14 },
  { name: '07/04', revenue: 5900000, sales: 17 },
];

const mockTopProducts = [
  { name: 'Áo thun Polo Classic', sold: 145, revenue: 36250000 },
  { name: 'Quần Jean Slimfit', sold: 98, revenue: 49000000 },
  { name: 'Sơ mi Oxford White', sold: 86, revenue: 43000000 },
  { name: 'Áo khoác Bomber Black', sold: 54, revenue: 40500000 },
  { name: 'Quần Khaki Beige', sold: 42, revenue: 16800000 },
];

// eslint-disable-next-line react-refresh/only-export-components
function ReportsPage() {
  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => dashboardApi.getDashboard().then((res) => res.data),
  });

  if (isLoading) {
    return <div className="flex h-full items-center justify-center">Đang tải dữ liệu...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Báo cáo & Thống kê</h1>
          <p className="text-muted-foreground">Phân tích hiệu quả kinh doanh và tồn kho</p>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Doanh thu tháng này</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(dashboard?.todaysSalesTotalVnd || 0)}
            </div>
            <p className="text-xs text-muted-foreground flex items-center gap-1 mt-1">
              Hôm nay
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Đơn hàng mới</CardTitle>
            <ShoppingBag className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">+{dashboard?.todaysSalesCount || 0}</div>
            <p className="text-xs text-muted-foreground flex items-center gap-1 mt-1">
              Hôm nay
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Tổng mặt hàng</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{dashboard?.totalSkuCount || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Biến thể đang kinh doanh
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Tồn kho thấp</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{dashboard?.lowStockCount || 0}</div>
            <p className="text-xs text-muted-foreground flex items-center gap-1 mt-1">
              Cần nhập hàng
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-7">
        <Card className="lg:col-span-4">
          <CardHeader>
            <CardTitle>Biểu đồ doanh thu 7 ngày gần nhất</CardTitle>
          </CardHeader>
          <CardContent className="h-[350px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={mockChartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" />
                <YAxis
                  tickFormatter={(value) => `${(value / 1000000).toFixed(1)}M`}
                />
                <Tooltip
                  formatter={(value: any) => [
                    `${Number(value).toLocaleString('vi-VN')}₫`,
                    'Doanh thu',
                  ]}
                />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="revenue"
                  stroke="hsl(var(--primary))"
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                  name="Doanh thu"
                />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="lg:col-span-3">
          <CardHeader>
            <CardTitle>Sản phẩm bán chạy</CardTitle>
            <CardDescription>Top 5 sản phẩm có doanh thu cao nhất</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sản phẩm</TableHead>
                  <TableHead className="text-right">Đã bán</TableHead>
                  <TableHead className="text-right">Doanh thu</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {mockTopProducts.map((p) => (
                  <TableRow key={p.name}>
                    <TableCell className="font-medium">{p.name}</TableCell>
                    <TableCell className="text-right">{p.sold}</TableCell>
                    <TableCell className="text-right font-bold">
                      {(p.revenue / 1000000).toFixed(1)}M
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
