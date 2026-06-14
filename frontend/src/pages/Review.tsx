import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { MapPin, Sparkles, Star } from "lucide-react";
import { Layout } from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { hotels } from "@/data/hotels";
import { Penguin } from "@/components/Penguin";
import { toast } from "sonner";
import { reviewsApi, isApiEnabled } from "@/lib/api";

const RATING_LABELS = ["", "Terrible", "Poor", "Good", "Great", "Excellent"];

function StarRow({
  label,
  value,
  onChange,
}: {
  label: string;
  value: number;
  onChange: (v: number) => void;
}) {
  const [hover, setHover] = useState(0);
  return (
    <div className="flex items-center justify-between bg-background rounded-2xl px-4 py-3 border border-border">
      <span className="text-sm font-semibold text-foreground">{label}</span>
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map(n => (
          <button
            key={n}
            type="button"
            onMouseEnter={() => setHover(n)}
            onMouseLeave={() => setHover(0)}
            onClick={() => onChange(n)}
            aria-label={`${n} star`}
          >
            <Star
              className={`h-5 w-5 transition-colors ${
                (hover || value) >= n ? "fill-accent text-accent" : "text-muted-foreground/30"
              }`}
            />
          </button>
        ))}
      </div>
    </div>
  );
}

const Review = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const hotel = hotels.find(h => h.id === id) ?? hotels[0];

  const [rating, setRating] = useState(0);
  const [hover, setHover] = useState(0);
  const [cleanliness, setCleanliness] = useState(4);
  const [location, setLocation] = useState(4);
  const [service, setService] = useState(4);
  const [value, setValue] = useState(4);
  const [title, setTitle] = useState("");
  const [text, setText] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (rating === 0) { toast.error("Please select a star rating."); return; }
    setSubmitting(true);
    try {
      if (isApiEnabled()) {
        await reviewsApi.create(hotel.id, { rating, text });
      }
      toast.success("Review submitted! Thanks ✨");
      navigate("/user/bookings");
    } catch (err: any) {
      toast.error(err?.message ?? "Could not submit review");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout>
      {/* Hero */}
      <section className="container pt-10 pb-6">
        <div className="rounded-3xl bg-gradient-hero p-8 md:p-10 grid md:grid-cols-[1fr_auto] gap-6 items-center overflow-hidden shadow-soft">
          <div className="space-y-3">
            <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-primary bg-card px-3 py-1.5 rounded-full">
              <Sparkles className="h-3 w-3" /> Share your stay
            </span>
            <h1 className="text-3xl md:text-5xl font-bold text-primary-deep leading-tight">Write a review</h1>
            <p className="text-muted-foreground max-w-xl">Help other waddlers find their next happy place.</p>
          </div>
          <div className="hidden md:block" style={{ animation: "float 4s ease-in-out infinite" }}>
            <Penguin mood="waving" className="w-36 md:w-44 drop-shadow-xl justify-self-end" />
          </div>
        </div>
      </section>

      {/* Body */}
      <div className="container pb-12 grid lg:grid-cols-[1fr_320px] gap-6 items-start">
        {/* Form */}
        <form onSubmit={handleSubmit} className="bg-card rounded-3xl p-6 sm:p-8 space-y-7 shadow-soft">
          {/* Overall rating */}
          <div className="space-y-3">
            <p className="text-xs font-bold uppercase tracking-wider text-muted-foreground">How was it?</p>
            <div className="flex items-center gap-3">
              {[1, 2, 3, 4, 5].map(n => (
                <button
                  key={n}
                  type="button"
                  onMouseEnter={() => setHover(n)}
                  onMouseLeave={() => setHover(0)}
                  onClick={() => setRating(n)}
                  aria-label={`${n} star`}
                >
                  <Star
                    className={`h-10 w-10 transition-colors ${
                      (hover || rating) >= n ? "fill-accent text-accent" : "text-muted-foreground/25"
                    }`}
                  />
                </button>
              ))}
              {(hover || rating) > 0 && (
                <span className="text-lg font-bold text-foreground ml-1">
                  {RATING_LABELS[hover || rating]}
                </span>
              )}
            </div>
          </div>

          {/* Sub-ratings */}
          <div className="grid sm:grid-cols-2 gap-3">
            <StarRow label="Cleanliness" value={cleanliness} onChange={setCleanliness} />
            <StarRow label="Location" value={location} onChange={setLocation} />
            <StarRow label="Service" value={service} onChange={setService} />
            <StarRow label="Value" value={value} onChange={setValue} />
          </div>

          {/* Title */}
          <div className="space-y-2">
            <p className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Title your review</p>
            <Input
              value={title}
              onChange={e => setTitle(e.target.value)}
              placeholder="A snowy paradise"
              className="h-12 rounded-2xl border-border"
            />
          </div>

          {/* Body */}
          <div className="space-y-2">
            <p className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Your review</p>
            <Textarea
              value={text}
              onChange={e => setText(e.target.value.slice(0, 1000))}
              placeholder="Share your experience with other travelers..."
              className="rounded-2xl min-h-36 border-border"
            />
            <p className="text-xs text-muted-foreground text-right">{text.length} / 1000</p>
          </div>

          <Button
            type="submit"
            disabled={rating === 0 || submitting}
            className="w-full h-12 rounded-2xl bg-primary text-primary-foreground font-bold hover:opacity-90"
          >
            {submitting ? "Submitting…" : "Submit review"}
          </Button>
        </form>

        {/* Sidebar: hotel card */}
        <aside className="bg-card rounded-3xl overflow-hidden shadow-soft lg:sticky lg:top-24">
          <img src={hotel.images[0]} alt={hotel.name} className="w-full aspect-[4/3] object-cover" />
          <div className="p-5 space-y-2">
            <p className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Reviewing</p>
            <h3 className="font-extrabold text-lg leading-snug">{hotel.name}</h3>
            <p className="text-sm text-muted-foreground flex items-center gap-1">
              <MapPin className="h-3.5 w-3.5 shrink-0" /> {hotel.location}
            </p>
            <div className="pt-2 border-t border-border space-y-1 text-sm text-muted-foreground">
              <p>Stay: 02 – 06 Jan 2026</p>
              <p>Deluxe Suite · 4 nights</p>
            </div>
          </div>
        </aside>
      </div>
    </Layout>
  );
};

export default Review;
