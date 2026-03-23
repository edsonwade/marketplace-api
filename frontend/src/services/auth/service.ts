import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient, { setAccessToken } from '../../api/client/apiClient';
import { cookies } from '../../utils/cookies';
import { decodeToken } from '../../utils/jwt';
import { useAuthStore } from '../../store';
import type { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../../api/types';

const AUTH_ENDPOINTS = {
  LOGIN: '/api/v1/auth/login',
  REGISTER: '/api/v1/auth/register',
  REFRESH_TOKEN: '/api/v1/auth/refresh-token',
  LOGOUT: '/api/v1/auth/logout',
};

export const authService = {
  login: async (credentials: AuthenticationRequest): Promise<AuthenticationResponse> => {
    const response = await apiClient.post<AuthenticationResponse>(AUTH_ENDPOINTS.LOGIN, credentials);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthenticationResponse> => {
    const response = await apiClient.post<AuthenticationResponse>(AUTH_ENDPOINTS.REGISTER, data);
    return response.data;
  },

  refreshToken: async (): Promise<AuthenticationResponse> => {
    const response = await apiClient.post<AuthenticationResponse>(AUTH_ENDPOINTS.REFRESH_TOKEN);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post(AUTH_ENDPOINTS.LOGOUT);
    setAccessToken(null);
    cookies.remove('refresh_token');
  },
};

export const useLogin = () => {
  const { setAuth, setUser, setRole } = useAuthStore();
  return useMutation({
    mutationFn: (credentials: AuthenticationRequest) => authService.login(credentials),
    onSuccess: async (data) => {
      setAccessToken(data.access_token);
      cookies.set('refresh_token', data.refresh_token, 7);
      // Decode role from access token
      const payload = decodeToken(data.access_token);
      const role = payload?.role ?? 'USER';
      setRole(role);
      // Resolve the Customer record linked to this user email
      try {
        const res = await apiClient.get('/api/v1/customers/me');
        const customer = res.data;
        setAuth(customer, role);
        setUser(customer);
      } catch {
        setAuth(undefined, role);
      }
    },
  });
};

export const useRegister = () => {
  return useMutation({
    mutationFn: (data: RegisterRequest) => authService.register(data),
  });
};

export const useLogout = () => {
  const queryClient = useQueryClient();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  return useMutation({
    mutationFn: () => authService.logout(),
    onSuccess: () => {
      clearAuth();
      queryClient.clear();
    },
  });
};

export const useAuth = () => {
  return {
    isAuthenticated: !!cookies.get('refresh_token'),
  };
};

export const useTokenRefresh = () => {
  return useMutation({
    mutationFn: () => authService.refreshToken(),
    onSuccess: (data) => {
      setAccessToken(data.access_token);
      cookies.set('refresh_token', data.refresh_token, 7);
    },
  });
};
