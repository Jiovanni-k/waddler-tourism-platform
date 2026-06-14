import { useState, useRef, useCallback } from "react";
import { GoogleMap, Marker, Autocomplete, useLoadScript } from "@react-google-maps/api";

const MAP_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
const LIBRARIES = ["places"];

const mapContainerStyle = { width: "100%", height: "100%", borderRadius: 12 };

const mapOptions = {
  zoomControl: true,
  streetViewControl: false,
  mapTypeControl: false,
  styles: [
    { featureType: "water", elementType: "geometry", stylers: [{ color: "#C3AEED" }] },
    { featureType: "road", elementType: "geometry", stylers: [{ color: "#EAE3FA" }] },
  ],
};

const DEFAULT_CENTER = { lat: 31.7683, lng: 35.2137 }; // Palestine

const MapPicker = ({ onLocationSelect }) => {
  const { isLoaded, loadError } = useLoadScript({
    googleMapsApiKey: MAP_API_KEY,
    libraries: LIBRARIES,
  });

  const [marker, setMarker] = useState(null);
  const [mapCenter, setMapCenter] = useState(DEFAULT_CENTER);
  const autocompleteRef = useRef(null);

  const handleMapClick = useCallback((e) => {
    const lat = e.latLng.lat();
    const lng = e.latLng.lng();
    setMarker({ lat, lng });

    // Reverse geocode to get address
    const geocoder = new window.google.maps.Geocoder();
    geocoder.geocode({ location: { lat, lng } }, (results, status) => {
      if (status === "OK" && results[0]) {
        const place = results[0];
        const city = place.address_components.find((c) =>
          c.types.includes("locality"))?.long_name || "";
        const region = place.address_components.find((c) =>
          c.types.includes("administrative_area_level_1"))?.long_name || "";
        onLocationSelect({
          address: place.formatted_address,
          city,
          region,
          latitude: lat,
          longitude: lng,
        });
      }
    });
  }, [onLocationSelect]);

  const handlePlaceChanged = useCallback(() => {
    const place = autocompleteRef.current?.getPlace();
    if (!place?.geometry) return;

    const lat = place.geometry.location.lat();
    const lng = place.geometry.location.lng();
    setMarker({ lat, lng });
    setMapCenter({ lat, lng });

    const city = place.address_components?.find((c) =>
      c.types.includes("locality"))?.long_name || "";
    const region = place.address_components?.find((c) =>
      c.types.includes("administrative_area_level_1"))?.long_name || "";

    onLocationSelect({
      address: place.formatted_address || "",
      city,
      region,
      latitude: lat,
      longitude: lng,
    });
  }, [onLocationSelect]);

  if (loadError) return (
    <div style={{ padding: 20, textAlign: "center", color: "#9B88BF", fontWeight: 700, border: "2px dashed #D8CFF2", borderRadius: 12 }}>
      Map unavailable — fill in address manually
    </div>
  );

  if (!isLoaded) return (
    <div style={{ height: 340, display: "flex", alignItems: "center", justifyContent: "center", background: "#F0EDF9", borderRadius: 12 }}>
      <div style={{ width: 32, height: 32, border: "4px solid #D8CFF2", borderTopColor: "#A480F2", borderRadius: "50%", animation: "spin 0.75s linear infinite", marginRight: 12 }} />
      <span style={{ fontWeight: 700, color: "#9B88BF" }}>Loading map…</span>
    </div>
  );

  return (
    <div>
      {/* Search box */}
      <Autocomplete
        onLoad={(ref) => (autocompleteRef.current = ref)}
        onPlaceChanged={handlePlaceChanged}
      >
        <input
          type="text"
          placeholder="Search for hotel location…"
          style={{
            width: "100%",
            padding: "12px 16px",
            borderRadius: 12,
            border: "2px solid #D8CFF2",
            fontFamily: "'Nunito', sans-serif",
            fontWeight: 700,
            fontSize: 14,
            marginBottom: 10,
            outline: "none",
            boxSizing: "border-box",
            background: "#fff",
          }}
          onFocus={(e) => e.target.style.borderColor = "#A480F2"}
          onBlur={(e) => e.target.style.borderColor = "#D8CFF2"}
        />
      </Autocomplete>

      <div style={{ height: 300, borderRadius: 12, overflow: "hidden", border: "2px solid #D8CFF2" }}>
        <GoogleMap
          mapContainerStyle={mapContainerStyle}
          center={mapCenter}
          zoom={marker ? 15 : 8}
          options={mapOptions}
          onClick={handleMapClick}
        >
          {marker && <Marker position={marker} />}
        </GoogleMap>
      </div>

      <p style={{ marginTop: 8, fontSize: 13, fontWeight: 700, color: "#9B88BF" }}>
        Search above or click on the map to pin your hotel location
      </p>
    </div>
  );
};

export default MapPicker;
