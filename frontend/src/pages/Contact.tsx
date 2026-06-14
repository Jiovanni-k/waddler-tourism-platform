import { useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Penguin } from "@/components/Penguin";
import { toast } from "sonner";
import { getMyContactRequests, submitContactRequest } from "@/api/contactApi";
import { listFromApiData } from "@/lib/apiResponse";

type Status = "In Progress" | "Answered" | "Closed";
interface Req { id: string; subject: string; date: string; status: Status; reply?: string; }

const styles: Record<Status, string> = {
  "In Progress": "bg-accent/15 text-accent",
  "Answered": "bg-success/15 text-success",
  "Closed": "bg-muted text-muted-foreground",
};

type ContactProps = {
  audience?: "guest" | "user";
};

const normalizeRequest = (raw: any): Req => {
  const replied = Boolean(raw.replied || raw.response || raw.reply || raw.status === "RESOLVED" || raw.status === "REPLIED");

  return {
    id: String(raw.id ?? raw.requestId ?? ""),
    subject: raw.subject || "Support request",
    date: raw.createdAt ? String(raw.createdAt).split("T")[0] : "Today",
    status: replied ? "Answered" : raw.status === "CLOSED" ? "Closed" : "In Progress",
    reply: raw.response || raw.reply,
  };
};

const Contact = ({ audience = "user" }: ContactProps) => {
  const [reqs, setReqs] = useState<Req[]>([]);
  const [form, setForm] = useState({ subject: "", category: "", message: "" });
  const [loading, setLoading] = useState(audience === "user");
  const [submitting, setSubmitting] = useState(false);
  const isGuest = audience === "guest";

  useEffect(() => {
    if (isGuest) return;

    let active = true;
    setLoading(true);
    getMyContactRequests()
      .then((res) => {
        if (active) setReqs(listFromApiData(res.data).map(normalizeRequest));
      })
      .catch((err) => {
        console.error("Failed to load support requests:", err);
        toast.error("Could not load your support requests.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [isGuest]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.subject || !form.message) return;

    setSubmitting(true);
    try {
      const res = await submitContactRequest({
        subject: form.subject,
        category: form.category,
        message: form.message,
      });
      const created = res.data ? normalizeRequest(res.data) : {
        id: `SR-${Date.now()}`,
        subject: form.subject,
        date: "Today",
        status: "In Progress" as Status,
      };
      setReqs((r) => [created, ...r]);
      setForm({ subject: "", category: "", message: "" });
      toast.success("Request sent. Admin support will see it in their inbox.");
    } catch (err: any) {
      console.error("Failed to submit support request:", err);
      toast.error(err?.response?.data?.message || "Could not send the request.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppShell
      title={isGuest ? "Guest Support" : "Member Support"}
      subtitle={isGuest ? "Ask about stays, availability, or signing in" : "We're here to help with your trips"}
      eyebrow={isGuest ? "Guest help" : "Your support"}
    >
      <div className="grid lg:grid-cols-[1fr_1.3fr] gap-6">
        <aside className="bg-card rounded-3xl shadow-soft p-6 text-center space-y-3 h-fit">
          <Penguin mood="music" className="h-32 w-32 mx-auto" />
          <h3 className="font-extrabold text-lg">Contact support</h3>
          <p className="text-sm text-muted-foreground">Submit a request and our team will get back to you within 24 hours.</p>
        </aside>

        <form onSubmit={submit} className="bg-card rounded-3xl shadow-soft p-6 sm:p-8 space-y-4">
          <h3 className="font-extrabold text-lg">Submit a request</h3>
          <div className="space-y-2">
            <Label>Subject</Label>
            <Input
              value={form.subject}
              onChange={e => setForm(f => ({ ...f, subject: e.target.value }))}
              placeholder="How can we help you?"
              className="h-12 rounded-2xl"
              required
            />
          </div>
          <div className="space-y-2">
            <Label>Category</Label>
            <Select value={form.category} onValueChange={v => setForm(f => ({ ...f, category: v ?? "" }))}>
              <SelectTrigger className="h-12 rounded-2xl"><SelectValue placeholder="Select a category" /></SelectTrigger>
              <SelectContent>
                <SelectItem value="booking">Booking</SelectItem>
                <SelectItem value="payment">Payment</SelectItem>
                <SelectItem value="account">Account</SelectItem>
                <SelectItem value="other">Other</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Message</Label>
            <Textarea
              value={form.message}
              onChange={e => setForm(f => ({ ...f, message: e.target.value }))}
              placeholder="Describe your issue in detail..."
              className="rounded-2xl min-h-32"
              required
            />
          </div>
          <Button
            type="submit"
            disabled={submitting}
            className="w-full h-12 rounded-2xl bg-primary hover:opacity-90 text-primary-foreground font-bold"
          >
            {submitting ? "Submitting..." : "Submit request"}
          </Button>
        </form>
      </div>

      {!isGuest && (
        <section className="mt-8">
          <h3 className="font-extrabold text-lg mb-4">Your requests</h3>
          {loading ? (
            <div className="bg-card rounded-3xl shadow-soft p-5 text-sm font-semibold text-muted-foreground">
              Loading your requests...
            </div>
          ) : reqs.length === 0 ? (
            <div className="bg-card rounded-3xl shadow-soft p-5 text-sm font-semibold text-muted-foreground">
              No support requests yet.
            </div>
          ) : (
            <ul className="space-y-3">
              {reqs.map(r => (
                <li key={r.id} className="bg-card rounded-3xl shadow-soft p-5">
                  <div className="flex items-start justify-between gap-3 flex-wrap">
                    <div>
                      <p className="font-extrabold">{r.subject}</p>
                      <p className="text-xs text-muted-foreground">#{r.id} - {r.date}</p>
                    </div>
                    <span className={`text-xs font-bold px-3 py-1 rounded-full ${styles[r.status]}`}>{r.status}</span>
                  </div>
                  {r.reply && (
                    <div className="mt-3 bg-secondary rounded-2xl p-4 text-sm">
                      <p className="font-bold text-primary mb-1">Support reply</p>
                      <p className="text-muted-foreground">{r.reply}</p>
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </AppShell>
  );
};

export default Contact;
