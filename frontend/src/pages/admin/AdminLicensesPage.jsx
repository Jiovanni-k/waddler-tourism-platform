import { useState, useEffect } from "react";
import { 
  CheckCircle, 
  Star, 
  MapPin, 
  Check, 
  X,
  ShieldCheck,
  Phone,
  Mail,
  UserCheck,
  AlertCircle,
  FileSearch
} from "lucide-react";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import { 
  adminGetPendingHotels, 
  adminApproveHotel, 
  adminRejectHotel 
} from "../../api/adminApi";
import travelerPenguin from "../../assets/penguins/Traveler.png";
import { demoPendingHotels, listFromApiData, localFallbackEnabled } from "../../lib/hotelFallback";

const normalizePendingHotel = (hotel) => {
  const city = hotel.city || hotel.town || "";
  const country = hotel.country || hotel.region || "";

  return {
    ...hotel,
    city,
    country,
    image: hotel.image || hotel.coverImageUrl || hotel.imageUrl || hotel.images?.[0],
    manager: hotel.manager || hotel.managerName || hotel.ownerName || hotel.owner?.name,
    managerEmail: hotel.managerEmail || hotel.owner?.email || hotel.email,
    managerPhone: hotel.managerPhone || hotel.owner?.phone || hotel.phone,
    licenseNum: hotel.licenseNum || hotel.licenseNumber || hotel.businessLicenseNumber,
    rooms: hotel.rooms || hotel.roomCount || hotel.totalRooms,
  };
};

const VerificationModal = ({ hotel, isOpen, onClose }) => {
  if (!isOpen || !hotel) return null;

  const items = [
    { label: "Manager Identity", value: hotel.managerIdChecked, icon: UserCheck },
    { label: "Manager Phone", value: hotel.managerPhone, icon: Phone },
    { label: "Manager Email", value: hotel.managerEmail, icon: Mail },
    { label: "Business License", value: hotel.licenseNum, icon: ShieldCheck },
  ];

  return (
    <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className="bg-card rounded-[32px] w-full max-w-md shadow-glow p-8 animate-in zoom-in duration-300 border border-white/20">
        <div className="flex items-center gap-4 mb-8">
          <div className="w-14 h-14 bg-secondary rounded-2xl flex items-center justify-center text-primary shadow-sm">
            <ShieldCheck size={28} />
          </div>
          <div>
            <h2 className="text-xl font-black text-primary-deep tracking-tight">Security Audit</h2>
            <p className="text-sm font-bold text-muted-foreground">Checking verification status</p>
          </div>
        </div>

        <div className="space-y-3 mb-8">
          {items.map((item, i) => (
            <div key={i} className="flex items-center justify-between p-4 bg-muted/20 rounded-2xl border border-border/30 hover:border-primary/20 transition-all">
              <div className="flex items-center gap-4">
                <item.icon size={18} className="text-muted-foreground" />
                <span className="text-sm font-bold text-primary-deep">{item.label}</span>
              </div>
              
              {item.value ? (
                <div className="flex items-center gap-2 text-success font-black text-[10px] uppercase tracking-widest">
                  <CheckCircle size={14} />
                  Verified
                </div>
              ) : (
                <div className="flex items-center gap-2 text-destructive font-black text-[10px] uppercase tracking-widest">
                  <AlertCircle size={14} />
                  Missing
                </div>
              )}
            </div>
          ))}
        </div>

        <button 
          onClick={onClose}
          className="w-full py-4 bg-primary text-primary-foreground font-black rounded-2xl hover:opacity-90 transition-all shadow-soft"
        >
          Close Report
        </button>
      </div>
    </div>
  );
};

const AdminLicensesPage = () => {
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [acting, setActing] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);
  const [rejectReason, setRejectReason] = useState("");
  const [verifyingHotel, setVerifyingHotel] = useState(null);

  useEffect(() => {
    fetchHotels();
  }, []);

  const fetchHotels = async () => {
    try {
      const res = await adminGetPendingHotels();
      const pending = listFromApiData(res.data).map(normalizePendingHotel);
      setHotels(pending.length > 0 || !localFallbackEnabled() ? pending : demoPendingHotels);
    } catch (err) {
      console.error(err);
      setHotels(localFallbackEnabled() ? demoPendingHotels : []);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id) => {
    setActing(id);
    try {
      if (!String(id).startsWith("demo-pending-")) {
        await adminApproveHotel(id);
      }
      setHotels(prev => prev.filter(h => h.id !== id));
    } catch (err) {
      console.error("Approval failed:", err);
    } finally {
      setActing(null);
    }
  };

  const handleReject = async () => {
    if (!rejectingId) return;
    setActing(rejectingId);
    try {
      if (!String(rejectingId).startsWith("demo-pending-")) {
        await adminRejectHotel(rejectingId, rejectReason);
      }
      setHotels(prev => prev.filter(h => h.id !== rejectingId));
      setRejectingId(null);
      setRejectReason("");
    } catch (err) {
      console.error("Rejection failed:", err);
    } finally {
      setActing(null);
    }
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
        category="APPROVALS"
        title="License approvals"
        subtitle="Review and approve hotels submitted by managers. Approved hotels become publicly visible on Waddler."
        penguin={travelerPenguin}
      />

      <VerificationModal 
        hotel={verifyingHotel} 
        isOpen={!!verifyingHotel} 
        onClose={() => setVerifyingHotel(null)} 
      />

      {rejectingId && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-card rounded-[32px] w-full max-w-md shadow-glow p-8 animate-in zoom-in duration-300 border border-white/20">
             <div className="w-16 h-16 bg-destructive/10 rounded-2xl flex items-center justify-center mb-6 text-destructive">
                <X size={32} />
             </div>
             <h2 className="text-2xl font-black text-primary-deep mb-2">Reject License?</h2>
             <p className="text-muted-foreground font-bold text-sm mb-6">Please provide a reason for the rejection.</p>
             
             <textarea 
               className="w-full p-4 bg-muted/20 border-2 border-border/30 rounded-2xl focus:border-destructive outline-none font-semibold mb-6 transition-all"
               placeholder="e.g. Missing required health safety documents..."
               rows={3}
               value={rejectReason}
               onChange={(e) => setRejectReason(e.target.value)}
             />

             <div className="flex gap-4">
               <button className="flex-1 py-3 bg-muted text-muted-foreground font-bold rounded-xl hover:bg-muted/80 transition-all" onClick={() => setRejectingId(null)}>Cancel</button>
               <button 
                 className="flex-1 py-3 bg-destructive text-destructive-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft"
                 onClick={handleReject}
                 disabled={acting === rejectingId}
               >
                 {acting === rejectingId ? "Processing..." : "Confirm Reject"}
               </button>
             </div>
          </div>
        </div>
      )}

      <div className="space-y-8">
        {hotels.map((h) => (
          <article key={h.id} className="bg-card rounded-[32px] shadow-soft overflow-hidden flex flex-col md:flex-row border border-border/30 hover:shadow-glow transition-all group">
            <div className="md:w-80 h-64 md:h-auto relative overflow-hidden shrink-0">
              <img 
                src={h.image || "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=400&q=80"} 
                alt={h.name} 
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
              />
              <div className="absolute top-4 left-4">
                <span className="bg-white/90 backdrop-blur-md text-[10px] font-black text-primary px-3 py-1 rounded-full shadow-sm uppercase tracking-widest">
                  Pending review
                </span>
              </div>
            </div>

            <div className="flex-1 p-8 flex flex-col">
              <div className="flex justify-between items-start mb-2">
                <h2 className="text-2xl font-black text-primary-deep tracking-tight">{h.name}</h2>
                <div className="flex items-center gap-1.5 text-accent font-black text-xs">
                  <Star size={16} className="fill-accent" />
                  New listing
                </div>
              </div>

              <div className="flex items-center gap-2 text-muted-foreground font-bold text-sm mb-6">
                <MapPin size={16} />
                {h.city}, {h.country}
              </div>

              <p className="text-muted-foreground text-sm font-medium leading-relaxed mb-8 flex-1 line-clamp-3">
                {h.description}
              </p>

              <div className="grid grid-cols-3 gap-6 py-6 border-y border-border/30 mb-8">
                <div>
                  <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Manager</div>
                  <div className="font-bold text-sm text-primary-deep">{h.manager || "N/A"}</div>
                </div>
                <div>
                  <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Rooms</div>
                  <div className="font-bold text-sm text-primary-deep">{h.rooms || 0}</div>
                </div>
                <div>
                  <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">License #</div>
                  <div className="font-bold text-sm text-primary-deep truncate max-w-[100px]">{h.licenseNum || "N/A"}</div>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row justify-between items-center gap-4 mt-auto">
                <button 
                  onClick={() => setVerifyingHotel(h)}
                  className="flex items-center gap-2 text-primary font-black text-sm hover:underline"
                >
                  <FileSearch size={18} />
                  Check verification report
                </button>
                <div className="flex items-center gap-3 w-full sm:w-auto">
                  <button 
                    className="flex-1 sm:flex-none px-6 py-2.5 rounded-xl border-2 border-destructive/20 text-destructive font-black text-sm hover:bg-destructive/5 transition-all"
                    onClick={() => setRejectingId(h.id)}
                  >
                    Reject
                  </button>
                  <button 
                    className="flex-1 sm:flex-none px-6 py-2.5 rounded-xl bg-primary text-primary-foreground font-black text-sm hover:opacity-90 transition-all shadow-soft"
                    onClick={() => handleApprove(h.id)}
                    disabled={acting === h.id}
                  >
                    {acting === h.id ? "Approving..." : "Approve Hotel"}
                  </button>
                </div>
              </div>
            </div>
          </article>
        ))}

        {hotels.length === 0 && (
          <div className="bg-card rounded-[32px] shadow-soft p-16 text-center border-2 border-dashed border-border/50 flex flex-col items-center">
            <div className="w-20 h-20 bg-success/10 rounded-full flex items-center justify-center text-success mb-6 shadow-sm">
              <CheckCircle size={40} />
            </div>
            <h3 className="text-2xl font-black text-primary-deep mb-2">All caught up!</h3>
            <p className="text-muted-foreground font-bold">There are no pending hotel licenses waiting for your review.</p>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminLicensesPage;
