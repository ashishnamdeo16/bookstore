import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authService } from '../api/authService';
import { apiClient, setSessionClearedListener } from '../api/client';
import { getUserFromToken, isTokenExpired } from './jwt';
import { tokenStorage } from './tokenStorage';
import type { AuthTokens, AuthUser, LoginRequest, RegisterRequest } from '../types/auth';

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (payload: Omit<LoginRequest, 'deviceId'>) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

async function restoreSession(): Promise<AuthUser | null> {
  const accessToken = tokenStorage.getAccessToken();
  const refreshToken = tokenStorage.getRefreshToken();

  if (!accessToken && !refreshToken) {
    return null;
  }

  if (accessToken && !isTokenExpired(accessToken)) {
    return getUserFromToken(accessToken);
  }

  if (!refreshToken) {
    tokenStorage.clear();
    return null;
  }

  try {
    const tokens = await apiClient<AuthTokens>('/auth/refresh', {
      method: 'POST',
      body: { refreshToken },
      auth: false,
      retry: false,
    });
    tokenStorage.setTokens(tokens);
    return getUserFromToken(tokens.accessToken);
  } catch {
    tokenStorage.clear();
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    let active = true;

    void (async () => {
      const restored = await restoreSession();
      if (active) {
        setUser(restored);
        setIsInitializing(false);
      }
    })();

    setSessionClearedListener(() => {
      if (active) setUser(null);
    });

    return () => {
      active = false;
      setSessionClearedListener(null);
    };
  }, []);

  const login = useCallback(async (payload: Omit<LoginRequest, 'deviceId'>) => {
    const tokens = await authService.login(payload);
    const nextUser = getUserFromToken(tokens.accessToken);
    if (!nextUser) {
      tokenStorage.clear();
      throw new Error('Unable to read authentication details from token.');
    }
    setUser(nextUser);
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    await authService.register(payload);
  }, []);

  const logout = useCallback(async () => {
    await authService.logout();
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isInitializing,
      login,
      register,
      logout,
    }),
    [user, isInitializing, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
