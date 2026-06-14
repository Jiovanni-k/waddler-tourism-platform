import { Link } from "react-router-dom";
import { MapPin, Star } from "lucide-react";

// Matches real backend HotelResponseDto.SummaryResponse fields:
// id, name, city, region, starRating, averageGuestRating, coverImageUrl, lowestRoomPrice

const HotelCard = ({ hotel }) => {
  const {
    id,
    name,
    city,
    region,
    starRating,
    averageGuestRating,
    coverImageUrl,
  } = hotel;

  const location = [city, region].filter(Boolean).join(", ");
  const stars    = "★".repeat(starRating || 0);
  const fallback = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&q=80";

  return (
    <Link to={`/guest/hotel/${id}`} className="hotel-card">
      <div className="hotel-card-image">
        <img
          src={coverImageUrl || fallback}
          alt={name}
          loading="lazy"
        />
        {starRating && <span className="hotel-card-stars">{stars}</span>}
      </div>

      <div className="hotel-card-body">
        <h3 className="hotel-card-name">{name}</h3>

        {location && (
          <p className="hotel-card-location" style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <MapPin size={13} /> {location}
          </p>
        )}

        {averageGuestRating != null && (
          <p className="hotel-card-rating" style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <Star size={13} style={{ fill: "currentColor" }} /> {Number(averageGuestRating).toFixed(1)}
          </p>
        )}

        <div className="hotel-card-footer">
          <span className="hotel-card-cta">View →</span>
        </div>
      </div>
    </Link>
  );
};

export default HotelCard;
