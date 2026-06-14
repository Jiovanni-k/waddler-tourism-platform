import { useState, useEffect } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, AlertCircle } from "lucide-react";
import { GoogleLogin, type CredentialResponse } from "@react-oauth/google";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { useAuth, ROLES } from "@/context/AuthContext";
import logo from "@/assets/logo.png";
import travellingPenguin from "@/assets/penguins/penguin-travelling.png";

type Mode = "login" | "register";

/* ── tiny keyframe injected once ── */
const STYLE = `
@keyframes auth-slide-in {
  from { opacity: 0; transform: translateY(18px); }
  to   { opacity: 1; transform: translateY(0);    }
}
@keyframes auth-fade-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}
.auth-form-enter  { animation: auth-slide-in 0.38s cubic-bezier(.22,1,.36,1) both; }
.auth-panel-enter { animation: auth-fade-in  0.45s ease both; }
`;

export default function AuthPage() {
  const location = useLocation();
  const navigate  = useNavigate();
  const { login, loginWithGoogle } = useAuth();

  const initialMode: Mode = location.pathname === "/register" ? "register" : "login";
  const [mode,    setMode]    = useState<Mode>(initialMode);
  const [animKey, setAnimKey] = useState(0);

  /* keep URL in sync when switching modes */
  const switchTo = (m: Mode) => {
    navigate(m === "login" ? "/login" : "/register", { replace: true });
    setMode(m);
    setAnimKey(k => k + 1);
  };

  /* sync if user navigates via browser back/forward */
  useEffect(() => {
    const m: Mode = location.pathname === "/register" ? "register" : "login";
    if (m !== mode) { setMode(m); setAnimKey(k => k + 1); }
  }, [location.pathname]);

  /* ── Login state ── */
  const [email,    setEmail]    = useState("");
  const [password, setPassword] = useState("");
  const [show,     setShow]     = useState(false);
  const [error,    setError]    = useState("");
  const [loading,  setLoading]  = useState(false);

  /* ── Register state ── */
  const [reg, setReg] = useState({
    email: "", password: "", firstName: "", lastName: "", gender: "",
  });
  const [regShow,     setRegShow]     = useState(false);
  const [birthDay,    setBirthDay]    = useState("");
  const [birthMonth,  setBirthMonth]  = useState("");
  const [birthYear,   setBirthYear]   = useState("");
  const [agreed,      setAgreed]      = useState(false);

  const setR = (k: string) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setReg(p => ({ ...p, [k]: e.target.value }));

  const redirectByRole = (role: string) => {
    const r = role?.replace(/^ROLE_/, "");
    if (r === ROLES.ADMIN)        navigate("/admin");
    else if (r === ROLES.MANAGER) navigate("/manager");
    else                          navigate("/hotels");
  };

  /* ── Login submit ── */
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      const user = await login({ email, password });
      redirectByRole(user.role);
    } catch (err: any) {
      const status = err?.response?.status;
      const msg    = err?.response?.data?.message ?? err?.message;
      setError(`Sign-in failed${status ? ` (${status})` : ""}: ${msg ?? "Check your email and password."}`);
    } finally { setLoading(false); }
  };

  /* ── Register submit ── */
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!agreed) { setError("Please agree to the Terms & Conditions."); return; }
    setError(""); setLoading(true);
    try {
      const birthDate = birthYear && birthMonth && birthDay
        ? `${birthYear}-${birthMonth.padStart(2,"0")}-${birthDay.padStart(2,"0")}`
        : "";
      const res = await fetch("/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...reg, birthDate }),
      });
      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || `Registration failed (${res.status})`);
      }
      switchTo("login");
    } catch (err: any) {
      setError(err.message ?? "Something went wrong.");
    } finally { setLoading(false); }
  };

  /* ── Google ── */
  const handleGoogle = async (credentialResponse: CredentialResponse) => {
    const idToken = credentialResponse.credential;
    if (!idToken) { setError("Google did not return a credential."); return; }
    setError("");
    try {
      const user = await loginWithGoogle(idToken);
      redirectByRole(user.role);
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? err?.message ?? "";
      const isExpired = msg.toLowerCase().includes("expired") || msg.toLowerCase().includes("jwt");
      setError(isExpired
        ? "Google session expired. Please click 'Sign in with Google' again."
        : `Google sign-in failed: ${msg || "Try again."}`);
    }
  };

  const isRegister = mode === "register";

  return (
    <>
      <style>{STYLE}</style>
      <div className="min-h-screen grid lg:grid-cols-2">

        {/* ── Purple brand panel (left) ── */}
        <div
          className={`hidden lg:flex flex-col items-center justify-center gap-8 p-12 relative overflow-hidden ${isRegister ? "order-1" : "order-2"}`}
          style={{ background: "#B180FC" }}
        >
          {/* blobs */}
          <div className="absolute top-0 right-0 w-64 h-64 rounded-full opacity-20"
            style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(30%,-30%)" }} />
          <div className="absolute bottom-0 left-0 w-48 h-48 rounded-full opacity-15"
            style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(-30%,30%)" }} />
          <div className="absolute top-1/2 left-0 w-32 h-32 rounded-full opacity-10"
            style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(-50%,-50%)" }} />

          {/* image / video — fades when mode switches */}
          <div key={`panel-${animKey}`} className="auth-panel-enter relative flex items-center justify-center">
            <div className="absolute inset-0 rounded-full blur-3xl opacity-30 scale-110"
              style={{ background: "rgba(255,255,255,0.6)" }} />
            <div className="absolute w-[320px] h-[320px] rounded-full border-4 border-dashed border-white/30 animate-spin"
              style={{ animationDuration: "20s" }} />
            <div className="absolute top-0 right-8 h-10 w-10 rounded-full bg-white/30" />
            <div className="absolute bottom-4 left-4 h-7 w-7 rounded-full bg-white/20" />
            <div className="absolute top-1/3 -left-4 h-5 w-5 rounded-full bg-yellow-200/60" />

            {isRegister ? (
              <img src={travellingPenguin} alt=""
                className="relative z-10 w-64 drop-shadow-2xl"
                style={{ filter: "drop-shadow(0 20px 40px rgba(0,0,0,0.3))" }} />
            ) : (
              <div className="relative rounded-[32px] overflow-hidden shadow-2xl z-10"
                style={{ width: 240, height: 300, boxShadow: "0 0 0 3px rgba(255,255,255,0.5), 0 0 0 8px rgba(255,255,255,0.15), 0 30px 60px rgba(0,0,0,0.25)" }}>
                <video src="/penguin-walking.mp4" autoPlay muted loop playsInline className="w-full h-full object-cover" />
                <div className="absolute inset-x-0 bottom-0 h-16"
                  style={{ background: "linear-gradient(to top, rgba(177,128,252,0.7), transparent)" }} />
              </div>
            )}
          </div>

          {/* text — fades when mode switches */}
          <div key={`text-${animKey}`} className="auth-panel-enter text-center space-y-2 relative z-10">
            {isRegister ? (
              <>
                <h2 className="text-3xl font-extrabold text-white leading-tight">
                  The world is waiting,<br />
                  <span className="text-yellow-200">start waddling!</span>
                </h2>
                <p className="text-white/75 text-sm max-w-xs mx-auto">
                  Create your free account and discover amazing hotels across the globe.
                </p>
              </>
            ) : (
              <>
                <h2 className="text-3xl font-extrabold text-white leading-tight">
                  Your next stay,<br />
                  <span className="text-yellow-200">made happy.</span>
                </h2>
                <p className="text-white/75 text-sm max-w-xs mx-auto">
                  Sign in to keep waddling toward paradise.
                </p>
              </>
            )}
          </div>
        </div>

        {/* ── White form panel (right) ── */}
        <div className={`bg-white flex items-center justify-center p-6 lg:p-12 overflow-y-auto ${isRegister ? "order-2" : "order-1"}`}>
          <div className="w-full max-w-md py-8">

            {/* form content slides in on switch */}
            <div key={animKey} className="auth-form-enter space-y-6">
              <div className="space-y-1">
                <img src={logo} alt="Waddler" className="h-16 object-contain mb-2" />
                <h1 className="text-2xl font-extrabold" style={{ color: "#291940" }}>
                  {isRegister ? "Create your account" : "Welcome back!"}
                </h1>
                <p className="text-sm text-muted-foreground">
                  {isRegister ? "Join Waddler and start exploring the world!" : "Sign in to your account"}
                </p>
              </div>

              {error && (
                <div className="flex items-start gap-2 text-sm text-destructive bg-destructive/10 rounded-2xl px-4 py-3">
                  <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
                  <span>{error}</span>
                </div>
              )}

              {/* ── LOGIN FORM ── */}
              {!isRegister && (
                <form onSubmit={handleLogin} className="space-y-5">
                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)}
                      placeholder="you@example.com" required className="h-12 rounded-2xl" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="pw">Password</Label>
                    <div className="relative">
                      <Input id="pw" type={show ? "text" : "password"} value={password}
                        onChange={e => setPassword(e.target.value)} placeholder="Enter your password"
                        required className="h-12 rounded-2xl pr-12" />
                      <button type="button" onClick={() => setShow(s => !s)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                        {show ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                      </button>
                    </div>
                    <a href="#" className="text-xs font-bold block text-right" style={{ color: "#B180FC" }}>
                      Forgot password?
                    </a>
                  </div>
                  <Button type="submit" disabled={loading} className="w-full h-12 rounded-2xl font-bold text-base text-white"
                    style={{ background: "#B180FC" }}>
                    {loading ? "Signing in…" : "Sign in"}
                  </Button>
                  <div className="flex items-center gap-3">
                    <div className="flex-1 h-px bg-border" />
                    <span className="text-xs text-muted-foreground font-medium">or continue with</span>
                    <div className="flex-1 h-px bg-border" />
                  </div>
                  <div className="flex justify-center">
                    <GoogleLogin onSuccess={handleGoogle}
                      onError={() => setError("Google sign-in failed.")}
                      theme="outline" shape="pill" size="large" text="signin_with" width={360} />
                  </div>
                  <p className="text-sm text-center text-muted-foreground">
                    Don't have an account?{" "}
                    <button type="button" onClick={() => switchTo("register")}
                      className="font-bold" style={{ color: "#B180FC" }}>Register</button>
                  </p>
                  <div className="flex items-center gap-3">
                    <div className="flex-1 h-px bg-border" />
                    <span className="text-xs text-muted-foreground font-medium">or</span>
                    <div className="flex-1 h-px bg-border" />
                  </div>
                  <button type="button" onClick={() => navigate("/hotels")}
                    className="w-full text-sm font-semibold text-muted-foreground hover:text-foreground transition-colors py-1">
                    Continue as guest →
                  </button>
                </form>
              )}

              {/* ── REGISTER FORM ── */}
              {isRegister && (
                <form onSubmit={handleRegister} className="space-y-4">
                  <div className="space-y-1.5">
                    <Label htmlFor="r-email">Email</Label>
                    <Input id="r-email" type="email" required placeholder="you@example.com"
                      value={reg.email} onChange={setR("email")} className="h-12 rounded-2xl" />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="r-pw">Password</Label>
                    <div className="relative">
                      <Input id="r-pw" type={regShow ? "text" : "password"} required
                        placeholder="Create a strong password"
                        value={reg.password} onChange={setR("password")} className="h-12 rounded-2xl pr-12" />
                      <button type="button" onClick={() => setRegShow(s => !s)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                        {regShow ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                      </button>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1.5">
                      <Label htmlFor="r-fn">First name</Label>
                      <Input id="r-fn" required placeholder="First name"
                        value={reg.firstName} onChange={setR("firstName")} className="h-12 rounded-2xl" />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="r-ln">Last name</Label>
                      <Input id="r-ln" required placeholder="Last name"
                        value={reg.lastName} onChange={setR("lastName")} className="h-12 rounded-2xl" />
                    </div>
                  </div>
                  <div className="space-y-4">
                    <div className="space-y-1.5">
                      <Label>Birth date</Label>
                      <div className="grid grid-cols-3 gap-3">
                        <Select onValueChange={setBirthDay}>
                          <SelectTrigger className="h-12 rounded-2xl"><SelectValue placeholder="Day" /></SelectTrigger>
                          <SelectContent>
                            {Array.from({ length: 31 }, (_, i) => i + 1).map(d => (
                              <SelectItem key={d} value={String(d)}>{d}</SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <Select onValueChange={setBirthMonth}>
                          <SelectTrigger className="h-12 rounded-2xl"><SelectValue placeholder="Month" /></SelectTrigger>
                          <SelectContent>
                            {["January","February","March","April","May","June","July","August","September","October","November","December"]
                              .map((m, i) => <SelectItem key={i} value={String(i+1)}>{m}</SelectItem>)}
                          </SelectContent>
                        </Select>
                        <Select onValueChange={setBirthYear}>
                          <SelectTrigger className="h-12 rounded-2xl"><SelectValue placeholder="Year" /></SelectTrigger>
                          <SelectContent>
                            {Array.from({ length: new Date().getFullYear() - 1919 }, (_, i) => new Date().getFullYear() - i)
                              .map(y => <SelectItem key={y} value={String(y)}>{y}</SelectItem>)}
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                    <div className="space-y-1.5">
                      <Label>Gender</Label>
                      <Select onValueChange={v => setReg(p => ({ ...p, gender: v }))}>
                        <SelectTrigger className="h-12 rounded-2xl"><SelectValue placeholder="Select gender" /></SelectTrigger>
                        <SelectContent>
                          <SelectItem value="FEMALE">Female</SelectItem>
                          <SelectItem value="MALE">Male</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                  <label className="flex items-start gap-3 text-sm text-muted-foreground cursor-pointer">
                    <Checkbox checked={agreed} onCheckedChange={v => setAgreed(Boolean(v))} className="mt-0.5" />
                    <span>
                      I agree to the{" "}
                      <a href="#" className="font-bold" style={{ color: "#B180FC" }}>Terms & Conditions</a>{" "}
                      and <a href="#" className="font-bold" style={{ color: "#B180FC" }}>Privacy Policy</a>
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
                    <GoogleLogin onSuccess={handleGoogle}
                      onError={() => setError("Google sign-up failed.")}
                      theme="outline" shape="pill" size="large" text="signup_with" width={360} />
                  </div>
                  <p className="text-sm text-center text-muted-foreground">
                    Already have an account?{" "}
                    <button type="button" onClick={() => switchTo("login")}
                      className="font-bold" style={{ color: "#B180FC" }}>Sign in</button>
                  </p>
                  <div className="flex items-center gap-3">
                    <div className="flex-1 h-px bg-border" />
                    <span className="text-xs text-muted-foreground font-medium">or</span>
                    <div className="flex-1 h-px bg-border" />
                  </div>
                  <button type="button" onClick={() => navigate("/hotels")}
                    className="w-full text-sm font-semibold text-muted-foreground hover:text-foreground transition-colors py-1">
                    Continue as guest →
                  </button>
                </form>
              )}
            </div>
          </div>
        </div>

      </div>
    </>
  );
}
