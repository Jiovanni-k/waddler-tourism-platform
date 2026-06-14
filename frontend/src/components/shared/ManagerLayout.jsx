import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { LayoutDashboard, BedDouble, CalendarDays, Search, BookOpen, User, LogOut } from "lucide-react";
import wavingPenguin from "../../assets/penguins/Waving.png";

const navItems = [
  { to: "/manager",          Icon: LayoutDashboard, label: "Dashboard",     end: true },
  { to: "/manager/rooms",    Icon: BedDouble,        label: "Manage Rooms"  },
  { to: "/manager/bookings", Icon: BookOpen,         label: "All Bookings"  },
  { to: "/manager/events",   Icon: CalendarDays,     label: "Manage Events" },
  { to: "/guest/hotels",     Icon: Search,           label: "Browse Hotels" },
];

const ManagerLayout = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate("/login"); };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink to="/manager" className="sidebar-logo">
          <img src={wavingPenguin} alt="" style={{ height: 32, width: "auto" }} />
          <span>Waddler</span>
        </NavLink>

        <div className="sidebar-section-label">Manager</div>

        {navItems.map(({ to, Icon, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) => "sidebar-link" + (isActive ? " active" : "")}
          >
            <span className="icon"><Icon size={16} /></span>
            {label}
          </NavLink>
        ))}

        <div className="sidebar-divider" />
        <div className="sidebar-section-label">Account</div>

        <div className="sidebar-link" style={{ pointerEvents: "none", opacity: 0.8 }}>
          <span className="icon"><User size={16} /></span>
          {user?.firstName || user?.username || "Manager"}
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-divider" />
          <button
            onClick={handleLogout}
            className="sidebar-link"
            style={{ background: "none", border: "none", cursor: "pointer", width: "100%", color: "#FF4B4B", fontFamily: "inherit" }}
          >
            <span className="icon"><LogOut size={16} /></span>
            Log out
          </button>
        </div>
      </aside>

      <main className="page-content">{children}</main>
    </div>
  );
};

export default ManagerLayout;
