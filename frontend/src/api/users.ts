import apiClient from './client';
export type { User } from '@/types/auth';

export interface CreateUserRequest {
  username: string;
  fullName: string;
  password?: string;
  role: string;
}

export const userApi = {
  getUsers: (params?: any) => apiClient.get<User[]>('/users', { params }),
  getUser: (id: string) => apiClient.get<User>(`/users/${id}`),
  createUser: (data: CreateUserRequest) => apiClient.post<User>('/users', data),
  updateUser: (id: string, data: Partial<CreateUserRequest>) => apiClient.put<User>(`/users/${id}`, data),
  deactivateUser: (id: string) => apiClient.delete(`/users/${id}`),
  resetPassword: (id: string, data: { newPassword: string }) => apiClient.post(`/users/${id}/reset-password`, data),
};
