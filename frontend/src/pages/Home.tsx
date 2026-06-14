import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Building2, Hotel, MapPin, Palmtree, Search, Sparkles, Star } from "lucide-react";
import { Home as HomeIcon } from "lucide-react";
import { Layout } from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { hotels, categories, type HotelCategory } from "@/data/hotels";
import { Penguin } from "@/components/Penguin";

const categoryIcons: Record<HotelCategory, typeof Hotel> = {
  Hotels: Hotel,
  Resorts: Palmtree,
  Apartments: HomeIcon,
  Villas: Building2,
};

const categoryDesc: Record<HotelCategory, string> = {
  Hotels: "Find great hotels",
  Resorts: "Relax & unwind",
  Apartments: "Feel at home",
  Villas: "Luxury stays",
};

const Home = () => {
  const [query, setQuery] = useState("");
  const [active, setActive] = useState<HotelCategory | "All">("All");

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return hotels.filter(h => {
      if (active !== "All" && h.category !== active) return false;
      if (!q) return true;
      return h.name.toLowerCase().includes(q) || h.location.toLowerCase().includes(q);
    });
  }, [query, active]);

  return (
    <Layout>
      {/* Hero */}
      <section className="container pt-10 pb-6">
        <div className="rounded-3xl bg-gradient-hero p-8 md:p-12 grid md:grid-cols-[1fr_auto] gap-6 items-center overflow-hidden shadow-soft">
          <div className="space-y-4">
            <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-primary bg-card px-3 py-1.5 rounded-full">
              <Sparkles className="h-3 w-3" /> Welcome to Waddler
            </span>
            <h1 className="text-3xl md:text-5xl font-bold text-primary-deep leading-tight">
              Your next stay,<br />made happy.
            </h1>
            <p className="text-muted-foreground max-w-lg">
              Find and book amazing hotels, resorts and villas around the world.
            </p>
            <div className="flex items-center gap-2 bg-card rounded-full p-2 shadow-soft max-w-lg">
              <Search className="h-5 w-5 text-muted-foreground ml-3 shrink-0" />
              <Input
                value={query}
                onChange={e => setQuery(e.target.value)}
                placeholder="Search by name or location..."
                className="border-0 focus-visible:ring-0 bg-transparent flex-1"
              />
            </div>
          </div>
          <Penguin mood="happy" className="w-36 md:w-52 drop-shadow-xl hidden md:block" />
        </div>
      </section>

      <div className="container pb-12 space-y-8">
        {/* Categories */}
        <section>
          <h2 className="text-xl font-extrabold mb-4">Browse by type</h2>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
            <button
              onClick={() => setActive("All")}
              className={`rounded-2xl p-4 flex items-center gap-3 text-left transition-all ${
                active === "All"
                  ? "bg-primary text-primary-foreground shadow-soft"
                  : "bg-card shadow-soft hover:shadow-glow"
              }`}
            >
              <div className={`h-10 w-10 rounded-xl flex items-center justify-center shrink-0 ${active === "All" ? "bg-white/20" : "bg-secondary text-primary"}`}>
                <Search className="h-5 w-5" />
              </div>
              <div>
                <p className="font-bold text-sm">All</p>
                <p className={`text-xs ${active === "All" ? "text-white/70" : "text-muted-foreground"}`}>Everything</p>
              </div>
            </button>
            {categories.map(cat => {
              const Icon = categoryIcons[cat];
              const isActive = active === cat;
              return (
                <button
                  key={cat}
                  onClick={() => setActive(cat)}
                  className={`rounded-2xl p-4 flex items-center gap-3 text-left transition-all ${
                    isActive
                      ? "bg-primary text-primary-foreground shadow-soft"
                      : "bg-card shadow-soft hover:shadow-glow"
                  }`}
                >
                  <div className={`h-10 w-10 rounded-xl flex items-center justify-center shrink-0 ${isActive ? "bg-white/20" : "bg-secondary text-primary"}`}>
                    <Icon className="h-5 w-5" />
                  </div>
                  <div>
                    <p className="font-bold text-sm">{cat}</p>
                    <p className={`text-xs ${isActive ? "text-white/70" : "text-muted-foreground"}`}>{categoryDesc[cat]}</p>
                  </div>
                </button>
              );
            })}
          </div>
        </section>

        {/* Results */}
        <section>
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-xl font-extrabold">{filtered.length} {filtered.length === 1 ? "stay" : "stays"} found</h2>
            <Button asChild variant="ghost" className="text-primary font-bold rounded-full gap-1">
              <Link to="/guest/hotels">View all <ArrowRight className="h-4 w-4" /></Link>
            </Button>
          </div>
          {filtered.length === 0 ? (
            <div className="bg-card rounded-3xl shadow-soft p-10 text-center">
              <p className="text-muted-foreground">No stays match your search. Try a different name or location.</p>
            </div>
          ) : (
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
              {filtered.slice(0, 6).map(h => (
                <Link
                  key={h.id}
                  to={`/guest/hotel/${h.id}`}
                  className="bg-card rounded-3xl overflow-hidden shadow-soft hover:-translate-y-1 hover:shadow-glow transition-all duration-300 group"
                >
                  <div className="relative aspect-[4/3] overflow-hidden">
                    <img
                      src={h.images[0]}
                      alt={h.name}
                      loading="lazy"
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    <span className="absolute top-3 left-3 bg-card/95 text-primary text-xs font-bold px-3 py-1 rounded-full">
                      {h.tag}
                    </span>
                  </div>
                  <div className="p-5 space-y-1.5">
                    <h3 className="font-extrabold text-base">{h.name}</h3>
                    <p className="text-sm text-muted-foreground flex items-center gap-1">
                      <MapPin className="h-3.5 w-3.5 shrink-0" /> {h.location}
                    </p>
                    <div className="flex items-center justify-between pt-1.5">
                      <div className="flex items-center gap-0.5">
                        {Array.from({ length: 5 }).map((_, i) => (
                          <Star key={i} className={`h-3.5 w-3.5 ${i < Math.round(h.rating) ? "fill-accent text-accent" : "text-muted-foreground/25"}`} />
                        ))}
                        <span className="text-muted-foreground text-xs ml-1">({h.reviews})</span>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </section>
      </div>
    </Layout>
  );
};

export default Home;
