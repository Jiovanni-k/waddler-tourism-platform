import axiosInstance from "./axiosInstance";
import { demoHotelsForApi, listFromApiData, localFallbackEnabled } from "../lib/hotelFallback";

const fallbackResponse = (data) => ({ data });

const withLocalHotelFallback = (response) => {
  if (!localFallbackEnabled()) return response;
  return listFromApiData(response.data).length > 0 ? response : fallbackResponse(demoHotelsForApi);
};

export const getHotels = (params = {}) =>
  axiosInstance.get("hotels", { params: { page: 0, size: 500, ...params } })
    .then(withLocalHotelFallback)
    .catch((error) => {
      if (!localFallbackEnabled()) throw error;
      console.warn("[hotelApi] Backend hotels request failed, using local demo data.", error?.message);
      return fallbackResponse(demoHotelsForApi);
    });

// GET /hotels/search?q=...
export const searchHotels = (q, page = 0, size = 9) =>
  axiosInstance.get("/hotels/search", { params: { q, page, size } });

// GET /hotels/{id}
export const getHotelById = (id) =>
  axiosInstance.get(`hotels/${id}`)
    .catch((error) => {
      if (!localFallbackEnabled()) throw error;
      const hotel = demoHotelsForApi.find((item) => String(item.id) === String(id));
      if (!hotel) throw error;
      console.warn("[hotelApi] Backend hotel detail request failed, using local demo data.", error?.message);
      return fallbackResponse(hotel);
    });

// GET /hotels/{hotelId}/rooms
export const getRoomsByHotel = (hotelId) =>
  axiosInstance.get(`/hotels/${hotelId}/rooms`);

// GET /hotels/{hotelId}/reviews
export const getReviewsByHotel = (hotelId, params = {}) =>
  axiosInstance.get(`/hotels/${hotelId}/reviews`, { params });

// GET /availability/check?hotelId=&checkIn=&checkOut=&guests=
export const checkAvailability = (params) =>
  axiosInstance.get("/availability/check", { params });

// POST /hotels/{hotelId}/reviews  body: { rating, comment }
export const submitReview = (hotelId, data) =>
  axiosInstance.post(`/hotels/${hotelId}/reviews`, data);
