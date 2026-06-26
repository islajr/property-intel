import axios from 'axios';
import { authStore } from '../store/authStore';

const getApiBaseUrl = (): string => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;
  if (!envUrl) {
    return 'https://property-intel.up.railway.app/api/v1';
  }
  const cleanUrl = envUrl.trim().replace(/\/$/, '');
  return cleanUrl.endsWith('/api/v1') ? cleanUrl : `${cleanUrl}/api/v1`;
};

const BASE_URL = getApiBaseUrl();

export const client = axios.create({
  baseURL: BASE_URL,
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

// Response interceptor: handle 401, attempt silent refresh
client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    
    // Check if error is 401 and we haven't retried yet
    if (error.response?.status === 401 && original && !original._retry) {
      original._retry = true;
      
      try {
        // Run refresh using clean axios instance to avoid infinite loop
        const res = await axios.post(`${BASE_URL}/auth/refresh`, {}, {
          withCredentials: true,
          headers: { 'Content-Type': 'application/json' },
        });
        
        const { accessToken, expiresIn } = res.data;
        
        // Update in-memory auth store
        authStore.getState().setToken(accessToken, expiresIn);
        
        // Re-inject token into headers and retry original request
        original.headers.Authorization = `Bearer ${accessToken}`;
        return client(original);
      } catch (refreshError) {
        // Clear auth state on refresh failure
        authStore.getState().clearToken();
        
        // Redirect to login preserving destination path
        const redirectPath = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `/login?redirect=${redirectPath}`;
        
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

export default client;
