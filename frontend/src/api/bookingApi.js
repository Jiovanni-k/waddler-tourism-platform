import axiosInstance from "./axiosInstance";

export const createBooking  = (data)       => axiosInstance.post("bookings", data);
export const getMyBookings  = ()           => axiosInstance.get("bookings");
export const cancelBooking  = (id)         => axiosInstance.patch(`bookings/${id}/cancel`);