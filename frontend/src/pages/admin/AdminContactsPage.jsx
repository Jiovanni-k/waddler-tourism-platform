import { useState, useEffect } from "react";
import { 
  MessageSquare, 
  Clock, 
  MoreHorizontal,
  Reply,
  Filter,
  Search,
  X
} from "lucide-react";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import { adminGetContactRequests, adminResolveContact } from "../../api/adminApi";
import greetingPenguin from "../../assets/penguins/Greeting_Speaking.png";
import { listFromApiData } from "../../lib/apiResponse";

const STATUS_OPTIONS = [
  { value: "ACTIVE", label: "Active inquiries" },
  { value: "ALL", label: "All inquiries" },
  { value: "REPLIED", label: "Replied" },
  { value: "PENDING", label: "Pending" }
];

const isReplied = (contact) =>
  Boolean(contact.replied || contact.status === "RESOLVED" || contact.status === "REPLIED" || contact.response);

const getContactStatus = (contact) => isReplied(contact) ? "REPLIED" : "PENDING";

const AdminContactsPage = () => {
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ACTIVE");
  const [selectedContact, setSelectedContact] = useState(null);
  const [replyingContact, setReplyingContact] = useState(null);
  const [replyText, setReplyText] = useState("");
  const [replyError, setReplyError] = useState("");
  const [actingId, setActingId] = useState(null);

  useEffect(() => {
    fetchContacts();
  }, []);

  const fetchContacts = async () => {
    setLoading(true);
    try {
      const res = await adminGetContactRequests();
      setContacts(listFromApiData(res.data));
    } catch (err) {
      console.error(err);
      setContacts([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredContacts = contacts.filter((contact) => {
    const query = searchQuery.trim().toLowerCase();
    const status = getContactStatus(contact);

    const matchesSearch =
      !query ||
      contact.name?.toLowerCase().includes(query) ||
      contact.email?.toLowerCase().includes(query) ||
      contact.subject?.toLowerCase().includes(query) ||
      contact.message?.toLowerCase().includes(query);

    const matchesStatus =
      statusFilter === "ALL" ||
      (statusFilter === "ACTIVE" && status === "PENDING") ||
      status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const openReply = (contact) => {
    setReplyingContact(contact);
    setReplyText(contact.response || "");
    setReplyError("");
  };

  const handleResolve = async (event) => {
    event.preventDefault();
    if (!replyingContact) return;
    if (!replyText.trim()) {
      setReplyError("Please write a reply before resolving this inquiry.");
      return;
    }

    const previousContact = replyingContact;
    const resolvedContact = {
      ...replyingContact,
      replied: true,
      status: "RESOLVED",
      response: replyText.trim()
    };

    setActingId(replyingContact.id);
    setContacts((prev) => prev.map((contact) => contact.id === replyingContact.id ? resolvedContact : contact));

    try {
      await adminResolveContact(replyingContact.id, replyText.trim());
      setReplyingContact(null);
      setReplyText("");
      setReplyError("");
    } catch (err) {
      console.error("Failed to resolve contact request:", err);
      setContacts((prev) => prev.map((contact) => contact.id === previousContact.id ? previousContact : contact));
      setReplyError("Failed to send the reply. Please try again.");
    } finally {
      setActingId(null);
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
        category="SUPPORT"
        title="Help & Inquiries"
        subtitle="Manage user feedback, contact requests, and general inquiries from the community."
        penguin={greetingPenguin}
      />

      {replyingContact && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <form
            onSubmit={handleResolve}
            className="bg-card rounded-[32px] w-full max-w-lg shadow-glow p-8 border border-white/20 animate-in zoom-in duration-300"
          >
            <div className="flex items-start justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-black text-primary-deep">Reply to inquiry</h2>
                <p className="text-sm font-bold text-muted-foreground mt-1">
                  {replyingContact.name || "User"} · {replyingContact.email || "No email"}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setReplyingContact(null)}
                className="p-2 text-muted-foreground hover:text-destructive hover:bg-muted/20 rounded-xl transition-all"
              >
                <X size={20} />
              </button>
            </div>

            <div className="p-4 bg-muted/20 rounded-2xl mb-4">
              <div className="text-xs font-black text-muted-foreground uppercase tracking-widest mb-2">
                {replyingContact.subject || "No subject"}
              </div>
              <p className="text-sm font-bold text-primary-deep leading-relaxed">
                {replyingContact.message || "No message provided."}
              </p>
            </div>

            {replyError && (
              <div className="mb-4 rounded-2xl bg-destructive/10 text-destructive px-4 py-3 text-sm font-bold">
                {replyError}
              </div>
            )}

            <textarea
              rows={5}
              value={replyText}
              onChange={(event) => setReplyText(event.target.value)}
              placeholder="Write your reply..."
              className="w-full p-4 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-semibold mb-6 transition-all resize-y"
            />

            <div className="flex flex-col sm:flex-row gap-3">
              <button
                type="button"
                onClick={() => setReplyingContact(null)}
                className="flex-1 py-3 bg-muted text-muted-foreground font-bold rounded-xl hover:bg-muted/80 transition-all"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={actingId === replyingContact.id}
                className="flex-1 py-3 bg-primary text-primary-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft disabled:opacity-60"
              >
                {actingId === replyingContact.id ? "Sending..." : "Send reply"}
              </button>
            </div>
          </form>
        </div>
      )}

      {selectedContact && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-card rounded-[32px] w-full max-w-lg shadow-glow p-8 border border-white/20 animate-in zoom-in duration-300">
            <div className="flex items-start justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-black text-primary-deep">Inquiry details</h2>
                <p className="text-xs font-black text-muted-foreground uppercase tracking-widest mt-1">
                  {selectedContact.createdAt?.split("T")[0] || "Just now"}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setSelectedContact(null)}
                className="p-2 text-muted-foreground hover:text-destructive hover:bg-muted/20 rounded-xl transition-all"
              >
                <X size={20} />
              </button>
            </div>

            <div className="space-y-4 mb-6">
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Sender</div>
                <div className="font-black text-primary-deep">{selectedContact.name || "User"}</div>
                <div className="text-xs font-bold text-muted-foreground">{selectedContact.email || "No email"}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Subject</div>
                <div className="font-black text-primary-deep">{selectedContact.subject || "No Subject"}</div>
              </div>
              <div className="p-4 bg-muted/20 rounded-2xl">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-widest mb-1">Message</div>
                <p className="text-sm font-bold text-primary-deep leading-relaxed">{selectedContact.message || "No message provided."}</p>
              </div>
              {selectedContact.response && (
                <div className="p-4 bg-success/10 rounded-2xl">
                  <div className="text-[10px] font-black text-success uppercase tracking-widest mb-1">Reply</div>
                  <p className="text-sm font-bold text-primary-deep leading-relaxed">{selectedContact.response}</p>
                </div>
              )}
            </div>

            <div className="flex flex-col sm:flex-row gap-3">
              {!isReplied(selectedContact) && (
                <button
                  type="button"
                  onClick={() => {
                    setSelectedContact(null);
                    openReply(selectedContact);
                  }}
                  className="flex-1 py-3 bg-primary text-primary-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft"
                >
                  Reply
                </button>
              )}
              <button
                type="button"
                onClick={() => setSelectedContact(null)}
                className="flex-1 py-3 bg-muted text-muted-foreground font-bold rounded-xl hover:bg-muted/80 transition-all"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="bg-card rounded-[32px] shadow-soft overflow-hidden border border-border/30">
        <div className="p-6 border-b border-border/30 flex flex-col md:flex-row gap-4 items-center justify-between bg-muted/5">
          <div className="relative w-full md:w-96">
            <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input 
              type="text" 
              placeholder="Search by name, email or subject..." 
              className="w-full pl-12 pr-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm transition-all shadow-sm"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          
          <label className="flex items-center gap-2 w-full md:w-auto px-5 py-3 bg-white border-2 border-border/30 rounded-2xl font-black text-sm text-primary-deep shadow-sm">
            <Filter size={18} className="text-primary" />
            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              className="bg-transparent outline-none font-black text-sm flex-1 md:flex-none cursor-pointer"
            >
              {STATUS_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-muted/10">
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Inquiry Details</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Sender</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Received</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Status</th>
                <th className="p-6 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/30">
              {filteredContacts.map((c) => (
                <tr key={c.id} className="hover:bg-muted/5 transition-colors group">
                  <td className="p-6">
                    <div className="max-w-md">
                      <div className="font-black text-primary-deep text-base mb-1">{c.subject || "No Subject"}</div>
                      <div className="text-sm text-muted-foreground font-medium line-clamp-1">{c.message}</div>
                    </div>
                  </td>
                  <td className="p-6">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-secondary text-primary flex items-center justify-center text-xs font-black shadow-sm">
                        {c.name?.[0] || "U"}
                      </div>
                      <div>
                        <div className="font-bold text-sm text-primary-deep">{c.name}</div>
                        <div className="text-[10px] text-muted-foreground font-bold">{c.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="p-6">
                    <div className="flex items-center gap-2 text-xs font-bold text-muted-foreground">
                      <Clock size={14} className="text-primary" />
                      {c.createdAt?.split('T')[0] || "Just now"}
                    </div>
                  </td>
                  <td className="p-6">
                    <span className={`
                      inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest
                      ${isReplied(c) ? "bg-success/15 text-success" : "bg-primary/15 text-primary"}
                    `}>
                      <span className="w-1.5 h-1.5 rounded-full bg-current" />
                      {isReplied(c) ? "Replied" : "Pending"}
                    </span>
                  </td>
                  <td className="p-6 text-right">
                    <div className="flex items-center justify-end gap-2">
                       <button
                         type="button"
                         onClick={() => openReply(c)}
                         className="p-2 text-primary hover:bg-primary/10 rounded-xl transition-all shadow-sm"
                       >
                         <Reply size={18} />
                       </button>
                       <button
                         type="button"
                         onClick={() => setSelectedContact(c)}
                         className="p-2 text-muted-foreground hover:bg-muted/20 rounded-xl transition-all"
                       >
                         <MoreHorizontal size={18} />
                       </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {filteredContacts.length === 0 && (
            <div className="p-20 text-center flex flex-col items-center">
              <div className="w-16 h-16 bg-muted/20 rounded-full flex items-center justify-center text-muted-foreground mb-4">
                 <MessageSquare size={32} />
              </div>
              <h3 className="text-xl font-black text-primary-deep mb-1">Inbox clear!</h3>
              <p className="text-muted-foreground font-bold text-sm">No support requests match your current filters.</p>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminContactsPage;
