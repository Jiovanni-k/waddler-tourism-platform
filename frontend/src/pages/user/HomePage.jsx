import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { getHotels } from "../../api/hotelApi";
import Navbar from "../../components/shared/Navbar";
import travelPenguin from "../../assets/penguins/Travel_Mode_Walking.png";
import celebrationPenguin from "../../assets/penguins/Celebration.png";

const StarRating = ({ rating }) => {
  if (!rating) return null;
  return <span style={{ color: "#F2AE2E", fontWeight: 800 }}>{"★".repeat(rating)}{"☆".repeat(5 - rating)}</span>;
};

const HotelPublicCard = ({ hotel, onBook }) => {
  const fallback = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&q=80";
  return (
    <div
      style={{
        background: "#fff",
        borderRadius: 26,
        border: "2.5px solid #D8CFF2",
        boxShadow: "0 5px 0 #C3AEED",
        overflow: "hidden",
        display: "flex",
        flexDirection: "column",
        transition: "transform 0.18s, box-shadow 0.18s",
        cursor: "pointer",
      }}
      onMouseEnter={(e) => { e.currentTarget.style.transform = "translateY(-5px)"; e.currentTarget.style.boxShadow = "0 10px 0 #C3AEED"; }}
      onMouseLeave={(e) => { e.currentTarget.style.transform = ""; e.currentTarget.style.boxShadow = "0 5px 0 #C3AEED"; }}
    >
      {/* Image */}
      <div style={{ position: "relative", height: 200, overflow: "hidden", background: "#EAE3FA" }}>
        <img
          src={hotel.coverImageUrl || fallback}
          alt={hotel.name}
          style={{ width: "100%", height: "100%", objectFit: "cover" }}
          loading="lazy"
        />
        {hotel.starRating && (
          <span style={{
            position: "absolute", top: 12, left: 12,
            background: "rgba(41,25,64,0.88)", color: "#F2AE2E",
            padding: "4px 12px", borderRadius: 999, fontSize: 13, fontWeight: 900,
            backdropFilter: "blur(6px)", border: "1px solid rgba(242,174,46,0.3)",
          }}>
            {"★".repeat(hotel.starRating)}
          </span>
        )}
      </div>

      {/* Body */}
      <div style={{ padding: "18px 20px 20px", flex: 1, display: "flex", flexDirection: "column", gap: 6 }}>
        <div style={{ fontFamily: "'Nunito', sans-serif", fontWeight: 900, fontSize: 19, color: "#291940", letterSpacing: -0.2 }}>
          {hotel.name}
        </div>
        {(hotel.city || hotel.region) && (
          <div style={{ fontSize: 13, fontWeight: 700, color: "#5C4A82" }}>
            {[hotel.city, hotel.region].filter(Boolean).join(", ")}
          </div>
        )}
        {hotel.averageGuestRating != null && (
          <div style={{ fontSize: 14, fontWeight: 800, color: "#291940" }}>
            {Number(hotel.averageGuestRating).toFixed(1)} guest rating
          </div>
        )}

        <button
          onClick={() => onBook(hotel.id)}
          style={{
            marginTop: "auto",
            paddingTop: 14,
            width: "100%",
            padding: "13px",
            background: "linear-gradient(135deg, #A480F2, #8B5CF6)",
            color: "#fff",
            border: "none",
            borderRadius: 999,
            fontFamily: "'Nunito', sans-serif",
            fontWeight: 900,
            fontSize: 15,
            cursor: "pointer",
            boxShadow: "0 5px 0 #6D3FCC",
            transition: "transform 0.1s, box-shadow 0.1s",
          }}
          onMouseDown={(e) => { e.currentTarget.style.transform = "translateY(4px)"; e.currentTarget.style.boxShadow = "none"; }}
          onMouseUp={(e) => { e.currentTarget.style.transform = ""; e.currentTarget.style.boxShadow = "0 5px 0 #6D3FCC"; }}
        >
          Book this hotel →
        </button>
      </div>
    </div>
  );
};

const HomePage = () => {
  const { user } = useAuth();
  const navigate  = useNavigate();
  const [hotels, setHotels]   = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getHotels({ page: 0, size: 6, sortBy: "averageGuestRating", sortDir: "desc" })
      .then((res) => setHotels(res.data?.content ?? res.data ?? []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleBook = (hotelId) => {
    if (user) {
      navigate(`/user/hotel/${hotelId}`);
    } else {
      navigate("/login");
    }
  };

  return (
    <div style={{ minHeight: "100vh", background: "#F0EDF9" }}>
      <Navbar />

      {/* Hero */}
      <section style={{
        background: "linear-gradient(145deg, #1E0E35 0%, #291940 55%, #3D2265 100%)",
        padding: "80px 40px 100px",
        textAlign: "center",
        position: "relative",
        overflow: "hidden",
      }}>
        {/* blobs */}
        <div style={{ position: "absolute", width: 600, height: 600, borderRadius: "50%", background: "radial-gradient(circle, rgba(164,128,242,0.2) 0%, transparent 70%)", top: -200, right: -100, pointerEvents: "none" }} />
        <div style={{ position: "absolute", width: 400, height: 400, borderRadius: "50%", background: "radial-gradient(circle, rgba(242,174,46,0.1) 0%, transparent 70%)", bottom: -150, left: -50, pointerEvents: "none" }} />

        <div style={{ position: "relative", zIndex: 1, maxWidth: 700, margin: "0 auto" }}>
          <div style={{ marginBottom: 8, animation: "wobble 3s ease-in-out infinite", display: "inline-block" }}>
            <img src={travelPenguin} alt="Waddler" style={{ height: 120, width: "auto", filter: "drop-shadow(0 8px 24px rgba(164,128,242,0.4))" }} />
          </div>
          <h1 style={{
            fontFamily: "'Nunito', sans-serif",
            fontWeight: 900,
            fontSize: "clamp(36px, 6vw, 62px)",
            color: "#fff",
            lineHeight: 1.1,
            letterSpacing: -1,
            marginBottom: 16,
          }}>
            Discover your next<br />
            <span style={{ background: "linear-gradient(135deg, #A480F2, #D6C2F2)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
              great adventure
            </span>
          </h1>
          <p style={{ color: "rgba(255,255,255,0.7)", fontWeight: 700, fontSize: 18, marginBottom: 36 }}>
            Browse top-rated hotels across Palestine — book with confidence.
          </p>
          <div style={{ display: "flex", gap: 14, justifyContent: "center", flexWrap: "wrap" }}>
            <a
              href="#hotels"
              style={{
                background: "linear-gradient(135deg, #A480F2, #8B5CF6)",
                color: "#fff",
                padding: "14px 32px",
                borderRadius: 999,
                fontFamily: "'Nunito', sans-serif",
                fontWeight: 900,
                fontSize: 16,
                textDecoration: "none",
                boxShadow: "0 5px 0 #6D3FCC",
                display: "inline-block",
              }}
            >
              Browse Hotels
            </a>
            {!user && (
              <Link
                to="/login"
                style={{
                  background: "rgba(255,255,255,0.12)",
                  color: "#fff",
                  padding: "14px 32px",
                  borderRadius: 999,
                  fontFamily: "'Nunito', sans-serif",
                  fontWeight: 900,
                  fontSize: 16,
                  textDecoration: "none",
                  border: "2px solid rgba(255,255,255,0.25)",
                  display: "inline-block",
                }}
              >
                Sign in
              </Link>
            )}
          </div>
        </div>
      </section>

      {/* How it works */}
      <section style={{ padding: "64px 40px", maxWidth: 1100, margin: "0 auto" }}>
        <h2 style={{ fontFamily: "'Nunito', sans-serif", fontWeight: 900, fontSize: 32, color: "#291940", textAlign: "center", marginBottom: 40, letterSpacing: -0.5 }}>
          How it works
        </h2>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: 24 }}>
          {[
            { step: "1", title: "Browse hotels", desc: "Explore hotels by city, stars, and price." },
            { step: "2", title: "Pick your dates", desc: "Check availability and choose your room." },
            { step: "3", title: "Confirm & pay", desc: "Book securely and get instant confirmation." },
            { step: "4", title: "Earn rewards", desc: "Every stay earns you loyalty points." },
          ].map(({ step, title, desc }) => (
            <div key={step} style={{
              background: "#fff",
              borderRadius: 26,
              border: "2.5px solid #D8CFF2",
              boxShadow: "0 5px 0 #C3AEED",
              padding: "28px 24px",
              textAlign: "center",
            }}>
              <div style={{ fontSize: 28, marginBottom: 12, fontWeight: 900, color: "#A480F2" }}>{step}</div>
              <div style={{ fontFamily: "'Nunito', sans-serif", fontWeight: 900, fontSize: 18, color: "#291940", marginBottom: 8 }}>{title}</div>
              <div style={{ fontSize: 14, fontWeight: 700, color: "#5C4A82" }}>{desc}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Featured Hotels */}
      <section id="hotels" style={{ padding: "0 40px 80px", maxWidth: 1100, margin: "0 auto" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 32 }}>
          <h2 style={{ fontFamily: "'Nunito', sans-serif", fontWeight: 900, fontSize: 32, color: "#291940", letterSpacing: -0.5 }}>
            Featured Hotels
          </h2>
         
            <Link
              to="/user/hotels"
              style={{
                background: "linear-gradient(135deg, #A480F2, #8B5CF6)",
                color: "#fff",
                padding: "10px 22px",
                borderRadius: 999,
                fontFamily: "'Nunito', sans-serif",
                fontWeight: 900,
                fontSize: 14,
                textDecoration: "none",
                boxShadow: "0 4px 0 #6D3FCC",
              }}
            >
              View all →
            </Link>
        
        </div>

        {loading && (
          <div style={{ display: "flex", justifyContent: "center", padding: 60, gap: 16, alignItems: "center", flexDirection: "column", color: "#9B88BF", fontWeight: 800 }}>
            <div style={{ width: 44, height: 44, border: "5px solid #D8CFF2", borderTopColor: "#A480F2", borderRadius: "50%", animation: "spin 0.75s linear infinite" }} />
            Loading hotels…
          </div>
        )}

        {!loading && hotels.length === 0 && (
          <div style={{ textAlign: "center", padding: 60, color: "#9B88BF", fontWeight: 700 }}>
            <div style={{ fontSize: 48, marginBottom: 12, color: "#A480F2" }}>—</div>
            No hotels found right now. Check back soon!
          </div>
        )}

        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: 24 }}>
          {hotels.map((hotel) => (
            <HotelPublicCard key={hotel.id} hotel={hotel} onBook={handleBook} />
          ))}
        </div>

        {!user && hotels.length > 0 && (
          <div style={{
            marginTop: 48,
            background: "linear-gradient(135deg, #291940, #3D2265)",
            borderRadius: 26,
            padding: "40px 32px",
            textAlign: "center",
            boxShadow: "0 5px 0 #14082A",
            border: "2px solid rgba(164,128,242,0.3)",
          }}>
            <div style={{ marginBottom: 12 }}>
              <img src={celebrationPenguin} alt="Waddler" style={{ height: 90, width: "auto" }} />
            </div>
            <h3 style={{ fontFamily: "'Nunito', sans-serif", fontWeight: 900, fontSize: 26, color: "#fff", marginBottom: 10, letterSpacing: -0.3 }}>
              Ready to book?
            </h3>
            <p style={{ color: "rgba(255,255,255,0.7)", fontWeight: 700, fontSize: 16, marginBottom: 24 }}>
              Create a free account to book hotels, earn loyalty points, and manage your trips.
            </p>
            <div style={{ display: "flex", gap: 14, justifyContent: "center" }}>
              <Link to="/register" style={{
                background: "linear-gradient(135deg, #F2AE2E, #F7C85A)",
                color: "#291940",
                padding: "14px 32px",
                borderRadius: 999,
                fontFamily: "'Nunito', sans-serif",
                fontWeight: 900,
                fontSize: 16,
                textDecoration: "none",
                boxShadow: "0 5px 0 #BF8000",
              }}>
                Create account
              </Link>
              <Link to="/login" style={{
                background: "rgba(255,255,255,0.12)",
                color: "#fff",
                padding: "14px 32px",
                borderRadius: 999,
                fontFamily: "'Nunito', sans-serif",
                fontWeight: 900,
                fontSize: 16,
                textDecoration: "none",
                border: "2px solid rgba(255,255,255,0.25)",
              }}>
                Sign in
              </Link>
            </div>
          </div>
        )}
      </section>

      <style>{`
        @keyframes wobble { 0%,100%{transform:rotate(-4deg) scale(1)} 50%{transform:rotate(4deg) scale(1.05)} }
        @keyframes spin { to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
};

export default HomePage;
