import { createContext, useContext, useState, useCallback, type ReactNode } from "react";
import * as authApi from "@/services/authApi";
import type { AuthUser } from "@/types/auth";

// Minimal JWT payload decode (no verification - the backend is the source of truth,
// this is purely so the UI knows who's logged in without an extra round-trip).
function decodeJwtEmail(token: string): string {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.sub ?? "";
  } catch {
    return "";
  }
}

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadInitialUser(): AuthUser | null {
  const token = localStorage.getItem("accessToken");
  const userId = localStorage.getItem("userId");
  const role = localStorage.getItem("role");
  if (!token || !userId || !role) return null;
  return { userId: Number(userId), role: role as AuthUser["role"], email: decodeJwtEmail(token) };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadInitialUser);

  function persistSession(res: Awaited<ReturnType<typeof authApi.login>>) {
    localStorage.setItem("accessToken", res.accessToken);
    localStorage.setItem("refreshToken", res.refreshToken);
    localStorage.setItem("userId", String(res.userId));
    localStorage.setItem("role", res.role);
    setUser({ userId: res.userId, role: res.role, email: decodeJwtEmail(res.accessToken) });
  }

  const login = useCallback(async (email: string, password: string) => {
    const res = await authApi.login(email, password);
    persistSession(res);
  }, []);

  const register = useCallback(async (name: string, email: string, password: string) => {
    const res = await authApi.register(name, email, password);
    persistSession(res);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    setUser(null);
  }, []);

  const value: AuthContextValue = {
    user,
    isAuthenticated: user !== null,
    isAdmin: user?.role === "ADMIN",
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
