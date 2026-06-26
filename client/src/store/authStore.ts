import { create } from 'zustand';

interface AuthState {
  accessToken: string | null;
  expiresAt: number | null;        // Unix timestamp in ms
  isAuthenticated: boolean;
  setToken: (token: string, expiresIn: number) => void;
  clearToken: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  expiresAt: null,
  isAuthenticated: false,
  setToken: (token, expiresIn) => {
    const expiresAt = Date.now() + expiresIn * 1000;
    set({
      accessToken: token,
      expiresAt,
      isAuthenticated: true,
    });
  },
  clearToken: () => {
    set({
      accessToken: null,
      expiresAt: null,
      isAuthenticated: false,
    });
  },
}));

// Static wrapper to read/write state outside of React contexts (e.g. Axios interceptors)
export const authStore = {
  getState: () => useAuthStore.getState(),
  subscribe: (listener: (state: AuthState) => void) => useAuthStore.subscribe(listener),
};
