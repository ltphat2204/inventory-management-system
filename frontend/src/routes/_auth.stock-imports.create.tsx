import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { useQuery, useMutation } from '@tanstack/react-query';
import { inventoryApi } from '@/api/inventory';
import { catalogApi } from '@/api/catalog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Field,
  FieldLabel,
  FieldContent,
  FieldError,
} from '@/components/ui/field';
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
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { Plus, Trash2, Loader2, Save, X } from 'lucide-react';
import { toast } from 'sonner';
import { useState, useMemo } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { v4 as uuidv4 } from 'uuid';

const itemSchema = z.object({
  variantId: z.number(),
  sku: z.string(),
  productName: z.string(),
  size: z.string(),
  color: z.string(),
  quantity: z.number().min(1, 'Tối thiểu 1'),
  unitCostVnd: z.number().min(0, 'Giá không hợp lệ'),
  reason: z.string().optional(),
});

const importSchema = z.object({
  importNumber: z.string().min(1, 'Bắt buộc'),
  supplierName: z.string().optional(),
  notes: z.string().optional(),
  items: z.array(itemSchema).min(1, 'Phải có ít nhất 1 mặt hàng'),
});

type ImportFormValues = z.infer<typeof importSchema>;

export const Route = createFileRoute('/_auth/stock-imports/create')({
  component: CreateStockImportPage,
});

// eslint-disable-next-line react-refresh/only-export-components
function CreateStockImportPage() {
  const navigate = useNavigate();
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const { data: products } = useQuery({
    queryKey: ['products', searchQuery],
    queryFn: () => catalogApi.getProducts({ q: searchQuery, size: 50 }).then((res) => res.data),
    enabled: searchQuery.length > 1,
  });

  const form = useForm<ImportFormValues>({
    resolver: zodResolver(importSchema),
    defaultValues: {
      importNumber: `PN-${new Date().getTime()}`,
      supplierName: '',
      notes: '',
      items: [],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'items',
  });

  const importMutation = useMutation({
    mutationFn: (data: ImportFormValues) => {
      const payload = {
        ...data,
        idempotencyKey: uuidv4(),
        items: data.items.map((item) => ({
          variantId: item.variantId,
          quantity: item.quantity,
          unitCostVnd: item.unitCostVnd,
          reason: item.reason,
        })),
      };
      return inventoryApi.importStock(payload as any);
    },
    onSuccess: () => {
      toast.success('Nhập kho thành công');
      navigate({ to: '/inventory' });
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Lỗi khi nhập kho');
    },
  });

  const allVariants = useMemo(() => {
    if (!products?.content) return [];
    return products.content.flatMap((p) =>
      p.variants.map((v) => ({
        ...v,
        productName: p.nameVn,
        basePrice: p.basePriceVnd,
      }))
    );
  }, [products]);

  const addVariant = (v: any) => {
    const existing = fields.findIndex((f) => f.variantId === v.id);
    if (existing >= 0) {
      toast.warning('Mặt hàng này đã có trong danh sách');
      return;
    }

    append({
      variantId: v.id,
      sku: v.sku,
      productName: v.productName,
      size: v.size,
      color: v.color,
      quantity: 1,
      unitCostVnd: v.variantPriceVnd || v.basePrice,
      reason: '',
    });
    setIsSearchOpen(false);
    setSearchQuery('');
  };

  const totalAmount = fields.reduce((acc, _, idx) => {
    const q = form.watch(`items.${idx}.quantity`) || 0;
    const c = form.watch(`items.${idx}.unitCostVnd`) || 0;
    return acc + q * c;
  }, 0);

  return (
    <div className="space-y-6 max-w-5xl mx-auto pb-20">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Tạo phiếu nhập kho</h1>
          <p className="text-muted-foreground">Nhập thêm hàng vào kho từ nhà cung cấp</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate({ to: '/inventory' })}>
            <X className="mr-2 h-4 w-4" /> Hủy
          </Button>
          <Button
            onClick={form.handleSubmit((v) => importMutation.mutate(v))}
            disabled={importMutation.isPending}
          >
            {importMutation.isPending ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Save className="mr-2 h-4 w-4" />
            )}
            Hoàn tất nhập kho
          </Button>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle>Mặt hàng nhập kho</CardTitle>
                <CardDescription>Chọn các sản phẩm cần nhập thêm</CardDescription>
              </div>
              <Popover open={isSearchOpen} onOpenChange={setIsSearchOpen}>
                <PopoverTrigger asChild>
                  <Button variant="outline" size="sm">
                    <Plus className="mr-2 h-4 w-4" /> Thêm mặt hàng
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-[400px] p-0" align="end">
                  <Command shouldFilter={false}>
                    <CommandInput
                      placeholder="Tìm theo tên hoặc mã SP..."
                      value={searchQuery}
                      onValueChange={setSearchQuery}
                    />
                    <CommandList>
                      <CommandEmpty>
                        {searchQuery.length < 2 ? 'Nhập ít nhất 2 ký tự' : 'Không tìm thấy sản phẩm nào.'}
                      </CommandEmpty>
                      <CommandGroup>
                        {allVariants.map((v) => (
                          <CommandItem
                            key={v.id}
                            onSelect={() => addVariant(v)}
                            className="flex flex-col items-start gap-1 py-3"
                          >
                            <div className="font-medium">{v.productName}</div>
                            <div className="text-xs text-muted-foreground flex gap-2">
                              <span className="font-mono">{v.sku}</span>
                              <span>•</span>
                              <span>Size: {v.size}</span>
                              <span>•</span>
                              <span>Màu: {v.color}</span>
                            </div>
                          </CommandItem>
                        ))}
                      </CommandGroup>
                    </CommandList>
                  </Command>
                </PopoverContent>
              </Popover>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mặt hàng</TableHead>
                    <TableHead className="w-[100px]">Số lượng</TableHead>
                    <TableHead className="w-[150px]">Giá nhập</TableHead>
                    <TableHead className="text-right">Thành tiền</TableHead>
                    <TableHead className="w-[50px]"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {fields.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} className="h-32 text-center text-muted-foreground italic">
                        Chưa có mặt hàng nào. Vui lòng bấm "Thêm mặt hàng" để bắt đầu.
                      </TableCell>
                    </TableRow>
                  ) : (
                    fields.map((field, index) => {
                      const q = form.watch(`items.${index}.quantity`) || 0;
                      const c = form.watch(`items.${index}.unitCostVnd`) || 0;
                      return (
                        <TableRow key={field.id}>
                          <TableCell>
                            <div className="font-medium">{field.productName}</div>
                            <div className="text-xs text-muted-foreground">
                              {field.sku} - {field.size} / {field.color}
                            </div>
                          </TableCell>
                          <TableCell>
                            <Input
                              type="number"
                              className="h-8"
                              {...form.register(`items.${index}.quantity`, { valueAsNumber: true })}
                            />
                          </TableCell>
                          <TableCell>
                            <Input
                              type="number"
                              className="h-8"
                              {...form.register(`items.${index}.unitCostVnd`, { valueAsNumber: true })}
                            />
                          </TableCell>
                          <TableCell className="text-right font-medium">
                            {(q * c).toLocaleString('vi-VN')}₫
                          </TableCell>
                          <TableCell>
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => remove(index)}
                              className="h-8 w-8 text-destructive"
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      );
                    })
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Thông tin phiếu nhập</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <Field>
                <FieldLabel>Số phiếu nhập</FieldLabel>
                <FieldContent>
                  <Input {...form.register('importNumber')} />
                  <FieldError errors={[form.formState.errors.importNumber]} />
                </FieldContent>
              </Field>
              <Field>
                <FieldLabel>Nhà cung cấp</FieldLabel>
                <FieldContent>
                  <Input {...form.register('supplierName')} placeholder="Tên nhà cung cấp..." />
                </FieldContent>
              </Field>
              <Field>
                <FieldLabel>Ghi chú</FieldLabel>
                <FieldContent>
                  <Textarea {...form.register('notes')} rows={3} />
                </FieldContent>
              </Field>

              <div className="pt-4 border-t space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Tổng mặt hàng:</span>
                  <span className="font-medium">{fields.length}</span>
                </div>
                <div className="flex justify-between text-lg font-bold">
                  <span>Tổng tiền:</span>
                  <span className="text-primary">{totalAmount.toLocaleString('vi-VN')}₫</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
