import { createFileRoute, redirect } from '@tanstack/react-router';
import { AppLayout } from '@/components/layout/AppLayout';
import { useAuthStore } from '@/store/auth';

// eslint-disable-next-line react-refresh/only-export-components
export const Route = createFileRoute('/_auth')({
  beforeLoad: ({ location }) => {
    // Check if authenticated
    const { accessToken } = useAuthStore.getState();
    if (!accessToken) {
      throw redirect({
        to: '/login',
        search: {
          redirect: location.href,
        },
      });
    }
  },
  component: AppLayout,
});
