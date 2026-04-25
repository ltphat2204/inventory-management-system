import { createFileRoute } from '@tanstack/react-router';
import { useQuery } from '@tanstack/react-query';
import { inventoryApi } from '@/api/inventory';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import { Search, AlertTriangle } from 'lucide-react';
import { useState } from 'react';

export const Route = createFileRoute('/_auth/inventory/')({
  component: InventoryPage,
});

// eslint-disable-next-line react-refresh/only-export-components
function InventoryPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [page] = useState(1);

  const { data: inventoryData, isLoading } = useQuery({
    queryKey: ['inventory', page, lowStockOnly],
    queryFn: () =>
      inventoryApi
        .getInventory({
          page,
          limit: 20,
          lowStockOnly: lowStockOnly || undefined,
        })
        .then((res) => res.data),
  });

  const filteredInventory = inventoryData?.content.filter((item) =>
    item.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    item.sku.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Tổng quan kho</h1>
          <p className="text-muted-foreground">Theo dõi số lượng tồn kho của tất cả biến thể</p>
        </div>
      </div>

      <div className="flex items-center justify-between gap-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Tìm theo tên hoặc SKU..."
            className="pl-8"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="flex items-center space-x-2">
          <Switch
            id="low-stock"
            checked={lowStockOnly}
            onCheckedChange={setLowStockOnly}
          />
          <Label htmlFor="low-stock" className="flex items-center gap-1 cursor-pointer">
            <AlertTriangle className="h-4 w-4 text-destructive" />
            Sắp hết hàng
          </Label>
        </div>
      </div>

      <div className="rounded-md border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>SKU</TableHead>
              <TableHead>Sản phẩm</TableHead>
              <TableHead>Size</TableHead>
              <TableHead>Màu</TableHead>
              <TableHead className="text-center">Số lượng tồn</TableHead>
              <TableHead className="text-center">Ngưỡng cảnh báo</TableHead>
              <TableHead className="text-center">Trạng thái</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={7} className="h-24 text-center">
                  Đang tải dữ liệu...
                </TableCell>
              </TableRow>
            ) : filteredInventory?.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="h-24 text-center text-muted-foreground">
                  Không có dữ liệu tồn kho.
                </TableCell>
              </TableRow>
            ) : (
              filteredInventory?.map((item) => (
                <TableRow key={item.variantId}>
                  <TableCell className="font-mono text-xs">{item.sku}</TableCell>
                  <TableCell className="font-medium">{item.productName}</TableCell>
                  <TableCell>{item.size}</TableCell>
                  <TableCell>{item.color}</TableCell>
                  <TableCell className="text-center">
                    <span className={`font-bold ${item.isLowStock ? 'text-destructive' : 'text-primary'}`}>
                      {item.currentQuantity}
                    </span>
                  </TableCell>
                  <TableCell className="text-center">{item.lowStockThreshold}</TableCell>
                  <TableCell className="text-center">
                    {item.isLowStock ? (
                      <Badge variant="destructive">Cảnh báo</Badge>
                    ) : (
                      <Badge variant="outline" className="text-green-600 border-green-200 bg-green-50">
                        An toàn
                      </Badge>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
