import { createFileRoute } from '@tanstack/react-router';
import { useQuery, useMutation } from '@tanstack/react-query';
import { salesApi } from '@/api/sales';
import { inventoryApi } from '@/api/inventory';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { 
  Plus, 
  Minus, 
  Trash2, 
  ShoppingCart, 
  CreditCard, 
  Barcode,
  Loader2,
  PackageSearch
} from 'lucide-react';
import { toast } from 'sonner';
import { useState, useMemo, useRef } from 'react';
import { v4 as uuidv4 } from 'uuid';

export const Route = createFileRoute('/_auth/sales')({
  component: POSPage,
});

interface CartItem {
  variantId: number;
  sku: string;
  productName: string;
  size: string;
  color: string;
  price: number;
  quantity: number;
  stock: number;
}

// eslint-disable-next-line react-refresh/only-export-components
function POSPage() {
  const [cart, setCart] = useState<CartItem[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [discount, setDiscount] = useState(0);
  const barcodeInputRef = useRef<HTMLInputElement>(null);

  const { data: inventory } = useQuery({
    queryKey: ['inventory-all'],
    queryFn: () => inventoryApi.getInventory({ limit: 1000 }).then((res) => res.data),
  });

  const saleMutation = useMutation({
    mutationFn: (payload: any) => salesApi.createSale(payload),
    onSuccess: (res) => {
      toast.success(`Thanh toán thành công! Mã đơn: ${res.data.saleNumber}`);
      setCart([]);
      setDiscount(0);
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Lỗi khi thanh toán');
    },
  });

  const addToCart = (variant: any) => {
    if (variant.currentQuantity <= 0) {
      toast.error('Sản phẩm này đã hết hàng');
      return;
    }

    setCart((prev) => {
      const existing = prev.find((item) => item.variantId === variant.variantId);
      if (existing) {
        if (existing.quantity >= variant.currentQuantity) {
          toast.warning('Đã đạt giới hạn tồn kho');
          return prev;
        }
        return prev.map((item) =>
          item.variantId === variant.variantId
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [
        ...prev,
        {
          variantId: variant.variantId,
          sku: variant.sku,
          productName: variant.productName,
          size: variant.size,
          color: variant.color,
          price: variant.priceVnd || 0,
          quantity: 1,
          stock: variant.currentQuantity,
        },
      ];
    });
    setSearchQuery('');
    barcodeInputRef.current?.focus();
  };

  const updateQuantity = (variantId: number, delta: number) => {
    setCart((prev) =>
      prev.map((item) => {
        if (item.variantId === variantId) {
          const newQty = Math.max(1, Math.min(item.stock, item.quantity + delta));
          return { ...item, quantity: newQty };
        }
        return item;
      })
    );
  };

  const removeFromCart = (variantId: number) => {
    setCart((prev) => prev.filter((item) => item.variantId !== variantId));
  };

  const subtotal = useMemo(() => {
    return cart.reduce((acc, item) => acc + item.price * item.quantity, 0);
  }, [cart]);

  const finalTotal = Math.max(0, subtotal - discount);

  const handleCheckout = () => {
    if (cart.length === 0) {
      toast.error('Giỏ hàng trống');
      return;
    }

    saleMutation.mutate({
      idempotencyKey: uuidv4(),
      discountVnd: discount,
      items: cart.map((item) => ({
        variantId: item.variantId,
        quantity: item.quantity,
      })),
    });
  };

  // Giả lập quét mã vạch
  const handleBarcodeSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const found = inventory?.content.find(
      (v) => v.sku.toLowerCase() === searchQuery.toLowerCase()
    );
    if (found) {
      addToCart(found);
    } else {
      toast.error('Không tìm thấy sản phẩm với mã này');
    }
    setSearchQuery('');
  };

  const filteredResults = useMemo(() => {
    if (!searchQuery || searchQuery.length < 2) return [];
    return inventory?.content.filter(
      (v) =>
        v.productName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        v.sku.toLowerCase().includes(searchQuery.toLowerCase())
    ).slice(0, 5);
  }, [searchQuery, inventory]);

  return (
    <div className="flex h-[calc(100vh-8rem)] gap-6 overflow-hidden">
      {/* Cột trái: Tìm kiếm và Giỏ hàng */}
      <div className="flex-1 flex flex-col gap-6 min-w-0">
        <Card className="flex-shrink-0">
          <CardContent className="pt-6">
            <form onSubmit={handleBarcodeSubmit} className="relative">
              <Barcode className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
              <Input
                ref={barcodeInputRef}
                placeholder="Quét mã vạch hoặc tìm tên sản phẩm (F2)..."
                className="pl-10 h-12 text-lg"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                autoFocus
              />
              {filteredResults && filteredResults.length > 0 && (
                <div className="absolute top-full left-0 right-0 mt-1 bg-popover border rounded-md shadow-lg z-50 overflow-hidden">
                  {filteredResults.map((v) => (
                    <button
                      key={v.variantId}
                      className="w-full text-left px-4 py-3 hover:bg-accent flex justify-between items-center border-b last:border-0"
                      onClick={() => addToCart(v)}
                    >
                      <div>
                        <div className="font-medium">{v.productName}</div>
                        <div className="text-xs text-muted-foreground">
                          {v.sku} • {v.size} / {v.color}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-bold text-primary">
                          {(v.priceVnd || 0).toLocaleString('vi-VN')}₫
                        </div>
                        <Badge variant={v.currentQuantity > 0 ? 'outline' : 'destructive'}>
                          Kho: {v.currentQuantity}
                        </Badge>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </form>
          </CardContent>
        </Card>

        <Card className="flex-1 min-h-0 flex flex-col">
          <CardHeader className="flex flex-row items-center justify-between py-4">
            <CardTitle className="flex items-center gap-2">
              <ShoppingCart className="h-5 w-5 text-primary" />
              Giỏ hàng ({cart.length})
            </CardTitle>
            <Button variant="ghost" size="sm" onClick={() => setCart([])} className="text-muted-foreground">
              Xóa tất cả
            </Button>
          </CardHeader>
          <CardContent className="flex-1 overflow-auto p-0">
            <Table>
              <TableHeader className="sticky top-0 bg-card z-10">
                <TableRow>
                  <TableHead className="pl-6">Sản phẩm</TableHead>
                  <TableHead className="text-right">Đơn giá</TableHead>
                  <TableHead className="text-center w-[150px]">Số lượng</TableHead>
                  <TableHead className="text-right">Thành tiền</TableHead>
                  <TableHead className="w-[50px]"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {cart.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="h-64 text-center">
                      <div className="flex flex-col items-center gap-2 text-muted-foreground">
                        <PackageSearch className="h-12 w-12 opacity-20" />
                        <p>Giỏ hàng đang trống</p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  cart.map((item) => (
                    <TableRow key={item.variantId}>
                      <TableCell className="pl-6">
                        <div className="font-medium">{item.productName}</div>
                        <div className="text-xs text-muted-foreground">
                          {item.sku} - {item.size} / {item.color}
                        </div>
                      </TableCell>
                      <TableCell className="text-right">
                        {item.price.toLocaleString('vi-VN')}₫
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="outline"
                            size="icon"
                            className="h-7 w-7"
                            onClick={() => updateQuantity(item.variantId, -1)}
                          >
                            <Minus className="h-3 w-3" />
                          </Button>
                          <span className="w-8 text-center font-medium">{item.quantity}</span>
                          <Button
                            variant="outline"
                            size="icon"
                            className="h-7 w-7"
                            onClick={() => updateQuantity(item.variantId, 1)}
                            disabled={item.quantity >= item.stock}
                          >
                            <Plus className="h-3 w-3" />
                          </Button>
                        </div>
                      </TableCell>
                      <TableCell className="text-right font-medium">
                        {(item.price * item.quantity).toLocaleString('vi-VN')}₫
                      </TableCell>
                      <TableCell>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-destructive"
                          onClick={() => removeFromCart(item.variantId)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>

      {/* Cột phải: Thanh toán */}
      <Card className="w-[380px] flex flex-col shrink-0">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="h-5 w-5 text-primary" />
            Thanh toán
          </CardTitle>
        </CardHeader>
        <CardContent className="flex-1 space-y-6">
          <div className="space-y-4">
            <div className="flex justify-between items-center text-muted-foreground">
              <span>Tạm tính:</span>
              <span className="font-medium">{subtotal.toLocaleString('vi-VN')}₫</span>
            </div>
            <div className="space-y-2">
              <div className="flex justify-between items-center">
                <span className="text-muted-foreground">Chiết khấu (VNĐ):</span>
                <Input
                  type="number"
                  className="w-32 h-8 text-right"
                  value={discount}
                  onChange={(e) => setDiscount(Number(e.target.value))}
                />
              </div>
            </div>
          </div>

          <Separator />

          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-lg font-semibold">Tổng cộng:</span>
              <span className="text-2xl font-bold text-primary">
                {finalTotal.toLocaleString('vi-VN')}₫
              </span>
            </div>
          </div>

          <div className="pt-4 space-y-2">
            <Button
              className="w-full h-16 text-xl font-bold gap-2"
              size="lg"
              onClick={handleCheckout}
              disabled={cart.length === 0 || saleMutation.isPending}
            >
              {saleMutation.isPending ? (
                <Loader2 className="h-6 w-6 animate-spin" />
              ) : (
                'THANH TOÁN'
              )}
            </Button>
            <p className="text-xs text-center text-muted-foreground">
              Bấm phím <kbd className="px-1 py-0.5 rounded bg-muted border">Enter</kbd> để hoàn tất
            </p>
          </div>
        </CardContent>
        <CardFooter className="bg-muted/30 py-4 flex flex-col gap-2">
          <div className="w-full flex justify-between text-xs">
            <span className="text-muted-foreground">Phím tắt:</span>
            <span>F2: Tìm kiếm | F4: Thanh toán | F9: In bill</span>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
