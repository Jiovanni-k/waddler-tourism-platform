import { useState, useEffect } from "react";
import AppShell from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { CalendarIcon } from "lucide-react";
import { format, parseISO, isValid } from "date-fns";
import { cn } from "@/lib/utils";
import { Penguin } from "@/components/Penguin";
import { toast } from "sonner";
import { useAuth } from "@/context/AuthContext";
import { updateMe } from "@/api/authApi";

const Profile = () => {
  const { user } = useAuth();
  const [form, setForm] = useState({
    firstName: "", lastName: "",
    email: "", username: "",
    birth: "",
  });
  const [birthDate, setBirthDate] = useState<Date | undefined>();
  const [tempDate,  setTempDate]  = useState<Date | undefined>();
  const [calOpen,   setCalOpen]   = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (user) {
      const rawBirth = (user as any).birthDate ?? "";
      setForm({
        firstName: user.firstName ?? "",
        lastName:  user.lastName  ?? "",
        email:     user.email     ?? "",
        username:  (user as any).username  ?? "",
        birth:     rawBirth,
      });
      if (rawBirth) {
        try {
          const d = parseISO(rawBirth);
          if (isValid(d)) setBirthDate(d);
        } catch {}
      }
    }
  }, [user]);

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSave = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateMe({ firstName: form.firstName, lastName: form.lastName });
      localStorage.setItem("user", JSON.stringify({ ...user, firstName: form.firstName, lastName: form.lastName }));
      toast.success("Changes saved!");
    } catch {
      toast.error("Failed to save changes.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <AppShell title="My Profile" subtitle="Manage your account info" eyebrow="Account">
      <div className="grid lg:grid-cols-[260px_1fr] gap-6">
        {/* Side card */}
        <aside className="bg-card rounded-3xl shadow-soft p-6 text-center space-y-3 h-fit">
          <Penguin mood="happy" className="h-24 w-24 mx-auto" />
          <div>
            <p className="font-extrabold text-lg">{form.firstName} {form.lastName}</p>
            <p className="text-sm text-muted-foreground">{form.email}</p>
          </div>
          <span className="inline-block bg-accent/15 text-accent text-xs font-bold px-3 py-1 rounded-full">
            Gold Member
          </span>
        </aside>

        <div className="space-y-5">
          {/* Personal info */}
          <form
            onSubmit={handleSave}
            className="bg-card rounded-3xl shadow-soft p-6 sm:p-8 space-y-6"
          >
            <h2 className="font-extrabold text-lg">Personal information</h2>
            <div className="grid sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>First name</Label>
                <Input value={form.firstName} onChange={set("firstName")} className="h-12 rounded-2xl" />
              </div>
              <div className="space-y-2">
                <Label>Last name</Label>
                <Input value={form.lastName} onChange={set("lastName")} className="h-12 rounded-2xl" />
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label>Email</Label>
                <Input type="email" value={form.email} onChange={set("email")} className="h-12 rounded-2xl" />
              </div>
              <div className="space-y-2">
                <Label>Username</Label>
                <Input value={form.username} onChange={set("username")} className="h-12 rounded-2xl" />
              </div>
              <div className="space-y-2">
                <Label>Birth date</Label>
                <Popover open={calOpen} onOpenChange={open => { setCalOpen(open); if (open) setTempDate(birthDate); }}>
                  <PopoverTrigger asChild>
                    <Button type="button" variant="outline" className={cn("w-full justify-start text-left font-normal h-12 rounded-2xl border-input shadow-none", !birthDate && "text-muted-foreground")}>
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {birthDate ? format(birthDate, "PPP") : "Pick a date"}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0 overflow-hidden" align="start">
                    <Calendar
                      mode="single"
                      captionLayout="dropdown"
                      fromYear={1920}
                      toYear={new Date().getFullYear()}
                      selected={tempDate}
                      onSelect={setTempDate}
                      disabled={d => d > new Date()}
                      initialFocus
                      className="pointer-events-auto"
                    />
                    <div className="flex justify-end gap-2 px-4 pb-4 pt-2 border-t border-border/30">
                      <Button type="button" variant="outline" size="sm" className="rounded-full font-bold"
                        onClick={() => setCalOpen(false)}>
                        Cancel
                      </Button>
                      <Button type="button" size="sm" className="rounded-full bg-primary text-primary-foreground font-bold"
                        onClick={() => {
                          setBirthDate(tempDate);
                          setForm(f => ({ ...f, birth: tempDate ? format(tempDate, "yyyy-MM-dd") : "" }));
                          setCalOpen(false);
                        }}>
                        Apply
                      </Button>
                    </div>
                  </PopoverContent>
                </Popover>
              </div>
            </div>
            <div className="flex justify-end">
              <Button type="submit" disabled={saving} className="rounded-full bg-primary text-primary-foreground hover:opacity-90 font-bold px-8 h-12">
                {saving ? "Saving…" : "Save changes"}
              </Button>
            </div>
          </form>

          {/* Change password */}
          <form
            onSubmit={e => { e.preventDefault(); toast.success("Password updated!"); }}
            className="bg-card rounded-3xl shadow-soft p-6 sm:p-8 space-y-5"
          >
            <h2 className="font-extrabold text-lg">Change password</h2>
            <div className="grid sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>New password</Label>
                <Input type="password" placeholder="••••••••" className="h-12 rounded-2xl" />
              </div>
              <div className="space-y-2">
                <Label>Confirm password</Label>
                <Input type="password" placeholder="••••••••" className="h-12 rounded-2xl" />
              </div>
            </div>
            <div className="flex justify-end">
              <Button type="submit" className="rounded-full bg-primary text-primary-foreground hover:opacity-90 font-bold px-8 h-12">
                Update password
              </Button>
            </div>
          </form>
        </div>
      </div>
    </AppShell>
  );
};

export default Profile;
