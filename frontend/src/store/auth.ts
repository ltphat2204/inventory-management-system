import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserDto } from '@/types/auth';

interface AuthState {
  user: UserDto | null;
  accessToken: string | null;
  setAuth: (user: UserDto, accessToken: string) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      setAuth: (user, accessToken) => {
        localStorage.setItem('accessToken', accessToken);
        set({ user, accessToken });
      },
      logout: async () => {
        try {
          const { authApi } = await import('@/api/auth');
          await authApi.logout();
        } catch (e) {
          console.error('Logout failed', e);
        } finally {
          localStorage.removeItem('accessToken');
          set({ user: null, accessToken: null });
          window.location.href = '/login';
        }
      },
      isAuthenticated: () => !!get().accessToken,
    }),
    {
      name: 'auth-storage',
    }
  )
);
