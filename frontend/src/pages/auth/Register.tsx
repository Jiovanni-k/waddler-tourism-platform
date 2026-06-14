import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff, AlertCircle } from "lucide-react";
import { GoogleLogin, type CredentialResponse } from "@react-oauth/google";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { CalendarIcon } from "lucide-react";
import { format } from "date-fns";
import { cn } from "@/lib/utils";
import { useAuth, ROLES } from "@/context/AuthContext";
import logo from "@/assets/logo.png";
import travellingPenguin from "@/assets/penguins/penguin-travelling.png";

const Register = () => {
  const navigate = useNavigate();
  const { loginWithGoogle } = useAuth();

  const [form, setForm] = useState({
    email: "", password: "",
    firstName: "", lastName: "", gender: "",
  });
  const [birthDate,  setBirthDate]  = useState<Date | undefined>();
  const [tempDate,   setTempDate]   = useState<Date | undefined>();
  const [calOpen,    setCalOpen]    = useState(false);
  const [show,    setShow]    = useState(false);
  const [agreed,  setAgreed]  = useState(false);
  const [error,   setError]   = useState("");
  const [loading, setLoading] = useState(false);

  const set = (key: string) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(prev => ({ ...prev, [key]: e.target.value }));

  const redirectByRole = (role: string) => {
    const normalized = role?.replace(/^ROLE_/, "");
    if (normalized === ROLES.ADMIN)        navigate("/admin");
    else if (normalized === ROLES.MANAGER) navigate("/manager");
    else                                   navigate("/hotels");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!agreed) { setError("Please agree to the Terms & Conditions."); return; }
    setError(""); setLoading(true);
    try {
      const res = await fetch("/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, birthDate: birthDate ? format(birthDate, "yyyy-MM-dd") : "" }),
      });
      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || `Registration failed (${res.status})`);
      }
      navigate("/login");
    } catch (err: any) {
      setError(err.message ?? "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogle = async (credentialResponse: CredentialResponse) => {
    const idToken = credentialResponse.credential;
    if (!idToken) { setError("Google did not return a credential. Try again."); return; }
    setError("");
    try {
      const user = await loginWithGoogle(idToken);
      redirectByRole(user.role);
    } catch (err: any) {
      const status = err?.response?.status;
      const msg    = err?.response?.data?.message ?? err?.response?.data ?? err?.message;
      setError(`Google sign-up failed${status ? ` (${status})` : ""}: ${msg ?? "Check console for details."}`);
    }
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2">

      {/* ── Right: white form ── */}
      <div className="bg-white flex items-center justify-center p-6 lg:p-12 order-2 lg:order-2">
        <div className="w-full max-w-md space-y-6">
          <div className="space-y-1">
            <img src={logo} alt="Waddler" className="h-16 object-contain mb-2" />
            <h1 className="text-2xl font-extrabold text-foreground">Create your account</h1>
            <p className="text-sm text-muted-foreground">Join Waddler and start exploring the world!</p>
          </div>

          {error && (
            <div className="flex items-start gap-2 text-sm text-destructive bg-destructive/10 rounded-2xl px-4 py-3">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email */}
            <div className="space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" required placeholder="you@example.com"
                value={form.email} onChange={set("email")} className="h-12 rounded-2xl" />
            </div>

            {/* Password */}
            <div className="space-y-1.5">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                <Input id="password" type={show ? "text" : "password"} required
                  placeholder="Create a strong password"
                  value={form.password} onChange={set("password")}
                  className="h-12 rounded-2xl pr-12" />
                <button type="button" onClick={() => setShow(s => !s)}
                  aria-label="Toggle password"
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                  {show ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                </button>
              </div>
            </div>

            {/* First + Last name */}
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="firstName">First name</Label>
                <Input id="firstName" required placeholder="First name"
                  value={form.firstName} onChange={set("firstName")} className="h-12 rounded-2xl" />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="lastName">Last name</Label>
                <Input id="lastName" required placeholder="Last name"
                  value={form.lastName} onChange={set("lastName")} className="h-12 rounded-2xl" />
              </div>
            </div>

            {/* Birth date + Gender */}
            <div className="space-y-4">
              <div className="space-y-1.5">
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
                        onClick={() => { setBirthDate(tempDate); setCalOpen(false); }}>
                        Apply
                      </Button>
                    </div>
                  </PopoverContent>
                </Popover>
              </div>
              <div className="space-y-1.5">
                <Label>Gender</Label>
                <Select onValueChange={v => setForm(prev => ({ ...prev, gender: v }))}>
                  <SelectTrigger className="h-12 rounded-2xl">
                    <SelectValue placeholder="Select gender" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="FEMALE">Female</SelectItem>
                    <SelectItem value="MALE">Male</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Terms */}
            <label className="flex items-start gap-3 text-sm text-muted-foreground cursor-pointer">
              <Checkbox
                checked={agreed}
                onCheckedChange={v => setAgreed(Boolean(v))}
                className="mt-0.5"
              />
              <span>
                I agree to the{" "}
                <a href="#" className="font-bold" style={{ color: "#B180FC" }}>Terms & Conditions</a>{" "}
                and{" "}
                <a href="#" className="font-bold" style={{ color: "#B180FC" }}>Privacy Policy</a>
              </span>
            </label>

            <Button type="submit" disabled={loading}
              className="w-full h-12 rounded-2xl font-bold text-base text-white"
              style={{ background: "#B180FC" }}>
              {loading ? "Creating account…" : "Create account"}
            </Button>

            <div className="flex items-center gap-3">
              <div className="flex-1 h-px bg-border" />
              <span className="text-xs text-muted-foreground font-medium">or sign up with</span>
              <div className="flex-1 h-px bg-border" />
            </div>

            <div className="flex justify-center">
              <GoogleLogin
                onSuccess={handleGoogle}
                onError={() => setError("Google sign-up failed. Please try again.")}
                theme="outline"
                shape="pill"
                size="large"
                text="signup_with"
                width={360}
              />
            </div>

            <p className="text-sm text-center text-muted-foreground">
              Already have an account?{" "}
              <Link to="/login" className="font-bold" style={{ color: "#B180FC" }}>Sign in</Link>
            </p>
          </form>
        </div>
      </div>

      {/* ── Left: purple brand panel ── */}
      <div
        className="hidden lg:flex flex-col items-center justify-center gap-8 p-12 order-1 lg:order-1 relative overflow-hidden"
        style={{ background: "#B180FC" }}
      >
        {/* Decorative blobs */}
        <div className="absolute top-0 right-0 w-64 h-64 rounded-full opacity-20"
          style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(30%, -30%)" }} />
        <div className="absolute bottom-0 left-0 w-48 h-48 rounded-full opacity-15"
          style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(-30%, 30%)" }} />
        <div className="absolute top-1/2 left-0 w-32 h-32 rounded-full opacity-10"
          style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(-50%, -50%)" }} />

        {/* Penguin travelling image */}
        <div className="relative flex items-center justify-center">
          <div className="absolute inset-0 rounded-full blur-3xl opacity-30 scale-110"
            style={{ background: "rgba(255,255,255,0.6)" }} />
          <div className="absolute w-[340px] h-[340px] rounded-full border-4 border-dashed border-white/30 animate-spin"
            style={{ animationDuration: "20s" }} />
          <div className="absolute top-0 right-8 h-10 w-10 rounded-full bg-white/30 backdrop-blur-sm" />
          <div className="absolute bottom-4 left-4 h-7 w-7 rounded-full bg-white/20" />
          <div className="absolute top-1/3 -left-4 h-5 w-5 rounded-full bg-yellow-200/60" />
          <div className="absolute bottom-1/3 -right-2 h-6 w-6 rounded-full bg-white/25" />
          <img
            src={travellingPenguin}
            alt="Waddler travelling penguin"
            className="relative z-10 w-72 drop-shadow-2xl"
            style={{ filter: "drop-shadow(0 20px 40px rgba(0,0,0,0.3))" }}
          />
        </div>

        <div className="text-center space-y-2 relative z-10">
          <h2 className="text-3xl font-extrabold text-white leading-tight">
            The world is waiting,<br />
            <span className="text-yellow-200">start waddling!</span>
          </h2>
          <p className="text-white/75 text-sm max-w-xs mx-auto">
            Create your free account and discover amazing hotels across the globe.
          </p>
        </div>
      </div>

    </div>
  );
};

export default Register;
