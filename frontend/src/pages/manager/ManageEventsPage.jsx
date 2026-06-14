import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { Plus, Pencil, Trash2, UploadCloud, Ban, Building2, CalendarDays, MapPin, Calendar } from "lucide-react";
import {
  getMyHotels,
  getEvents,
  createEvent,
  updateEvent,
  deleteEvent,
  publishEvent,
  cancelEvent,
} from "../../api/managerApi";
import LoadingSpinner from "../../components/shared/LoadingSpinner";
import ErrorMessage from "../../components/shared/ErrorMessage";
import EmptyState from "../../components/shared/EmptyState";


import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";

const CATEGORIES = [
  "CONCERT","TRIP","TOUR","WORKSHOP","KIDS","FOOD",
  "CULTURE","PARTY","ADVENTURE","SPORTS","WEDDING",
  "CONFERENCE","FAMILY","OTHER",
];

const LOCATION_TYPES = ["INDOOR", "OUTDOOR", "ONLINE", "HYBRID"];

const EMPTY_FORM = {
  title: "",
  description: "",
  category: "OTHER",
  startDateTime: "",
  endDateTime: "",
  locationType: "INDOOR",
  address: "",
  city: "",
  price: 0,
  currency: "USD",
  capacityTotal: 10,
  maxPerUser: 1,
  bookingCutoffMinutes: 60,
  bannerImageUrl: "",
  refundEnabled: false,
  refundPercent: 0,
  requiresApproval: false,
};

const badgeForStatus = (status) => {
  switch (status) {
    case "PUBLISHED": return "bg-secondary text-primary";
    case "CANCELLED": return "bg-destructive/10 text-destructive";
    case "ENDED": return "bg-muted text-muted-foreground";
    default: return "bg-muted text-muted-foreground";
  }
};

const ManageEventsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [hotels, setHotels] = useState([]);
  const [selectedHotelId, setSelectedHotelId] = useState(searchParams.get("hotelId") || "");
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState(null);
  const [saving, setSaving] = useState(false);

  const [actionLoading, setActionLoading] = useState(null);

  useEffect(() => {
    const fetchHotels = async () => {
      try {
        const res = await getMyHotels();
        const data = res.data || [];
        setHotels(data);
        if (!selectedHotelId && data.length > 0) setSelectedHotelId(String(data[0].id));
      } catch (err) {
        setError(err.response?.data?.message || "Something went wrong.");
      }
    };
    fetchHotels();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!selectedHotelId) return;
    setSearchParams({ hotelId: selectedHotelId });
    fetchEvents();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedHotelId]);

  const fetchEvents = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getEvents(selectedHotelId, { page: 0, size: 50, sortBy: "startDateTime", sortDir: "asc" });
      const data = res.data;
      setEvents(data?.content || data?.items || (Array.isArray(data) ? data : []));
    } catch (err) {
      setError(err.response?.data?.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  };

  const openCreate = () => {
    setForm(EMPTY_FORM);
    setEditingId(null);
    setFormError(null);
    setShowForm(true);
  };

  const openEdit = (event) => {
    setForm({
      title: event.title || "",
      description: event.description || "",
      category: event.category || "OTHER",
      startDateTime: event.startDateTime ? event.startDateTime.slice(0, 16) : "",
      endDateTime: event.endDateTime ? event.endDateTime.slice(0, 16) : "",
      locationType: event.locationType || "INDOOR",
      address: event.address || "",
      city: event.city || "",
      price: event.price ?? 0,
      currency: event.currency || "USD",
      capacityTotal: event.capacityTotal || 10,
      maxPerUser: event.maxPerUser || 1,
      bookingCutoffMinutes: event.bookingCutoffMinutes ?? 60,
      bannerImageUrl: event.bannerImageUrl || "",
      refundEnabled: event.refundEnabled || false,
      refundPercent: event.refundPercent || 0,
      requiresApproval: event.requiresApproval || false,
    });
    setEditingId(event.id);
    setFormError(null);
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setForm(EMPTY_FORM);
    setFormError(null);
  };

  const validateForm = () => {
    if (!form.title.trim()) return "Title is required.";
    if (!form.startDateTime) return "Start date is required.";
    if (!form.endDateTime) return "End date is required.";
    if (new Date(form.endDateTime) <= new Date(form.startDateTime)) return "End date must be after start date.";
    if (!form.address.trim()) return "Address is required.";
    if (!form.city.trim()) return "City is required.";
    if (form.capacityTotal < 1) return "Capacity must be at least 1.";
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationError = validateForm();
    if (validationError) { setFormError(validationError); return; }

    setSaving(true);
    setFormError(null);
    try {
      const payload = {
        title: form.title.trim(),
        description: form.description || undefined,
        category: form.category,
        startDateTime: form.startDateTime,
        endDateTime: form.endDateTime,
        locationType: form.locationType,
        address: form.address.trim(),
        city: form.city.trim(),
        price: parseInt(form.price) || 0,
        currency: form.currency,
        capacityTotal: parseInt(form.capacityTotal),
        maxPerUser: parseInt(form.maxPerUser),
        bookingCutoffMinutes: parseInt(form.bookingCutoffMinutes),
        bannerImageUrl: form.bannerImageUrl || undefined,
        refundEnabled: form.refundEnabled,
        refundPercent: form.refundEnabled ? parseInt(form.refundPercent) : undefined,
        requiresApproval: form.requiresApproval,
      };

      if (editingId) await updateEvent(editingId, payload);
      else await createEvent(selectedHotelId, payload);

      closeForm();
      fetchEvents();
    } catch (err) {
      setFormError(err.response?.data?.message || "Failed to save event.");
    } finally {
      setSaving(false);
    }
  };

  const handleAction = async (id, action) => {
    setActionLoading(id + action);
    try {
      if (action === "publish") await publishEvent(id);
      if (action === "cancel") await cancelEvent(id);
      if (action === "delete") await deleteEvent(id);
      await fetchEvents();
    } catch (err) {
      alert(err.response?.data?.message || `Failed to ${action} event.`);
    } finally {
      setActionLoading(null);
    }
  };

  const formatDate = (dt) => {
    if (!dt) return "—";
    return new Date(dt).toLocaleString("en-GB", {
      day: "2-digit", month: "short", year: "numeric",
      hour: "2-digit", minute: "2-digit",
    });
  };

  return (
    <>
      <div className="flex items-center justify-between flex-wrap gap-3 mb-6">
        {selectedHotelId && (
          <Button onClick={openCreate} className="h-11 rounded-2xl font-bold">
            <Plus className="h-4 w-4 mr-2" />
            Add Event
          </Button>
        )}
      </div>

      {/* Hotel selector */}
      <section className="bg-card rounded-3xl shadow-soft p-6 mb-6">
        <div className="grid md:grid-cols-[240px_1fr] gap-3 items-center">
          <Label className="font-bold">Select Hotel</Label>
          <Select value={selectedHotelId} onValueChange={(v) => setSelectedHotelId(v)}>
            <SelectTrigger className="rounded-2xl h-11">
              <SelectValue placeholder="— Choose a hotel —" />
            </SelectTrigger>
            <SelectContent>
              {hotels.map((h) => (
                <SelectItem key={h.id} value={String(h.id)}>{h.name}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </section>

      {/* Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4" onClick={closeForm}>
          <div
            className="w-full max-w-4xl bg-card rounded-3xl shadow-soft p-6"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-start justify-between gap-4 mb-4">
              <div>
                <h2 className="text-lg font-extrabold">{editingId ? "Edit Event" : "Add Event"}</h2>
                <p className="text-sm text-muted-foreground">Fill the details below.</p>
              </div>
              <Button variant="outline" className="rounded-full" onClick={closeForm}>Close</Button>
            </div>

            {formError && <ErrorMessage message={formError} />}

            <form onSubmit={handleSubmit} className="space-y-4 mt-4">
              <div className="grid md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Title *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    value={form.title}
                    onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
                    required
                    placeholder="Event title"
                  />
                </div>

                <div className="space-y-2">
                  <Label>Category *</Label>
                  <Select value={form.category} onValueChange={(v) => setForm((p) => ({ ...p, category: v }))}>
                    <SelectTrigger className="rounded-2xl h-11">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {CATEGORIES.map((c) => (
                        <SelectItem key={c} value={c}>{c}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="space-y-2">
                <Label>Description</Label>
                <textarea
                  className="w-full rounded-2xl border border-border bg-background px-3 py-2 text-sm min-h-[80px] focus:outline-none focus:ring-2 focus:ring-ring"
                  value={form.description}
                  onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                  rows={2}
                  placeholder="Optional description..."
                />
              </div>

              <div className="grid md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Start Date & Time *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="datetime-local"
                    value={form.startDateTime}
                    onChange={(e) => setForm((p) => ({ ...p, startDateTime: e.target.value }))}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label>End Date & Time *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="datetime-local"
                    value={form.endDateTime}
                    onChange={(e) => setForm((p) => ({ ...p, endDateTime: e.target.value }))}
                    required
                  />
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Location Type *</Label>
                  <Select value={form.locationType} onValueChange={(v) => setForm((p) => ({ ...p, locationType: v }))}>
                    <SelectTrigger className="rounded-2xl h-11">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {LOCATION_TYPES.map((l) => (
                        <SelectItem key={l} value={l}>{l}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>City *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    value={form.city}
                    onChange={(e) => setForm((p) => ({ ...p, city: e.target.value }))}
                    required
                    placeholder="City"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label>Address *</Label>
                <Input
                  className="h-11 rounded-2xl"
                  value={form.address}
                  onChange={(e) => setForm((p) => ({ ...p, address: e.target.value }))}
                  required
                  placeholder="Full address"
                />
              </div>

              <div className="grid md:grid-cols-4 gap-4">
                <div className="space-y-2">
                  <Label>Price</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="number"
                    min={0}
                    value={form.price}
                    onChange={(e) => setForm((p) => ({ ...p, price: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Currency</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    value={form.currency}
                    onChange={(e) => setForm((p) => ({ ...p, currency: e.target.value }))}
                    maxLength={3}
                    placeholder="USD"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Total Capacity *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="number"
                    min={1}
                    value={form.capacityTotal}
                    onChange={(e) => setForm((p) => ({ ...p, capacityTotal: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Max Per User</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="number"
                    min={1}
                    value={form.maxPerUser}
                    onChange={(e) => setForm((p) => ({ ...p, maxPerUser: e.target.value }))}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label>Banner Image URL</Label>
                <Input
                  className="h-11 rounded-2xl"
                  value={form.bannerImageUrl}
                  onChange={(e) => setForm((p) => ({ ...p, bannerImageUrl: e.target.value }))}
                  placeholder="https://..."
                />
              </div>

              <div className="grid md:grid-cols-2 gap-4">
                <div className="flex items-center gap-2 pt-2">
                  <Checkbox
                    id="refundEnabled"
                    checked={!!form.refundEnabled}
                    onCheckedChange={(v) => setForm((p) => ({ ...p, refundEnabled: !!v }))}
                  />
                  <Label htmlFor="refundEnabled" className="font-bold">Refund Enabled</Label>
                </div>

                <div className="flex items-center gap-2 pt-2">
                  <Checkbox
                    id="requiresApproval"
                    checked={!!form.requiresApproval}
                    onCheckedChange={(v) => setForm((p) => ({ ...p, requiresApproval: !!v }))}
                  />
                  <Label htmlFor="requiresApproval" className="font-bold">Requires Approval</Label>
                </div>
              </div>

              {form.refundEnabled && (
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Refund %</Label>
                    <Input
                      className="h-11 rounded-2xl"
                      type="number"
                      min={0}
                      max={100}
                      value={form.refundPercent}
                      onChange={(e) => setForm((p) => ({ ...p, refundPercent: e.target.value }))}
                    />
                  </div>
                </div>
              )}

              <div className="flex items-center justify-end gap-2 pt-2">
                <Button type="button" variant="outline" className="rounded-full" onClick={closeForm}>
                  Cancel
                </Button>
                <Button type="submit" className="rounded-2xl font-bold" disabled={saving}>
                  {saving ? "Saving..." : editingId ? "Save Changes" : "Create Event"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Events list */}
      {!selectedHotelId ? (
        <EmptyState icon={<Building2 className="h-8 w-8 text-muted-foreground" />} title="Select a hotel" message="Choose a hotel above to manage its events." />
      ) : loading ? (
        <LoadingSpinner message="Loading events..." />
      ) : error ? (
        <ErrorMessage message={error} />
      ) : events.length === 0 ? (
        <EmptyState
          icon={<CalendarDays className="h-8 w-8 text-muted-foreground" />}
          title="No events yet"
          message="Add your first event for this hotel."
          action={
            <Button onClick={openCreate} className="h-11 rounded-2xl font-bold">
              <Plus className="h-4 w-4 mr-2" />
              Add Event
            </Button>
          }
        />
      ) : (
        <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-5">
          {events.map((event) => {
            const isActioning = actionLoading?.startsWith(String(event.id));
            return (
              <div
                key={event.id}
                className="bg-card rounded-3xl overflow-hidden shadow-soft hover:-translate-y-1 hover:shadow-glow transition-all duration-300"
              >
                {event.bannerImageUrl ? (
                  <div className="aspect-[4/3] overflow-hidden">
                    <img src={event.bannerImageUrl} alt={event.title} className="w-full h-full object-cover" />
                  </div>
                ) : (
                  <div className="aspect-[4/3] bg-muted" />
                )}

                <div className="p-5 space-y-2">
                  <div className="flex items-center justify-between gap-2">
                    <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-muted text-muted-foreground">
                      {event.category}
                    </span>
                    <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold ${badgeForStatus(event.status)}`}>
                      {event.status}
                    </span>
                  </div>

                  <h3 className="font-extrabold text-base leading-snug">{event.title}</h3>
                  <p className="text-sm text-muted-foreground flex items-center gap-1"><Calendar className="h-3.5 w-3.5" /> {formatDate(event.startDateTime)}</p>
                  <p className="text-sm text-muted-foreground flex items-center gap-1"><MapPin className="h-3.5 w-3.5" /> {event.city} · {event.locationType}</p>
                  <p className="text-sm">
                    <span className="font-extrabold text-accent">
                      {event.price === 0 ? "Free" : `$${event.price} ${event.currency}`}
                    </span>
                    <span className="text-muted-foreground">
                      {" · "}{event.reservationsCount || 0} / {event.capacityTotal} booked
                    </span>
                  </p>

                  <div className="flex flex-wrap gap-2 pt-2">
                    <Button size="sm" variant="outline" className="rounded-full" onClick={() => openEdit(event)}>
                      <Pencil className="h-4 w-4 mr-2" />
                      Edit
                    </Button>

                    {event.status === "DRAFT" && (
                      <Button
                        size="sm"
                        className="rounded-full"
                        disabled={isActioning}
                        onClick={() => handleAction(event.id, "publish")}
                      >
                        <UploadCloud className="h-4 w-4 mr-2" />
                        {isActioning ? "..." : "Publish"}
                      </Button>
                    )}

                    {event.status === "PUBLISHED" && (
                      <Button
                        size="sm"
                        variant="destructive"
                        className="rounded-full"
                        disabled={isActioning}
                        onClick={() => handleAction(event.id, "cancel")}
                      >
                        <Ban className="h-4 w-4 mr-2" />
                        {isActioning ? "..." : "Cancel"}
                      </Button>
                    )}

                    {["DRAFT", "CANCELLED", "ENDED"].includes(event.status) && (
                      <Button
                        size="sm"
                        variant="destructive"
                        className="rounded-full"
                        disabled={isActioning}
                        onClick={() => handleAction(event.id, "delete")}
                      >
                        <Trash2 className="h-4 w-4 mr-2" />
                        {isActioning ? "..." : "Delete"}
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </>
  );
};

export default ManageEventsPage;