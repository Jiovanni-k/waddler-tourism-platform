import { Link, NavLink, useLocation } from "react-router-dom";
import {
  Bell,
  Hotel,
  Search,
  Heart,
  Mail,
  Award,
  User,
  Sparkles,
  LogOut,
  LayoutDashboard,
  BedDouble,
  CalendarDays,
  ListChecks,
  TicketCheck,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import logo from "@/assets/logo.png";

const navItems = [
  { to: "/guest/hotels",       label: "Hotels",       icon: Hotel },
  { to: "/guest/availability", label: "Availability", icon: Search },
  { to: "/guest/contact",      label: "Support",      icon: Mail },
];

const userNavItems = [
  { to: "/user/hotels",        label: "Hotels",       icon: Hotel },
  { to: "/user/availability",  label: "Availability", icon: Search },
  { to: "/user/bookings",      label: "My Bookings",  icon: Heart },
  { to: "/user/reservations",  label: "Reservations", icon: TicketCheck },
  { to: "/user/loyalty",       label: "Loyalty",      icon: Award },
  { to: "/user/notifications", label: "Inbox",        icon: Bell },
  { to: "/user/contact",       label: "Support",      icon: Mail },
];

const managerNavItems = [
  { to: "/manager",        label: "Dashboard",    icon: LayoutDashboard },
  { to: "/manager/rooms",  label: "Manage Rooms", icon: BedDouble },
  // TODO: confirm route for "All Bookings"
  { to: "/manager/bookings", label: "All Bookings", icon: ListChecks },
  { to: "/manager/events", label: "Manage Events", icon: CalendarDays },
  { to: "/guest/hotels",   label: "Browse Hotels", icon: Hotel },
];

/* ── Top-nav layout shared by all pages ─────────────────────────────── */
export const Layout = ({ children }: { children: React.ReactNode }) => {
  const { user, logout } = useAuth();
  const { pathname } = useLocation();

  const isManagerRoute = pathname.startsWith("/manager");
  const isUserRoute = pathname.startsWith("/user");
  const visibleNavItems = isManagerRoute ? managerNavItems : isUserRoute ? userNavItems : navItems;
  const logoTarget = isManagerRoute ? "/manager" : isUserRoute ? "/user/bookings" : "/";

  return (
    <div className="min-h-screen bg-muted/40">
      <header className="bg-background sticky top-0 z-40 shadow-soft">
        <div className="container flex items-center justify-between h-20">
          {/* Logo */}
          <Link to={logoTarget} className="flex items-center">
            <img src={logo} alt="Waddler" className="h-10 object-contain" />
          </Link>

          {/* Desktop nav */}
          <nav className="hidden lg:flex items-center gap-1 text-sm">
            {visibleNavItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  `flex items-center gap-2 px-3 2xl:px-4 py-2 rounded-full font-semibold whitespace-nowrap transition-colors ${
                    isActive
                      ? "bg-secondary text-primary font-bold"
                      : "text-muted-foreground hover:text-primary hover:bg-secondary/60"
                  }`
                }
              >
                <Icon className="h-4 w-4" /> {label}
              </NavLink>
            ))}
          </nav>

          {/* Auth actions */}
          <div className="flex items-center gap-2">
            {user ? (
              <>
                {!isManagerRoute && !isUserRoute && (
                  <NavLink
                    to="/user/notifications"
                    className={({ isActive }) =>
                      `h-10 w-10 rounded-full grid place-items-center relative transition-all duration-300 ${
                        isActive
                          ? "bg-primary text-primary-foreground shadow-glow -translate-y-0.5"
                          : "bg-muted text-primary hover:bg-secondary"
                      }`
                    }
                  >
                    <Bell className="h-4 w-4" />
                    <span className="absolute top-2 right-2 h-2 w-2 rounded-full bg-accent" />
                  </NavLink>
                )}

                {!isManagerRoute && (
                  <NavLink
                    to="/user/profile"
                    aria-label="Profile"
                    title="Profile"
                    className={({ isActive }) =>
                      `h-10 w-10 rounded-full grid place-items-center transition-all duration-300 ${
                        isActive
                          ? "bg-primary text-primary-foreground shadow-glow -translate-y-0.5"
                          : "bg-muted text-primary hover:bg-secondary"
                      }`
                    }
                  >
                    <User className="h-4 w-4" />
                  </NavLink>
                )}

                <button
                  type="button"
                  onClick={logout}
                  aria-label="Sign out"
                  title="Sign out"
                  className="h-10 w-10 rounded-full bg-muted grid place-items-center text-muted-foreground hover:text-primary hover:bg-secondary transition-colors"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-sm font-semibold text-primary hover:text-primary/80 px-4 py-2 transition-colors"
                >
                  Sign in
                </Link>
                <Link
                  to="/register"
                  className="h-10 px-5 rounded-full bg-primary text-primary-foreground text-sm font-bold inline-flex items-center hover:opacity-90 transition-opacity"
                >
                  Register
                </Link>
              </>
            )}
          </div>
        </div>

        {/* Mobile horizontal nav */}
        <div className="lg:hidden border-t border-border overflow-x-auto">
          <div className="container flex gap-1 py-2">
            {visibleNavItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  `flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-colors ${
                    isActive ? "bg-secondary text-primary" : "text-muted-foreground hover:text-primary"
                  }`
                }
              >
                <Icon className="h-3 w-3" /> {label}
              </NavLink>
            ))}
          </div>
        </div>
      </header>

      <main className="pb-16">{children}</main>
    </div>
  );
};

/* ── Gradient hero banner used at the top of most pages ─────────────────── */
export const PageHeader = ({
  eyebrow,
  title,
  subtitle,
  penguin,
  children,
}: {
  eyebrow?: string;
  title: string;
  subtitle?: string;
  penguin?: string;
  children?: React.ReactNode;
}) => (
  <section className="container pt-10 pb-6">
    <div className="rounded-3xl bg-gradient-hero p-8 md:p-10 grid md:grid-cols-[1fr_auto] gap-6 items-center overflow-hidden shadow-soft">
      <div className="space-y-3">
        {eyebrow && (
          <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-primary bg-card px-3 py-1.5 rounded-full">
            <Sparkles className="h-3 w-3" /> {eyebrow}
          </span>
        )}
        <h1 className="text-3xl md:text-5xl font-bold text-primary-deep leading-tight">{title}</h1>
        {subtitle && <p className="text-muted-foreground max-w-xl">{subtitle}</p>}
        {children}
      </div>
      {penguin && (
        <img
          src={penguin}
          alt=""
          className="w-40 md:w-56 drop-shadow-xl justify-self-end"
          style={{ animation: "float 4s ease-in-out infinite" }}
        />
      )}
    </div>
  </section>
);

interface AppShellProps {
  children: React.ReactNode;
  title?: string;
  eyebrow?: string;
  subtitle?: string;
  penguin?: string;
}

export const AppShell = ({ children, title, eyebrow, subtitle, penguin }: AppShellProps) => {
  const { pathname } = useLocation();
  const isManagerRoute = pathname.startsWith("/manager");

  return (
    <Layout>
      {/* Remove the "dashboard up top" hero on manager routes */}
      {!isManagerRoute && title && (
        <PageHeader eyebrow={eyebrow} title={title} subtitle={subtitle} penguin={penguin} />
      )}

      <div className="container pb-10">{children}</div>
    </Layout>
  );
};

export default AppShell;
