import apiClient from './client';
import type { AuthResponse, LoginRequest } from '@/types/auth';

export const authApi = {
  login: (data: LoginRequest) => 
    apiClient.post<AuthResponse>('/auth/login', data),
  
  refresh: () => 
    apiClient.post<AuthResponse>('/auth/refresh'),
  
  logout: () => 
    apiClient.post('/auth/logout'),
};
