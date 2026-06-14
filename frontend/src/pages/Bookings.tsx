import { useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Penguin } from "@/components/Penguin";
import { Link, useNavigate } from "react-router-dom";
import { MapPin } from "lucide-react";
import { getMyBookings, cancelBooking } from "@/api/bookingApi";
import { toast } from "@/hooks/use-toast";
import { format, differenceInCalendarDays } from "date-fns";

type Status = "Upcoming" | "Completed" | "Cancelled" | "Pending";

interface Booking {
  id: string;
  hotelId: string;
  hotelName: string;
  hotelCity: string;
  hotelImage: string;
  roomName: string;
  checkIn: string;
  checkOut: string;
  status: Status;
  totalPrice?: number;
}

const STATUS_MAP: Record<string, Status> = {
  UPCOMING:  "Upcoming",
  CONFIRMED: "Upcoming",
  PENDING:   "Pending",
  COMPLETED: "Completed",
  CANCELLED: "Cancelled",
};

const normalizeBooking = (raw: any): Booking => {
  const rawStatus = (raw.status ?? "").toUpperCase();
  return {
    id:         String(raw.id ?? raw.bookingId ?? ""),
    hotelId:    String(raw.hotel?.id ?? raw.hotelId ?? ""),
    hotelName:  raw.hotel?.name ?? raw.hotelName ?? "Hotel",
    hotelCity:  raw.hotel?.city ?? raw.hotel?.location ?? raw.hotelCity ?? "",
    hotelImage: raw.hotel?.coverImageUrl ?? raw.hotel?.imageUrl ?? raw.hotelImage ?? "",
    roomName:   raw.room?.name ?? raw.roomName ?? "Standard Room",
    checkIn:    raw.checkInDate ?? raw.checkIn ?? "",
    checkOut:   raw.checkOutDate ?? raw.checkOut ?? "",
    status:     STATUS_MAP[rawStatus] ?? "Upcoming",
    totalPrice: raw.totalPrice ?? raw.total ?? undefined,
  };
};

const formatDate = (d: string) => {
  if (!d) return "—";
  try { return format(new Date(d), "d MMM yyyy"); } catch { return d; }
};

const tabs: ("All" | Status)[] = ["All", "Upcoming", "Pending", "Completed", "Cancelled"];

const statusStyles: Record<Status | "Pending", string> = {
  Upcoming:  "bg-primary/15 text-primary",
  Pending:   "bg-amber-100 text-amber-700",
  Completed: "bg-success/15 text-success",
  Cancelled: "bg-destructive/15 text-destructive",
};

const Bookings = () => {
  const navigate = useNavigate();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading,  setLoading]  = useState(true);
  const [tab,      setTab]      = useState<"All" | Status>("All");

  useEffect(() => {
    getMyBookings()
      .then((res: any) => {
        const raw = Array.isArray(res.data) ? res.data
          : Array.isArray(res.data?.content) ? res.data.content : [];
        setBookings(raw.map(normalizeBooking));
      })
      .catch(() => toast({ title: "Could not load bookings", variant: "destructive" }))
      .finally(() => setLoading(false));
  }, []);

  const list = bookings.filter(b => tab === "All" || b.status === tab);

  const cancel = async (id: string) => {
    try {
      await cancelBooking(id);
      setBookings(b => b.map(x => x.id === id ? { ...x, status: "Cancelled" as Status } : x));
      toast({ title: "Booking cancelled" });
    } catch {
      toast({ title: "Could not cancel booking", variant: "destructive" });
    }
  };

  return (
    <AppShell title="My Bookings" subtitle="Trips, past and upcoming" eyebrow="Your journeys">
      <div className="flex gap-2 mb-6 overflow-x-auto pb-1">
        {tabs.map(t => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-5 h-10 rounded-full font-bold text-sm whitespace-nowrap transition-colors ${
              tab === t ? "bg-primary text-primary-foreground" : "bg-card shadow-soft text-muted-foreground hover:text-primary"
            }`}
          >{t}</button>
        ))}
      </div>

      {loading ? (
        <div className="flex flex-col items-center gap-4 py-16">
          <Penguin mood="thinking" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">Loading your bookings…</p>
        </div>
      ) : list.length === 0 ? (
        <div className="bg-card rounded-3xl shadow-soft p-12 text-center flex flex-col items-center gap-4">
          <Penguin mood="curious" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">No bookings here yet.</p>
          <Button asChild className="rounded-full bg-primary text-primary-foreground font-bold">
            <Link to="/user/hotels">Find a stay</Link>
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          {list.map(b => (
            <article key={b.id} className="bg-card rounded-3xl shadow-soft p-5 flex flex-col sm:flex-row gap-5">
              {b.hotelImage ? (
                <img src={b.hotelImage} alt={b.hotelName} className="w-full sm:w-44 h-36 object-cover rounded-2xl" />
              ) : (
                <div className="w-full sm:w-44 h-36 rounded-2xl bg-muted flex items-center justify-center text-muted-foreground text-sm font-bold">
                  No image
                </div>
              )}
              <div className="flex-1 space-y-2">
                <div className="flex items-start justify-between gap-3 flex-wrap">
                  <div>
                    <h3 className="font-extrabold text-lg">{b.hotelName}</h3>
                    {b.hotelCity && (
                      <p className="text-sm text-muted-foreground flex items-center gap-1">
                        <MapPin className="h-3.5 w-3.5" /> {b.hotelCity}
                      </p>
                    )}
                    <p className="text-xs text-muted-foreground mt-0.5">{b.roomName}</p>
                  </div>
                  <span className={`text-xs font-bold px-3 py-1 rounded-full ${statusStyles[b.status]}`}>{b.status}</span>
                </div>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-sm pt-2">
                  <div><p className="text-xs text-muted-foreground">Check-in</p><p className="font-bold">{formatDate(b.checkIn)}</p></div>
                  <div><p className="text-xs text-muted-foreground">Check-out</p><p className="font-bold">{formatDate(b.checkOut)}</p></div>
                  <div><p className="text-xs text-muted-foreground">Booking ID</p><p className="font-bold text-xs">{b.id}</p></div>
                  {b.totalPrice !== undefined && (
                    <div><p className="text-xs text-muted-foreground">Total paid</p><p className="font-bold">${b.totalPrice}</p></div>
                  )}
                </div>
                <div className="flex flex-wrap gap-2 pt-2">
                  {b.status === "Pending" && (
                    <Button
                      onClick={() => {
                        const nights = b.checkIn && b.checkOut
                          ? Math.max(0, differenceInCalendarDays(new Date(b.checkOut), new Date(b.checkIn)))
                          : 0;
                        navigate(`/user/bookings/${b.id}/pay`, {
                          state: {
                            hotel:    { name: b.hotelName, city: b.hotelCity },
                            room:     { name: b.roomName },
                            nights,
                            checkIn:  b.checkIn,
                            checkOut: b.checkOut,
                            total:    b.totalPrice ?? 0,
                            bookingId: b.id,
                          },
                        });
                      }}
                      className="rounded-full bg-primary font-bold"
                    >
                      Complete payment
                    </Button>
                  )}
                  {b.hotelId && (
                    <Button asChild variant="outline" className="rounded-full font-bold">
                      <Link to={`/user/hotel/${b.hotelId}`}>View hotel</Link>
                    </Button>
                  )}
                  {b.status === "Completed" && b.hotelId && (
                    <Button asChild className="rounded-full bg-primary font-bold">
                      <Link to={`/user/review/${b.hotelId}`}>Write a review</Link>
                    </Button>
                  )}
                  {(b.status === "Upcoming" || b.status === "Pending") && (
                    <Button
                      onClick={() => cancel(b.id)}
                      variant="outline"
                      className="rounded-full font-bold text-destructive border-destructive/40 hover:bg-destructive/5"
                    >
                      Cancel booking
                    </Button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </AppShell>
  );
};

export default Bookings;
