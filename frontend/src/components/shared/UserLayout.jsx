import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import wavingPenguin from "../../assets/penguins/Waving.png";

const navItems = [
  { to: "/user/bookings", icon: "Bookings", label: "My Bookings" },
  { to: "/user/loyalty", icon: "Loyalty", label: "Loyalty" },
];

const UserLayout = ({ children }) => {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink to="/user/bookings" className="sidebar-logo">
          <img src={wavingPenguin} alt="" style={{ height: 32, width: "auto" }} />
          <span>Waddler</span>
        </NavLink>

        <div className="sidebar-section-label">My Account</div>

        {navItems.map(({ to, icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === "/user/bookings"}
            className={({ isActive }) => "sidebar-link" + (isActive ? " active" : "")}
          >
            <span className="icon">{icon}</span>
            {label}
          </NavLink>
        ))}

        <div className="sidebar-bottom">
          <div className="sidebar-divider" />
          <button
            onClick={handleLogout}
            className="sidebar-link"
            style={{
              background: "none",
              border: "none",
              cursor: "pointer",
              width: "100%",
              color: "#FF4B4B",
              fontFamily: "inherit",
            }}
          >
            <span className="icon">Exit</span>
            Log out
          </button>
        </div>
      </aside>

      <main className="page-content">{children}</main>
    </div>
  );
};

export default UserLayout;
