import { useEffect, useState, type ReactNode } from 'react';
import { cookies } from '../utils/cookies';
import { getAccessToken, setAccessToken } from '../api/client/apiClient';
import apiClient from '../api/client/apiClient';
import { decodeToken } from '../utils/jwt';
import { useAuthStore } from '../store';
import { FullPageSpinner } from '../components/ui/Spinner';

/**
 * Runs once on app mount.
 * 1. No refresh_token cookie → render immediately (guest)
 * 2. Restores access token via /auth/refresh-token if needed
 * 3. Decodes access token to get role
 * 4. Calls /customers/me to restore user profile
 * 5. Only on 401 (token invalid) → clears auth
 */
export const AuthInitializer = ({ children }: { children: ReactNode }) => {
  const [ready, setReady] = useState(false);
  const { setAuth, setUser, setRole, clearAuth } = useAuthStore();

  useEffect(() => {
    const init = async () => {
      const refreshToken = cookies.get('refresh_token');

      if (!refreshToken) {
        setReady(true);
        return;
      }

      try {
        // Restore access token if not in memory
        if (!getAccessToken()) {
          const res = await apiClient.post('/api/v1/auth/refresh-token');
          const { access_token, refresh_token } = res.data;
          setAccessToken(access_token);
          cookies.set('refresh_token', refresh_token, 7);
        }

        // Decode access token to get role
        const token = getAccessToken();
        const payload = token ? decodeToken(token) : null;
        const role = payload?.role ?? 'USER';

        // Fetch customer profile — backend auto-creates if missing
        const meRes = await apiClient.get('/api/v1/customers/me');
        const customer = meRes.data;

        setAuth(customer, role);
        setUser(customer);
        setRole(role);

      } catch (err: any) {
        const status = err?.response?.status;
        if (status === 401 || !status) {
          setAccessToken(null);
          cookies.remove('refresh_token');
          sessionStorage.removeItem('_at');
          clearAuth();
        }
      } finally {
        setReady(true);
      }
    };

    init();
  }, []);

  if (!ready) return <FullPageSpinner />;
  return <>{children}</>;
};
