import { createFileRoute } from '@tanstack/react-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '@/api/users';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
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
import { MoreHorizontal, Search, UserPlus, Key, Trash2, Edit, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import type { User } from '@/api/users';

export const Route = createFileRoute('/_auth/users')({
  component: UsersPage,
});

const userSchema = z.object({
  fullName: z.string().min(2, 'Họ tên phải có ít nhất 2 ký tự'),
  role: z.string().min(1, 'Vui lòng chọn vai trò'),
});

const passwordSchema = z.object({
  newPassword: z.string().min(8, 'Mật khẩu phải có ít nhất 8 ký tự'),
});

type UserFormValues = z.infer<typeof userSchema>;
type PasswordFormValues = z.infer<typeof passwordSchema>;

// eslint-disable-next-line react-refresh/only-export-components
function UsersPage() {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isPasswordOpen, setIsPasswordOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  const { data: users, isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: () => userApi.getUsers().then((res) => res.data),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => userApi.deactivateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('Đã vô hiệu hóa người dùng');
    },
    onError: () => {
      toast.error('Không thể vô hiệu hóa người dùng');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: UserFormValues) => userApi.updateUser(selectedUser!.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('Đã cập nhật thông tin người dùng');
      setIsEditOpen(false);
    },
    onError: () => {
      toast.error('Lỗi khi cập nhật thông tin');
    },
  });

  const resetPasswordMutation = useMutation({
    mutationFn: (data: PasswordFormValues) => userApi.resetPassword(selectedUser!.id, data),
    onSuccess: () => {
      toast.success('Đã đặt lại mật khẩu thành công');
      setIsPasswordOpen(false);
    },
    onError: () => {
      toast.error('Lỗi khi đặt lại mật khẩu');
    },
  });

  const userForm = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
  });

  const selectedRole = useWatch({
    control: userForm.control,
    name: 'role',
  });

  const passwordForm = useForm<PasswordFormValues>({
    resolver: zodResolver(passwordSchema),
    defaultValues: { newPassword: '' },
  });

  const openEdit = (user: User) => {
    setSelectedUser(user);
    userForm.reset({
      fullName: user.fullName,
      role: user.role,
    });
    setIsEditOpen(true);
  };

  const openPassword = (user: User) => {
    setSelectedUser(user);
    passwordForm.reset({ newPassword: '' });
    setIsPasswordOpen(true);
  };

  const filteredUsers = users?.filter((user) =>
    user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.username.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getRoleBadge = (role: string) => {
    const roles: Record<string, { label: string; color: string }> = {
      ADMIN: { label: 'Quản trị viên', color: 'bg-red-100 text-red-700' },
      SYSTEM_ADMIN: { label: 'Quản trị HT', color: 'bg-purple-100 text-purple-700' },
      MANAGER: { label: 'Quản lý', color: 'bg-blue-100 text-blue-700' },
      CASHIER: { label: 'Thu ngân', color: 'bg-green-100 text-green-700' },
      VIEWER: { label: 'Người xem', color: 'bg-gray-100 text-gray-700' },
    };
    const r = roles[role] || { label: role, color: 'bg-gray-100' };
    return <Badge className={r.color} variant="outline">{r.label}</Badge>;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Quản lý người dùng</h1>
          <p className="text-muted-foreground">Quản lý tài khoản và phân quyền truy cập hệ thống</p>
        </div>
        <Button>
          <UserPlus className="mr-2 h-4 w-4" />
          Thêm người dùng
        </Button>
      </div>

      <div className="flex items-center gap-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Tìm kiếm theo tên hoặc tài khoản..."
            className="pl-8"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="rounded-md border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Tài khoản</TableHead>
              <TableHead>Họ tên</TableHead>
              <TableHead>Vai trò</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center">
                  Đang tải dữ liệu...
                </TableCell>
              </TableRow>
            ) : filteredUsers?.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center">
                  Không tìm thấy người dùng nào.
                </TableCell>
              </TableRow>
            ) : (
              filteredUsers?.map((user) => (
                <TableRow key={user.id}>
                  <TableCell className="font-medium">{user.username}</TableCell>
                  <TableCell>{user.fullName}</TableCell>
                  <TableCell>{getRoleBadge(user.role)}</TableCell>
                  <TableCell>
                    <Badge variant={user.isActive ? 'default' : 'secondary'}>
                      {user.isActive ? 'Đang hoạt động' : 'Đã khóa'}
                    </Badge>
                  </TableCell>
                  <TableCell>{new Date(user.createdAt).toLocaleDateString('vi-VN')}</TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="h-8 w-8 p-0">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuLabel>Hành động</DropdownMenuLabel>
                        <DropdownMenuItem onClick={() => openEdit(user)}>
                          <Edit className="mr-2 h-4 w-4" /> Sửa thông tin
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => openPassword(user)}>
                          <Key className="mr-2 h-4 w-4" /> Đặt lại mật khẩu
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          className="text-destructive focus:text-destructive"
                          onClick={() => deactivateMutation.mutate(user.id)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" /> Vô hiệu hóa
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Dialog Sửa thông tin */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Sửa thông tin người dùng</DialogTitle>
            <DialogDescription>
              Cập nhật thông tin cho tài khoản <strong>{selectedUser?.username}</strong>.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={userForm.handleSubmit((v) => updateMutation.mutate(v))} className="space-y-4 py-4">
            <Field>
              <FieldLabel>Họ tên</FieldLabel>
              <FieldContent>
                <Input {...userForm.register('fullName')} />
                <FieldError errors={[userForm.formState.errors.fullName]} />
              </FieldContent>
            </Field>
            <Field>
              <FieldLabel>Vai trò</FieldLabel>
              <FieldContent>
                <Select
                  value={selectedRole}
                  onValueChange={(v) => userForm.setValue('role', v)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Chọn vai trò" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ADMIN">Quản trị viên</SelectItem>
                    <SelectItem value="MANAGER">Quản lý</SelectItem>
                    <SelectItem value="CASHIER">Thu ngân</SelectItem>
                    <SelectItem value="VIEWER">Người xem</SelectItem>
                  </SelectContent>
                </Select>
                <FieldError errors={[userForm.formState.errors.role]} />
              </FieldContent>
            </Field>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsEditOpen(false)}>
                Hủy
              </Button>
              <Button type="submit" disabled={updateMutation.isPending}>
                {updateMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Lưu thay đổi
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Dialog Đặt lại mật khẩu */}
      <Dialog open={isPasswordOpen} onOpenChange={setIsPasswordOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Đặt lại mật khẩu</DialogTitle>
            <DialogDescription>
              Nhập mật khẩu mới cho tài khoản <strong>{selectedUser?.username}</strong>.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={passwordForm.handleSubmit((v) => resetPasswordMutation.mutate(v))} className="space-y-4 py-4">
            <Field>
              <FieldLabel>Mật khẩu mới</FieldLabel>
              <FieldContent>
                <Input type="password" {...passwordForm.register('newPassword')} />
                <FieldError errors={[passwordForm.formState.errors.newPassword]} />
              </FieldContent>
            </Field>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsPasswordOpen(false)}>
                Hủy
              </Button>
              <Button type="submit" disabled={resetPasswordMutation.isPending}>
                {resetPasswordMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Cập nhật mật khẩu
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
