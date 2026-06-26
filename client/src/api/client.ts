import axios from 'axios';
import { authStore } from '../store/authStore';

export const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://property-intel.up.railway.app/api/v1',
  withCredentials: true,         // Required for HttpOnly refresh cookie
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach access token
client.interceptors.request.use((config) => {
  const token = authStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default client;
