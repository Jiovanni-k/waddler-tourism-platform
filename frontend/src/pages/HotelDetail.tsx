import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Award, Bell, Check, Heart, Hotel, LogOut, Mail, MapPin, Search, Star, TicketCheck, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { getHotelById, getRoomsByHotel } from "@/api/hotelApi";
import { Penguin } from "@/components/Penguin";
import logo from "@/assets/logo.png";
import fallbackHotelImg from "@/assets/hotel-1.jpeg";
import { useAuth } from "@/context/AuthContext";

const parseAmenities = (value: any): string[] => {
  if (Array.isArray(value)) {
    return value
      .filter(Boolean)
      .map(a => typeof a === "object" ? (a.name ?? a.amenityName ?? a.label ?? String(a.id ?? "")) : String(a))
      .filter(Boolean);
  }
  if (typeof value === "string") return value.split(",").map(a => a.trim()).filter(Boolean);
  return [];
};

const parseImages = (hotel: any): string[] => {
  const images = [
    hotel.coverImageUrl,
    hotel.imageUrl,
    hotel.mainImageUrl,
    ...(Array.isArray(hotel.images) ? hotel.images : []),
    ...(Array.isArray(hotel.imageUrls) ? hotel.imageUrls : []),
  ].filter(Boolean);
  return images.length > 0 ? images.map(String) : [fallbackHotelImg];
};

const normalizeHotel = (hotel: any) => {
  const city = hotel.city || hotel.town || "";
  const region = hotel.region || hotel.country || "";

  return {
    id: hotel.id,
    name: hotel.name || "Unnamed hotel",
    location: hotel.location || hotel.address || [city, region].filter(Boolean).join(", ") || "Location unavailable",
    category: hotel.category || hotel.type || "Hotel",
    tag: hotel.status === "ACTIVE" ? "Available" : hotel.status || "Hotel",
    rating: Number(hotel.averageGuestRating ?? hotel.average_guest_rating ?? hotel.averageRating ?? hotel.rating ?? 0),
    reviews: Number(hotel.reviewCount ?? hotel.reviewsCount ?? hotel.reviews ?? 0),
    price: Number(hotel.price ?? hotel.minPrice ?? hotel.basePrice ?? hotel.pricePerNight ?? hotel.lowestRoomPrice ?? hotel.startingPrice ?? hotel.fromPrice ?? 0),
    stars: Number(hotel.starRating ?? hotel.star_rating ?? hotel.stars ?? hotel.numberOfStars ?? 0),
    description: hotel.description || "Details for this hotel will be available soon.",
    amenities: parseAmenities(hotel.amenities),
    images: parseImages(hotel),
  };
};

type Audience = "guest" | "user";

const HotelShell = ({ children, audience }: { children: React.ReactNode; audience: Audience }) => {
  const isUser = audience === "user";
  const { logout } = useAuth();
  const userLinks = [
    { to: "/user/hotels", label: "Hotels", icon: Hotel },
    { to: "/user/availability", label: "Availability", icon: Search },
    { to: "/user/bookings", label: "My Bookings", icon: Heart },
    { to: "/user/reservations", label: "Reservations", icon: TicketCheck },
    { to: "/user/loyalty", label: "Loyalty", icon: Award },
    { to: "/user/notifications", label: "Inbox", icon: Bell },
    { to: "/user/contact", label: "Support", icon: Mail },
  ];

  return (
  <div className="min-h-screen bg-muted/40">
    <header className="bg-background sticky top-0 z-40 shadow-soft">
      <div className="container flex items-center justify-between h-20">
        <Link to={isUser ? "/user/bookings" : "/"} className="flex items-center">
          <img src={logo} alt="Waddler" className="h-10 object-contain" />
        </Link>

        <nav className="hidden md:flex items-center gap-2 text-sm">
          {isUser ? (
            <>
              {userLinks.map(({ to, label, icon: Icon }) => (
                <Link key={to} to={to} className="flex items-center gap-2 px-3 2xl:px-4 py-2 rounded-full text-muted-foreground hover:text-primary hover:bg-secondary/60 font-semibold whitespace-nowrap transition-colors">
                  <Icon className="h-4 w-4" />
                  {label}
                </Link>
              ))}
            </>
          ) : (
            <>
              <Link to="/guest/hotels" className="flex items-center gap-2 px-4 py-2 rounded-full bg-secondary text-primary font-bold">
                <Hotel className="h-4 w-4" />
                Hotels
              </Link>
              <Link to="/guest/availability" className="flex items-center gap-2 px-4 py-2 rounded-full text-muted-foreground hover:text-primary hover:bg-secondary/60 font-semibold transition-colors">
                <Search className="h-4 w-4" />
                Availability
              </Link>
              <Link to="/guest/contact" className="flex items-center gap-2 px-4 py-2 rounded-full text-muted-foreground hover:text-primary hover:bg-secondary/60 font-semibold transition-colors">
                <Mail className="h-4 w-4" />
                Support
              </Link>
            </>
          )}
        </nav>

        <div className="flex items-center gap-3">
          {isUser ? (
            <>
              <Link
                to="/user/profile"
                aria-label="Profile"
                title="Profile"
                className="h-10 w-10 rounded-full bg-muted text-primary grid place-items-center hover:bg-secondary transition-colors"
              >
                <User className="h-4 w-4" />
              </Link>
              <button
                type="button"
                onClick={logout}
                aria-label="Sign out"
                title="Sign out"
                className="h-10 w-10 rounded-full bg-muted grid place-items-center text-muted-foreground hover:text-primary hover:bg-secondary transition-colors"
              >
                <LogOut className="h-4 w-4" />
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm font-bold text-primary hover:text-primary/80 px-4 py-2 transition-colors">
                Sign in
              </Link>
              <Link to="/register" className="h-10 px-5 rounded-full bg-primary text-primary-foreground text-sm font-bold inline-flex items-center hover:opacity-90 transition-opacity">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </header>

    <main className="pb-16">{children}</main>
  </div>
  );
};

const HotelDetail = ({ audience = "guest" }: { audience?: Audience }) => {
  const { id } = useParams<{ id: string }>();
  const [hotel, setHotel] = useState<ReturnType<typeof normalizeHotel> | null>(null);
  const [minPrice, setMinPrice] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    const loadHotel = async () => {
      if (!id) return;
      setLoading(true);
      setError("");
      try {
        const [hotelRes, roomsRes] = await Promise.all([
          getHotelById(id),
          getRoomsByHotel(id).catch(() => ({ data: [] })),
        ]);
        if (!active) return;

        const normalized = normalizeHotel(hotelRes.data);
        setHotel(normalized);

        // Get min room price if hotel has no direct price
        const rooms = Array.isArray(roomsRes.data)
          ? roomsRes.data
          : roomsRes.data?.content || roomsRes.data?.rooms || [];
        const prices = rooms.map((r: any) => Number(r.basePrice ?? r.pricePerNight ?? r.price ?? 0)).filter((p: number) => p > 0);
        const roomMin = prices.length > 0 ? Math.min(...prices) : null;
        setMinPrice(normalized.price > 0 ? normalized.price : roomMin);
      } catch (err: any) {
        if (active) setError(err?.response?.data?.message || "Failed to load this hotel from the backend.");
      } finally {
        if (active) setLoading(false);
      }
    };

    loadHotel();
    return () => { active = false; };
  }, [id]);

  if (loading) {
    return (
      <HotelShell audience={audience}>
        <div className="container py-16 flex flex-col items-center gap-4 text-center">
          <Penguin mood="thinking" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">Loading hotel...</p>
        </div>
      </HotelShell>
    );
  }

  if (error || !hotel) {
    return (
      <HotelShell audience={audience}>
        <div className="container py-16 flex flex-col items-center gap-4 text-center">
          <Penguin mood="sad" className="h-32 w-32" />
          <p className="text-destructive font-semibold">{error || "Hotel not found."}</p>
          <Button asChild variant="outline" className="rounded-full">
            <Link to={audience === "user" ? "/user/hotels" : "/guest/hotels"}>Back to hotels</Link>
          </Button>
        </div>
      </HotelShell>
    );
  }

  return (
    <HotelShell audience={audience}>
      <div className="container py-8 space-y-8">
        {/* Back link */}
        <Button asChild variant="ghost" className="rounded-full font-bold -ml-2">
          <Link to={audience === "user" ? "/user/hotels" : "/guest/hotels"}><ArrowLeft className="mr-2 h-4 w-4" /> Back to hotels</Link>
        </Button>

        {/* Gallery */}
        <section className="relative rounded-3xl overflow-hidden aspect-[16/9] w-full">
          <img src={hotel.images[0]} alt={hotel.name} className="w-full h-full object-cover" />
          <span className="absolute top-4 left-4 bg-card/95 text-primary text-xs font-bold px-3 py-1 rounded-full backdrop-blur-sm">
            {hotel.tag}
          </span>
        </section>

        {/* Info + booking sidebar */}
        <section className="grid lg:grid-cols-[2fr_1fr] gap-8">
          <div className="space-y-5">
            <div>
              <h1 className="text-3xl sm:text-4xl font-bold">{hotel.name}</h1>
              <div className="flex flex-wrap items-center gap-4 mt-3 text-sm">
                <span className="inline-flex items-center gap-1 text-muted-foreground">
                  <MapPin className="h-4 w-4" /> {hotel.location}
                </span>
                <span className="inline-flex items-center gap-1">
                  <Star className="h-4 w-4 fill-accent text-accent" />
                  <span className="font-bold">{hotel.rating}</span>
                  <span className="text-muted-foreground">({hotel.reviews} reviews)</span>
                </span>
                <span className="px-3 py-1 rounded-full bg-secondary text-secondary-foreground font-bold text-xs">
                  {hotel.category}
                </span>
              </div>
            </div>

            <div className="bg-card rounded-3xl shadow-soft p-6">
              <h2 className="text-xl font-bold mb-3">About this stay</h2>
              <p className="text-muted-foreground leading-relaxed">{hotel.description}</p>
            </div>

            <div className="bg-card rounded-3xl shadow-soft p-6">
              <h2 className="text-xl font-bold mb-4">What this place offers</h2>
              <ul className="grid sm:grid-cols-2 gap-3">
                {(hotel.amenities.length > 0 ? hotel.amenities : ["Free Wi-Fi"]).map(a => (
                  <li key={a} className="flex items-center gap-2 text-sm">
                    <span className="h-7 w-7 rounded-full bg-secondary text-primary flex items-center justify-center shrink-0">
                      <Check className="h-4 w-4" />
                    </span>
                    {a}
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Booking sidebar */}
          <aside className="lg:sticky lg:top-24 h-fit bg-card rounded-3xl shadow-soft p-6 space-y-4">
            <div className="flex items-baseline gap-1">
              {minPrice != null && minPrice > 0 ? (
                <>
                  <span className="text-xs text-muted-foreground">From</span>
                  <span className="text-3xl font-bold text-accent">${minPrice}</span>
                  <span className="text-muted-foreground">/ night</span>
                </>
              ) : (
                <span className="text-sm text-muted-foreground">Price on request</span>
              )}
            </div>
            <p className="text-sm text-muted-foreground">Free cancellation up to 48 hours before check-in.</p>
            {hotel.stars > 0 && (
              <div className="flex gap-0.5 mb-1">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Star key={i} className={`h-4 w-4 ${i < hotel.stars ? "fill-accent text-accent" : "text-muted-foreground/25"}`} />
                ))}
                <span className="text-sm text-muted-foreground ml-1">{hotel.stars}-star hotel</span>
              </div>
            )}
            {hotel.rating > 0 && (
              <p className="text-sm text-muted-foreground flex items-center gap-1">
                <Star className="h-3.5 w-3.5 fill-accent text-accent" />
                {hotel.rating.toFixed(1)} ({hotel.reviews} reviews)
              </p>
            )}
            <Button asChild size="lg" className="w-full rounded-full bg-primary hover:opacity-95 font-bold h-12">
              <Link to={audience === "user" ? `/user/book/${hotel.id}` : "/login"}>{audience === "user" ? "Book this stay" : "Sign in to book"}</Link>
            </Button>
            <p className="text-xs text-muted-foreground text-center">Guests can browse prices. Sign in or register to book.</p>
          </aside>
        </section>
      </div>
    </HotelShell>
  );
};

export default HotelDetail;
