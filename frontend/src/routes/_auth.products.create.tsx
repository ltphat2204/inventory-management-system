import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { useQuery, useMutation } from '@tanstack/react-query';
import { catalogApi } from '@/api/catalog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Field,
  FieldLabel,
  FieldContent,
  FieldError,
} from '@/components/ui/field';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Plus, Trash2, Wand2, Loader2, Save, X } from 'lucide-react';
import { toast } from 'sonner';
import { useState } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';

const variantSchema = z.object({
  size: z.string().min(1, 'Bắt buộc'),
  color: z.string().min(1, 'Bắt buộc'),
  designStyle: z.string().optional(),
  variantPriceVnd: z.number().min(0, 'Giá không hợp lệ'),
  barcode: z.string().optional(),
  lowStockThreshold: z.number().min(0),
});

const productSchema = z.object({
  productCode: z.string().min(3, 'Mã sản phẩm tối thiểu 3 ký tự'),
  nameVn: z.string().min(2, 'Tên sản phẩm tối thiểu 2 ký tự'),
  categoryId: z.number().min(1, 'Vui lòng chọn danh mục'),
  basePriceVnd: z.number().min(0, 'Giá cơ bản không hợp lệ'),
  description: z.string().optional(),
  variants: z.array(variantSchema).min(1, 'Phải có ít nhất một biến thể'),
});

type ProductFormValues = z.infer<typeof productSchema>;

export const Route = createFileRoute('/_auth/products/create')({
  component: CreateProductPage,
});

// eslint-disable-next-line react-refresh/only-export-components
function CreateProductPage() {
  const navigate = useNavigate();
  const [sizes, setSizes] = useState<string[]>([]);
  const [colors, setColors] = useState<string[]>([]);
  
  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => catalogApi.getCategories().then((res) => res.data),
  });

  const form = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      productCode: '',
      nameVn: '',
      basePriceVnd: 0,
      description: '',
      variants: [],
    },
  });

  const { fields, append, remove, replace } = useFieldArray({
    control: form.control,
    name: 'variants',
  });

  const createMutation = useMutation({
    mutationFn: (data: ProductFormValues) => catalogApi.createProduct(data as any),
    onSuccess: () => {
      toast.success('Đã tạo sản phẩm thành công');
      navigate({ to: '/products' });
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Lỗi khi tạo sản phẩm');
    },
  });

  const generateMatrix = () => {
    if (sizes.length === 0 || colors.length === 0) {
      toast.warning('Vui lòng nhập ít nhất một kích thước và một màu sắc');
      return;
    }

    const basePrice = form.getValues('basePriceVnd');
    const newVariants = [];

    for (const s of sizes) {
      for (const c of colors) {
        newVariants.push({
          size: s,
          color: c,
          designStyle: '',
          variantPriceVnd: basePrice,
          barcode: '',
          lowStockThreshold: 10,
        });
      }
    }

    replace(newVariants);
    toast.success(`Đã tạo ${newVariants.length} biến thể từ ma trận`);
  };

  const onSubmit = (values: ProductFormValues) => {
    createMutation.mutate(values);
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto pb-20">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Thêm sản phẩm mới</h1>
          <p className="text-muted-foreground">Tạo sản phẩm và các biến thể tương ứng</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate({ to: '/products' })}>
            <X className="mr-2 h-4 w-4" /> Hủy
          </Button>
          <Button onClick={form.handleSubmit(onSubmit)} disabled={createMutation.isPending}>
            {createMutation.isPending ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Save className="mr-2 h-4 w-4" />
            )}
            Lưu sản phẩm
          </Button>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Thông tin cơ bản</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <Field>
                  <FieldLabel>Mã sản phẩm</FieldLabel>
                  <FieldContent>
                    <Input {...form.register('productCode')} placeholder="Ví dụ: SP001" />
                    <FieldError errors={[form.formState.errors.productCode]} />
                  </FieldContent>
                </Field>
                <Field>
                  <FieldLabel>Danh mục</FieldLabel>
                  <FieldContent>
                    <Select
                      onValueChange={(value) => form.setValue('categoryId', parseInt(value))}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Chọn danh mục" />
                      </SelectTrigger>
                      <SelectContent>
                        {categories?.map((c) => (
                          <SelectItem key={c.id} value={c.id.toString()}>
                            {c.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FieldError errors={[form.formState.errors.categoryId]} />
                  </FieldContent>
                </Field>
              </div>

              <Field>
                <FieldLabel>Tên sản phẩm (Tiếng Việt)</FieldLabel>
                <FieldContent>
                  <Input {...form.register('nameVn')} placeholder="Ví dụ: Áo thun Polo cao cấp" />
                  <FieldError errors={[form.formState.errors.nameVn]} />
                </FieldContent>
              </Field>

              <div className="grid grid-cols-2 gap-4">
                <Field>
                  <FieldLabel>Giá bán cơ bản (VNĐ)</FieldLabel>
                  <FieldContent>
                    <Input
                      type="number"
                      {...form.register('basePriceVnd', { valueAsNumber: true })}
                      placeholder="0"
                    />
                    <FieldError errors={[form.formState.errors.basePriceVnd]} />
                  </FieldContent>
                </Field>
                <Field>
                  <FieldLabel>Thuế VAT (%)</FieldLabel>
                  <FieldContent>
                    <Input type="number" defaultValue={10} disabled />
                  </FieldContent>
                </Field>
              </div>

              <Field>
                <FieldLabel>Mô tả</FieldLabel>
                <FieldContent>
                  <Textarea {...form.register('description')} rows={4} />
                </FieldContent>
              </Field>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle>Danh sách biến thể</CardTitle>
                <CardDescription>Các tổ hợp thuộc tính của sản phẩm</CardDescription>
              </div>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() =>
                  append({
                    size: '',
                    color: '',
                    designStyle: '',
                    variantPriceVnd: form.getValues('basePriceVnd'),
                    barcode: '',
                    lowStockThreshold: 10,
                  })
                }
              >
                <Plus className="mr-2 h-4 w-4" /> Thêm thủ công
              </Button>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Size</TableHead>
                    <TableHead>Màu</TableHead>
                    <TableHead>Giá (VNĐ)</TableHead>
                    <TableHead>Mã vạch</TableHead>
                    <TableHead className="w-[50px]"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {fields.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} className="h-24 text-center text-muted-foreground">
                        Chưa có biến thể nào. Sử dụng bộ tạo ma trận bên cạnh hoặc thêm thủ công.
                      </TableCell>
                    </TableRow>
                  ) : (
                    fields.map((field, index) => (
                      <TableRow key={field.id}>
                        <TableCell>
                          <Input {...form.register(`variants.${index}.size`)} className="h-8" />
                        </TableCell>
                        <TableCell>
                          <Input {...form.register(`variants.${index}.color`)} className="h-8" />
                        </TableCell>
                        <TableCell>
                          <Input
                            type="number"
                            {...form.register(`variants.${index}.variantPriceVnd`, {
                              valueAsNumber: true,
                            })}
                            className="h-8"
                          />
                        </TableCell>
                        <TableCell>
                          <Input {...form.register(`variants.${index}.barcode`)} className="h-8" />
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
                    ))
                  )}
                </TableBody>
              </Table>
              {form.formState.errors.variants && (
                <p className="text-sm text-destructive mt-2">{form.formState.errors.variants.message}</p>
              )}
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Bộ tạo Ma trận</CardTitle>
              <CardDescription>Tạo hàng loạt biến thể nhanh chóng</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">Kích thước (Size)</label>
                <Input
                  placeholder="S, M, L, XL..."
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      const val = e.currentTarget.value.trim();
                      if (val && !sizes.includes(val)) setSizes([...sizes, val]);
                      e.currentTarget.value = '';
                    }
                  }}
                />
                <div className="flex flex-wrap gap-1 mt-1">
                  {sizes.map((s) => (
                    <Badge key={s} variant="secondary" className="gap-1">
                      {s} <X className="h-3 w-3 cursor-pointer" onClick={() => setSizes(sizes.filter(x => x !== s))} />
                    </Badge>
                  ))}
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">Màu sắc (Color)</label>
                <Input
                  placeholder="Trắng, Đen, Đỏ..."
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      const val = e.currentTarget.value.trim();
                      if (val && !colors.includes(val)) setColors([...colors, val]);
                      e.currentTarget.value = '';
                    }
                  }}
                />
                <div className="flex flex-wrap gap-1 mt-1">
                  {colors.map((c) => (
                    <Badge key={c} variant="secondary" className="gap-1">
                      {c} <X className="h-3 w-3 cursor-pointer" onClick={() => setColors(colors.filter(x => x !== c))} />
                    </Badge>
                  ))}
                </div>
              </div>

              <Button type="button" className="w-full" variant="secondary" onClick={generateMatrix}>
                <Wand2 className="mr-2 h-4 w-4" /> Tạo biến thể
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
