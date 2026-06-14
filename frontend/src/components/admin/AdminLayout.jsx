import { useState, useEffect } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  ShieldCheck,
  Users,
  CalendarCheck,
  CreditCard,
  MessageSquare,
  ShieldAlert,
  Bell,
  LogOut
} from "lucide-react";
import logo from "../../assets/logo.png";
import { useAuth } from "../../context/AuthContext";
import { adminGetContactRequests } from "../../api/adminApi";
import { listFromApiData } from "../../lib/apiResponse";

const AdminLayout = ({ children }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [counts, setCounts] = useState({ support: 0 });

  useEffect(() => {
    const fetchCounts = async () => {
      try {
        const contactsRes = await adminGetContactRequests();
        const supportRequests = listFromApiData(contactsRes.data);
        setCounts({
          support: supportRequests.length
        });
      } catch (err) {
        console.error("Failed to fetch admin counts:", err);
      }
    };
    fetchCounts();
  }, [location.pathname]);

  const navItems = [
    { to: "/admin",          label: "Dashboard", icon: LayoutDashboard },
    { to: "/admin/licenses", label: "Licenses",  icon: ShieldCheck },
    { to: "/admin/users",    label: "Users",     icon: Users },
    { to: "/admin/bookings", label: "Bookings",  icon: CalendarCheck },
    { to: "/admin/payments", label: "Payments",  icon: CreditCard },
    { to: "/admin/contacts", label: "Support",   icon: MessageSquare, badge: counts.support },
    { to: "/admin/policies", label: "Policies",  icon: ShieldAlert },
  ];

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-muted/40 font-sans">
      {/* ── Unified Header ── */}
      <header className="bg-background sticky top-0 z-40 shadow-soft">
        <div className="container flex items-center justify-between gap-4 h-20">
          <div className="flex items-center gap-6 min-w-0 flex-1">
            {/* Logo */}
            <Link to="/admin" className="flex items-center shrink-0">
              <img src={logo} alt="Waddler" className="h-10 object-contain" />
            </Link>

            {/* Desktop Nav */}
            <nav className="hidden xl:flex items-center gap-1 min-w-0">
              {navItems.map(({ to, label, icon: Icon, badge }) => (
                <NavLink
                  key={to}
                  to={to}
                  end={to === "/admin"}
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 2xl:px-4 py-2 rounded-full text-sm font-bold whitespace-nowrap transition-all ${
                      isActive
                        ? "bg-secondary text-primary shadow-sm"
                        : "text-muted-foreground hover:text-primary hover:bg-secondary/50"
                    }`
                  }
                >
                  <Icon className="h-4.5 w-4.5" />
                  <span>{label}</span>
                  {badge > 0 && (
                    <span className="ml-1 bg-accent text-accent-foreground text-[10px] px-1.5 py-0.5 rounded-full font-black">
                      {badge}
                    </span>
                  )}
                </NavLink>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <Link
              to="/admin/notifications"
              aria-label="Notifications"
              title="Notifications"
              className="h-10 w-10 rounded-full bg-muted grid place-items-center text-primary relative hover:bg-secondary transition-colors"
            >
              <Bell className="h-4 w-4" />
              {counts.support > 0 && (
                <span className="absolute top-2 right-2 h-2 w-2 rounded-full bg-accent" />
              )}
            </Link>

            <div className="flex items-center gap-2 ml-2 pl-4 border-l border-border">
               <Link
                to="/admin/profile"
                aria-label="Admin profile"
                title="Admin profile"
                className="h-10 w-10 rounded-full bg-primary text-primary-foreground grid place-items-center font-black text-sm hover:opacity-90 hover:-translate-y-0.5 transition-all"
               >
                 {user?.firstName?.charAt(0) || "A"}
               </Link>
               <button 
                onClick={handleLogout}
                className="p-2 text-muted-foreground hover:text-destructive transition-colors"
               >
                 <LogOut className="h-5 w-5" />
               </button>
            </div>
          </div>
        </div>

        {/* Mobile Nav */}
        <div className="xl:hidden border-t border-border overflow-x-auto bg-card/50 backdrop-blur-md">
          <div className="container flex gap-1 py-2">
            {navItems.map(({ to, label, icon: Icon, badge }) => (
              <NavLink
                key={to}
                to={to}
                end={to === "/admin"}
                className={({ isActive }) =>
                  `flex items-center gap-2 px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
                    isActive ? "bg-secondary text-primary" : "text-muted-foreground"
                  }`
                }
              >
                <Icon className="h-3.5 w-3.5" />
                {label}
                {badge > 0 && (
                   <span className="ml-1 bg-accent text-accent-foreground text-[9px] px-1.5 py-0.5 rounded-full">
                    {badge}
                  </span>
                )}
              </NavLink>
            ))}
          </div>
        </div>
      </header>

      {/* ── Main Content ── */}
      <main className="container pt-8 pb-16">
        <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
          {children}
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;
