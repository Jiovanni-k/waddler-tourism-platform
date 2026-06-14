import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Calendar as CalIcon, Coffee, MapPin, Users, Waves, Wifi } from "lucide-react";
import { format } from "date-fns";
import AppShell from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Penguin } from "@/components/Penguin";
import { getHotels } from "@/api/hotelApi";
import { cn } from "@/lib/utils";
import fallbackHotelImg from "@/assets/hotel-1.jpeg";

interface HotelResult {
  id: string | number;
  name: string;
  location: string;
  images: string[];
  tag: string;
}

const asList = (data: any): any[] =>
  Array.isArray(data) ? data : data?.content || data?.items || data?.hotels || [];

const normalizeHotel = (raw: any): HotelResult => {
  const city = raw.city || raw.town || "";
  const region = raw.region || raw.country || "";
  const location = raw.location || raw.address || [city, region].filter(Boolean).join(", ") || "Location unavailable";
  const images = [
    raw.coverImageUrl,
    raw.imageUrl,
    raw.mainImageUrl,
    ...(Array.isArray(raw.images) ? raw.images : []),
    ...(Array.isArray(raw.imageUrls) ? raw.imageUrls : []),
  ].filter(Boolean);

  return {
    id: raw.id,
    name: raw.name || "Unnamed hotel",
    location,
    images: images.length > 0 ? images.map(String) : [fallbackHotelImg],
    tag: raw.status === "ACTIVE" ? "Ready to book" : raw.status || "Hotel",
  };
};

const UserAvailabilityPage = () => {
  const [checkIn, setCheckIn] = useState<Date | undefined>();
  const [checkOut, setCheckOut] = useState<Date | undefined>();
  const [guests, setGuests] = useState(2);
  const [searched, setSearched] = useState(false);
  const [hotels, setHotels] = useState<HotelResult[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getHotels()
      .then((res: any) => setHotels(asList(res.data).map(normalizeHotel)))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const bookingDraft = checkIn && checkOut
    ? {
        checkIn: format(checkIn, "yyyy-MM-dd"),
        checkOut: format(checkOut, "yyyy-MM-dd"),
        guests,
      }
    : undefined;

  return (
    <AppShell
      title="Member Availability"
      eyebrow="Find rooms"
      subtitle="Check dates from your user space and continue straight into booking."
    >
      <div className="bg-card rounded-3xl shadow-soft p-6 mb-8 grid md:grid-cols-4 gap-4 items-end">
        <div className="space-y-2">
          <label className="text-xs font-bold text-muted-foreground flex items-center gap-1">
            <CalIcon className="h-3 w-3" /> Check-in
          </label>
          <Popover>
            <PopoverTrigger asChild>
              <Button
                type="button"
                variant="outline"
                className={cn("w-full justify-start text-left font-normal h-12 rounded-2xl border-input shadow-none", !checkIn && "text-muted-foreground")}
              >
                <CalIcon className="mr-2 h-4 w-4" />
                {checkIn ? format(checkIn, "PPP") : "Pick a date"}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0 overflow-hidden" align="start">
              <Calendar
                mode="single"
                selected={checkIn}
                onSelect={setCheckIn}
                disabled={d => d < new Date(new Date().setHours(0, 0, 0, 0))}
                initialFocus
                className="pointer-events-auto pb-3"
              />
            </PopoverContent>
          </Popover>
        </div>

        <div className="space-y-2">
          <label className="text-xs font-bold text-muted-foreground flex items-center gap-1">
            <CalIcon className="h-3 w-3" /> Check-out
          </label>
          <Popover>
            <PopoverTrigger asChild>
              <Button
                type="button"
                variant="outline"
                className={cn("w-full justify-start text-left font-normal h-12 rounded-2xl border-input shadow-none", !checkOut && "text-muted-foreground")}
              >
                <CalIcon className="mr-2 h-4 w-4" />
                {checkOut ? format(checkOut, "PPP") : "Pick a date"}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0 overflow-hidden" align="start">
              <Calendar
                mode="single"
                selected={checkOut}
                onSelect={setCheckOut}
                disabled={d => d < (checkIn ?? new Date(new Date().setHours(0, 0, 0, 0)))}
                initialFocus
                className="pointer-events-auto pb-3"
              />
            </PopoverContent>
          </Popover>
        </div>

        <div className="space-y-2">
          <label className="text-xs font-bold text-muted-foreground flex items-center gap-1">
            <Users className="h-3 w-3" /> Guests
          </label>
          <Input
            type="number"
            min={1}
            max={10}
            value={guests}
            onChange={e => setGuests(+e.target.value)}
            className="h-12 rounded-2xl"
          />
        </div>

        <Button
          onClick={() => setSearched(true)}
          disabled={loading}
          className="h-12 rounded-2xl bg-primary text-primary-foreground hover:opacity-90 font-bold"
        >
          Check availability
        </Button>
      </div>

      {!searched ? (
        <div className="bg-card rounded-3xl p-10 text-center flex flex-col items-center gap-4 shadow-soft">
          <Penguin mood="planning" className="h-40 w-40" />
          <p className="text-muted-foreground max-w-md">
            Pick your dates and your saved booking flow stays inside the user area.
          </p>
        </div>
      ) : loading ? (
        <div className="text-center py-12 text-muted-foreground font-semibold">Loading hotels...</div>
      ) : hotels.length === 0 ? (
        <div className="bg-card rounded-3xl p-10 text-center flex flex-col items-center gap-4 shadow-soft">
          <Penguin mood="sad" className="h-40 w-40" />
          <p className="text-muted-foreground font-semibold">No hotels found.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {hotels.map(h => (
            <article key={h.id} className="bg-card rounded-3xl shadow-soft p-5 flex flex-col sm:flex-row gap-5">
              <img src={h.images[0]} alt={h.name} className="w-full sm:w-56 h-44 object-cover rounded-2xl" />
              <div className="flex-1 space-y-2">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-extrabold text-lg">{h.name}</h3>
                    <p className="text-sm text-muted-foreground flex items-center gap-1">
                      <MapPin className="h-3.5 w-3.5" /> {h.location}
                    </p>
                  </div>
                  <span className="bg-secondary text-primary text-xs font-bold px-3 py-1 rounded-full shrink-0">{h.tag}</span>
                </div>
                <div className="flex flex-wrap gap-3 text-xs text-muted-foreground pt-1">
                  <span className="flex items-center gap-1"><Wifi className="h-3 w-3" /> Free Wi-Fi</span>
                  <span className="flex items-center gap-1"><Coffee className="h-3 w-3" /> Breakfast</span>
                  <span className="flex items-center gap-1"><Waves className="h-3 w-3" /> Pool</span>
                </div>
                <div className="flex items-end justify-end pt-2">
                  <Button asChild className="rounded-full bg-primary hover:opacity-95 font-bold">
                    <Link to={`/user/book/${h.id}`} state={{ bookingDraft }}>
                      Book from user account
                    </Link>
                  </Button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </AppShell>
  );
};

export default UserAvailabilityPage;
