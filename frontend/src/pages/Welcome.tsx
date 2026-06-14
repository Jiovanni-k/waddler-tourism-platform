import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Compass, ArrowRight } from "lucide-react";
import logo from "@/assets/logo.png";
import dest1 from "@/assets/dest-1.jpeg";
import dest2 from "@/assets/dest-2.jpeg";
import dest3 from "@/assets/dest-3.jpeg";
import dest4 from "@/assets/dest-4.jpeg";

const destinations = [
  { src: dest1, label: "Santorini · Greece" },
  { src: dest2, label: "Maldives" },
  { src: dest3, label: "Swiss Alps" },
  { src: dest4, label: "Bali · Indonesia" },
];

const Welcome = () => {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    const id = setInterval(() => {
      setIndex((i) => (i + 1) % destinations.length);
    }, 3500);
    return () => clearInterval(id);
  }, []);

  return (
    <div className="flex h-screen w-screen overflow-hidden">

      {/* ═══════════════ LEFT — #FDFDFD, video ═══════════════ */}
      <aside
        className="flex flex-col overflow-hidden"
        style={{ flex: "0 0 42%", background: "#FDFDFD" }}
      >
        {/* Logo */}
        <div className="shrink-0" style={{ padding: "28px 32px 0" }}>
          <img src={logo} alt="Waddler" style={{ height: 40, objectFit: "contain" }} />
        </div>

        {/* Video — slightly bigger, centered with breathing room */}
        <div className="flex flex-1 items-center justify-center min-h-0" style={{ padding: "8px 16px" }}>
          <video
            src="/intro.mp4"
            autoPlay
            muted
            loop
            playsInline
            style={{
              width: "100%",
              maxHeight: "96%",
              objectFit: "contain",
              display: "block",
              borderRadius: 16,
            }}
          />
        </div>

        {/* Tagline — bottom */}
        <div className="shrink-0" style={{ padding: "12px 32px 36px" }}>
          <p
            className="uppercase tracking-widest"
            style={{ fontSize: 11, color: "#C4B5D8", marginBottom: 8, fontWeight: 700 }}
          >
            Meet Waddler
          </p>
          <p style={{ fontSize: 19, color: "#A480F2", lineHeight: 1.4, fontWeight: 800 }}>
            Your friendly travel buddy,<br />ready to waddle you to paradise.
          </p>
        </div>
      </aside>

      {/* ═══════════════ RIGHT — gradient + carousel + CTA ═══════════════ */}
      <section
        className="flex flex-1 flex-col overflow-hidden"
        style={{
          padding: "20px 32px 40px",
          background: "linear-gradient(160deg, #D6C2F2 0%, #A480F2 60%, #291940 100%)",
        }}
      >
        {/* Photo carousel */}
        <div
          className="relative flex-1 min-h-0 overflow-hidden"
          style={{ borderRadius: 24 }}
        >
          {destinations.map((d, i) => (
            <img
              key={d.src}
              src={d.src}
              alt={d.label}
              className="absolute inset-0 w-full h-full object-cover transition-opacity duration-1000 ease-in-out"
              style={{ opacity: i === index ? 1 : 0 }}
            />
          ))}

          {/* Location badge — white pill, purple text */}
          <span
            className="absolute bottom-5 left-5 inline-block rounded-full"
            style={{
              background: "rgba(255,255,255,0.95)",
              padding: "8px 18px",
              fontWeight: 700,
              fontSize: 13,
              color: "#7C3AED",
              backdropFilter: "blur(4px)",
            }}
          >
            {destinations[index].label}
          </span>

          {/* Dots — bottom right */}
          <div className="absolute bottom-5 right-5 flex items-center gap-1.5">
            {destinations.map((_, i) => (
              <button
                key={i}
                onClick={() => setIndex(i)}
                className="transition-all duration-300"
                style={{
                  height: 8,
                  width: i === index ? 32 : 8,
                  borderRadius: 99,
                  background: i === index ? "#ffffff" : "rgba(255,255,255,0.6)",
                  border: "none",
                  cursor: "pointer",
                  padding: 0,
                }}
              />
            ))}
          </div>
        </div>

        {/* Headline + CTA */}
        <div className="shrink-0" style={{ paddingTop: 28 }}>
          <h1
            className="leading-tight"
            style={{ fontSize: 38, color: "#ffffff", fontWeight: 900 }}
          >
            Your journey begins with{" "}
            <span style={{ color: "#F2AE2E" }}>one booking.</span>
          </h1>

          <p
            className="mt-2.5"
            style={{ fontSize: 15, color: "rgba(255,255,255,0.75)", lineHeight: 1.5, fontWeight: 400 }}
          >
            Discover hand-picked stays around the world — from sunlit shores to snowy peaks.
          </p>

          {/* Button */}
          <Link
            to="/guest/hotels"
            aria-label="Explore hotels"
            className="explore-btn mt-6 inline-flex items-center rounded-full font-bold text-base shadow-soft"
            style={{ height: 56, padding: "0 32px", gap: 8, textDecoration: "none", fontWeight: 700 }}
          >
            <Compass style={{ width: 20, height: 20 }} />
            Explore hotels
            <ArrowRight style={{ width: 20, height: 20 }} />
          </Link>

          <style>{`
            .explore-btn {
              background: #F2AE2E;
              color: #291940;
              transition: background 0.25s ease, color 0.25s ease;
            }
            .explore-btn:hover {
              background: #A784F2;
              color: #F2AE2E;
            }
          `}</style>
        </div>
      </section>
    </div>
  );
};

export default Welcome;
