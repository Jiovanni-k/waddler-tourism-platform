import { useState, useEffect } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft, Lock, CheckCircle2,
  CreditCard, Banknote, Star, Scissors,
  BedDouble, CalendarCheck, Moon, Building2, MapPin, AlertTriangle,
} from "lucide-react";
import { payBooking, getMyLoyalty } from "../../api/paymentApi";
import { Layout } from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/hooks/use-toast";
import { cn } from "@/lib/utils";
import celebrationPenguin from "../../assets/penguins/Celebration.png";
import walkingPenguin    from "../../assets/penguins/Walking_Casual.png";
import moneyPenguin      from "../../assets/penguins/Action_Reward.png";

const METHODS = [
  { key: "CREDIT_CARD",    Icon: CreditCard, label: "Credit Card"    },
  { key: "CASH",           Icon: Banknote,   label: "Cash"           },
  { key: "LOYALTY_POINTS", Icon: Star,       label: "Loyalty Points" },
  { key: "SPLIT",          Icon: Scissors,   label: "Split Payment"  },
];

const STEPS = ["Select Room", "Guest Info", "Payment"];

const fmt = (n) =>
  Number(n).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const PaymentPage = () => {
  const { state }      = useLocation();
  const { bookingId: urlBookingId } = useParams();
  const navigate       = useNavigate();

  const hotel     = state?.hotel     || { name: "The Glacier Suites", city: "Reykjavik" };
  const room      = state?.room      || { name: "Deluxe Suite" };
  const nights    = state?.nights    || 3;
  const checkIn   = state?.checkIn   || "";
  const checkOut  = state?.checkOut  || "";
  const total     = state?.total     || 960;
  const bookingId = state?.bookingId ?? urlBookingId;

  const [method,  setMethod]  = useState("CREDIT_CARD");
  const [loyalty, setLoyalty] = useState(null);
  const [loading, setLoading] = useState(false);
  const [done,    setDone]    = useState(false);
  const [error,   setError]   = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const [cardNumber, setCardNumber] = useState("");
  const [cardName,   setCardName]   = useState("");
  const [expiry,     setExpiry]     = useState("");
  const [cvv,        setCvv]        = useState("");
  const [splitPoints, setSplitPoints] = useState(0);

  useEffect(() => {
    getMyLoyalty()
      .then((res) => {
        const d = res.data;
        setLoyalty({
          points: d.pointsBalance ?? d.currentPoints ?? d.current_points ?? d.balance ?? 0,
          tier:   d.tier ?? d.tierName ?? d.membershipTier ?? "—",
        });
      })
      .catch(() => setLoyalty(null));
  }, []);

  const pointsValue    = loyalty ? Math.floor((loyalty.points ?? 0) / 100) : 0;
  const splitDiscount  = Math.min(splitPoints / 100, total);
  const splitCardTotal = total - splitDiscount;

  const showError = (msg) => {
    setError(msg);
    toast({ title: "Check your details", description: msg, variant: "destructive" });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handlePay = async () => {
    // ── Validate ─────────────────────────────────────────────────────────────
    if (method === "CREDIT_CARD" || method === "SPLIT") {
      const digits = cardNumber.replace(/\s/g, "");
      if (digits.length < 16) return showError("Please enter a valid 16-digit card number.");
      if (!cardName.trim())   return showError("Please enter the cardholder name.");
      if (!/^\d{1,2}\/\d{2}$/.test(expiry)) return showError("Please enter a valid expiry date (MM/YY).");
      if (cvv.length < 3)    return showError("Please enter a valid 3-digit CVV.");
    }

    setLoading(true);
    setError(null);
    try {
      const payload = {
        bookingId:     Number(bookingId),
        amount:        parseFloat(Number(total).toFixed(2)),
        currency:      "USD",
        paymentMethod: method,
      };
      if (method === "SPLIT" && splitPoints > 0) {
        payload.splitBetweenUsers = String(splitPoints);
      }

      await payBooking(payload);
      setDone(true);
    } catch (err) {
      const msg = err?.response?.data?.message
        ?? err?.response?.data?.error
        ?? err?.message
        ?? "Payment failed. Please try again.";
      setError(msg);
      toast({ title: "Payment failed", description: msg, variant: "destructive" });
      window.scrollTo({ top: 0, behavior: "smooth" });
      setLoading(false);
    }
  };

  // ── Success screen ──────────────────────────────────────────────────────────
  if (done) return (
    <Layout>
      <div className="container py-20 flex flex-col items-center text-center gap-6">
        <img src={celebrationPenguin} alt="Success" className="h-32 w-auto" />
        <div>
          <h1 className="text-4xl font-extrabold text-primary mb-2">You're all set!</h1>
          <p className="text-muted-foreground font-semibold">
            Booking confirmed for <strong className="text-foreground">{hotel.name}</strong>.
          </p>
          <p className="text-sm text-muted-foreground mt-1">A confirmation email has been sent to you. See you soon!</p>
        </div>

        <div className="bg-card rounded-3xl shadow-soft p-6 w-full max-w-md text-left space-y-0">
          {[
            [Building2,     "Hotel",     hotel.name],
            [BedDouble,     "Room",      room.name],
            [CalendarCheck, "Check-in",  checkIn],
            [CalendarCheck, "Check-out", checkOut],
            [Moon,          "Nights",    `${nights} night${nights !== 1 ? "s" : ""}`],
            [CreditCard,    "Paid",      `$${fmt(total)}`],
          ].map(([Icon, label, value]) => (
            <div key={label} className="flex justify-between py-3 border-b border-border last:border-0 text-sm">
              <span className="font-bold text-muted-foreground flex items-center gap-1.5">
                <Icon className="h-3.5 w-3.5" />{label}
              </span>
              <span className="font-bold text-foreground">{value}</span>
            </div>
          ))}
        </div>

        <div className="flex gap-3">
          <Button variant="outline" className="rounded-full font-bold" onClick={() => navigate("/bookings")}>
            My Bookings
          </Button>
          <Button className="rounded-full font-bold bg-primary hover:opacity-95 gap-2" onClick={() => navigate("/hotels")}>
            Explore More
            <img src={walkingPenguin} alt="" className="h-5 w-auto" />
          </Button>
        </div>
      </div>
    </Layout>
  );

  // ── Main payment page ───────────────────────────────────────────────────────
  return (
    <Layout>
      <div className="container py-8">

        {/* Back */}
        <Button variant="ghost" className="rounded-full font-bold -ml-2 mb-6" onClick={() => navigate(-1)}>
          <ArrowLeft className="mr-2 h-4 w-4" /> Back
        </Button>

        {/* Progress steps */}
        <div className="flex items-center mb-8">
          {STEPS.map((step, i) => (
            <div key={step} className={cn("flex items-center", i < STEPS.length - 1 && "flex-1")}>
              <div className={cn(
                "w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold text-white shrink-0",
                i < 2 ? "bg-green-500" : "bg-primary"
              )}>
                {i < 2 ? <CheckCircle2 className="h-4 w-4" /> : "3"}
              </div>
              <span className={cn("ml-2 text-xs font-bold whitespace-nowrap", i === 2 ? "text-primary" : "text-green-500")}>
                {step}
              </span>
              {i < 2 && <div className="flex-1 h-0.5 bg-green-500 mx-3 rounded-full" />}
            </div>
          ))}
        </div>

        <div className="grid lg:grid-cols-[1fr_340px] gap-8 items-start">

          {/* ── Left: form ── */}
          <div className="space-y-5">
            <div>
              <h1 className="text-3xl font-extrabold">Payment</h1>
              <p className="text-muted-foreground mt-1">Choose how you'd like to pay for your stay.</p>
            </div>

            {error && (
              <div className="bg-destructive/10 border border-destructive/30 text-destructive rounded-2xl px-4 py-3 text-sm font-semibold">
                {error}
              </div>
            )}

            {/* Loyalty banner */}
            {loyalty && (
              <div className="bg-primary rounded-2xl px-5 py-4 flex items-center gap-4">
                <Star className="h-7 w-7 text-yellow-300 shrink-0" />
                <div>
                  <p className="font-bold text-white">{loyalty.points} Loyalty Points — worth ${fmt(pointsValue)}</p>
                  <p className="text-sm text-white/70 font-semibold">Tier: {loyalty.tier} · 100 pts = $1</p>
                </div>
              </div>
            )}

            {/* Method selector */}
            <div className="bg-card rounded-3xl shadow-soft p-6 space-y-4">
              <h2 className="text-xl font-bold">Payment Method</h2>
              <div className="grid grid-cols-2 gap-3">
                {METHODS.map((m) => (
                  <button
                    key={m.key}
                    onClick={() => setMethod(m.key)}
                    className={cn(
                      "flex flex-col items-center gap-2 p-4 rounded-2xl border-2 cursor-pointer transition-all font-bold text-sm",
                      method === m.key
                        ? "border-primary bg-primary/5 text-primary"
                        : "border-border hover:border-primary/40 text-muted-foreground"
                    )}
                  >
                    <m.Icon className="h-6 w-6" />
                    {m.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Credit card fields */}
            {(method === "CREDIT_CARD" || method === "SPLIT") && (
              <div className="bg-card rounded-3xl shadow-soft p-6 space-y-4">
                <h2 className="text-xl font-bold flex items-center gap-2"><CreditCard className="h-5 w-5" /> Card Details</h2>
                <div className="space-y-2">
                  <Label className="font-bold">Card Number</Label>
                  <Input placeholder="1234 5678 9012 3456" maxLength={19} className="h-12 rounded-2xl"
                    value={cardNumber}
                    onChange={(e) => setCardNumber(e.target.value.replace(/\D/g,"").replace(/(.{4})/g,"$1 ").trim())} />
                </div>
                <div className="space-y-2">
                  <Label className="font-bold">Cardholder Name</Label>
                  <Input placeholder="JANE DOE" className="h-12 rounded-2xl"
                    value={cardName} onChange={(e) => setCardName(e.target.value)} />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="font-bold">Expiry</Label>
                    <Input placeholder="MM/YY" maxLength={5} className="h-12 rounded-2xl"
                      value={expiry} onChange={(e) => setExpiry(e.target.value)} />
                  </div>
                  <div className="space-y-2">
                    <Label className="font-bold">CVV</Label>
                    <Input placeholder="123" maxLength={3} type="password" className="h-12 rounded-2xl"
                      value={cvv} onChange={(e) => setCvv(e.target.value)} />
                  </div>
                </div>
                {method === "SPLIT" && loyalty && (
                  <div className="space-y-2">
                    <Label className="font-bold">Loyalty Points to Use (you have {loyalty.points})</Label>
                    <Input type="number" min={0} max={loyalty.points} className="h-12 rounded-2xl"
                      value={splitPoints} onChange={(e) => setSplitPoints(Number(e.target.value))} />
                    <p className="text-xs text-muted-foreground font-semibold">
                      Points discount: −${fmt(splitDiscount)} · Card charge: ${fmt(splitCardTotal)}
                    </p>
                  </div>
                )}
              </div>
            )}

            {/* Loyalty only */}
            {method === "LOYALTY_POINTS" && loyalty && (
              <div className="bg-card rounded-3xl shadow-soft p-6">
                <div className={cn(
                  "rounded-2xl px-4 py-3 text-sm font-bold",
                  pointsValue >= total
                    ? "bg-green-50 border border-green-200 text-green-700"
                    : "bg-amber-50 border border-amber-200 text-amber-700"
                )}>
                  {pointsValue >= total
                    ? <span className="flex items-center gap-1.5"><CheckCircle2 className="h-4 w-4" /> You have enough points! ({loyalty.points} pts = ${fmt(pointsValue)})</span>
                    : <span className="flex items-center gap-1.5"><AlertTriangle className="h-4 w-4" /> You only have ${fmt(pointsValue)} in points. Remaining ${fmt(total - pointsValue)} will be charged to a card.</span>}
                </div>
              </div>
            )}

            {/* Cash info */}
            {method === "CASH" && (
              <div className="bg-card rounded-3xl shadow-soft p-6">
                <div className="bg-primary/5 border border-primary/20 rounded-2xl px-4 py-3 text-sm font-bold text-primary flex items-center gap-2">
                  <Banknote className="h-4 w-4 shrink-0" /> Pay at the hotel on check-in. Your booking is still confirmed.
                </div>
              </div>
            )}

            <Button size="lg" disabled={loading} onClick={handlePay}
              className="w-full rounded-full bg-primary hover:opacity-95 font-bold h-14 text-base gap-3">
              <img src={moneyPenguin} alt="" className="h-6 w-auto" />
              {loading ? "Processing…" : `Pay $${fmt(total)} Now`}
            </Button>

            <p className="text-center text-xs text-muted-foreground font-semibold flex items-center justify-center gap-1">
              <Lock className="h-3 w-3" /> Secured with 256-bit encryption
            </p>
          </div>

          {/* ── Right: Order summary ── */}
          <aside className="lg:sticky lg:top-24 h-fit bg-card rounded-3xl shadow-soft overflow-hidden">
            <div className="bg-primary px-6 py-5">
              <h3 className="text-xl font-extrabold text-white">{hotel.name}</h3>
              <p className="text-sm text-white/70 font-semibold mt-0.5 flex items-center gap-1"><MapPin className="h-3.5 w-3.5" /> {hotel.city}</p>
            </div>
            <div className="p-6 space-y-0">
              {[
                [BedDouble,     "Room",      room.name],
                [CalendarCheck, "Check-in",  checkIn  || "—"],
                [CalendarCheck, "Check-out", checkOut || "—"],
                [Moon,          "Nights",    `${nights}`],
              ].map(([Icon, label, value]) => (
                <div key={label} className="flex justify-between py-3 border-b border-border text-sm">
                  <span className="font-bold text-muted-foreground flex items-center gap-1.5">
                    <Icon className="h-3.5 w-3.5" />{label}
                  </span>
                  <span className="font-bold text-foreground">{value}</span>
                </div>
              ))}
              <div className="flex justify-between items-center pt-4 text-lg font-extrabold">
                <span>Total</span>
                <span className="text-accent text-2xl">${fmt(total)}</span>
              </div>
              {method === "LOYALTY_POINTS" && pointsValue > 0 && (
                <p className="text-xs font-bold text-green-600 mt-1 flex items-center gap-1">
                  <Star className="h-3 w-3" /> Points discount: −${fmt(Math.min(pointsValue, total))}
                </p>
              )}
            </div>
          </aside>

        </div>
      </div>
    </Layout>
  );
};

export default PaymentPage;
