import axiosInstance from "./axiosInstance";

export const getNotifications     = ()   => axiosInstance.get("notifications/my");
export const markNotificationRead = (id) => axiosInstance.patch(`notifications/${id}/read`);
export const markAllRead          = ()   => axiosInstance.patch("notifications/read-all");
