import { apiClient } from "./apiClient";
import type { AuthResponse } from "@/types/auth";

export async function register(name: string, email: string, password: string): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>("/auth/register", { name, email, password });
  return res.data;
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>("/auth/login", { email, password });
  return res.data;
}

export async function refreshToken(refreshToken: string): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>("/auth/refresh", { refreshToken });
  return res.data;
}
