import axios, { type AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { cookies } from '../../utils/cookies';

// In dev:  'http://localhost:8081' + '/api/v1/...' = full URL
// In prod: '' + '/api/v1/...'  = '/api/v1/...' → nginx proxies to backend
const BASE_URL = import.meta.env.VITE_API_URL ?? '';

let accessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
  accessToken = token;
  // Persist in sessionStorage so page refresh within the same tab survives
  if (token) {
    sessionStorage.setItem('_at', token);
  } else {
    sessionStorage.removeItem('_at');
  }
};

export const getAccessToken = () => {
  // Restore from sessionStorage if in-memory slot is empty (e.g. after hot-reload)
  if (!accessToken) {
    const stored = sessionStorage.getItem('_at');
    if (stored) accessToken = stored;
  }
  return accessToken;
};

export const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (v: unknown) => void; reject: (r?: unknown) => void }> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token)));
  failedQueue = [];
};

// ── Request interceptor — attach Bearer token ────────────────────────────────
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor — refresh on 401 OR 403 ────────────────────────────
// Spring Security returns 403 (not 401) when request has no credentials at all.
// We attempt a silent refresh in both cases; if refresh fails → redirect /login.
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const status = error.response?.status;

    // Only intercept 401 / 403 and skip auth endpoints themselves
    const isAuthEndpoint = originalRequest?.url?.includes('/api/v1/auth');
    if ((status === 401 || status === 403) && !originalRequest._retry && !isAuthEndpoint) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const res = await apiClient.post('/api/v1/auth/refresh-token');
        const { access_token, refresh_token } = res.data;

        setAccessToken(access_token);
        cookies.set('refresh_token', refresh_token, 7);

        processQueue(null, access_token);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${access_token}`;
        }
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        setAccessToken(null);
        cookies.remove('refresh_token');
        sessionStorage.removeItem('_at');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
