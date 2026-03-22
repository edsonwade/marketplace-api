import { useEffect, useState, type ReactNode } from 'react';
import { cookies } from '../utils/cookies';
import { getAccessToken, setAccessToken } from '../api/client/apiClient';
import apiClient from '../api/client/apiClient';
import { useAuthStore } from '../store';
import { FullPageSpinner } from '../components/ui/Spinner';

/**
 * Runs once on app mount.
 *
 * 1. No refresh_token cookie → render immediately (guest)
 * 2. Has refresh_token but no access token → exchange it via /auth/refresh-token
 * 3. Then call /customers/me → backend auto-creates Customer if missing
 * 4. Only on 401 (token truly invalid) → clear auth and redirect to login
 */
export const AuthInitializer = ({ children }: { children: ReactNode }) => {
  const [ready, setReady] = useState(false);
  const { setAuth, setUser, clearAuth } = useAuthStore();

  useEffect(() => {
    const init = async () => {
      const refreshToken = cookies.get('refresh_token');

      // No cookie — guest, render immediately
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

        // /customers/me — backend auto-creates Customer if one doesn't exist yet
        const meRes = await apiClient.get('/api/v1/customers/me');
        const customer = meRes.data;
        setAuth(customer);
        setUser(customer);

      } catch (err: any) {
        const status = err?.response?.status;
        if (status === 401 || !status) {
          // Refresh token expired/invalid → full logout
          setAccessToken(null);
          cookies.remove('refresh_token');
          sessionStorage.removeItem('_at');
          clearAuth();
        }
        // Other errors (network, 500) → keep auth, app will retry on next request
      } finally {
        setReady(true);
      }
    };

    init();
  }, []);

  if (!ready) return <FullPageSpinner />;
  return <>{children}</>;
};
