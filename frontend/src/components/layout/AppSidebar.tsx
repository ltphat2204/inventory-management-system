import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarFooter,
} from '@/components/ui/sidebar';
import {
  LayoutDashboard,
  Package,
  Tags,
  PlusCircle,
  ShoppingCart,
  Users,
  BarChart3,
  History,
  LogOut,
  ChevronRight,
} from 'lucide-react';
import { Link, useLocation } from '@tanstack/react-router';
import { useAuthStore } from '@/store/auth';

const menuItems = [
  { title: 'Tổng quan', icon: LayoutDashboard, url: '/' },
  { title: 'Kho hàng', icon: Package, url: '/inventory' },
  { title: 'Sản phẩm', icon: ChevronRight, url: '/products' },
  { title: 'Danh mục', icon: Tags, url: '/categories' },
  { title: 'Nhập kho', icon: PlusCircle, url: '/stock-imports' },
  { title: 'Bán hàng', icon: ShoppingCart, url: '/sales' },
  { title: 'Người dùng', icon: Users, url: '/users', roles: ['ADMIN', 'SYSTEM_ADMIN'] },
  { title: 'Báo cáo', icon: BarChart3, url: '/reports' },
  { title: 'Nhật ký', icon: History, url: '/audit-log' },
];

export function AppSidebar() {
  const { user, logout } = useAuthStore();
  const location = useLocation();

  const filteredMenuItems = menuItems.filter((item) => {
    if (!item.roles) return true;
    return user && item.roles.includes(user.role);
  });

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader className="flex h-16 items-center border-b px-6">
        <div className="flex items-center gap-2 font-bold text-xl">
          <Package className="h-6 w-6 text-primary" />
          <span className="group-data-[collapsible=icon]:hidden">V-Inventory</span>
        </div>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Menu Chính</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {filteredMenuItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton
                    asChild
                    isActive={location.pathname === item.url}
                    tooltip={item.title}
                  >
                    <Link to={item.url}>
                      <item.icon />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter className="border-t p-4">
        <div className="flex items-center gap-3 px-2 py-1 group-data-[collapsible=icon]:hidden">
          <div className="flex-1 overflow-hidden">
            <p className="truncate text-sm font-medium">{user?.fullName}</p>
            <p className="truncate text-xs text-muted-foreground capitalize">{user?.role.toLowerCase()}</p>
          </div>
          <button
            onClick={logout}
            className="rounded-md p-2 hover:bg-accent hover:text-accent-foreground"
            title="Đăng xuất"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </SidebarFooter>
    </Sidebar>
  );
}
