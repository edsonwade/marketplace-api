import { useEffect, useState, type ReactNode } from 'react';
import { cookies } from '../utils/cookies';
import { getAccessToken, setAccessToken } from '../api/client/apiClient';
import apiClient from '../api/client/apiClient';
import { FullPageSpinner } from '../components/ui/Spinner';

/**
 * AuthInitializer — runs once on app mount.
 *
 * Problem it solves:
 *   The access token lives in memory (and sessionStorage as backup).
 *   On a full page refresh, sessionStorage survives BUT a new tab / Docker
 *   container restart loses it. If the user has a valid refresh_token cookie
 *   we silently exchange it for a fresh access token before rendering anything.
 *
 * Flow:
 *   1. refresh_token cookie missing → nothing to do, render children (guest)
 *   2. access token already in memory/sessionStorage → nothing to do, render
 *   3. refresh_token exists but no access token → call /refresh-token, store,
 *      then render children (authenticated)
 *   4. refresh fails → clear cookie, render children (guest, redirected by ProtectedRoute)
 */
export const AuthInitializer = ({ children }: { children: ReactNode }) => {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const init = async () => {
      const refreshToken = cookies.get('refresh_token');

      // No cookie → nothing to initialize
      if (!refreshToken) {
        setReady(true);
        return;
      }

      // Access token already present (same-tab navigation or sessionStorage restore)
      if (getAccessToken()) {
        setReady(true);
        return;
      }

      // Cookie exists but no access token → exchange it
      try {
        const res = await apiClient.post('/api/v1/auth/refresh-token');
        const { access_token, refresh_token } = res.data;
        setAccessToken(access_token);
        cookies.set('refresh_token', refresh_token, 7);
      } catch {
        // Refresh token expired / invalid → clear and let ProtectedRoute redirect
        setAccessToken(null);
        cookies.remove('refresh_token');
        sessionStorage.removeItem('_at');
      } finally {
        setReady(true);
      }
    };

    init();
  }, []);

  if (!ready) return <FullPageSpinner />;
  return <>{children}</>;
};
