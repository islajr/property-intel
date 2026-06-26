import client from './client';
import type { AuthResponse } from '../types/api';

export const login = async (body: Record<string, string>): Promise<AuthResponse> => {
  const response = await client.post<AuthResponse>('/auth/login', body);
  return response.data;
};

export const register = async (body: Record<string, string>): Promise<AuthResponse> => {
  const response = await client.post<AuthResponse>('/auth/register', body);
  return response.data;
};

export const refresh = async (): Promise<AuthResponse> => {
  const response = await client.post<AuthResponse>('/auth/refresh');
  return response.data;
};

export const logout = async (): Promise<void> => {
  await client.post('/auth/logout');
};
