import { createFileRoute, Link } from '@tanstack/react-router';
import { useQuery } from '@tanstack/react-query';
import { catalogApi } from '@/api/catalog';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { ChevronLeft, Edit, AlertTriangle } from 'lucide-react';

export const Route = createFileRoute('/_auth/products/$id')({
  component: ProductDetailPage,
});

// eslint-disable-next-line react-refresh/only-export-components
function ProductDetailPage() {
  const { id } = Route.useParams();

  const { data: product, isLoading } = useQuery({
    queryKey: ['product', id],
    queryFn: () => catalogApi.getProduct(parseInt(id)).then((res) => res.data),
  });

  if (isLoading) return <div className="p-8 text-center">Đang tải thông tin sản phẩm...</div>;
  if (!product) return <div className="p-8 text-center text-destructive">Không tìm thấy sản phẩm.</div>;

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link to="/products">
              <ChevronLeft className="h-4 w-4" />
            </Link>
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">{product.nameVn}</h1>
          <Badge variant={product.isActive ? 'default' : 'secondary'}>
            {product.isActive ? 'Đang kinh doanh' : 'Ngừng kinh doanh'}
          </Badge>
        </div>
        <Button onClick={() => alert('Đang phát triển')}>
          <Edit className="mr-2 h-4 w-4" /> Sửa sản phẩm
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <Card className="md:col-span-1">
          <CardHeader>
            <CardTitle>Thông tin chi tiết</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Mã sản phẩm</p>
              <p className="text-lg font-mono">{product.productCode}</p>
            </div>
            <div>
              <p className="text-sm font-medium text-muted-foreground">Danh mục</p>
              <p className="text-lg">{product.categoryName}</p>
            </div>
            <div>
              <p className="text-sm font-medium text-muted-foreground">Giá cơ bản</p>
              <p className="text-lg font-bold">{product.basePriceVnd.toLocaleString('vi-VN')}₫</p>
            </div>
            <div>
              <p className="text-sm font-medium text-muted-foreground">Thuế VAT</p>
              <p className="text-lg">{product.vatRate}%</p>
            </div>
            <div>
              <p className="text-sm font-medium text-muted-foreground">Mô tả</p>
              <p className="text-sm whitespace-pre-wrap">{product.description || 'Không có mô tả'}</p>
            </div>
          </CardContent>
        </Card>

        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>Ma trận tồn kho biến thể</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>SKU</TableHead>
                  <TableHead>Size</TableHead>
                  <TableHead>Màu</TableHead>
                  <TableHead className="text-right">Giá biến thể</TableHead>
                  <TableHead className="text-center">Tồn kho</TableHead>
                  <TableHead className="text-center">Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {product.variants.map((variant) => {
                  const isLowStock = (variant.currentStock || 0) <= variant.lowStockThreshold;
                  return (
                    <TableRow key={variant.id}>
                      <TableCell className="font-mono text-xs">{variant.sku}</TableCell>
                      <TableCell>{variant.size}</TableCell>
                      <TableCell>{variant.color}</TableCell>
                      <TableCell className="text-right">
                        {(variant.variantPriceVnd || product.basePriceVnd).toLocaleString('vi-VN')}₫
                      </TableCell>
                      <TableCell className="text-center">
                        <span className={`font-bold ${isLowStock ? 'text-destructive' : 'text-primary'}`}>
                          {variant.currentStock || 0}
                        </span>
                      </TableCell>
                      <TableCell className="text-center">
                        {isLowStock && (
                          <Badge variant="destructive" className="gap-1">
                            <AlertTriangle className="h-3 w-3" /> Sắp hết
                          </Badge>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
