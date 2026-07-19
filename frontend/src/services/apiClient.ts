import axios from "axios";

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Attaches the JWT access token (if present) to every outgoing request.
// Token is read fresh from localStorage on each request rather than cached,
// so a login/logout in another tab or AuthContext update takes effect immediately.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Access tokens expire after 15 minutes (see backend app.jwt.access-token-expiration-ms).
// Without this, any session running longer than that would start failing every
// authenticated request with a silent 401 until the user manually logged out and
// back in. This transparently exchanges the refresh token for a new access token
// once, retries the original request, and only forces a logout if the refresh
// token itself is invalid/expired too.
let refreshPromise: Promise<string> | null = null;

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    const isAuthEndpoint = originalRequest?.url?.includes("/auth/");
    if (error.response?.status !== 401 || isAuthEndpoint || originalRequest._retry) {
      return Promise.reject(error);
    }

    const storedRefreshToken = localStorage.getItem("refreshToken");
    if (!storedRefreshToken) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = axios
          .post(`${apiClient.defaults.baseURL}/auth/refresh`, { refreshToken: storedRefreshToken })
          .then((res) => {
            const newAccessToken = res.data.accessToken;
            localStorage.setItem("accessToken", newAccessToken);
            return newAccessToken;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const newAccessToken = await refreshPromise;
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return apiClient(originalRequest);
    } catch {
      // Refresh token itself is invalid/expired - force a clean logout.
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("userId");
      localStorage.removeItem("role");
      window.location.href = "/login";
      return Promise.reject(error);
    }
  }
);
