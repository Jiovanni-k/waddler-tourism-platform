import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff, AlertCircle } from "lucide-react";
import { GoogleLogin, type CredentialResponse } from "@react-oauth/google";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/context/AuthContext";
import { ROLES } from "@/context/AuthContext";
import logo from "@/assets/logo.png";

const Login = () => {
  const [email, setEmail]       = useState("");
  const [password, setPassword] = useState("");
  const [show, setShow]         = useState(false);
  const [error, setError]       = useState("");
  const [loading, setLoading]   = useState(false);

  const { login, loginWithGoogle, logout } = useAuth();
  const navigate = useNavigate();
  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

  const redirectByRole = (role: string) => {
    const normalized = role?.replace(/^ROLE_/, "");
    if (normalized === ROLES.ADMIN)          navigate("/admin");
    else if (normalized === ROLES.MANAGER)   navigate("/manager");
    else                                     navigate("/user/hotels");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      const user = await login({ email, password });
      redirectByRole(user.role);
    } catch (err: any) {
      const status = err?.response?.status;
      const msg    = err?.response?.data?.message ?? err?.response?.data ?? err?.message;
      setError(`Sign-in failed${status ? ` (${status})` : ""}: ${msg ?? "Check your email and password."}`);
    } finally { setLoading(false); }
  };

  const handleGoogle = async (credentialResponse: CredentialResponse) => {
    const idToken = credentialResponse.credential;
    if (!idToken) { setError("Google did not return a credential. Try again."); return; }
    setError("");
    try {
      const user = await loginWithGoogle(idToken);
      redirectByRole(user.role);
    } catch (err: any) {
      console.error("Google login error:", err?.response ?? err);
      const status = err?.response?.status;
      const msg    = err?.response?.data?.message ?? err?.response?.data ?? err?.message;
      setError(`Google sign-in failed${status ? ` (${status})` : ""}: ${msg ?? "Check console for details."}`);
    }
  };

  const handleGuest = () => {
    logout();
    navigate("/guest/hotels", { replace: true });
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2">

      {/* ── Left: white form ── */}
      <div className="bg-white flex items-center justify-center p-6 lg:p-14 order-2 lg:order-1">
        <div className="w-full max-w-md space-y-6">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2">
              <img src={logo} alt="Waddler" className="h-20 object-contain" />
              <h1 className="text-2xl font-extrabold text-foreground">Welcome back!</h1>
              <p className="text-sm text-muted-foreground">Sign in to your account</p>
            </div>

            {error && (
              <div className="flex items-start gap-2 text-sm text-destructive bg-destructive/10 rounded-2xl px-4 py-3">
                <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

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
                <button type="button" onClick={() => setShow(s => !s)} aria-label="Toggle password"
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
              {googleClientId ? (
                <GoogleLogin
                  onSuccess={handleGoogle}
                  onError={() => setError("Google sign-in failed. Check the console.")}
                  theme="outline"
                  shape="pill"
                  size="large"
                  text="signin_with"
                  width={360}
                />
              ) : (
                <div className="w-full rounded-2xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-center text-sm font-bold text-destructive">
                  Google sign-in is missing VITE_GOOGLE_CLIENT_ID.
                </div>
              )}
            </div>

            <p className="text-sm text-center text-muted-foreground">
              Don't have an account?{" "}
              <Link to="/register" className="font-bold" style={{ color: "#B180FC" }}>Register</Link>
            </p>
          </form>

          <p className="text-center text-sm">
            <button
              type="button"
              onClick={handleGuest}
              className="font-bold underline-offset-2 hover:underline"
              style={{ color: "#B180FC" }}
            >
              Continue as guest →
            </button>
          </p>
        </div>
      </div>

      {/* ── Right: purple brand panel with walking penguin video ── */}
      <div
        className="hidden lg:flex flex-col items-center justify-center gap-8 p-12 order-1 lg:order-2 relative overflow-hidden"
        style={{ background: "#B180FC" }}
      >
        {/* Decorative blobs */}
        <div className="absolute top-0 right-0 w-64 h-64 rounded-full opacity-20"
          style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(30%, -30%)" }} />
        <div className="absolute bottom-0 left-0 w-48 h-48 rounded-full opacity-15"
          style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(-30%, 30%)" }} />
        <div className="absolute top-1/2 left-0 w-32 h-32 rounded-full opacity-10"
          style={{ background: "radial-gradient(circle, #fff 0%, transparent 70%)", transform: "translate(-50%, -50%)" }} />


        {/* Video in decorative frame */}
        <div className="relative flex items-center justify-center">
          {/* Outer glow ring */}
          <div className="absolute inset-0 rounded-[40px] blur-2xl opacity-40 scale-105"
            style={{ background: "rgba(255,255,255,0.5)" }} />

          {/* Rotating dashed ring */}
          <div className="absolute inset-0 rounded-[44px] border-4 border-dashed border-white/30 scale-110
            animate-spin" style={{ animationDuration: "20s" }} />

          {/* Dot decorations */}
          <div className="absolute -top-5 -right-5 h-10 w-10 rounded-full bg-white/30 backdrop-blur-sm" />
          <div className="absolute -bottom-3 -left-7 h-7 w-7 rounded-full bg-white/20" />
          <div className="absolute top-1/4 -left-8 h-5 w-5 rounded-full bg-yellow-200/60" />
          <div className="absolute bottom-1/4 -right-6 h-6 w-6 rounded-full bg-white/25" />

          {/* Video card */}
          <div className="relative rounded-[32px] overflow-hidden shadow-2xl"
            style={{
              width: 260, height: 320,
              boxShadow: "0 0 0 3px rgba(255,255,255,0.5), 0 0 0 8px rgba(255,255,255,0.15), 0 30px 60px rgba(0,0,0,0.25)",
            }}>
            <video
              src="/penguin-walking.mp4"
              autoPlay muted loop playsInline
              className="w-full h-full object-cover"
            />
            {/* Shimmer overlay at top */}
            <div className="absolute inset-x-0 top-0 h-12"
              style={{ background: "linear-gradient(to bottom, rgba(177,128,252,0.4), transparent)" }} />
            {/* Gradient fade at bottom */}
            <div className="absolute inset-x-0 bottom-0 h-16"
              style={{ background: "linear-gradient(to top, rgba(177,128,252,0.7), transparent)" }} />
          </div>
        </div>

        {/* Text */}
        <div className="text-center space-y-2 relative z-10">
          <h2 className="text-3xl font-extrabold text-white leading-tight">
            Your next stay,<br />
            <span className="text-yellow-200">made happy.</span>
          </h2>
          <p className="text-white/75 text-sm max-w-xs mx-auto">
            Sign in to keep waddling toward paradise.
          </p>
        </div>
      </div>

    </div>
  );
};

export default Login;
