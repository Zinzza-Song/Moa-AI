import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { getMyInfo, logoutApi } from '../api/auth';
import type { TokenResponse, UserResponse } from '../types/auth';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

interface AuthContextValue {
  user: UserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (tokens: TokenResponse) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<UserResponse | null>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function clearStoredTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshUser = useCallback(async (): Promise<UserResponse | null> => {
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (!accessToken) {
      setUser(null);
      return null;
    }

    try {
      const me = await getMyInfo();
      setUser(me);
      return me;
    } catch {
      clearStoredTokens();
      setUser(null);
      return null;
    }
  }, []);

  useEffect(() => {
    let mounted = true;

    async function initAuth() {
      const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

      if (!accessToken && !refreshToken) {
        if (mounted) {
          setIsLoading(false);
        }
        return;
      }

      try {
        const me = await getMyInfo();
        if (mounted) {
          setUser(me);
        }
      } catch {
        clearStoredTokens();
        if (mounted) {
          setUser(null);
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    void initAuth();

    return () => {
      mounted = false;
    };
  }, []);

  const login = useCallback(
    async (tokens: TokenResponse): Promise<void> => {
      localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
      localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
      const me = await refreshUser();
      if (!me) {
        throw new Error('사용자 정보를 불러오지 못했습니다.');
      }
    },
    [refreshUser],
  );

  const logout = useCallback(async (): Promise<void> => {
    try {
      if (localStorage.getItem(ACCESS_TOKEN_KEY)) {
        await logoutApi();
      }
    } catch {
      // 백엔드 로그아웃 호출이 실패하더라도 클라이언트 토큰은 반드시 정리한다.
    } finally {
      clearStoredTokens();
      setUser(null);
    }
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isLoading,
      login,
      logout,
      refreshUser,
    }),
    [user, isLoading, login, logout, refreshUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.');
  }
  return context;
}
