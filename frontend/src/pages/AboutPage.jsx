import { Link } from "react-router-dom";
import { Search, CalendarCheck, Building2, Lock } from "lucide-react";

const TEAM = [
  {
    name:   "Zeina Ibrahim",
    role:   "Frontend Developer",
    tasks:  "Auth, Shared Components, Manager Pages",
    emoji:  "👩‍💻",
  },
  {
    name:   "Salma",
    role:   "Frontend Developer",
    tasks:  "Hotel Search, Hotel Detail, Booking Flow",
    emoji:  "👩‍💻",
  },
  {
    name:   "Jiovanni",
    role:   "Frontend Developer",
    tasks:  "Payments, Admin Panel, Notifications",
    emoji:  "👨‍💻",
  },
];

const SOCIALS = [
  { label: "Instagram", icon: "📸", href: "#" },
  { label: "Twitter",   icon: "🐦", href: "#" },
  { label: "Facebook",  icon: "👥", href: "#" },
  { label: "LinkedIn",  icon: "💼", href: "#" },
];

const AboutPage = () => {
  return (
    <div className="about-page">

      {/* ── Banner ─────────────────────────────────────────────────── */}
      <section className="about-hero">
        <div className="about-hero-content">
          <h1 className="about-hero-title">About Waddler Tourism</h1>
          <p className="about-hero-sub">
            Your trusted platform for discovering and booking exceptional hotels
            around the world. We make travel simple, beautiful, and memorable.
          </p>
          <Link to="/guest/hotels" className="btn btn-primary btn-lg">
            Start Exploring →
          </Link>
        </div>
      </section>

      {/* ── What We Do ─────────────────────────────────────────────── */}
      <section className="about-section">
        <div className="about-container">
          <h2 className="about-section-title">What We Do</h2>
          <p className="about-section-text">
            Waddler Tourism is a full-stack hotel booking platform built as part
            of the SWER354 Advanced Web Technologies course. We connect travellers
            with hotels, manage bookings end-to-end, and give hotel managers the
            tools they need to run their properties efficiently.
          </p>

          <div className="about-features">
            <div className="about-feature">
              <span className="about-feature-icon"><Search size={24} /></span>
              <h3>Search & Discover</h3>
              <p>Browse hotels by city, price, rating and amenities.</p>
            </div>
            <div className="about-feature">
              <span className="about-feature-icon"><CalendarCheck size={24} /></span>
              <h3>Easy Booking</h3>
              <p>Book rooms in seconds with instant confirmation.</p>
            </div>
            <div className="about-feature">
              <span className="about-feature-icon"><Building2 size={24} /></span>
              <h3>Manager Tools</h3>
              <p>Hotel managers can list properties, manage rooms and events.</p>
            </div>
            <div className="about-feature">
              <span className="about-feature-icon"><Lock size={24} /></span>
              <h3>Secure Payments</h3>
              <p>Safe and reliable payment processing for every booking.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ── Team ───────────────────────────────────────────────────── */}
      <section className="about-section about-section-alt">
        <div className="about-container">
          <h2 className="about-section-title">Meet the Team</h2>
          <p className="about-section-text">
            Built by three students from An-Najah National University as part of
            SWER354 — Advanced Web Technologies, supervised by Dr. Anas Samara.
          </p>

          <div className="team-grid">
            {TEAM.map((member) => (
              <div key={member.name} className="team-card">
                <div className="team-avatar">{member.emoji}</div>
                <h3 className="team-name">{member.name}</h3>
                <p className="team-role">{member.role}</p>
                <p className="team-tasks">{member.tasks}</p>
              </div>
            ))}
          </div>

          <div className="supervisor-card">
            <span className="supervisor-icon">🎓</span>
            <div>
              <p className="supervisor-name">Dr. Anas Samara</p>
              <p className="supervisor-role">Project Supervisor · SWER354 & SWER313</p>
            </div>
          </div>
        </div>
      </section>

      {/* ── Social Media ───────────────────────────────────────────── */}
      <section className="about-section">
        <div className="about-container">
          <h2 className="about-section-title">Follow Us</h2>
          <p className="about-section-text">
            Stay updated with our latest hotel additions, travel tips and
            exclusive offers.
          </p>
          <div className="socials-grid">
            {SOCIALS.map((s) => (
              <a key={s.label} href={s.href} className="social-card">
                <span className="social-icon">{s.icon}</span>
                <span className="social-label">{s.label}</span>
              </a>
            ))}
          </div>
        </div>
      </section>

    </div>
  );
};

export default AboutPage;
