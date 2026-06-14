import axiosInstance from "./axiosInstance";

// POST /api/reservations/rooms — book a room
export const bookRoom = (data) =>
  axiosInstance.post("reservations/rooms", data);

// POST /api/reservations/events — book an event
export const bookEvent = (data) =>
  axiosInstance.post("reservations/events", data);

// GET /api/reservations/me — current user's reservations
export const getMyReservations = () =>
  axiosInstance.get("reservations/me");

// DELETE /api/reservations/:id — cancel a reservation
export const cancelReservation = (id) =>
  axiosInstance.delete(`reservations/${id}`);
