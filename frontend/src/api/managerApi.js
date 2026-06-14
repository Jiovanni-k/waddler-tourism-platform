import axiosInstance from "./axiosInstance";

// ─── Hotels ──────────────────────────────────────────────────────────────────
export const getMyHotels  = ()          => axiosInstance.get("hotels/my");
export const createHotel  = (data)      => axiosInstance.post("hotels", data);
export const updateHotel  = (id, data)  => axiosInstance.put(`hotels/${id}`, data);
export const deleteHotel  = (id)        => axiosInstance.delete(`hotels/${id}`);

// ─── Rooms ───────────────────────────────────────────────────────────────────
export const getRooms    = (hotelId)              => axiosInstance.get(`hotels/${hotelId}/rooms`);
export const createRoom  = (hotelId, data)        => axiosInstance.post(`hotels/${hotelId}/rooms`, data);
export const updateRoom  = (hotelId, roomId, data)=> axiosInstance.put(`hotels/${hotelId}/rooms/${roomId}`, data);
export const deleteRoom  = (hotelId, roomId)      => axiosInstance.delete(`hotels/${hotelId}/rooms/${roomId}`);

// ─── Events ──────────────────────────────────────────────────────────────────
export const getEvents    = (hotelId, params = {}) => axiosInstance.get(`events/hotels/${hotelId}/events`, { params });
export const createEvent  = (hotelId, data)        => axiosInstance.post(`events/hotels/${hotelId}/events`, data);
export const updateEvent  = (id, data)             => axiosInstance.put(`events/${id}`, data);
export const deleteEvent  = (id)                   => axiosInstance.delete(`events/${id}`);
export const publishEvent = (id)                   => axiosInstance.patch(`events/${id}/publish`);
export const cancelEvent  = (id)                   => axiosInstance.patch(`events/${id}/cancel`);

// ─── Bookings ─────────────────────────────────────────────────────────────────
export const getManagerBookings = (params = {}) => axiosInstance.get("bookings", { params });
export const confirmBooking     = (id)          => axiosInstance.patch(`bookings/${id}/confirm`);
export const completeBooking    = (id)          => axiosInstance.patch(`bookings/${id}/complete`);
export const cancelBooking      = (id)          => axiosInstance.patch(`bookings/${id}/cancel`);