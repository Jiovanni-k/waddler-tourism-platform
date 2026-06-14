import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_URL || "/api";

const axiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// ── Request: attach JWT automatically ────────────────────────────────────────
// Auth endpoints (login, signup, refresh, google) use credentials — never send a Bearer token.
const AUTH_ENDPOINTS = ["auth/login", "auth/signup", "auth/refresh", "auth/google"];

/** Returns true if the JWT is expired (or undecodable). */
const isJwtExpired = (token) => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.exp * 1000 < Date.now();
  } catch {
    return true; // can't decode → treat as expired
  }
};

axiosInstance.interceptors.request.use(
  (config) => {
    const isAuthCall = AUTH_ENDPOINTS.some((ep) => config.url?.includes(ep));
    if (isAuthCall) return config; // no token for credential-based calls

    // Try unified key first, then legacy key
    let token = localStorage.getItem("waddler_token") || localStorage.getItem("token");

    // If the token is expired, clear it immediately — don't send it
    if (token && isJwtExpired(token)) {
      localStorage.removeItem("waddler_token");
      localStorage.removeItem("waddler_refreshToken");
      localStorage.removeItem("waddler_user");
      token = null;
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token.replace(/^\"|\"$/g, "")}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response: on 401 try to refresh, then retry original request ──────────────
let isRefreshing = false;
let failedQueue  = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else       prom.resolve(token);
  });
  failedQueue = [];
};

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status !== 401) {
      return Promise.reject(error);
    }

    // /auth/me failing just means the user is a guest — don't redirect
    if (originalRequest.url?.includes("/auth/me")) {
      return Promise.reject(error);
    }

    // login/signup failing means wrong credentials — don't redirect, just reject
    if (AUTH_ENDPOINTS.some((ep) => originalRequest.url?.includes(ep))) {
      return Promise.reject(error);
    }

    // /auth/refresh failing means refresh token is expired — clear and redirect
    if (originalRequest.url?.includes("/auth/refresh")) {
      clearAndRedirect();
      return Promise.reject(error);
    }

    // Already retried — only redirect if user had tokens (was logged in)
    if (originalRequest._retry) {
      if (localStorage.getItem("waddler_token")) clearAndRedirect();
      return Promise.reject(error);
    }

    // No refresh token — only redirect if user had an active session
    const refreshToken = localStorage.getItem("waddler_refreshToken");
    if (!refreshToken) {
      if (localStorage.getItem("waddler_token")) {
        // User was logged in but has no refresh token → clear and redirect
        clearAndRedirect();
      }
      // Guest user (no tokens at all) → just reject, let the page handle it
      return Promise.reject(error);
    }

    // Try to refresh the token
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosInstance(originalRequest);
        })
        .catch((err) => Promise.reject(err));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const res = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken });
      const { accessToken } = res.data;

      localStorage.setItem("waddler_token", accessToken);
      axiosInstance.defaults.headers.common["Authorization"] = `Bearer ${accessToken}`;

      processQueue(null, accessToken);
      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      clearAndRedirect();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

function clearAndRedirect() {
  localStorage.removeItem("waddler_token");
  localStorage.removeItem("waddler_refreshToken");
  localStorage.removeItem("waddler_user");
  window.location.href = "/login";
}

export default axiosInstance;
