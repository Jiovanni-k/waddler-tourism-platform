import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  Award, Bell, ChevronLeft, ChevronRight, Heart, Hotel, Mail, MapPin,
  Search, SlidersHorizontal, Sparkles, Star,
  TicketCheck, User,
  LogOut,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { getHotels } from "@/api/hotelApi";
import { Penguin } from "@/components/Penguin";
import logo from "@/assets/logo.png";
import hotelImg from "@/assets/penguins/penguin-hotel.png";
import { useAuth } from "@/context/AuthContext";

const FilterCheckbox = ({ id, checked, onChange, label }: { id: string; checked: boolean; onChange: () => void; label: React.ReactNode }) => (
  <label htmlFor={id} className="flex items-center gap-2 cursor-pointer select-none">
    <input
      type="checkbox"
      id={id}
      checked={checked}
      onChange={onChange}
      className="sr-only peer"
    />
    <span className="h-4 w-4 shrink-0 rounded-[4px] border border-input peer-checked:bg-primary peer-checked:border-primary flex items-center justify-center transition-colors">
      {checked && <svg className="h-3 w-3 text-white" viewBox="0 0 12 12" fill="none"><path d="M2 6l3 3 5-5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/></svg>}
    </span>
    <span className="text-sm">{label}</span>
  </label>
);

const AMENITY_OPTIONS = ["Free Wi-Fi", "Pool", "Breakfast", "Spa"];
const PER_PAGE = 6;

type PaginationItem = number | "start-ellipsis" | "end-ellipsis";

type HotelCard = {
  id: string | number;
  name: string;
  location: string;
  city: string;
  tag: string;
  stars: number;
  rating: number;
  reviews: number;
  price: number;
  amenities: string[];
  images: string[];
};

function toggle<T>(set: Set<T>, val: T): Set<T> {
  const next = new Set(set);
  if (next.has(val)) next.delete(val); else next.add(val);
  return next;
}

const getPaginationItems = (currentPage: number, totalPages: number): PaginationItem[] => {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  const items: PaginationItem[] = [1, 2];
  const middleStart = Math.max(3, currentPage - 1);
  const middleEnd = Math.min(totalPages - 2, currentPage + 1);

  if (middleStart > 3) items.push("start-ellipsis");

  for (let pageNumber = middleStart; pageNumber <= middleEnd; pageNumber += 1) {
    items.push(pageNumber);
  }

  if (middleEnd < totalPages - 2) items.push("end-ellipsis");

  items.push(totalPages - 1, totalPages);

  return items;
};

const ratingLabel = (r: number) =>
  r === 0 ? "" : r === 1 ? "Terrible" : r === 2 ? "Poor" : r === 3 ? "Good" : r === 4 ? "Great" : "Excellent";

const asList = (data: any): any[] =>
  Array.isArray(data) ? data : data?.content || data?.items || data?.hotels || [];

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

const normalizeHotel = (hotel: any): HotelCard => {
  const city = hotel.city || hotel.town || "";
  const region = hotel.region || hotel.country || "";
  const location = hotel.location || hotel.address || [city, region].filter(Boolean).join(", ") || "Location unavailable";
  const price = Number(hotel.price ?? hotel.minPrice ?? hotel.basePrice ?? hotel.pricePerNight ?? hotel.lowestRoomPrice ?? 0);

  return {
    id: hotel.id,
    name: hotel.name || "Unnamed hotel",
    location,
    city,
    tag: hotel.status === "ACTIVE" ? "Available" : hotel.status || "Hotel",
    stars: Number(hotel.starRating ?? hotel.star_rating ?? hotel.stars ?? hotel.numberOfStars ?? 0),
    rating: Number(hotel.averageGuestRating ?? hotel.average_guest_rating ?? hotel.averageRating ?? hotel.rating ?? 0),
    reviews: Number(hotel.reviewCount ?? hotel.reviewsCount ?? hotel.reviews ?? 0),
    price,
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
                <Link key={to} to={to} className={`flex items-center gap-2 px-3 2xl:px-4 py-2 rounded-full font-semibold whitespace-nowrap transition-colors ${
                  to === "/user/hotels"
                    ? "bg-secondary text-primary font-bold"
                    : "text-muted-foreground hover:text-primary hover:bg-secondary/60"
                }`}>
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

      <div className="md:hidden border-t border-border overflow-x-auto">
        <div className="container flex gap-1 py-2">
          <Link to={isUser ? "/user/hotels" : "/guest/hotels"} className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap bg-secondary text-primary">
            <Hotel className="h-3 w-3" />
            Hotels
          </Link>
          {isUser ? (
            <>
              {userLinks.slice(1).map(({ to, label, icon: Icon }) => (
                <Link key={to} to={to} className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap text-muted-foreground">
                  <Icon className="h-3 w-3" />
                  {label}
                </Link>
              ))}
            </>
          ) : (
            <>
              <Link to="/guest/availability" className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap text-muted-foreground">
                <Search className="h-3 w-3" />
                Availability
              </Link>
              <Link to="/guest/contact" className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap text-muted-foreground">
                <Mail className="h-3 w-3" />
                Support
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

const HotelsPage = ({ audience = "guest" }: { audience?: Audience }) => {
  const [hotels, setHotels] = useState<HotelCard[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [q, setQ] = useState("");
  const [selectedCities, setSelectedCities] = useState<Set<string>>(new Set());
  const [selectedStars, setSelectedStars] = useState<Set<number>>(new Set());
  const [selectedAmenities, setSelectedAmenities] = useState<Set<string>>(new Set());
  const [maxPrice, setMaxPrice] = useState(500);
  const [sortBy, setSortBy] = useState("recommended");
  const [page, setPage] = useState(1);
  const [cityFilter, setCityFilter] = useState("");
  const [likedHotels, setLikedHotels] = useState<Set<number | string>>(new Set());

  useEffect(() => {
  let active = true;

  const loadHotels = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await getHotels();
      if (active) setHotels(asList(res.data).map(normalizeHotel));
    } catch (err: any) {
      if (active) setError(err?.response?.data?.message || "Failed to load hotels from the backend.");
    } finally {
      if (active) setLoading(false);
    }
  };

  loadHotels();
  return () => { active = false; };
}, []);

  const cities = useMemo(() => {
  const all = Array.from(new Set(hotels.map((h) => h.city).filter(Boolean))).sort();
  if (!cityFilter.trim()) return all;
  const q = cityFilter.toLowerCase();
  return all.filter((c) => c.toLowerCase().includes(q));
}, [hotels, cityFilter]);

  const filtered = useMemo(() => {
    let list = hotels.filter(h => {
      if (selectedCities.size > 0 && !selectedCities.has(h.city)) return false;
      if (selectedStars.size > 0 && !selectedStars.has(h.stars)) return false;
      if (h.price > maxPrice && maxPrice < 500) return false;
      if (selectedAmenities.size > 0) {
        const normalize = (s: string) => s.toLowerCase().replace(/[-_\s]/g, "");
        const amenityKeywords: Record<string, string[]> = {
          "freewifi": ["wifi", "wi-fi", "internet", "freewifi", "wireless"],
          "pool":     ["pool", "swimming"],
          "breakfast":["breakfast", "brunch", "morningmeal"],
          "spa":      ["spa", "wellness", "massage"],
        };
        for (const a of selectedAmenities) {
          const key = normalize(a);
          const keywords = amenityKeywords[key] ?? [key];
          const hasIt = h.amenities.some(am => keywords.some(k => normalize(am).includes(k)));
          if (!hasIt) return false;
        }
      }
      if (q && !(h.name + h.location).toLowerCase().includes(q.toLowerCase())) return false;
      return true;
    });
    if (sortBy === "price-asc") list = [...list].sort((a, b) => a.price - b.price);
    else if (sortBy === "price-desc") list = [...list].sort((a, b) => b.price - a.price);
    else if (sortBy === "rating") list = [...list].sort((a, b) => b.rating - a.rating);
    return list;
  }, [hotels, q, selectedCities, selectedStars, selectedAmenities, maxPrice, sortBy]);

  const pages = Math.max(1, Math.ceil(filtered.length / PER_PAGE));
  const safePage = Math.min(page, pages);
  const view = filtered.slice((safePage - 1) * PER_PAGE, safePage * PER_PAGE);
  const paginationItems = useMemo(() => getPaginationItems(safePage, pages), [safePage, pages]);
  const showingFrom = filtered.length === 0 ? 0 : (safePage - 1) * PER_PAGE + 1;
  const showingTo = Math.min(safePage * PER_PAGE, filtered.length);

  const handleSearch = () => { setQ(searchInput); setPage(1); };
  const clearFilters = () => {
    setSelectedCities(new Set()); setSelectedStars(new Set());
    setSelectedAmenities(new Set()); setMaxPrice(500); setQ(""); setSearchInput(""); setPage(1);
  };

  return (
    <HotelShell audience={audience}>
      {/* Hero */}
      <section className="container pt-10 pb-6">
        <div className="rounded-3xl bg-gradient-hero p-8 md:p-10 grid md:grid-cols-[1fr_auto] gap-6 items-center overflow-hidden shadow-soft">
          <div className="space-y-4">
            <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-primary bg-card px-3 py-1.5 rounded-full">
              <Sparkles className="h-3 w-3" /> Browse Hotels
            </span>
            <h1 className="text-3xl md:text-5xl font-bold text-primary-deep leading-tight">
              Find your happy stay
            </h1>
            <p className="text-muted-foreground max-w-lg">
              Discover handpicked hotels, resorts and villas across the world's most beautiful destinations.
            </p>
            <div className="flex items-center gap-2 bg-card rounded-full p-2 shadow-soft max-w-lg">
              <Search className="h-5 w-5 text-muted-foreground ml-3 shrink-0" />
              <Input
                value={searchInput}
                onChange={e => setSearchInput(e.target.value)}
                onKeyDown={e => e.key === "Enter" && handleSearch()}
                placeholder="Search hotels, cities..."
                className="border-0 focus-visible:ring-0 bg-transparent flex-1"
              />
              <Button
                onClick={handleSearch}
                className="rounded-full bg-primary text-primary-foreground font-bold px-5 hover:opacity-90 shrink-0"
              >
                Search
              </Button>
            </div>
          </div>
          <img
            src={hotelImg}
            alt=""
            className="w-56 md:w-80 drop-shadow-xl justify-self-end hidden md:block"
            style={{ animation: "float 4s ease-in-out infinite" }}
          />
        </div>
      </section>

      {/* Main */}
      <div className="container pb-12 grid lg:grid-cols-[260px_1fr] gap-6 items-start">
        {/* Sidebar filters */}
        <aside className="bg-card rounded-3xl p-6 space-y-6 shadow-soft lg:sticky lg:top-24">
          <div className="flex items-center gap-2">
            <SlidersHorizontal className="h-4 w-4 text-primary" />
            <h2 className="font-extrabold text-base">Filters</h2>
          </div>

          {/* Price */}
          <div className="space-y-3">
            <div className="flex justify-between text-sm font-bold">
              <span>Price / night</span>
              <span className="text-primary">{maxPrice >= 500 ? "$500+" : `$${maxPrice}`}</span>
            </div>
            <input
              type="range"
              min={50}
              max={500}
              step={10}
              value={maxPrice}
              onChange={e => { setMaxPrice(Number(e.target.value)); setPage(1); }}
              className="w-full cursor-pointer"
              style={{ accentColor: "hsl(var(--primary))" }}
            />
            <div className="flex justify-between text-xs text-muted-foreground">
              <span>$0</span><span>$500+</span>
            </div>
          </div>

          {/* City */}
          {/* City */}
<div className="space-y-2.5">
  <h3 className="text-sm font-bold">City</h3>

  <Input
    value={cityFilter}
    onChange={(e) => setCityFilter(e.target.value)}
    placeholder="Search city..."
    className="h-10 rounded-2xl"
  />

  <div className="max-h-64 overflow-auto pr-1 space-y-2">
    {cities.length === 0 && (
      <p className="text-sm text-muted-foreground">No matches</p>
    )}

    {cities.map((c) => (
      <FilterCheckbox
        key={c}
        id={`city-${c}`}
        checked={selectedCities.has(c)}
        onChange={() => { setSelectedCities(toggle(selectedCities, c)); setPage(1); }}
        label={c}
      />
    ))}
  </div>
</div>

          {/* Stars */}
          <div className="space-y-2.5">
            <h3 className="text-sm font-bold">Star rating</h3>
            {[3, 4, 5].map(s => (
              <FilterCheckbox
                key={s}
                id={`star-${s}`}
                checked={selectedStars.has(s)}
                onChange={() => { setSelectedStars(toggle(selectedStars, s)); setPage(1); }}
                label={
                  <span className="inline-flex items-center gap-1 bg-secondary px-2 py-0.5 rounded-full font-medium text-muted-foreground">
                    <Star className="h-3 w-3 fill-accent text-accent" />
                    {s}-star
                  </span>
                }
              />
            ))}
          </div>

          {/* Amenities */}
          <div className="space-y-2.5">
            <h3 className="text-sm font-bold">Amenities</h3>
            {AMENITY_OPTIONS.map(a => (
              <FilterCheckbox
                key={a}
                id={`am-${a}`}
                checked={selectedAmenities.has(a)}
                onChange={() => { setSelectedAmenities(toggle(selectedAmenities, a)); setPage(1); }}
                label={a}
              />
            ))}
          </div>
        </aside>

        {/* Right: grid + pagination */}
        <div className="space-y-5">
          {/* Sort bar */}
          <div className="flex items-center justify-between flex-wrap gap-3">
            <p className="text-sm text-muted-foreground">
              Showing{" "}
              <span className="font-bold text-foreground">{showingFrom}-{showingTo}</span> of{" "}
              <span className="font-bold text-foreground">{filtered.length}</span> hotels
            </p>
            <Select value={sortBy} onValueChange={v => { setSortBy(v); setPage(1); }}>
              <SelectTrigger className="w-48">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="recommended">Recommended</SelectItem>
                <SelectItem value="price-asc">Price: Low to High</SelectItem>
                <SelectItem value="price-desc">Price: High to Low</SelectItem>
                <SelectItem value="rating">Top Rated</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {loading ? (
            <div className="bg-card rounded-3xl p-12 text-center flex flex-col items-center gap-4 shadow-soft">
              <Penguin mood="thinking" className="h-32 w-32" />
              <p className="text-muted-foreground font-semibold">Loading hotels from the backend...</p>
            </div>
          ) : error ? (
            <div className="bg-card rounded-3xl p-12 text-center flex flex-col items-center gap-4 shadow-soft">
              <Penguin mood="sad" className="h-32 w-32" />
              <p className="text-destructive font-semibold">{error}</p>
              <Button variant="outline" className="rounded-full" onClick={() => window.location.reload()}>
                Try again
              </Button>
            </div>
          ) : view.length === 0 ? (
            <div className="bg-card rounded-3xl p-12 text-center flex flex-col items-center gap-4 shadow-soft">
              <Penguin mood="sad" className="h-32 w-32" />
              <p className="text-muted-foreground font-semibold">No hotels match your filters.</p>
              <Button variant="outline" className="rounded-full" onClick={clearFilters}>
                Clear filters
              </Button>
            </div>
          ) : (
            <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-5">
              {view.map(h => (
                <Link
                  key={h.id}
                  to={audience === "user" ? `/user/hotel/${h.id}` : `/guest/hotel/${h.id}`}
                  className="bg-card rounded-3xl overflow-hidden shadow-soft hover:-translate-y-1 hover:shadow-glow transition-all duration-300 group"
                >
                  <div className="relative aspect-[4/3] overflow-hidden">
                    <img
                      src={h.images[0]}
                      alt={h.name}
                      loading="lazy"
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    <span className="absolute top-3 left-3 bg-card/95 text-primary text-xs font-bold px-3 py-1 rounded-full backdrop-blur-sm">
                      {h.tag}
                    </span>
                    <button
                      onClick={e => { e.preventDefault(); setLikedHotels(prev => { const next = new Set(prev); next.has(h.id) ? next.delete(h.id) : next.add(h.id); return next; }); }}
                      className="absolute top-3 right-3 h-9 w-9 rounded-full bg-card/95 flex items-center justify-center backdrop-blur-sm hover:bg-card transition-colors"
                      aria-label="Save"
                    >
                      <Heart className={`h-4 w-4 transition-colors ${likedHotels.has(h.id) ? "fill-primary text-primary" : "text-primary"}`} />
                    </button>
                  </div>
                  <div className="p-5 space-y-1.5">
                    <h3 className="font-extrabold text-base leading-snug">{h.name}</h3>
                    <p className="text-sm text-muted-foreground flex items-center gap-1">
                      <MapPin className="h-3.5 w-3.5 shrink-0" /> {h.location}
                    </p>
                    <p className="text-sm font-black text-accent">
                      ${h.price.toLocaleString()} <span className="text-xs font-bold text-muted-foreground">/ night</span>
                    </p>
                    <div className="flex items-center pt-1.5 gap-2">
                      {h.rating > 0 ? (
                        <span className="inline-flex items-center gap-1 text-xs font-medium text-muted-foreground bg-secondary px-2 py-0.5 rounded-full">
                          <Star className="h-3 w-3 fill-accent text-accent" />
                          {h.rating.toFixed(1)}{h.reviews > 0 && ` (${h.reviews})`}
                        </span>
                      ) : h.stars > 0 ? (
                        <span className="inline-flex items-center gap-1 text-xs font-medium text-muted-foreground bg-secondary px-2 py-0.5 rounded-full">
                          <Star className="h-3 w-3 fill-accent text-accent" />
                          {h.stars}-star hotel
                        </span>
                      ) : null}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}

          {/* Pagination */}
          {pages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-2 flex-wrap">
              <button
                type="button"
                onClick={() => setPage(p => Math.max(1, p - 1))}
                disabled={safePage === 1}
                aria-label="Previous page"
                className="h-10 w-10 rounded-full bg-card shadow-soft flex items-center justify-center disabled:opacity-40 hover:bg-secondary transition-colors"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              {paginationItems.map((item) => {
                if (typeof item !== "number") {
                  return (
                    <span
                      key={item}
                      className="h-10 min-w-8 px-1 inline-flex items-center justify-center text-sm font-bold text-muted-foreground"
                      aria-hidden="true"
                    >
                      ...
                    </span>
                  );
                }

                return (
                  <button
                    type="button"
                    key={item}
                    onClick={() => setPage(item)}
                    aria-label={`Go to page ${item}`}
                    aria-current={safePage === item ? "page" : undefined}
                    className={`h-10 w-10 rounded-full font-bold text-sm transition-colors ${
                      safePage === item
                        ? "bg-primary text-primary-foreground"
                        : "bg-card shadow-soft hover:bg-secondary"
                    }`}
                  >
                    {item}
                  </button>
                );
              })}
              <button
                type="button"
                onClick={() => setPage(p => Math.min(pages, p + 1))}
                disabled={safePage === pages}
                aria-label="Next page"
                className="h-10 w-10 rounded-full bg-card shadow-soft flex items-center justify-center disabled:opacity-40 hover:bg-secondary transition-colors"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          )}
        </div>
      </div>
    </HotelShell>
  );
};

export default HotelsPage;
