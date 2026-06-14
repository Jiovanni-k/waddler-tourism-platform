import axiosInstance from "./axiosInstance";

// POST /contact  body: { subject, message }
export const submitContactRequest = (data) =>
  axiosInstance.post("contact", data);

// GET /me/contact-requests → list of user's own requests
export const getMyContactRequests = () =>
  axiosInstance.get("me/contact-requests");
