import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { CalendarDays, MapPin, TicketCheck } from "lucide-react";
import AppShell from "../../components/AppShell";
import { Button } from "../../components/ui/button";
import { Penguin } from "../../components/Penguin";
import BookingStatusBadge from "../../components/shared/BookingStatusBadge";
import { getMyReservations, cancelReservation } from "../../api/reservationApi";

const asList = (data) =>
  Array.isArray(data) ? data : data?.content || data?.items || data?.reservations || [];

const normalizeReservation = (raw) => ({
  id: String(raw.id ?? raw.reservationId ?? ""),
  type: raw.type ?? raw.reservationType ?? (raw.event ? "EVENT" : "ROOM"),
  status: raw.status ?? "PENDING",
  title: raw.hotel?.name ?? raw.event?.name ?? raw.room?.name ?? raw.title ?? "Reservation",
  subtitle: raw.room?.name ?? raw.event?.hotel?.name ?? raw.hotelName ?? "",
  location: raw.hotel?.city ?? raw.hotel?.location ?? raw.event?.location ?? raw.location ?? "",
  startDate: raw.checkInDate ?? raw.startDate ?? raw.date ?? "",
  endDate: raw.checkOutDate ?? raw.endDate ?? "",
  total: raw.totalPrice ?? raw.total ?? raw.price,
});

const formatDate = (value) => {
  if (!value) return "Not set";
  try {
    return new Intl.DateTimeFormat("en", { month: "short", day: "numeric", year: "numeric" }).format(new Date(value));
  } catch {
    return value;
  }
};

const ReservationsPage = () => {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [cancellingId, setCancellingId] = useState("");

  useEffect(() => {
    getMyReservations()
      .then((res) => setReservations(asList(res.data).map(normalizeReservation)))
      .catch((err) => {
        console.error("Failed to load user reservations:", err);
        setError("Could not load your reservations.");
      })
      .finally(() => setLoading(false));
  }, []);

  const cancel = async (id) => {
    setCancellingId(id);
    try {
      await cancelReservation(id);
      setReservations((items) =>
        items.map((item) => item.id === id ? { ...item, status: "CANCELLED" } : item),
      );
    } catch (err) {
      console.error("Failed to cancel reservation:", err);
      setError("Could not cancel that reservation.");
    } finally {
      setCancellingId("");
    }
  };

  return (
    <AppShell
      title="My Reservations"
      eyebrow="Reserved extras"
      subtitle="Room and event reservations owned by your user account."
    >
      {error && (
        <div className="mb-5 rounded-2xl bg-destructive/10 px-4 py-3 text-sm font-bold text-destructive">
          {error}
        </div>
      )}

      {loading ? (
        <div className="flex flex-col items-center gap-4 py-16">
          <Penguin mood="thinking" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">Loading your reservations...</p>
        </div>
      ) : reservations.length === 0 ? (
        <div className="bg-card rounded-3xl shadow-soft p-12 text-center flex flex-col items-center gap-4">
          <Penguin mood="curious" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">No reservations yet.</p>
          <Button asChild className="rounded-full bg-primary font-bold">
            <Link to="/user/hotels">Find a stay</Link>
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          {reservations.map((reservation) => (
            <article key={reservation.id} className="bg-card rounded-3xl shadow-soft p-5">
              <div className="flex items-start justify-between gap-4 flex-wrap">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-black uppercase tracking-widest text-primary bg-secondary px-2.5 py-1 rounded-full">
                      {reservation.type}
                    </span>
                    <BookingStatusBadge status={reservation.status} />
                  </div>
                  <h2 className="font-extrabold text-lg text-primary-deep">{reservation.title}</h2>
                  {reservation.subtitle && <p className="text-sm text-muted-foreground">{reservation.subtitle}</p>}
                  {reservation.location && (
                    <p className="text-sm text-muted-foreground flex items-center gap-1 mt-1">
                      <MapPin className="h-3.5 w-3.5" />
                      {reservation.location}
                    </p>
                  )}
                </div>
                {reservation.total !== undefined && (
                  <p className="font-black text-accent">${Number(reservation.total).toLocaleString()}</p>
                )}
              </div>

              <div className="grid sm:grid-cols-3 gap-3 mt-4 text-sm">
                <div>
                  <p className="text-xs font-bold text-muted-foreground">Start</p>
                  <p className="font-bold flex items-center gap-1">
                    <CalendarDays className="h-3.5 w-3.5" />
                    {formatDate(reservation.startDate)}
                  </p>
                </div>
                <div>
                  <p className="text-xs font-bold text-muted-foreground">End</p>
                  <p className="font-bold">{formatDate(reservation.endDate)}</p>
                </div>
                <div>
                  <p className="text-xs font-bold text-muted-foreground">Reservation ID</p>
                  <p className="font-bold">{reservation.id}</p>
                </div>
              </div>

              {reservation.status !== "CANCELLED" && (
                <div className="flex justify-end mt-4">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => cancel(reservation.id)}
                    disabled={cancellingId === reservation.id}
                    className="rounded-full font-bold text-destructive border-destructive/40 hover:bg-destructive/5"
                  >
                    {cancellingId === reservation.id ? "Cancelling..." : "Cancel reservation"}
                  </Button>
                </div>
              )}
            </article>
          ))}
        </div>
      )}
    </AppShell>
  );
};

export default ReservationsPage;
