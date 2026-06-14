import axiosInstance from "./axiosInstance";
 
// POST /auth/login   body: { email, password }
export const login = (credentials) =>
  axiosInstance.post("auth/login", credentials);
 
// POST /auth/signup  body: { email, password, firstName, lastName, birthDate, gender, ... }
export const register = (userData) =>
  axiosInstance.post("auth/signup", userData);
 
// POST /auth/google  body: { idToken }
export const googleLogin = (idToken) =>
  axiosInstance.post("auth/google", { idToken });

// GET  /auth/me  → { id, email, firstName, lastName, role, status, ... }
export const getMe = () =>
  axiosInstance.get("auth/me");
 
// PUT  /auth/me  body: { firstName, lastName, phone, nationality, ... }
export const updateMe = (data) =>
  axiosInstance.put("auth/me", data);
 
// PUT  /auth/change-password  body: { currentPassword, newPassword, confirmPassword }
export const changePassword = (data) =>
  axiosInstance.put("auth/change-password", data);

// GET  /me/bookings
export const getMyBookings = () =>
  axiosInstance.get("me/bookings");

// PATCH /bookings/{id}/cancel
export const cancelBooking = (id) =>
  axiosInstance.patch(`bookings/${id}/cancel`);
