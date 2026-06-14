import { useState, useCallback } from "react";
import { GoogleMap, Marker, useLoadScript } from "@react-google-maps/api";

const MAP_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

const mapContainerStyle = { width: "100%", height: "100%", borderRadius: 16 };

const mapOptions = {
  disableDefaultUI: false,
  zoomControl: true,
  streetViewControl: false,
  mapTypeControl: false,
  styles: [
    { featureType: "all", elementType: "geometry", stylers: [{ saturation: -20 }] },
    { featureType: "water", elementType: "geometry", stylers: [{ color: "#C3AEED" }] },
    { featureType: "road", elementType: "geometry", stylers: [{ color: "#EAE3FA" }] },
  ],
};

// Haversine formula — returns distance in km
const haversine = (lat1, lon1, lat2, lon2) => {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
};

const HotelMap = ({ lat, lng, name }) => {
  const { isLoaded, loadError } = useLoadScript({ googleMapsApiKey: MAP_API_KEY });
  const [distance, setDistance] = useState(null);
  const [locating, setLocating] = useState(false);
  const [userPos, setUserPos] = useState(null);

  const center = { lat: Number(lat), lng: Number(lng) };

  const handleFindDistance = useCallback(() => {
    if (!navigator.geolocation) return;
    setLocating(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        setUserPos({ lat: latitude, lng: longitude });
        setDistance(haversine(latitude, longitude, Number(lat), Number(lng)));
        setLocating(false);
      },
      () => setLocating(false)
    );
  }, [lat, lng]);

  if (loadError) return (
    <div style={{ padding: 24, textAlign: "center", color: "#9B88BF", fontWeight: 700 }}>
      Map unavailable
    </div>
  );

  if (!isLoaded) return (
    <div style={{ height: 320, display: "flex", alignItems: "center", justifyContent: "center", color: "#9B88BF", fontWeight: 700 }}>
      <div style={{ width: 32, height: 32, border: "4px solid #D8CFF2", borderTopColor: "#A480F2", borderRadius: "50%", animation: "spin 0.75s linear infinite", marginRight: 12 }} />
      Loading map…
    </div>
  );

  return (
    <div>
      <div style={{ height: 320, borderRadius: 16, overflow: "hidden", border: "2px solid #D8CFF2" }}>
        <GoogleMap
          mapContainerStyle={mapContainerStyle}
          center={center}
          zoom={15}
          options={mapOptions}
        >
          <Marker
            position={center}
            title={name}
          />
          {userPos && <Marker position={userPos} label="You" />}
        </GoogleMap>
      </div>

      <div style={{ marginTop: 14, display: "flex", alignItems: "center", gap: 14, flexWrap: "wrap" }}>
        <button
          onClick={handleFindDistance}
          disabled={locating}
          style={{
            background: "linear-gradient(135deg, #A480F2, #8B5CF6)",
            color: "#fff",
            border: "none",
            borderRadius: 999,
            padding: "10px 22px",
            fontFamily: "'Nunito', sans-serif",
            fontWeight: 900,
            fontSize: 14,
            cursor: "pointer",
            boxShadow: "0 4px 0 #6D3FCC",
            display: "flex",
            alignItems: "center",
            gap: 8,
          }}
        >
          📏 {locating ? "Locating…" : "How far am I?"}
        </button>

        {distance !== null && (
          <div style={{
            background: "#fff",
            border: "2px solid #D8CFF2",
            borderRadius: 999,
            padding: "10px 20px",
            fontFamily: "'Nunito', sans-serif",
            fontWeight: 900,
            fontSize: 14,
            color: "#291940",
            boxShadow: "0 4px 0 #C3AEED",
          }}>
            🗺️ {distance < 1
              ? `${Math.round(distance * 1000)} m away`
              : `${distance.toFixed(1)} km away`}
          </div>
        )}
      </div>
    </div>
  );
};

export default HotelMap;
