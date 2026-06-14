import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { 
  Hotel, 
  Users, 
  CalendarCheck, 
  DollarSign, 
  Plus,
  ArrowRight,
  TrendingUp,
  ShieldCheck
} from "lucide-react";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import StatCard from "../../components/admin/StatCard";
import wavingPenguin from "../../assets/penguins/Waving.png";
import { useAuth } from "../../context/AuthContext";
import { adminGetAllHotels, adminGetAllUsers, adminGetPendingHotels, adminGetAllBookings, adminGetAllPayments } from "../../api/adminApi";
import {
  bookingListFromResponse,
  bookingTotalFromResponse,
  getBookingGuestName,
  getBookingHotelName,
  getBookingPrice,
  getBookingStatus,
} from "../../lib/bookingFormat";

const AnnouncementModal = ({ isOpen, onClose, onPublish }) => {
  const [content, setContent] = useState("");
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className="bg-card rounded-3xl w-full max-w-md shadow-glow p-8 animate-in zoom-in duration-300">
        <h2 className="text-2xl font-black text-primary-deep mb-4">Post Announcement</h2>
        <textarea 
          className="w-full p-4 bg-muted/30 border-2 border-border rounded-2xl focus:border-primary outline-none font-semibold mb-6 transition-all"
          placeholder="What's the news, Waddler?"
          rows={4}
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <div className="flex gap-4">
          <button className="flex-1 py-3 bg-muted text-muted-foreground font-bold rounded-xl hover:bg-muted/80 transition-all" onClick={onClose}>Cancel</button>
          <button 
            className="flex-1 py-3 bg-primary text-primary-foreground font-bold rounded-xl hover:opacity-90 transition-all"
            onClick={() => { onPublish(content); setContent(""); onClose(); }}
          >
            Publish
          </button>
        </div>
      </div>
    </div>
  );
};

const AdminDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({ totalHotels: 10, totalUsers: 48, totalBookings: 23, totalRevenue: 18500 });
  const [licenses, setLicenses] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [announcements, setAnnouncements] = useState([
    { id: 1, content: "Welcome to the new Waddler Admin Dashboard!", date: "Just now" },
    { id: 2, content: "System maintenance scheduled for May 15th.", date: "2 hours ago" }
  ]);

  useEffect(() => {
    const fetchData = async () => {
      const toList = (d) => Array.isArray(d) ? d : Array.isArray(d?.content) ? d.content : [];
      const toCount = (d) => (d?.totalElements != null && d.totalElements > 0) ? d.totalElements : toList(d).length;
      const val = (r) => r.status === "fulfilled" ? r.value?.data : null;

      try {
        const [hotelsR, usersR, bookingsR, paymentsR, pendingR] = await Promise.allSettled([
          adminGetAllHotels(),
          adminGetAllUsers(),
          adminGetAllBookings({ page: 0, size: 5, limit: 5, sortBy: "createdAt", sortDir: "desc" }),
          adminGetAllPayments(),
          adminGetPendingHotels(),
        ]);

        const hotelsData = val(hotelsR);
        const usersData = val(usersR);
        const bookingsData = val(bookingsR);
        const paymentsData = val(paymentsR);
        const pendingData = val(pendingR);

        const bookingsList = bookingListFromResponse(bookingsData);
        const paymentsList = toList(paymentsData);
        const totalRevenue = paymentsList.reduce((sum, p) => sum + Number(p.amount ?? p.totalAmount ?? 0), 0);

        setStats({
          totalHotels: toCount(hotelsData),
          totalUsers: toCount(usersData),
          totalBookings: bookingTotalFromResponse(bookingsData),
          totalRevenue,
        });
        setLicenses(toList(pendingData));
        setBookings(bookingsList);
      } catch (error) {
        console.error("Dashboard fetch error:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handlePublishAnnouncement = (content) => {
    const newAnnouncement = {
      id: Date.now(),
      content,
      date: "Just now"
    };
    setAnnouncements([newAnnouncement, ...announcements]);
  };

  if (loading) return (
    <AdminLayout>
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    </AdminLayout>
  );

  return (
    <AdminLayout>
      <PageHeader
        category="OVERVIEW"
        title={`Welcome back, ${user?.firstName || "Admin"}`}
        subtitle="Here's what's happening across Waddler today — pending approvals, fresh bookings, and revenue at a glance."
        penguin={wavingPenguin}
        action={
          <button 
            className="btn bg-primary text-primary-foreground hover:opacity-90 shadow-soft px-6 py-2.5 rounded-full font-bold flex items-center gap-2 transition-all"
            onClick={() => setIsModalOpen(true)}
          >
            <Plus size={18} />
            New announcement
          </button>
        }
      />

      <AnnouncementModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onPublish={handlePublishAnnouncement}
      />

      {/* ── Stats Grid ── */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
        <StatCard title="Active hotels" value={stats.totalHotels || 0} icon={Hotel} />
        <StatCard title="Registered users" value={(stats.totalUsers || 0).toLocaleString()} icon={Users} />
        <StatCard title="Bookings this week" value={(stats.totalBookings || 0).toLocaleString()} icon={CalendarCheck} />
        <StatCard title="Revenue (MTD)" value={`$${((stats.totalRevenue || 0) / 1000).toFixed(0)}k`} icon={DollarSign} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* ── Recent Bookings ── */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-card rounded-3xl shadow-soft overflow-hidden border border-border/50">
            <div className="flex items-center justify-between p-6 border-b border-border/50">
              <h2 className="text-xl font-black text-primary-deep flex items-center gap-2">
                <TrendingUp className="text-primary h-5 w-5" />
                Recent bookings
              </h2>
              <Link to="/admin/bookings" className="text-sm font-bold text-primary hover:underline">
                Review all bookings
              </Link>
            </div>
            
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-muted/10">
                    <th className="p-4 text-xs font-black text-muted-foreground uppercase tracking-widest">Guest</th>
                    <th className="p-4 text-xs font-black text-muted-foreground uppercase tracking-widest">Hotel</th>
                    <th className="p-4 text-xs font-black text-muted-foreground uppercase tracking-widest">Total</th>
                    <th className="p-4 text-xs font-black text-muted-foreground uppercase tracking-widest">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border/30">
                  {bookings.map((b, i) => {
                    const guestName = getBookingGuestName(b);
                    const hotelName = getBookingHotelName(b);
                    const totalPrice = getBookingPrice(b);
                    const status = getBookingStatus(b);

                    return (
                    <tr key={b.id || b.bookingId || i} className="hover:bg-muted/5 transition-colors group">
                      <td className="p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-secondary text-primary flex items-center justify-center font-black text-xs">
                            {guestName.split(" ").map(n => n[0]).join("").slice(0, 2).toUpperCase() || "GU"}
                          </div>
                          <div>
                            <div className="font-bold text-primary-deep">{guestName}</div>
                            <div className="text-[10px] text-muted-foreground font-bold tracking-tight">ID: {b.id || b.bookingId || "N/A"}</div>
                          </div>
                        </div>
                      </td>
                      <td className="p-4 font-bold text-sm text-muted-foreground">{hotelName}</td>
                      <td className="p-4 font-black text-primary-deep">${totalPrice.toLocaleString()}</td>
                      <td className="p-4">
                        <span className="text-[10px] font-black px-3 py-1 rounded-full bg-primary/15 text-primary uppercase tracking-wider">
                           {status}
                        </span>
                      </td>
                    </tr>
                    );
                  })}
                </tbody>
              </table>
              {bookings.length === 0 && (
                <div className="text-center py-12">
                  <p className="text-sm font-bold text-muted-foreground italic">No recent bookings found.</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* ── Side Actions ── */}
        <div className="space-y-6">
          <div className="bg-primary rounded-3xl shadow-glow p-8 text-white relative overflow-hidden group">
             <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:scale-110 transition-transform">
               <Hotel size={80} />
             </div>
             <div className="relative z-10">
               <h3 className="text-xl font-black mb-2 flex items-center gap-2">
                 <ShieldCheck size={20} />
                 Approvals
               </h3>
               <p className="text-white/80 font-bold text-sm mb-6 leading-relaxed">
                 {licenses.length} hotels are currently waiting for your verification.
               </p>
               <Link 
                 to="/admin/licenses" 
                 className="inline-flex items-center gap-2 bg-white text-primary px-6 py-2.5 rounded-full font-black text-sm hover:bg-white/90 transition-all shadow-lg"
               >
                 View all <ArrowRight size={16} />
               </Link>
             </div>
          </div>

          <div className="bg-card rounded-3xl shadow-soft border border-border/50 p-6">
             <h3 className="text-sm font-black text-primary-deep mb-4 uppercase tracking-widest">Team Announcements</h3>
             <div className="space-y-4">
               {announcements.map(a => (
                 <div key={a.id} className="p-4 bg-muted/20 rounded-2xl border border-border/30 hover:border-primary/30 transition-colors">
                   <p className="text-sm font-bold text-primary-deep leading-relaxed mb-2">{a.content}</p>
                   <p className="text-[10px] text-muted-foreground font-bold">{a.date}</p>
                 </div>
               ))}
             </div>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminDashboard;
