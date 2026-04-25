import { createFileRoute, redirect } from '@tanstack/react-router';
import { LoginPage } from '@/components/auth/LoginPage';
import { useAuthStore } from '@/store/auth';
import * as z from 'zod';

const loginSearchSchema = z.object({
  redirect: z.string().optional(),
});

// eslint-disable-next-line react-refresh/only-export-components
export const Route = createFileRoute('/login')({
  validateSearch: (search) => loginSearchSchema.parse(search),
  beforeLoad: () => {
    if (useAuthStore.getState().accessToken) {
      throw redirect({ to: '/' });
    }
  },
  component: LoginPage,
});
