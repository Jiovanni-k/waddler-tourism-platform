import { useEffect, useState } from "react";
import { BellRing } from "lucide-react";
import AppShell from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Penguin } from "@/components/Penguin";
import { getNotifications, markNotificationRead, markAllRead } from "@/api/notificationApi";
import { toast } from "@/hooks/use-toast";
import { format } from "date-fns";

interface Notif {
  id: string;
  title: string;
  body: string;
  date: string;
  read: boolean;
}

const normalizeNotif = (raw: any): Notif => ({
  id:    String(raw.id),
  title: raw.title ?? raw.subject ?? raw.type ?? "Notification",
  body:  raw.message ?? raw.body ?? raw.content ?? "",
  date:  raw.createdAt
    ? (() => { try { return format(new Date(raw.createdAt), "d MMM, HH:mm"); } catch { return raw.createdAt; } })()
    : "",
  read:  raw.read ?? raw.isRead ?? false,
});

const Notifications = () => {
  const [items,   setItems]   = useState<Notif[]>([]);
  const [loading, setLoading] = useState(true);
  const unread = items.filter(i => !i.read).length;

  useEffect(() => {
    getNotifications()
      .then((res: any) => {
        const raw = Array.isArray(res.data) ? res.data : [];
        setItems(raw.map(normalizeNotif));
      })
      .catch(() => toast({ title: "Could not load notifications", variant: "destructive" }))
      .finally(() => setLoading(false));
  }, []);

  const markAll = async () => {
    try {
      await markAllRead();
      setItems(it => it.map(i => ({ ...i, read: true })));
    } catch {
      toast({ title: "Could not mark all as read", variant: "destructive" });
    }
  };

  const markOne = async (id: string) => {
    try {
      await markNotificationRead(id);
      setItems(it => it.map(i => i.id === id ? { ...i, read: true } : i));
    } catch {
      toast({ title: "Could not mark notification as read", variant: "destructive" });
    }
  };

  return (
    <AppShell title="Inbox" subtitle={`${unread} unread notification${unread !== 1 ? "s" : ""}`} eyebrow="Stay updated">
      {loading ? (
        <div className="flex flex-col items-center gap-4 py-16">
          <Penguin mood="thinking" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">Loading notifications…</p>
        </div>
      ) : items.length === 0 ? (
        <div className="bg-card rounded-3xl shadow-soft p-12 text-center flex flex-col items-center gap-4">
          <Penguin mood="happy" className="h-32 w-32" />
          <p className="text-muted-foreground font-semibold">You're all caught up!</p>
        </div>
      ) : (
        <>
          <div className="flex justify-end mb-4">
            <Button variant="ghost" onClick={markAll} className="text-primary font-bold rounded-full">
              Mark all as read
            </Button>
          </div>

          <ul className="space-y-3">
            {items.map(n => (
              <li
                key={n.id}
                className={`bg-card rounded-3xl shadow-soft p-5 flex items-start gap-4 transition-opacity ${n.read ? "opacity-60" : ""}`}
              >
                <div className={`h-11 w-11 rounded-2xl flex items-center justify-center shrink-0 ${n.read ? "bg-muted text-muted-foreground" : "bg-secondary text-primary"}`}>
                  <BellRing className="h-5 w-5" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-extrabold">{n.title}</p>
                    {!n.read && <span className="h-2 w-2 rounded-full bg-primary shrink-0" />}
                  </div>
                  <p className="text-sm text-muted-foreground">{n.body}</p>
                  {n.date && <p className="text-xs text-muted-foreground mt-1">{n.date}</p>}
                </div>
                {!n.read && (
                  <button onClick={() => markOne(n.id)} className="text-xs font-bold text-primary whitespace-nowrap shrink-0">
                    Mark read
                  </button>
                )}
              </li>
            ))}
          </ul>
        </>
      )}
    </AppShell>
  );
};

export default Notifications;
