import { useEffect, useState } from "react";
import { Trophy, Plus, Minus, Gift, Star, RefreshCw } from "lucide-react";
import { format } from "date-fns";
import AppShell from "@/components/AppShell";
import rewardPenguin from "@/assets/penguins/Action_Reward.png";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
// @ts-ignore
import { getLoyalty, redeemPoints } from "@/api/loyaltyApi";

// ── Tier config ────────────────────────────────────────────────────────────
type Tier = "BRONZE" | "SILVER" | "GOLD" | "PLATINUM";

const TIERS: Tier[] = ["BRONZE", "SILVER", "GOLD", "PLATINUM"];

const TIER_THRESHOLDS: Record<Tier, number> = {
  BRONZE:   0,
  SILVER:   1_000,
  GOLD:     5_000,
  PLATINUM: 10_000,
};

const TIER_LABELS: Record<Tier, string> = {
  BRONZE:   "Bronze",
  SILVER:   "Silver",
  GOLD:     "Gold",
  PLATINUM: "Platinum",
};

const TIER_NEXT: Record<Tier, number | null> = {
  BRONZE:   1_000,
  SILVER:   5_000,
  GOLD:     10_000,
  PLATINUM: null,
};

// ── Transaction types ──────────────────────────────────────────────────────
type TxType = "EARN" | "REDEEM" | "REVERSAL";

interface Transaction {
  id: string;
  type: TxType;
  amount: number;
  bookingId?: string;
  createdAt: string;
  description?: string;
}

// ── Loyalty account ────────────────────────────────────────────────────────
interface LoyaltyAccount {
  tier: Tier;
  totalPoints: number;
  currentPoints: number;
  transactions: Transaction[];
}

const normalizeTx = (raw: any): Transaction => ({
  id:          String(raw.id ?? Math.random()),
  type:        raw.type ?? "EARN",
  amount:      Math.abs(Number(raw.amount ?? raw.points ?? 0)),
  bookingId:   raw.bookingId ?? raw.booking_id ?? undefined,
  createdAt:   raw.createdAt ?? raw.created_at ?? "",
  description: raw.description ?? raw.label ?? undefined,
});

const formatDate = (d: string) => {
  if (!d) return "";
  try { return format(new Date(d), "d MMM yyyy"); } catch { return d; }
};

const txLabel = (tx: Transaction): string => {
  if (tx.description) return tx.description;
  if (tx.type === "EARN")      return tx.bookingId ? `Booking reward` : "Points earned";
  if (tx.type === "REVERSAL")  return "Cancellation reversal";
  return "Points redeemed";
};

// ── Component ──────────────────────────────────────────────────────────────
const Loyalty = () => {
  const [account,  setAccount]  = useState<LoyaltyAccount | null>(null);
  const [loading,  setLoading]  = useState(true);
  const [redeem,   setRedeem]   = useState(100);
  const [redeeming, setRedeeming] = useState(false);

  const fetchLoyalty = () => {
    setLoading(true);
    getLoyalty()
      .then((res: any) => {
        const d = res.data;
        setAccount({
          tier:          d.tier ?? d.tierName ?? d.membershipTier ?? "BRONZE",
          totalPoints:   Number(d.lifetimePoints ?? d.totalPoints ?? d.total_points ?? d.lifetime_points ?? d.totalEarned ?? 0),
          currentPoints: Number(d.pointsBalance ?? d.currentPoints ?? d.current_points ?? d.balance ?? d.currentBalance ?? 0),
          transactions:  Array.isArray(d.transactions) ? d.transactions.map(normalizeTx) : [],
        });
      })
      .catch((err: any) => {
        console.error("loyalty/my error:", err?.response?.data ?? err?.message);
        toast.error("Could not load loyalty data.");
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchLoyalty(); }, []);

  const tier         = account?.tier ?? "BRONZE";
  const totalPoints  = account?.totalPoints  ?? 0;
  const currentPoints = account?.currentPoints ?? 0;
  const nextThreshold = TIER_NEXT[tier];
  const prevThreshold = TIER_THRESHOLDS[tier];

  // Progress within current tier
  const progress = nextThreshold
    ? Math.min(100, ((totalPoints - prevThreshold) / (nextThreshold - prevThreshold)) * 100)
    : 100;

  const onRedeem = async () => {
    if (redeem < 100) { toast.error("Minimum redemption is 100 pts."); return; }
    if (redeem > currentPoints) { toast.error("Not enough points."); return; }
    setRedeeming(true);
    try {
      await redeemPoints(redeem);
      toast.success(`Redeemed ${redeem} pts → $${(redeem / 100).toFixed(2)} credit!`);
      fetchLoyalty();
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? "Redemption failed. Try again.";
      toast.error(msg);
    } finally {
      setRedeeming(false);
    }
  };

  return (
    <AppShell title="Loyalty Rewards" subtitle="100 pts = $1 credit" eyebrow="Earn & redeem" penguin={rewardPenguin}>

      {loading ? (
        <div className="flex flex-col items-center gap-4 py-16">
          <p className="text-muted-foreground font-semibold">Loading your rewards…</p>
        </div>
      ) : (
        <>
          <div className="grid lg:grid-cols-[1.4fr_1fr] gap-6">

            {/* ── Tier card ── */}
            <section className="bg-gradient-primary text-primary-foreground rounded-3xl p-8 relative overflow-hidden shadow-soft">
              <div className="flex items-center gap-3 mb-2">
                <Trophy className="h-6 w-6 text-accent" />
                <span className="font-bold uppercase tracking-wide text-sm">
                  {TIER_LABELS[tier]} Member
                </span>
              </div>
              <h2 className="text-3xl font-extrabold mb-1">
                {tier === "PLATINUM" ? "You're at the top!" : "You're doing great!"}
              </h2>
              <p className="text-white/80 mb-6">
                {nextThreshold
                  ? `${(nextThreshold - totalPoints).toLocaleString()} pts to ${TIER_LABELS[TIERS[TIERS.indexOf(tier) + 1]]}`
                  : "Maximum tier reached — enjoy your perks!"}
              </p>

              {/* Points balance */}
              <div className="flex items-end justify-between mb-2 text-sm">
                <span className="font-bold">{currentPoints.toLocaleString()} pts available</span>
                <span className="text-white/70">${(currentPoints / 100).toFixed(2)} value</span>
              </div>

              {/* Tier progress bar */}
              <div className="h-3 rounded-full bg-white/20 overflow-hidden">
                <div
                  className="h-full bg-accent transition-all duration-500"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-xs text-white/60 mt-2">
                {totalPoints.toLocaleString()} lifetime pts
                {nextThreshold ? ` · ${nextThreshold.toLocaleString()} to ${TIER_LABELS[TIERS[TIERS.indexOf(tier) + 1]]}` : ""}
              </p>

              {/* Tier ladder */}
              <div className="flex justify-between mt-6 text-xs font-bold">
                {TIERS.map(t => (
                  <span
                    key={t}
                    className={t === tier ? "text-accent" : TIERS.indexOf(t) < TIERS.indexOf(tier) ? "text-white/70" : "text-white/30"}
                  >
                    {TIER_LABELS[t]}
                  </span>
                ))}
              </div>

            </section>

            {/* ── Redeem panel ── */}
            <section className="bg-card rounded-3xl shadow-soft p-6 space-y-4">
              <div className="flex items-center gap-2">
                <Gift className="h-5 w-5 text-primary" />
                <h3 className="font-extrabold text-lg">Redeem points</h3>
              </div>
              <p className="text-sm text-muted-foreground">
                Convert points to credit toward your next booking. Min 100 pts.
              </p>

              {/* Available balance */}
              <div className="bg-secondary rounded-2xl px-4 py-3 flex justify-between items-center">
                <span className="text-sm font-bold text-muted-foreground">Available</span>
                <span className="font-extrabold text-primary">{currentPoints.toLocaleString()} pts</span>
              </div>

              <div className="flex items-center gap-2">
                <Button
                  type="button" variant="outline" size="icon"
                  className="rounded-full border-input shadow-none"
                  onClick={() => setRedeem(r => Math.max(100, r - 100))}
                >
                  <Minus className="h-4 w-4" />
                </Button>
                <Input
                  type="number"
                  value={redeem}
                  onChange={e => setRedeem(+e.target.value)}
                  className="h-12 rounded-2xl text-center font-bold"
                />
                <Button
                  type="button" variant="outline" size="icon"
                  className="rounded-full border-input shadow-none"
                  onClick={() => setRedeem(r => Math.min(currentPoints, r + 100))}
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>

              <p className="text-sm text-center text-muted-foreground">
                = <span className="font-extrabold text-accent">${(redeem / 100).toFixed(2)}</span> credit
              </p>

              <Button
                onClick={onRedeem}
                disabled={redeeming || currentPoints < 100}
                className="w-full h-12 rounded-2xl bg-primary text-primary-foreground hover:opacity-90 font-bold"
              >
                {redeeming ? "Redeeming…" : "Redeem now"}
              </Button>
            </section>
          </div>

          {/* ── Transaction history ── */}
          <section className="bg-card rounded-3xl shadow-soft p-6 mt-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-extrabold text-lg">Points history</h3>
              <button
                onClick={fetchLoyalty}
                className="text-xs font-bold text-primary flex items-center gap-1 hover:opacity-70 transition-opacity"
              >
                <RefreshCw className="h-3 w-3" /> Refresh
              </button>
            </div>

            {account?.transactions.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground font-semibold">
                No transactions yet. Book a stay to start earning!
              </div>
            ) : (
              <ul className="divide-y divide-border">
                {account?.transactions.map((tx) => {
                  const isEarn = tx.type === "EARN";
                  return (
                    <li key={tx.id} className="py-3 flex items-center justify-between gap-4">
                      <div className="flex items-center gap-3">
                        <div className={`h-9 w-9 rounded-full flex items-center justify-center shrink-0 ${
                          isEarn ? "bg-secondary text-primary" : "bg-destructive/10 text-destructive"
                        }`}>
                          <Star className="h-4 w-4" />
                        </div>
                        <div>
                          <p className="font-bold text-sm">{txLabel(tx)}</p>
                          {tx.createdAt && (
                            <p className="text-xs text-muted-foreground">{formatDate(tx.createdAt)}</p>
                          )}
                        </div>
                      </div>
                      <span className={`font-extrabold whitespace-nowrap ${isEarn ? "text-success" : "text-destructive"}`}>
                        {isEarn ? "+" : "−"}{tx.amount.toLocaleString()} pts
                      </span>
                    </li>
                  );
                })}
              </ul>
            )}
          </section>
        </>
      )}
    </AppShell>
  );
};

export default Loyalty;
