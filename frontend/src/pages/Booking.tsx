import { useEffect, useMemo, useState } from "react";
import { Link, useParams, Navigate, useNavigate } from "react-router-dom";
import { format, differenceInCalendarDays } from "date-fns";
import type { DateRange } from "react-day-picker";
import { ArrowLeft, CalendarIcon, Minus, Plus } from "lucide-react";
import { z } from "zod";
import AppShell from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { toast } from "@/hooks/use-toast";
import { cn } from "@/lib/utils";
import { Penguin } from "@/components/Penguin";
import { useAuth } from "@/context/AuthContext";
import { getHotelById, getRoomsByHotel } from "@/api/hotelApi";
import { createBooking } from "@/api/bookingApi";

const bookingSchema = z
  .object({
    fullName:     z.string().trim().min(2, "Please enter your full name").max(80),
    email:        z.string().trim().email("Please enter a valid email").max(255),
    checkIn:      z.date({ required_error: "Pick a check-in date" }),
    checkOut:     z.date({ required_error: "Pick a check-out date" }),
    guests:       z.number().int().min(1, "At least 1 guest").max(10, "Max 10 guests"),
    selectedRoom: z.string().min(1, "Please select a room"),
  })
  .refine(d => d.checkOut > d.checkIn, { path: ["checkOut"], message: "Check-out must be after check-in" });

const normalizeHotel = (raw: any) => ({
  id:       raw.id,
  name:     raw.name || "Hotel",
  location: raw.location || raw.address || [raw.city, raw.region || raw.country].filter(Boolean).join(", ") || "",
  city:     raw.city || raw.location || "",
  price:    Number(raw.price ?? raw.minPrice ?? raw.pricePerNight ?? raw.lowestRoomPrice ?? 0),
  image:    raw.coverImageUrl || raw.imageUrl || raw.mainImageUrl || "",
});

const normalizeRoom = (raw: any) => ({
  id:       String(raw.id),
  name:     raw.name || raw.roomType || raw.type || "Standard Room",
  price:    Number(raw.price ?? raw.pricePerNight ?? raw.ratePerNight ?? raw.basePrice ?? raw.costPerNight ?? raw.nightlyRate ?? raw.rate ?? raw.pricing?.basePrice ?? raw.pricing?.pricePerNight ?? 0),
  capacity: raw.capacity ?? raw.maxGuests ?? raw.maxCapacity ?? 2,
});

const Booking = () => {
  const { id }     = useParams<{ id: string }>();
  const navigate   = useNavigate();
  const { user }   = useAuth() as any;

  const [hotel, setHotel]               = useState<ReturnType<typeof normalizeHotel> | null>(null);
  const [rooms, setRooms]               = useState<ReturnType<typeof normalizeRoom>[]>([]);
  const [hotelLoading, setHotelLoading] = useState(true);
  const [hotelError, setHotelError]     = useState("");

  const [fullName,      setFullName]      = useState("");
  const [email,         setEmail]         = useState("");
  const [range,         setRange]         = useState<DateRange | undefined>();
  const [guests,        setGuests]        = useState(2);
  const [selectedRoom,  setSelectedRoom]  = useState("");
  const [errors,        setErrors]        = useState<Record<string, string>>({});
  const [submitting,    setSubmitting]    = useState(false);

  useEffect(() => {
    if (!id) return;
    setHotelLoading(true);
    Promise.all([getHotelById(id), getRoomsByHotel(id)])
      .then(([hotelRes, roomsRes]: any[]) => {
        const h = normalizeHotel(hotelRes.data);
        setHotel(h);
        const rawRooms = Array.isArray(roomsRes.data)
          ? roomsRes.data
          : Array.isArray(roomsRes.data?.content)
          ? roomsRes.data.content
          : Array.isArray(roomsRes.data?.rooms)
          ? roomsRes.data.rooms
          : [];
        const mapped = rawRooms.map(normalizeRoom);
        setRooms(mapped);
        if (mapped.length > 0) setSelectedRoom(mapped[0].id);
      })
      .catch(() => setHotelError("Could not load hotel details."))
      .finally(() => setHotelLoading(false));
  }, [id]);

  useEffect(() => {
    if (user) {
      setFullName(`${user.firstName ?? ""} ${user.lastName ?? ""}`.trim());
      setEmail(user.email ?? "");
    }
  }, [user]);

  const nights = useMemo(() => {
    if (!range?.from || !range?.to) return 0;
    return Math.max(0, differenceInCalendarDays(range.to, range.from));
  }, [range]);

  const activeRoom  = rooms.find(r => r.id === selectedRoom);
  const price       = activeRoom?.price ?? hotel?.price ?? 0;
  const subtotal    = Math.round(nights * price * 100) / 100;
  const fees        = nights > 0 ? Math.round(subtotal * 0.1 * 100) / 100 : 0;
  const total       = Math.round((subtotal + fees) * 100) / 100;
  const fmt         = (n: number) => n.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const result = bookingSchema.safeParse({ fullName, email, checkIn: range?.from, checkOut: range?.to, guests, selectedRoom });
    if (!result.success) {
      const fieldErrors: Record<string, string> = {};
      for (const issue of result.error.issues) fieldErrors[issue.path[0] as string] = issue.message;
      setErrors(fieldErrors);
      return;
    }
    if (!hotel) return;
    setErrors({});
    setSubmitting(true);
    try {
      const res = await createBooking({
        roomId:         Number(selectedRoom),
        checkInDate:    format(range!.from!, "yyyy-MM-dd"),
        checkOutDate:   format(range!.to!, "yyyy-MM-dd"),
        numberOfGuests: guests,
      });
      const bookingId    = res.data?.id ?? res.data?.bookingId;
      const backendTotal = res.data?.totalPrice ?? res.data?.total ?? total;

      navigate(`/user/bookings/${bookingId}/pay`, {
        state: {
          hotel:    { name: hotel.name, city: hotel.city },
          room:     { name: activeRoom?.name ?? "Standard Room" },
          nights,
          checkIn:  format(range!.from!, "yyyy-MM-dd"),
          checkOut: format(range!.to!, "yyyy-MM-dd"),
          total:    backendTotal,
          bookingId,
        },
      });
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? err?.message ?? "Booking failed. Please try again.";
      toast({ title: "Booking failed", description: msg, variant: "destructive" });
    } finally {
      setSubmitting(false);
    }
  };

  if (hotelLoading) {
    return (
      <AppShell title="Confirm your booking" eyebrow="Almost there" subtitle="Loading the stay details for your booking.">
        <div className="py-16 flex flex-col items-center gap-4 text-center">
          <Penguin mood="thinking" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">Loading hotel...</p>
        </div>
      </AppShell>
    );
  }

  if (hotelError || !hotel) {
    return <Navigate to="/user/hotels" replace />;
  }

  return (
    <AppShell title="Confirm your booking" eyebrow="Almost there" subtitle="Review your dates, room, and guest details before payment.">
      <div className="py-8">
        <Button asChild variant="ghost" className="rounded-full font-bold -ml-2 mb-6">
          <Link to={`/user/hotel/${hotel.id}`}><ArrowLeft className="mr-2 h-4 w-4" /> Back</Link>
        </Button>

        <div className="grid lg:grid-cols-[2fr_1fr] gap-8">
          <form onSubmit={handleSubmit} className="space-y-5">
            <h1 className="text-3xl font-bold">Confirm your booking</h1>

            {/* Guest details */}
            <div className="bg-card rounded-3xl shadow-soft p-6 space-y-5">
              <h2 className="text-xl font-bold">Your details</h2>
              <div className="grid sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fullName" className="font-bold">Full name</Label>
                  <Input id="fullName" value={fullName} onChange={e => setFullName(e.target.value)}
                    maxLength={80} placeholder="Jane Doe" className="h-12 rounded-2xl" />
                  {errors.fullName && <p className="text-xs text-destructive">{errors.fullName}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email" className="font-bold">Email</Label>
                  <Input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)}
                    maxLength={255} placeholder="jane@example.com" className="h-12 rounded-2xl" />
                  {errors.email && <p className="text-xs text-destructive">{errors.email}</p>}
                </div>
              </div>
            </div>

            {/* Room selection */}
            {rooms.length > 0 && (
              <div className="bg-card rounded-3xl shadow-soft p-6 space-y-4">
                <h2 className="text-xl font-bold">Select a room</h2>
                <div className="grid gap-3">
                  {rooms.map(room => (
                    <label
                      key={room.id}
                      className={cn(
                        "flex items-center gap-4 p-4 rounded-2xl border-2 cursor-pointer transition-colors",
                        selectedRoom === room.id
                          ? "border-primary bg-primary/5"
                          : "border-border hover:border-primary/40"
                      )}
                    >
                      <input
                        type="radio"
                        name="room"
                        value={room.id}
                        checked={selectedRoom === room.id}
                        onChange={() => setSelectedRoom(room.id)}
                        className="accent-primary"
                      />
                      <div className="flex-1">
                        <p className="font-bold">{room.name}</p>
                        <p className="text-sm text-muted-foreground">Up to {room.capacity} guests</p>
                      </div>
                      <p className="font-bold text-accent">${room.price}<span className="text-xs font-normal text-muted-foreground">/night</span></p>
                    </label>
                  ))}
                </div>
                {errors.selectedRoom && <p className="text-xs text-destructive">{errors.selectedRoom}</p>}
              </div>
            )}

            {/* Trip dates & guests */}
            <div className="bg-card rounded-3xl shadow-soft p-6 space-y-5">
              <h2 className="text-xl font-bold">Trip details</h2>
              <div className="space-y-2">
                <Label className="font-bold">Dates</Label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button type="button" variant="outline" className={cn("w-full justify-start text-left font-normal h-12 rounded-2xl border-input shadow-none", !range?.from && "text-muted-foreground")}>
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {range?.from ? (
                        range.to ? (
                          <>{format(range.from, "d MMM yyyy")} → {format(range.to, "d MMM yyyy")}</>
                        ) : format(range.from, "d MMM yyyy")
                      ) : "Select check-in & check-out"}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0 overflow-hidden" align="start">
                    <Calendar
                      mode="range"
                      selected={range}
                      onSelect={setRange}
                      disabled={d => d < new Date(new Date().setHours(0, 0, 0, 0))}
                      numberOfMonths={2}
                      initialFocus
                      className="pointer-events-auto pb-3"
                    />
                  </PopoverContent>
                </Popover>
                {(errors.checkIn || errors.checkOut) && (
                  <p className="text-xs text-destructive">{errors.checkIn || errors.checkOut}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label className="font-bold">Guests</Label>
                <div className="flex items-center gap-3">
                  <Button type="button" size="icon" variant="outline" className="rounded-full border-input shadow-none"
                    onClick={() => setGuests(g => Math.max(1, g - 1))}>
                    <Minus className="h-4 w-4" />
                  </Button>
                  <span className="font-bold w-10 text-center">{guests}</span>
                  <Button type="button" size="icon" variant="outline" className="rounded-full border-input shadow-none"
                    onClick={() => setGuests(g => Math.min(10, g + 1))}>
                    <Plus className="h-4 w-4" />
                  </Button>
                  <span className="text-sm text-muted-foreground">{guests === 1 ? "guest" : "guests"}</span>
                </div>
                {errors.guests && <p className="text-xs text-destructive">{errors.guests}</p>}
              </div>
            </div>

            <Button type="submit" size="lg" disabled={submitting}
              className="w-full rounded-full bg-primary hover:opacity-95 font-bold h-14 text-base">
              {submitting ? "Creating booking…" : "Confirm & proceed to payment"}
            </Button>
          </form>

          {/* Summary sidebar */}
          <aside className="lg:sticky lg:top-24 h-fit bg-card rounded-3xl shadow-soft p-6 space-y-4">
            {hotel.image && (
              <img src={hotel.image} alt={hotel.name} className="w-full aspect-[4/3] object-cover rounded-2xl" />
            )}
            <div>
              <h3 className="font-extrabold text-lg">{hotel.name}</h3>
              <p className="text-sm text-muted-foreground">{hotel.location}</p>
              {activeRoom && <p className="text-sm font-bold text-primary mt-1">{activeRoom.name}</p>}
              <p className="text-sm font-bold text-accent mt-1">${fmt(price)} / night</p>
            </div>
            <hr className="border-border" />
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">${fmt(price)} × {nights} night{nights === 1 ? "" : "s"}</span>
                <span className="font-bold">${fmt(subtotal)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Service fee (10%)</span>
                <span className="font-bold">${fmt(fees)}</span>
              </div>
            </div>
            <hr className="border-border" />
            <div className="flex justify-between items-center text-lg">
              <span className="font-bold">Total</span>
              <span className="font-bold text-accent">${fmt(total)}</span>
            </div>
          </aside>
        </div>
      </div>
    </AppShell>
  );
};

export default Booking;
