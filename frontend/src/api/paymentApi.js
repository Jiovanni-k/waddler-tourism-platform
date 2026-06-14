import axiosInstance from "./axiosInstance";

export const payBooking   = (data)            => axiosInstance.post("payments", data);
export const getMyLoyalty = ()                => axiosInstance.get("loyalty/my");