import { useEffect, useState } from "react";
import { BellRing } from "lucide-react";
import { format } from "date-fns";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import { getNotifications, markNotificationRead, markAllRead } from "../../api/notificationApi";
import listeningPenguin from "../../assets/penguins/Listening_Sitting.png";

const normalizeNotification = (raw) => ({
  id: String(raw.id),
  title: raw.title ?? raw.subject ?? raw.type ?? "Notification",
  body: raw.message ?? raw.body ?? raw.content ?? "",
  date: raw.createdAt
    ? (() => {
        try { return format(new Date(raw.createdAt), "d MMM, HH:mm"); }
        catch { return raw.createdAt; }
      })()
    : "",
  read: raw.read ?? raw.isRead ?? false,
});

const AdminNotificationsPage = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const unread = items.filter((item) => !item.read).length;

  useEffect(() => {
    getNotifications()
      .then((res) => setItems((Array.isArray(res.data) ? res.data : []).map(normalizeNotification)))
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  }, []);

  const markAll = async () => {
    await markAllRead();
    setItems((current) => current.map((item) => ({ ...item, read: true })));
  };

  const markOne = async (id) => {
    await markNotificationRead(id);
    setItems((current) => current.map((item) => item.id === id ? { ...item, read: true } : item));
  };

  return (
    <AdminLayout>
      <PageHeader
        category="INBOX"
        title="Notifications"
        subtitle={`${unread} unread notification${unread !== 1 ? "s" : ""}`}
        penguin={listeningPenguin}
      />

      {loading ? (
        <div className="flex items-center justify-center min-h-[300px]">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary" />
        </div>
      ) : items.length === 0 ? (
        <div className="bg-card rounded-[32px] shadow-soft p-12 text-center border border-border/30">
          <p className="text-muted-foreground font-bold">You're all caught up.</p>
        </div>
      ) : (
        <>
          <div className="flex justify-end mb-4">
            <button type="button" onClick={markAll} className="px-5 py-2 rounded-full bg-card text-primary font-black text-sm shadow-soft hover:bg-secondary/60 transition-colors">
              Mark all as read
            </button>
          </div>
          <ul className="space-y-3">
            {items.map((item) => (
              <li key={item.id} className={`bg-card rounded-3xl shadow-soft p-5 flex items-start gap-4 transition-opacity border border-border/30 ${item.read ? "opacity-60" : ""}`}>
                <div className={`h-11 w-11 rounded-2xl flex items-center justify-center shrink-0 ${item.read ? "bg-muted text-muted-foreground" : "bg-secondary text-primary"}`}>
                  <BellRing className="h-5 w-5" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-extrabold text-primary-deep">{item.title}</p>
                    {!item.read && <span className="h-2 w-2 rounded-full bg-primary shrink-0" />}
                  </div>
                  <p className="text-sm text-muted-foreground font-medium">{item.body}</p>
                  {item.date && <p className="text-xs text-muted-foreground mt-1 font-bold">{item.date}</p>}
                </div>
                {!item.read && (
                  <button type="button" onClick={() => markOne(item.id)} className="text-xs font-black text-primary whitespace-nowrap shrink-0">
                    Mark read
                  </button>
                )}
              </li>
            ))}
          </ul>
        </>
      )}
    </AdminLayout>
  );
};

export default AdminNotificationsPage;
