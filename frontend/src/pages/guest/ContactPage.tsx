import { useState } from "react";
import { Link } from "react-router-dom";
import { Hotel, Mail, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { Penguin } from "@/components/Penguin";
import { toast } from "sonner";
import logo from "@/assets/logo.png";
import { submitContactRequest } from "@/api/contactApi";

const GuestShell = ({ children }: { children: React.ReactNode }) => (
  <div className="min-h-screen bg-muted/40">
    <header className="bg-background sticky top-0 z-40 shadow-soft">
      <div className="container flex items-center justify-between h-20">
        <Link to="/" className="flex items-center">
          <img src={logo} alt="Waddler" className="h-10 object-contain" />
        </Link>

        <nav className="hidden md:flex items-center gap-2 text-sm">
          <Link to="/guest/hotels" className="flex items-center gap-2 px-4 py-2 rounded-full text-muted-foreground hover:text-primary hover:bg-secondary/60 font-semibold transition-colors">
            <Hotel className="h-4 w-4" />
            Hotels
          </Link>
          <Link to="/guest/availability" className="flex items-center gap-2 px-4 py-2 rounded-full text-muted-foreground hover:text-primary hover:bg-secondary/60 font-semibold transition-colors">
            <Search className="h-4 w-4" />
            Availability
          </Link>
          <Link to="/guest/contact" className="flex items-center gap-2 px-4 py-2 rounded-full bg-secondary text-primary font-bold">
            <Mail className="h-4 w-4" />
            Support
          </Link>
        </nav>

        <div className="flex items-center gap-3">
          <Link to="/login" className="text-sm font-bold text-primary hover:text-primary/80 px-4 py-2 transition-colors">
            Sign in
          </Link>
          <Link to="/register" className="h-10 px-5 rounded-full bg-primary text-primary-foreground text-sm font-bold inline-flex items-center hover:opacity-90 transition-opacity">
            Register
          </Link>
        </div>
      </div>
    </header>

    <main className="container py-10 pb-16">{children}</main>
  </div>
);

const GuestContactPage = () => {
  const [form, setForm] = useState({ subject: "", category: "", message: "", email: "" });
  const [submitting, setSubmitting] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await submitContactRequest({
        email: form.email,
        subject: form.subject,
        category: form.category,
        message: form.message,
      });
      setForm({ subject: "", category: "", message: "", email: "" });
      toast.success("Guest request sent. Admin support will see it in their inbox.");
    } catch (err: any) {
      console.error("Failed to submit guest request:", err);
      toast.error(err?.response?.data?.message || "Could not send the request.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <GuestShell>
      <section className="rounded-3xl bg-gradient-hero p-8 md:p-10 mb-8 shadow-soft">
        <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-primary bg-card px-3 py-1.5 rounded-full mb-3">
          <Mail className="h-3 w-3" /> Guest help
        </span>
        <h1 className="text-3xl md:text-5xl font-bold text-primary-deep leading-tight">Guest Support</h1>
        <p className="text-muted-foreground max-w-xl mt-3">
          Ask about hotels, availability, registration, or anything before you sign in.
        </p>
      </section>

      <div className="grid lg:grid-cols-[1fr_1.3fr] gap-6">
        <aside className="bg-card rounded-3xl shadow-soft p-6 text-center space-y-3 h-fit">
          <Penguin mood="music" className="h-32 w-32 mx-auto" />
          <h3 className="font-extrabold text-lg">Need a hand?</h3>
          <p className="text-sm text-muted-foreground">
            Send a guest message. No member dashboard, booking history, or account sections appear here.
          </p>
        </aside>

        <form onSubmit={submit} className="bg-card rounded-3xl shadow-soft p-6 sm:p-8 space-y-4">
          <h3 className="font-extrabold text-lg">Contact Waddler</h3>
          <div className="space-y-2">
            <Label>Email</Label>
            <Input
              type="email"
              value={form.email}
              onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              placeholder="you@example.com"
              className="h-12 rounded-2xl"
              required
            />
          </div>
          <div className="space-y-2">
            <Label>Subject</Label>
            <Input
              value={form.subject}
              onChange={e => setForm(f => ({ ...f, subject: e.target.value }))}
              placeholder="How can we help?"
              className="h-12 rounded-2xl"
              required
            />
          </div>
          <div className="space-y-2">
            <Label>Category</Label>
            <Select value={form.category} onValueChange={v => setForm(f => ({ ...f, category: v ?? "" }))}>
              <SelectTrigger className="h-12 rounded-2xl"><SelectValue placeholder="Select a category" /></SelectTrigger>
              <SelectContent>
                <SelectItem value="availability">Availability</SelectItem>
                <SelectItem value="hotel">Hotel details</SelectItem>
                <SelectItem value="registration">Registration</SelectItem>
                <SelectItem value="other">Other</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Message</Label>
            <Textarea
              value={form.message}
              onChange={e => setForm(f => ({ ...f, message: e.target.value }))}
              placeholder="Tell us what you need..."
              className="rounded-2xl min-h-32"
              required
            />
          </div>
          <Button type="submit" disabled={submitting} className="w-full h-12 rounded-2xl bg-primary hover:opacity-90 text-primary-foreground font-bold">
            {submitting ? "Sending..." : "Send guest request"}
          </Button>
        </form>
      </div>
    </GuestShell>
  );
};

export default GuestContactPage;
