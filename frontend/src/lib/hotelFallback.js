import { hotels as localHotels } from "../data/hotels";
export { listFromApiData } from "./apiResponse";

export const demoHotelsForApi = localHotels.map((hotel) => {
  const [city = hotel.city, country = ""] = String(hotel.location || "").split(",").map((part) => part.trim());

  return {
    ...hotel,
    city,
    country,
    region: country,
    coverImageUrl: hotel.images?.[0],
    image: hotel.images?.[0],
    imageUrl: hotel.images?.[0],
    starRating: hotel.stars,
    averageGuestRating: hotel.rating,
    reviewCount: hotel.reviews,
    pricePerNight: hotel.price,
    minPrice: hotel.price,
    basePrice: hotel.price,
    status: "ACTIVE",
  };
});

export const demoPendingHotels = demoHotelsForApi.slice(0, 3).map((hotel, index) => ({
  ...hotel,
  id: `demo-pending-${hotel.id}`,
  status: "PENDING",
  manager: ["Maya Darwish", "Omar Saleh", "Lina Haddad"][index] || "Hotel Manager",
  managerEmail: ["maya@example.com", "omar@example.com", "lina@example.com"][index] || "manager@example.com",
  managerPhone: ["+970 599 111 234", "+970 599 222 345", "+970 599 333 456"][index] || "+970 599 000 000",
  managerIdChecked: true,
  licenseNum: `WDL-${2026}-${String(index + 41).padStart(3, "0")}`,
  rooms: 18 + index * 7,
}));

export const localFallbackEnabled = () => import.meta.env.VITE_USE_DEMO_DATA === "true";
