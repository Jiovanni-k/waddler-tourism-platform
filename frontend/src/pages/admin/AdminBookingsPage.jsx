import { useState, useEffect } from "react";
import { 
  Search, 
  Calendar, 
  ChevronRight,
  Filter,
  ArrowUpDown,
  X
} from "lucide-react";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import { adminGetAllBookings } from "../../api/adminApi";
import exploringPenguin from "../../assets/penguins/Exploring.png";
import {
  bookingListFromResponse,
  getBookingCheckIn,
  getBookingCheckOut,
  getBookingDate,
  getBookingGuestName,
  getBookingHotelName,
  getBookingPrice,
  getBookingStatus,
} from "../../lib/bookingFormat";

const STATUS_OPTIONS = [
  { value: "ALL", label: "All statuses" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "PAID", label: "Paid" },
  { value: "PENDING", label: "Pending" },
  { value: "CANCELLED", label: "Cancelled" }
];

const SORT_OPTIONS = [
  { value: "newest", label: "Newest first" },
  { value: "oldest", label: "Oldest first" },
  { value: "priceHigh", label: "Price high to low" },
  { value: "priceLow", label: "Price low to high" },
  { value: "guest", label: "Guest A-Z" },
  { value: "hotel", label: "Hotel A-Z" },
  { value: "status", label: "Status A-Z" }
];

const AdminBookingsPage = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [sortBy, setSortBy] = useState("newest");
  const [selectedBooking, setSelectedBooking] = useState(null);

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      const res = await adminGetAllBookings({ page: 0, size: 100, sortBy: "createdAt", sortDir: "desc" });
      setBookings(bookingListFromResponse(res.data));
    } catch (err) {
      console.error(err);
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredBookings = bookings
    .filter((booking) => {
      const query = searchQuery.trim().toLowerCase();
      const guestName = getBookingGuestName(booking).toLowerCase();
      const hotelName = getBookingHotelName(booking).toLowerCase();
      const matchesSearch =
        !query ||
        guestName.includes(query) ||
        hotelName.includes(query) ||
        String(booking.id || booking.bookingId || "").toLowerCase().includes(query);

      const matchesStatus = statusFilter === "ALL" || getBookingStatus(booking) === statusFilter;
      return matchesSearch && matchesStatus;
    })
    .sort((a, b) => {
      if (sortBy === "oldest") return new Date(getBookingDate(a)) - new Date(getBookingDate(b));
      if (sortBy === "priceHigh") return getBookingPrice(b) - getBookingPrice(a);
      if (sortBy === "priceLow") return getBookingPrice(a) - getBookingPrice(b);
      if (sortBy === "guest") return getBookingGuestName(a).localeCompare(getBookingGuestName(b));
      if (sortBy === "hotel") return getBookingHotelName(a).localeCompare(getBookingHotelName(b));
      if (sortBy === "status") return getBookingStatus(a).localeCompare(getBookingStatus(b));
      return new Date(getBookingDate(b)) - new Date(getBookingDate(a));
    });

  if (loading) return (
    <AdminLayout>
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    </AdminLayout>
  );

  return (
    <AdminLayout>
      <PageHeader
        category="RESERVATIONS"
        title="Global Bookings"
        subtitle="Monitor every reservation happening across the platform in real-time."
        penguin={exploringPenguin}
      />

      {selectedBooking && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-card rounded-[32px] w-full max-w-lg shadow-glow p-8 border border-white/20 animate-in zoom-in duration-300">
            <div className="flex items-start justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-black text-primary-deep">Booking details</h2>
                <p className="text-xs font-black text-muted-foreground uppercase tracking-widest mt-1">
                  ID: {selectedBooking.id || selectedBooking.bookingId || "N/A"}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setSelectedBooking(null)}
                className="p-2 text-muted-foreground hover:text-destructive hover:bg-muted/20 rounded-xl transition-all"
              >
                <X size={20} />
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Hotel</div>
                <div className="font-black text-primary-deep">{getBookingHotelName(selectedBooking)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Guest</div>
                <div className="font-black text-primary-deep">{getBookingGuestName(selectedBooking)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Check-in</div>
                <div className="font-black text-primary-deep">{getBookingCheckIn(selectedBooking)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Check-out</div>
                <div className="font-black text-primary-deep">{getBookingCheckOut(selectedBooking)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Total</div>
                <div className="font-black text-primary-deep">${getBookingPrice(selectedBooking).toLocaleString()}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Status</div>
                <div className="font-black text-primary-deep">{getBookingStatus(selectedBooking)}</div>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setSelectedBooking(null)}
              className="w-full py-3 bg-primary text-primary-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft"
            >
              Close
            </button>
          </div>
        </div>
      )}

      <div className="bg-card rounded-[32px] shadow-soft overflow-hidden border border-border/30">
        <div className="p-6 border-b border-border/30 flex flex-col md:flex-row gap-4 items-center justify-between bg-muted/5">
          <div className="relative w-full md:w-96">
            <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input 
              type="text" 
              placeholder="Search bookings, guests, or hotels..." 
              className="w-full pl-12 pr-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm transition-all shadow-sm"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          
          <div className="flex flex-col sm:flex-row gap-2 w-full md:w-auto">
            <label className="flex items-center gap-2 px-5 py-3 bg-white border-2 border-border/30 rounded-2xl font-black text-sm text-primary-deep shadow-sm w-full sm:w-auto">
              <Filter size={18} className="text-primary" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-transparent outline-none font-black text-sm flex-1 cursor-pointer"
              >
                {STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
            <label className="flex items-center gap-2 px-5 py-3 bg-white border-2 border-border/30 rounded-2xl font-black text-sm text-primary-deep shadow-sm w-full sm:w-auto">
              <ArrowUpDown size={18} className="text-primary" />
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="bg-transparent outline-none font-black text-sm flex-1 cursor-pointer"
              >
                {SORT_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-muted/10">
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Booking Info</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Guest</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Dates</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Price</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Status</th>
                <th className="p-6 text-right"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/30">
              {filteredBookings.map((b, index) => {
                const bookingId = b.id || b.bookingId || index;
                const guestName = getBookingGuestName(b);
                const hotelName = getBookingHotelName(b);
                const status = getBookingStatus(b);

                return (
                <tr key={bookingId} className="hover:bg-muted/5 transition-colors group">
                  <td className="p-6">
                    <div>
                      <div className="font-black text-primary-deep text-base">{hotelName}</div>
                      <div className="text-[10px] text-muted-foreground font-black tracking-widest uppercase">ID: {bookingId}</div>
                    </div>
                  </td>
                  <td className="p-6">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-secondary text-primary flex items-center justify-center text-xs font-black shadow-sm group-hover:rotate-12 transition-transform">
                        {guestName[0] || "G"}
                      </div>
                      <div className="font-bold text-sm text-primary-deep">{guestName}</div>
                    </div>
                  </td>
                  <td className="p-6">
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-2 text-xs font-bold text-muted-foreground">
                        <Calendar size={12} className="text-primary" />
                        {getBookingCheckIn(b)} - {getBookingCheckOut(b)}
                      </div>
                    </div>
                  </td>
                  <td className="p-6">
                    <div className="font-black text-primary-deep text-base">${getBookingPrice(b).toLocaleString()}</div>
                  </td>
                  <td className="p-6">
                    <span className={`
                      inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest
                      ${status === "CONFIRMED" || status === "PAID" ? "bg-success/15 text-success" : status === "CANCELLED" ? "bg-destructive/15 text-destructive" : "bg-primary/15 text-primary"}
                    `}>
                      <span className="w-1.5 h-1.5 rounded-full bg-current" />
                      {status}
                    </span>
                  </td>
                  <td className="p-6 text-right">
                    <button
                      type="button"
                      onClick={() => setSelectedBooking(b)}
                      className="p-2 text-muted-foreground hover:bg-muted/20 rounded-xl transition-all"
                    >
                      <ChevronRight size={20} />
                    </button>
                  </td>
                </tr>
                );
              })}
            </tbody>
          </table>
          
          {filteredBookings.length === 0 && (
            <div className="p-20 text-center flex flex-col items-center">
              <div className="w-16 h-16 bg-muted/20 rounded-full flex items-center justify-center text-muted-foreground mb-4">
                 <Search size={32} />
              </div>
              <h3 className="text-xl font-black text-primary-deep mb-1">No bookings found</h3>
              <p className="text-muted-foreground font-bold text-sm">We couldn't find any reservations matching your criteria.</p>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminBookingsPage;
