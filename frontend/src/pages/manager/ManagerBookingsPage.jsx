import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { ClipboardList } from "lucide-react";
import { getManagerBookings, confirmBooking, completeBooking, cancelBooking } from "../../api/managerApi";
import LoadingSpinner from "../../components/shared/LoadingSpinner";
import ErrorMessage from "../../components/shared/ErrorMessage";
import EmptyState from "../../components/shared/EmptyState";
import BookingStatusBadge from "../../components/shared/BookingStatusBadge";

const STATUS_FILTERS = ["ALL", "PENDING", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"];

const ManagerBookingsPage = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [actionLoading, setActionLoading] = useState(null);

  useEffect(() => { fetchBookings(); }, [statusFilter]);

  const fetchBookings = async () => {
    setLoading(true); setError(null);
    try {
      const params = {
        page: 0, size: 50, sortBy: "bookingDate", sortDir: "desc",
        ...(statusFilter !== "ALL" ? { status: statusFilter } : {})
      };
      const res = await getManagerBookings(params);
      const data = res.data;
      setBookings(data?.content || data?.items || (Array.isArray(data) ? data : []));
    } catch (err) { setError(err.response?.data?.message || "Something went wrong."); }
    finally { setLoading(false); }
  };

  const handleAction = async (id, action) => {
    setActionLoading(id + action);
    try {
      if (action === "confirm") await confirmBooking(id);
      if (action === "complete") await completeBooking(id);
      if (action === "cancel") await cancelBooking(id);
      await fetchBookings();
    } catch (err) { alert(err.response?.data?.message || `Failed to ${action}.`); }
    finally { setActionLoading(null); }
  };

  const nights = (ci, co) => {
    if (!ci || !co) return "—";
    return Math.max(0, Math.round((new Date(co) - new Date(ci)) / 86400000));
  };

  return (
    <div style={{ maxWidth: 1100, margin: "0 auto" }}>

      {/* ── Header ─────────────────────────────────────────────────────── */}
      <div style={{ marginBottom: 28 }}>
        <Link to="/manager" style={{ fontSize: 12, fontWeight: 900, color: "#A480F2", textDecoration: "none", letterSpacing: 0.5 }}>
          ← Dashboard
        </Link>
        <h1 style={{ fontFamily: "'Nunito', sans-serif", fontWeight: 900, fontSize: 34, color: "#291940", letterSpacing: -0.5, marginTop: 6, marginBottom: 4 }}>
          All Bookings
        </h1>
        <p style={{ color: "#9B88BF", fontWeight: 700, fontSize: 14 }}>
          {bookings.length} booking{bookings.length !== 1 ? "s" : ""} found
        </p>
      </div>

      {/* ── Filter Tabs ────────────────────────────────────────────────── */}
      <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginBottom: 24 }}>
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            style={{
              padding: "8px 18px",
              borderRadius: 999,
              fontFamily: "'Nunito', sans-serif",
              fontWeight: 900,
              fontSize: 13,
              cursor: "pointer",
              border: "none",
              transition: "transform 0.1s, box-shadow 0.1s",
              ...(statusFilter === s
                ? {
                  background: "linear-gradient(135deg, #A480F2, #8B5CF6)",
                  color: "#fff",
                  boxShadow: "0 4px 0 #6D3FCC",
                }
                : {
                  background: "#fff",
                  color: "#5C4A82",
                  border: "2.5px solid #D8CFF2",
                  boxShadow: "0 3px 0 #C3AEED",
                }),
            }}
            onMouseDown={e => { e.currentTarget.style.transform = "translateY(3px)"; e.currentTarget.style.boxShadow = "none"; }}
            onMouseUp={e => { e.currentTarget.style.transform = ""; e.currentTarget.style.boxShadow = statusFilter === s ? "0 4px 0 #6D3FCC" : "0 3px 0 #C3AEED"; }}
          >
            {s}
          </button>
        ))}
      </div>

      {/* ── Table ──────────────────────────────────────────────────────── */}
      {loading ? (
        <div className="card" style={{ padding: 40 }}><LoadingSpinner /></div>
      ) : error ? (
        <div className="card" style={{ padding: 40 }}><ErrorMessage message={error} /></div>
      ) : bookings.length === 0 ? (
        <div className="card">
          <EmptyState icon={<ClipboardList className="h-8 w-8 text-muted-foreground" />} title="No bookings found"
            message={statusFilter !== "ALL" ? `No ${statusFilter.toLowerCase()} bookings.` : "No bookings yet."} />
        </div>
      ) : (
        <div className="card">
          <div style={{ overflowX: "auto" }}>
            <table className="waddler-table">
              <thead>
                <tr>
                  {["ID", "Hotel", "Room", "Check-in", "Check-out", "Nights", "Guests", "Total", "Status", "Actions"].map(h => (
                    <th key={h} style={{ whiteSpace: "nowrap" }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {bookings.map(b => {
                  const isLoading = actionLoading?.startsWith(String(b.id));
                  return (
                    <tr key={b.id}>
                      <td style={{ color: "#9B88BF", fontWeight: 900 }}>#{b.id}</td>
                      <td style={{ fontWeight: 900, color: "#291940", whiteSpace: "nowrap" }}>{b.hotelName || "—"}</td>
                      <td style={{ color: "#5C4A82", whiteSpace: "nowrap" }}>{b.roomName || `Room #${b.roomId}`}</td>
                      <td style={{ color: "#5C4A82", whiteSpace: "nowrap" }}>{b.checkInDate || "—"}</td>
                      <td style={{ color: "#5C4A82", whiteSpace: "nowrap" }}>{b.checkOutDate || "—"}</td>
                      <td style={{ color: "#5C4A82" }}>{nights(b.checkInDate, b.checkOutDate)}</td>
                      <td style={{ color: "#5C4A82" }}>{b.numberOfGuests}</td>
                      <td style={{ fontWeight: 900, color: "#A480F2" }}>${Number(b.totalPrice || 0).toFixed(2)}</td>
                      <td><BookingStatusBadge status={b.status} /></td>
                      <td>
                        <div style={{ display: "flex", gap: 6 }}>
                          {b.status === "PENDING" && (
                            <button disabled={isLoading} onClick={() => handleAction(b.id, "confirm")}
                              className="btn btn-primary btn-sm" style={{ opacity: isLoading ? 0.5 : 1 }}>
                              {isLoading ? "..." : "Confirm"}
                            </button>
                          )}
                          {b.status === "CHECKED_IN" && (
                            <button disabled={isLoading} onClick={() => handleAction(b.id, "complete")}
                              className="btn btn-success btn-sm" style={{ opacity: isLoading ? 0.5 : 1 }}>
                              {isLoading ? "..." : "Complete"}
                            </button>
                          )}
                          {["PENDING", "CONFIRMED"].includes(b.status) && (
                            <button disabled={isLoading} onClick={() => handleAction(b.id, "cancel")}
                              className="btn btn-danger btn-sm" style={{ opacity: isLoading ? 0.5 : 1 }}>
                              {isLoading ? "..." : "Cancel"}
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManagerBookingsPage;
