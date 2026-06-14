import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Hotel, Plus, Clock, CheckCircle2, Trash2, Building2, ClipboardList } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import {
  getMyHotels,
  getManagerBookings,
  deleteHotel,
  confirmBooking,
} from "../../api/managerApi";
import LoadingSpinner from "../../components/shared/LoadingSpinner";
import ErrorMessage from "../../components/shared/ErrorMessage";
import EmptyState from "../../components/shared/EmptyState";
import BookingStatusBadge from "../../components/shared/BookingStatusBadge";


import { Button } from "@/components/ui/button";

const ManagerDashboard = () => {
  const { user } = useAuth();

  const [hotels, setHotels] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // For delete confirmation
  const [deletingId, setDeletingId] = useState(null);
  const [confirmingId, setConfirmingId] = useState(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [hotelsRes, bookingsRes] = await Promise.all([
        getMyHotels(),
        getManagerBookings({ size: 5, sortBy: "bookingDate", sortDir: "desc" }),
      ]);
      setHotels(hotelsRes.data || []);
      const bData = bookingsRes.data;
      setBookings(bData?.content || bData?.items || (Array.isArray(bData) ? bData : []));
    } catch (err) {
      setError(err.response?.data?.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteHotel = async (id) => {
    try {
      await deleteHotel(id);
      setHotels((prev) => prev.filter((h) => h.id !== id));
      setDeletingId(null);
    } catch (err) {
      alert(err.response?.data?.message || "Failed to delete hotel.");
    }
  };

  const handleConfirmBooking = async (id) => {
    try {
      await confirmBooking(id);
      setBookings((prev) =>
        prev.map((b) => (b.id === id ? { ...b, status: "CONFIRMED" } : b))
      );
      setConfirmingId(null);
    } catch (err) {
      alert(err.response?.data?.message || "Failed to confirm booking.");
    }
  };

  if (loading) return <LoadingSpinner message="Loading dashboard..." />;
  if (error) return <ErrorMessage message={error} />;

  // Stats
  const totalHotels = hotels.length;
  const pendingCount = bookings.filter((b) => b.status === "PENDING").length;
  const confirmedCount = bookings.filter((b) => b.status === "CONFIRMED").length;

  return (
    <>
      {/* Welcome section */}
      <div className="mb-8">
        <h1 className="text-3xl md:text-4xl font-extrabold text-primary-deep mb-2">
          Welcome back{user?.firstName ? `, ${user.firstName}` : ""}!
        </h1>
        <p className="text-muted-foreground text-lg">Here's your manager dashboard overview</p>
      </div>

      {/* Top actions */}
      <div className="flex items-center justify-end mb-6">
        <Button asChild className="h-11 rounded-2xl font-bold">
          <Link to="/manager/rooms">
            <Plus className="h-4 w-4 mr-2" />
            Add Room
          </Link>
        </Button>
      </div>

      {/* Stats */}
      <div className="grid md:grid-cols-3 gap-5">
        <div className="bg-card rounded-3xl shadow-soft p-6 flex items-center gap-4">
          <div className="h-11 w-11 rounded-2xl bg-secondary text-primary flex items-center justify-center">
            <Hotel className="h-5 w-5" />
          </div>
          <div>
            <p className="text-2xl font-extrabold leading-none">{totalHotels}</p>
            <p className="text-sm text-muted-foreground font-semibold mt-1">My Hotels</p>
          </div>
        </div>

        <div className="bg-card rounded-3xl shadow-soft p-6 flex items-center gap-4">
          <div className="h-11 w-11 rounded-2xl bg-secondary text-primary flex items-center justify-center">
            <Clock className="h-5 w-5" />
          </div>
          <div>
            <p className="text-2xl font-extrabold leading-none">{pendingCount}</p>
            <p className="text-sm text-muted-foreground font-semibold mt-1">Pending Bookings</p>
          </div>
        </div>

        <div className="bg-card rounded-3xl shadow-soft p-6 flex items-center gap-4">
          <div className="h-11 w-11 rounded-2xl bg-secondary text-primary flex items-center justify-center">
            <CheckCircle2 className="h-5 w-5" />
          </div>
          <div>
            <p className="text-2xl font-extrabold leading-none">{confirmedCount}</p>
            <p className="text-sm text-muted-foreground font-semibold mt-1">Confirmed</p>
          </div>
        </div>
      </div>

      {/* My Hotels */}
      <section className="mt-6 bg-card rounded-3xl shadow-soft p-6">
        <div className="flex items-center justify-between gap-4 flex-wrap mb-4">
          <h2 className="font-extrabold text-lg">My Hotels</h2>
          <Button asChild variant="outline" className="rounded-full">
            <Link to="/manager/rooms">Manage Rooms →</Link>
          </Button>
        </div>

        {hotels.length === 0 ? (
          <EmptyState icon={<Building2 className="h-8 w-8 text-muted-foreground" />} title="No hotels yet" message="You haven't added any hotels." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="text-xs uppercase text-muted-foreground">
                <tr className="border-b border-border">
                  <th className="py-3 text-left font-bold">Hotel</th>
                  <th className="py-3 text-left font-bold">City</th>
                  <th className="py-3 text-left font-bold">Stars</th>
                  <th className="py-3 text-left font-bold">Status</th>
                  <th className="py-3 text-right font-bold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {hotels.map((hotel) => (
                  <tr key={hotel.id} className="hover:bg-muted/30">
                    <td className="py-3">
                      <div className="flex items-center gap-3 min-w-[220px]">
                        {hotel.coverImageUrl ? (
                          <img
                            src={hotel.coverImageUrl}
                            alt={hotel.name}
                            className="h-10 w-14 rounded-xl object-cover border border-border"
                          />
                        ) : (
                          <div className="h-10 w-14 rounded-xl bg-muted border border-border" />
                        )}
                        <span className="font-bold">{hotel.name}</span>
                      </div>
                    </td>
                    <td className="py-3 text-muted-foreground">
                      {[hotel.city, hotel.region].filter(Boolean).join(", ")}
                    </td>
                    <td className="py-3">{hotel.starRating ? "★".repeat(hotel.starRating) : "—"}</td>
                    <td className="py-3">
                      <span
                        className={[
                          "inline-flex items-center px-3 py-1 rounded-full text-xs font-bold",
                          hotel.status === "ACTIVE"
                            ? "bg-secondary text-primary"
                            : "bg-muted text-muted-foreground",
                        ].join(" ")}
                      >
                        {hotel.status || "DRAFT"}
                      </span>
                    </td>
                    <td className="py-3">
                      <div className="flex items-center justify-end gap-2">
                        <Button asChild variant="outline" size="sm" className="rounded-full">
                          <Link to={`/manager/rooms?hotelId=${hotel.id}`}>Rooms</Link>
                        </Button>

                        {deletingId === hotel.id ? (
                          <>
                            <Button
                              size="sm"
                              variant="destructive"
                              className="rounded-full"
                              onClick={() => handleDeleteHotel(hotel.id)}
                            >
                              Yes, delete
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="rounded-full"
                              onClick={() => setDeletingId(null)}
                            >
                              Cancel
                            </Button>
                          </>
                        ) : (
                          <Button
                            size="sm"
                            variant="destructive"
                            className="rounded-full"
                            onClick={() => setDeletingId(hotel.id)}
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            Delete
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Recent Bookings */}
      <section className="mt-6 bg-card rounded-3xl shadow-soft p-6">
        <div className="flex items-center justify-between gap-4 flex-wrap mb-4">
          <h2 className="font-extrabold text-lg">Recent Bookings</h2>
        </div>

        {bookings.length === 0 ? (
          <EmptyState icon={<ClipboardList className="h-8 w-8 text-muted-foreground" />} title="No bookings yet" />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="text-xs uppercase text-muted-foreground">
                <tr className="border-b border-border">
                  <th className="py-3 text-left font-bold">ID</th>
                  <th className="py-3 text-left font-bold">Hotel</th>
                  <th className="py-3 text-left font-bold">Room</th>
                  <th className="py-3 text-left font-bold">Check-in</th>
                  <th className="py-3 text-left font-bold">Check-out</th>
                  <th className="py-3 text-left font-bold">Guests</th>
                  <th className="py-3 text-left font-bold">Status</th>
                  <th className="py-3 text-right font-bold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {bookings.map((b) => (
                  <tr key={b.id} className="hover:bg-muted/30">
                    <td className="py-3 font-bold">#{b.id}</td>
                    <td className="py-3">{b.hotelName || "—"}</td>
                    <td className="py-3 text-muted-foreground">{b.roomName || `Room #${b.roomId}`}</td>
                    <td className="py-3 text-muted-foreground">{b.checkInDate || "—"}</td>
                    <td className="py-3 text-muted-foreground">{b.checkOutDate || "—"}</td>
                    <td className="py-3">{b.numberOfGuests}</td>
                    <td className="py-3">
                      <BookingStatusBadge status={b.status} />
                    </td>
                    <td className="py-3">
                      <div className="flex items-center justify-end">
                        {b.status === "PENDING" && (
                          confirmingId === b.id ? (
                            <div className="flex items-center gap-2">
                              <Button
                                size="sm"
                                className="rounded-full"
                                onClick={() => handleConfirmBooking(b.id)}
                              >
                                Confirm
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                className="rounded-full"
                                onClick={() => setConfirmingId(null)}
                              >
                                Cancel
                              </Button>
                            </div>
                          ) : (
                            <Button
                              size="sm"
                              className="rounded-full"
                              onClick={() => setConfirmingId(b.id)}
                            >
                              Confirm
                            </Button>
                          )
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
};

export default ManagerDashboard;