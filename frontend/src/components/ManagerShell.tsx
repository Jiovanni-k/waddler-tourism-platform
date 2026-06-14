import { Link, NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  BedDouble,
  ListChecks,
  CalendarDays,
  Hotel,
  User,
  LogOut,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import logo from "@/assets/logo.png";

const managerNavItems = [
  { to: "/manager",          label: "Dashboard",    icon: LayoutDashboard },
  { to: "/manager/rooms",    label: "Manage Rooms", icon: BedDouble },
  { to: "/manager/bookings", label: "All Bookings", icon: ListChecks },
  { to: "/manager/events",   label: "Manage Events", icon: CalendarDays },
];

export default function ManagerShell({ children }: { children: React.ReactNode }) {
  const { logout } = useAuth();

  return (
    <div className="min-h-screen bg-muted/40">
      {/* Green navbar moved to the very top */}
      <header className="sticky top-0 z-40 bg-background shadow-soft">
        <div className="container h-20 flex items-center justify-between gap-4">
          {/* Left: logo */}
          <Link to="/manager" className="flex items-center gap-2 shrink-0">
            <img src={logo} alt="Waddler" className="h-10 object-contain" />
          </Link>

          {/* Center: manager nav pills */}
          <nav className="hidden lg:flex items-center gap-2 flex-1 justify-center">
            {managerNavItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                end={to === "/manager"}
                className={({ isActive }) =>
                  [
                    "inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-semibold transition-colors",
                    isActive
                      ? "bg-secondary text-primary font-extrabold"
                      : "text-muted-foreground hover:text-primary hover:bg-secondary/60",
                  ].join(" ")
                }
              >
                <Icon className="h-4 w-4" />
                {label}
              </NavLink>
            ))}
          </nav>

          {/* Right: profile + logout */}
          <div className="flex items-center gap-2 shrink-0">
            <Link
              to="/manager/profile"
              className="h-10 w-10 rounded-full bg-primary text-primary-foreground grid place-items-center hover:opacity-90 transition-opacity"
              aria-label="Profile"
              title="Manager profile"
            >
              <User className="h-4 w-4" />
            </Link>

            <button
              type="button"
              onClick={logout}
              className="h-10 w-10 rounded-full bg-muted grid place-items-center text-muted-foreground hover:text-primary hover:bg-secondary transition-colors"
              aria-label="Sign out"
              title="Sign out"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Mobile manager nav */}
        <div className="lg:hidden border-t border-border overflow-x-auto">
          <div className="container flex gap-2 py-2">
            {managerNavItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                end={to === "/manager"}
                className={({ isActive }) =>
                  [
                    "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-colors",
                    isActive
                      ? "bg-secondary text-primary"
                      : "text-muted-foreground hover:text-primary hover:bg-secondary/60",
                  ].join(" ")
                }
              >
                <Icon className="h-3 w-3" />
                {label}
              </NavLink>
            ))}
          </div>
        </div>
      </header>

      <main className="pb-16">
        <div className="container py-8">{children}</div>
      </main>
    </div>
  );
}
