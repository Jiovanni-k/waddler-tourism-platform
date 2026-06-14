// Lightweight API client for the Waddler backend.
// Falls back to local mock data when VITE_API_URL is unset or the request fails.

const BASE = import.meta.env.VITE_API_URL ?? "";

function authHeaders(): HeadersInit {
  const token = typeof window !== "undefined" ? localStorage.getItem("waddler_token") : null;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  if (!BASE) throw new Error("API_DISABLED");
  const res = await fetch(`${BASE}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
      ...(init.headers ?? {}),
    },
  });
  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status}: ${body || res.statusText}`);
  }
  return res.json() as Promise<T>;
}

// ----- Hotels -----
export interface HotelQuery {
  name?: string;
  city?: string;
  minStars?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
}

export const hotelsApi = {
  list: (q: HotelQuery = {}) => {
    const params = new URLSearchParams();
    if (q.name) params.set("name", q.name);
    if (q.city && q.city !== "All") params.set("city", q.city);
    if (q.minStars) params.set("minStars", String(q.minStars));
    if (q.maxPrice != null) params.set("maxPrice", String(q.maxPrice));
    params.set("page", String(q.page ?? 1));
    params.set("size", String(q.size ?? 6));
    return request<{ items: any[]; total: number; page: number; pages: number }>(
      `/hotels?${params.toString()}`,
    );
  },
};

// ----- Reviews -----
export interface ReviewPayload {
  rating: number;
  text: string;
  bookingId?: string;
}

export const reviewsApi = {
  create: (hotelId: string, payload: ReviewPayload) =>
    request<{ id: string }>(`/hotels/${hotelId}/reviews`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};

export const isApiEnabled = () => Boolean(BASE);
