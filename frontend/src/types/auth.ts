export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  role: "CITIZEN" | "ADMIN";
}

export interface AuthUser {
  userId: number;
  role: "CITIZEN" | "ADMIN";
  email: string;
}
