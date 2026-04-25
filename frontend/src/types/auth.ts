export interface UserDto {
  id: number;
  fullName: string;
  role: 'ADMIN' | 'MANAGER' | 'CASHIER' | 'VIEWER' | 'SYSTEM_ADMIN';
}

export interface User extends UserDto {
  username: string;
  isActive: boolean;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  user: UserDto;
}

export interface LoginRequest {
  username: string;
  password: string;
}
