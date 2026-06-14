import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { getHotels } from "../../api/hotelApi";
import HotelCard      from "../../components/shared/HotelCard";
import LoadingSpinner from "../../components/shared/LoadingSpinner";
import ErrorMessage   from "../../components/shared/ErrorMessage";

const HomePage = () => {
  const [hotels,  setHotels]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    const fetchHotels = async () => {
      try {
        const res = await getHotels({
          page:    0,
          size:    6,
          sortBy:  "averageGuestRating",
          sortDir: "desc",
        });
        // Backend returns a Page object — content is the array
        const data = res.data;
        setHotels(data.content || data || []);
      } catch (err) {
        setError("Failed to load hotels. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    fetchHotels();
  }, []);

  return (
    <div className="home-page">

      {/* ── Hero ─────────────────────────────────────────────────────── */}
      <section className="hero">
        <div className="hero-content">
          <h1 className="hero-title">Find Your Perfect Stay</h1>
          <p className="hero-subtitle">
            Discover handpicked hotels and book with confidence.
          </p>
          <Link to="/guest/hotels" className="btn btn-primary btn-lg">
            Browse Hotels →
          </Link>
        </div>
      </section>

      {/* ── Featured Hotels ──────────────────────────────────────────── */}
      <section className="featured-section">
        <div className="section-header">
          <h2 className="section-title">Featured Hotels</h2>
          <Link to="/guest/hotels" className="section-link">View all →</Link>
        </div>

        {loading && <LoadingSpinner message="Loading hotels..." />}
        {error   && <ErrorMessage message={error} />}

        {!loading && !error && (
          <div className="hotels-grid">
            {hotels.map((hotel) => (
              <HotelCard key={hotel.id} hotel={hotel} />
            ))}
          </div>
        )}
      </section>

    </div>
  );
};

export default HomePage;
