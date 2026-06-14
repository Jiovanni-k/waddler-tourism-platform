import axiosInstance from "./axiosInstance";

// ── Hotels / Licenses ─────────────────────────────────────────────────────────
export const adminGetAllHotels    = (params) => axiosInstance.get("hotels", { params });
export const adminGetPendingHotels= () => axiosInstance.get("hotels", { params: { status: "PENDING" } });
export const adminApproveHotel    = (id) => axiosInstance.patch(`hotels/${id}/approve`);
export const adminRejectHotel     = (id, reason) => axiosInstance.patch(`hotels/${id}/reject`, { reason });
export const adminCreateHotel     = (data) => axiosInstance.post("hotels", data);
export const adminUpdateHotel     = (id, data) => axiosInstance.put(`hotels/${id}`, data);
export const adminDeleteHotel     = (id) => axiosInstance.delete(`hotels/${id}`);

// ── Users ─────────────────────────────────────────────────────────────────────
export const adminGetAllUsers    = (params) => axiosInstance.get("users", { params });
export const adminUpdateUser     = (id, data) => axiosInstance.patch(`users/${id}`, data);
export const adminDeactivateUser = (id) => axiosInstance.patch(`users/${id}`, { active: false });

// ── Bookings ──────────────────────────────────────────────────────────────────
export const adminGetAllBookings = (params) => axiosInstance.get("bookings", { params });

// ── Payments ──────────────────────────────────────────────────────────────────
export const adminGetAllPayments = (params) => axiosInstance.get("payments", { params });

// ── Contacts / Support ────────────────────────────────────────────────────────
export const adminGetContactRequests = (params) => axiosInstance.get("contact-requests", { params });
export const adminResolveContact     = (id, response) =>
  axiosInstance.post(`contact-requests/${id}/resolve`, { response });

// ── Cancellation Policies ─────────────────────────────────────────────────────
export const adminGetPolicies    = () => axiosInstance.get("cancellation-policies");
export const adminCreatePolicy   = (data) => axiosInstance.post("cancellation-policies", data);
export const adminUpdatePolicy   = (id, data) => axiosInstance.put(`cancellation-policies/${id}`, data);
export const adminDeletePolicy   = (id) => axiosInstance.delete(`cancellation-policies/${id}`);
