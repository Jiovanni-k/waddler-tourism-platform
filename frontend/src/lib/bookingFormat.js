export const bookingListFromResponse = (data) => {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.bookings)) return data.bookings;
  if (Array.isArray(data?.data)) return data.data;
  return [];
};

export const bookingTotalFromResponse = (data) =>
  Number(data?.totalElements ?? data?.total ?? data?.totalBookings ?? bookingListFromResponse(data).length ?? 0);

const fullName = (...parts) => parts.filter(Boolean).join(" ").trim();

export const getBookingGuestName = (booking) =>
  booking.guestName ||
  booking.customerName ||
  booking.userName ||
  booking.guest?.name ||
  booking.customer?.name ||
  booking.user?.name ||
  fullName(booking.guest?.firstName, booking.guest?.lastName) ||
  fullName(booking.user?.firstName, booking.user?.lastName) ||
  booking.email ||
  booking.guest?.email ||
  booking.user?.email ||
  "Guest";

export const getBookingHotelName = (booking) =>
  booking.hotelName ||
  booking.hotel?.name ||
  booking.room?.hotelName ||
  booking.room?.hotel?.name ||
  "Hotel";

export const getBookingDate = (booking) =>
  booking.createdAt || booking.bookingDate || booking.checkInDate || booking.checkIn || booking.startDate || "";

export const getBookingCheckIn = (booking) =>
  booking.checkIn || booking.checkInDate || booking.startDate || "N/A";

export const getBookingCheckOut = (booking) =>
  booking.checkOut || booking.checkOutDate || booking.endDate || "N/A";

export const getBookingStatus = (booking) =>
  booking.status || booking.bookingStatus || "PENDING";

export const getBookingPrice = (booking) =>
  Number(booking.totalPrice ?? booking.total ?? booking.price ?? booking.amount ?? 0);
