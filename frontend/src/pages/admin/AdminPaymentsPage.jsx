import { useState, useEffect } from "react";
import { 
  Search, 
  DollarSign, 
  CreditCard, 
  ArrowUpRight,
  Filter,
  Download,
  MoreHorizontal,
  X
} from "lucide-react";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import { adminGetAllPayments } from "../../api/adminApi";
import celebrationPenguin from "../../assets/penguins/Celebration.png";

const STATUS_OPTIONS = [
  { value: "ALL", label: "All statuses" },
  { value: "SUCCESS", label: "Success" },
  { value: "PAID", label: "Paid" },
  { value: "PENDING", label: "Pending" },
  { value: "REFUNDED", label: "Refunded" },
  { value: "FAILED", label: "Failed" }
];

const METHOD_OPTIONS = [
  { value: "ALL", label: "All methods" },
  { value: "CARD", label: "Card" },
  { value: "CASH", label: "Cash" },
  { value: "WALLET", label: "Wallet" },
  { value: "BANK", label: "Bank" }
];

const getStatus = (payment) => payment.status || "SUCCESS";
const getAmount = (payment) => Number(payment.amount || payment.total || payment.totalPrice || 0);
const getMethod = (payment) => payment.method || payment.paymentMethod || payment.cardBrand || "Card";
const getGuest = (payment) => payment.guestName || payment.userName || payment.customerName || "Guest User";
const getBookingRef = (payment) => payment.bookingId || payment.booking?.id || payment.reference || payment.id || "N/A";
const getPaymentDate = (payment) => payment.createdAt || payment.paidAt || payment.date || "";

const csvEscape = (value) => `"${String(value ?? "").replaceAll('"', '""')}"`;

const AdminPaymentsPage = () => {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [methodFilter, setMethodFilter] = useState("ALL");
  const [selectedPayment, setSelectedPayment] = useState(null);

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    try {
      const res = await adminGetAllPayments();
      const raw = res.data;
      setPayments(Array.isArray(raw) ? raw : Array.isArray(raw?.content) ? raw.content : []);
    } catch (err) {
      console.error(err);
      setPayments([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredPayments = payments.filter((payment) => {
    const query = searchQuery.trim().toLowerCase();
    const method = getMethod(payment).toUpperCase();
    const status = getStatus(payment);

    const matchesSearch =
      !query ||
      String(payment.id || "").toLowerCase().includes(query) ||
      String(getBookingRef(payment)).toLowerCase().includes(query) ||
      getGuest(payment).toLowerCase().includes(query);

    const matchesStatus = statusFilter === "ALL" || status === statusFilter;
    const matchesMethod = methodFilter === "ALL" || method.includes(methodFilter);

    return matchesSearch && matchesStatus && matchesMethod;
  });

  const handleExportCsv = () => {
    const rows = [
      ["Payment ID", "Booking Ref", "Guest", "Method", "Amount", "Status", "Date"],
      ...filteredPayments.map((payment) => [
        payment.id || "",
        getBookingRef(payment),
        getGuest(payment),
        getMethod(payment),
        getAmount(payment).toFixed(2),
        getStatus(payment),
        getPaymentDate(payment)
      ])
    ];

    const csv = rows.map((row) => row.map(csvEscape).join(",")).join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `waddler-payments-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
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
        category="FINANCE"
        title="Payment Ledger"
        subtitle="Track every transaction, refund, and payout across the Waddler network."
        penguin={celebrationPenguin}
        action={
          <button
            type="button"
            onClick={handleExportCsv}
            className="btn bg-primary text-primary-foreground hover:opacity-90 shadow-soft px-6 py-2.5 rounded-full font-bold flex items-center gap-2 transition-all"
          >
            <Download size={18} />
            Export CSV
          </button>
        }
      />

      {selectedPayment && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-card rounded-[32px] w-full max-w-lg shadow-glow p-8 border border-white/20 animate-in zoom-in duration-300">
            <div className="flex items-start justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-black text-primary-deep">Payment details</h2>
                <p className="text-xs font-black text-muted-foreground uppercase tracking-widest mt-1">
                  ID: {selectedPayment.id || "N/A"}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setSelectedPayment(null)}
                className="p-2 text-muted-foreground hover:text-destructive hover:bg-muted/20 rounded-xl transition-all"
              >
                <X size={20} />
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Booking ref</div>
                <div className="font-black text-primary-deep">{getBookingRef(selectedPayment)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Guest</div>
                <div className="font-black text-primary-deep">{getGuest(selectedPayment)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Method</div>
                <div className="font-black text-primary-deep">{getMethod(selectedPayment)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Amount</div>
                <div className="font-black text-primary-deep">${getAmount(selectedPayment).toLocaleString()}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Status</div>
                <div className="font-black text-primary-deep">{getStatus(selectedPayment)}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Date</div>
                <div className="font-black text-primary-deep">{getPaymentDate(selectedPayment)?.split("T")[0] || "N/A"}</div>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setSelectedPayment(null)}
              className="w-full py-3 bg-primary text-primary-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft"
            >
              Close
            </button>
          </div>
        </div>
      )}

      <div className="bg-card rounded-[32px] shadow-soft overflow-hidden border border-border/30">
        <div className="p-6 border-b border-border/30 flex flex-col md:flex-row gap-4 items-center justify-between bg-muted/5">
          <div className="relative w-full md:w-96">
            <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input 
              type="text" 
              placeholder="Search by booking ID or guest..." 
              className="w-full pl-12 pr-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm transition-all shadow-sm"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          
          <div className="flex flex-col sm:flex-row gap-2 w-full md:w-auto">
            <label className="flex items-center gap-2 px-5 py-3 bg-white border-2 border-border/30 rounded-2xl font-black text-sm text-primary-deep shadow-sm w-full sm:w-auto">
              <Filter size={18} className="text-primary" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-transparent outline-none font-black text-sm flex-1 cursor-pointer"
              >
                {STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>

            <label className="flex items-center gap-2 px-5 py-3 bg-white border-2 border-border/30 rounded-2xl font-black text-sm text-primary-deep shadow-sm w-full sm:w-auto">
              <CreditCard size={18} className="text-primary" />
              <select
                value={methodFilter}
                onChange={(e) => setMethodFilter(e.target.value)}
                className="bg-transparent outline-none font-black text-sm flex-1 cursor-pointer"
              >
                {METHOD_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-muted/10">
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Transaction</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Guest</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Method</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Amount</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Status</th>
                <th className="p-6 text-right"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/30">
              {filteredPayments.map((p) => (
                <tr key={p.id} className="hover:bg-muted/5 transition-colors group">
                  <td className="p-6">
                    <div>
                      <div className="font-black text-primary-deep text-base">Booking Payment</div>
                      <div className="text-[10px] text-muted-foreground font-black tracking-widest uppercase">REF: {getBookingRef(p)}</div>
                    </div>
                  </td>
                  <td className="p-6">
                    <div className="font-bold text-sm text-primary-deep">{getGuest(p)}</div>
                  </td>
                  <td className="p-6 text-muted-foreground font-bold text-sm">
                     <div className="flex items-center gap-2">
                       <CreditCard size={14} className="text-primary" />
                       {getMethod(p)}
                     </div>
                  </td>
                  <td className="p-6">
                    <div className="font-black text-primary-deep text-base">
                      ${getAmount(p).toLocaleString()}
                      <span className="ml-1.5 inline-flex text-success"><ArrowUpRight size={14} /></span>
                    </div>
                  </td>
                  <td className="p-6">
                    <span className={`
                      inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest
                      ${getStatus(p) === "SUCCESS" || getStatus(p) === "PAID" ? "bg-success/15 text-success" : getStatus(p) === "FAILED" ? "bg-destructive/15 text-destructive" : getStatus(p) === "REFUNDED" ? "bg-accent/15 text-accent" : "bg-primary/15 text-primary"}
                    `}>
                      <span className="w-1.5 h-1.5 rounded-full bg-current" />
                      {getStatus(p)}
                    </span>
                  </td>
                  <td className="p-6 text-right">
                    <button
                      type="button"
                      onClick={() => setSelectedPayment(p)}
                      className="p-2 text-muted-foreground hover:bg-muted/20 rounded-xl transition-all"
                    >
                      <MoreHorizontal size={20} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {filteredPayments.length === 0 && (
            <div className="p-20 text-center flex flex-col items-center">
              <div className="w-16 h-16 bg-muted/20 rounded-full flex items-center justify-center text-muted-foreground mb-4">
                 <DollarSign size={32} />
              </div>
              <h3 className="text-xl font-black text-primary-deep mb-1">No transactions found</h3>
              <p className="text-muted-foreground font-bold text-sm">There are no payments recorded in the selected period.</p>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminPaymentsPage;
