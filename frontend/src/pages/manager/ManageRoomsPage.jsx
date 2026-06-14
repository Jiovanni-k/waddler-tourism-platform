import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { Plus, Pencil, Trash2, Building2, BedDouble } from "lucide-react";
import {
  getMyHotels,
  getRooms,
  createRoom,
  updateRoom,
  deleteRoom,
} from "../../api/managerApi";
import { adminGetAllHotels } from "../../api/adminApi";
import { useAuth, ROLES } from "../../context/AuthContext";
import LoadingSpinner from "../../components/shared/LoadingSpinner";
import ErrorMessage from "../../components/shared/ErrorMessage";
import EmptyState from "../../components/shared/EmptyState";


import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";

const ROOM_TYPES = [
  "SINGLE","DOUBLE","TWIN","TRIPLE",
  "SUITE","DELUXE","PRESIDENTIAL","FAMILY","STUDIO",
];

const EMPTY_FORM = {
  name: "",
  roomType: "DOUBLE",
  description: "",
  maxCapacity: 2,
  totalRooms: 1,
  basePrice: "",
  bedType: "",
  active: true,
};

const ManageRoomsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const isAdmin = user?.role === ROLES.ADMIN || user?.roles?.includes(ROLES.ADMIN);

  const [hotels, setHotels] = useState([]);
  const [selectedHotelId, setSelectedHotelId] = useState(searchParams.get("hotelId") || "");
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Form state
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState(null);
  const [saving, setSaving] = useState(false);

  // Delete confirmation
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    const fetchHotels = async () => {
      try {
        const res = isAdmin ? await adminGetAllHotels() : await getMyHotels();
        const data = Array.isArray(res.data) ? res.data : res.data?.content || res.data?.hotels || [];
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
    fetchRooms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedHotelId]);

  const fetchRooms = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getRooms(selectedHotelId);
      const data = res.data;
      setRooms(data?.content || data?.items || (Array.isArray(data) ? data : []));
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

  const openEdit = (room) => {
    setForm({
      name: room.name || "",
      roomType: room.roomType || "DOUBLE",
      description: room.description || "",
      maxCapacity: room.maxCapacity || 2,
      totalRooms: room.totalRooms || 1,
      basePrice: room.basePrice || "",
      bedType: room.bedType || "",
      active: room.active !== false,
    });
    setEditingId(room.id);
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
    if (!form.roomType) return "Room type is required.";
    if (!form.maxCapacity || form.maxCapacity < 1) return "Max capacity must be at least 1.";
    if (!form.totalRooms || form.totalRooms < 1) return "Total rooms must be at least 1.";
    if (!form.basePrice || form.basePrice <= 0) return "Base price must be greater than 0.";
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
        name: form.name || undefined,
        roomType: form.roomType,
        description: form.description || undefined,
        maxCapacity: parseInt(form.maxCapacity),
        totalRooms: parseInt(form.totalRooms),
        basePrice: parseFloat(form.basePrice),
        bedType: form.bedType || undefined,
        active: form.active,
      };

      if (editingId) await updateRoom(selectedHotelId, editingId, payload);
      else await createRoom(selectedHotelId, payload);

      closeForm();
      fetchRooms();
    } catch (err) {
      setFormError(err.response?.data?.message || "Failed to save room.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (roomId) => {
    try {
      await deleteRoom(selectedHotelId, roomId);
      setRooms((prev) => prev.filter((r) => r.id !== roomId));
      setDeletingId(null);
    } catch (err) {
      alert(err.response?.data?.message || "Failed to delete room.");
    }
  };

  return (
    <>
      <div className="flex items-center justify-between flex-wrap gap-3 mb-6">
        {selectedHotelId && (
          <Button onClick={openCreate} className="h-11 rounded-2xl font-bold">
            <Plus className="h-4 w-4 mr-2" />
            Add Room
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
            className="w-full max-w-2xl bg-card rounded-3xl shadow-soft p-6"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-start justify-between gap-4 mb-4">
              <div>
                <h2 className="text-lg font-extrabold">{editingId ? "Edit Room" : "Add Room"}</h2>
                <p className="text-sm text-muted-foreground">Fill the details below.</p>
              </div>
              <Button variant="outline" className="rounded-full" onClick={closeForm}>Close</Button>
            </div>

            {formError && <ErrorMessage message={formError} />}

            <form onSubmit={handleSubmit} className="space-y-4 mt-4">
              <div className="grid md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Room Name</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    value={form.name}
                    onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
                    placeholder="e.g. Ocean View Suite"
                  />
                </div>

                <div className="space-y-2">
                  <Label>Room Type *</Label>
                  <Select value={form.roomType} onValueChange={(v) => setForm((p) => ({ ...p, roomType: v }))}>
                    <SelectTrigger className="rounded-2xl h-11">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {ROOM_TYPES.map((t) => (
                        <SelectItem key={t} value={t}>{t}</SelectItem>
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

              <div className="grid md:grid-cols-4 gap-4">
                <div className="space-y-2">
                  <Label>Max Capacity *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="number"
                    min={1}
                    value={form.maxCapacity}
                    onChange={(e) => setForm((p) => ({ ...p, maxCapacity: +e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Total Rooms *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="number"
                    min={1}
                    value={form.totalRooms}
                    onChange={(e) => setForm((p) => ({ ...p, totalRooms: +e.target.value }))}
                  />
                </div>
                <div className="space-y-2 md:col-span-2">
                  <Label>Price / Night *</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    type="number"
                    min={0}
                    step="0.01"
                    value={form.basePrice}
                    onChange={(e) => setForm((p) => ({ ...p, basePrice: e.target.value }))}
                    placeholder="150.00"
                  />
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4 items-end">
                <div className="space-y-2">
                  <Label>Bed Type</Label>
                  <Input
                    className="h-11 rounded-2xl"
                    value={form.bedType}
                    onChange={(e) => setForm((p) => ({ ...p, bedType: e.target.value }))}
                    placeholder="King, Queen, Twin..."
                  />
                </div>

                <div className="flex items-center gap-2 pt-2">
                  <Checkbox
                    id="active"
                    checked={!!form.active}
                    onCheckedChange={(v) => setForm((p) => ({ ...p, active: !!v }))}
                  />
                  <Label htmlFor="active" className="font-bold">Active</Label>
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-2">
                <Button type="button" variant="outline" className="rounded-full" onClick={closeForm}>
                  Cancel
                </Button>
                <Button type="submit" className="rounded-2xl font-bold" disabled={saving}>
                  {saving ? "Saving..." : editingId ? "Save Changes" : "Create Room"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Rooms list */}
      {!selectedHotelId ? (
        <EmptyState icon={<Building2 className="h-8 w-8 text-muted-foreground" />} title="Select a hotel" message="Choose a hotel above to manage its rooms." />
      ) : loading ? (
        <LoadingSpinner message="Loading rooms..." />
      ) : error ? (
        <ErrorMessage message={error} />
      ) : rooms.length === 0 ? (
        <EmptyState
          icon={<BedDouble className="h-8 w-8 text-muted-foreground" />}
          title="No rooms yet"
          message="Add your first room type to this hotel."
          action={
            <Button onClick={openCreate} className="h-11 rounded-2xl font-bold">
              <Plus className="h-4 w-4 mr-2" />
              Add Room
            </Button>
          }
        />
      ) : (
        <section className="bg-card rounded-3xl shadow-soft p-6">
          <div className="flex items-center justify-between gap-4 flex-wrap mb-4">
            <h2 className="font-extrabold text-lg">Rooms</h2>
            <p className="text-sm text-muted-foreground">{rooms.length} total</p>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="text-xs uppercase text-muted-foreground">
                <tr className="border-b border-border">
                  <th className="py-3 text-left font-bold">Name</th>
                  <th className="py-3 text-left font-bold">Type</th>
                  <th className="py-3 text-left font-bold">Capacity</th>
                  <th className="py-3 text-left font-bold">Total</th>
                  <th className="py-3 text-left font-bold">Price/Night</th>
                  <th className="py-3 text-left font-bold">Bed</th>
                  <th className="py-3 text-left font-bold">Active</th>
                  <th className="py-3 text-right font-bold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {rooms.map((room) => (
                  <tr key={room.id} className="hover:bg-muted/30">
                    <td className="py-3 font-bold">{room.name || "—"}</td>
                    <td className="py-3 text-muted-foreground">{room.roomTypeDisplayName || room.roomType}</td>
                    <td className="py-3">{room.maxCapacity}</td>
                    <td className="py-3">{room.totalRooms}</td>
                    <td className="py-3 font-bold text-accent">${Number(room.basePrice || 0).toFixed(0)}</td>
                    <td className="py-3 text-muted-foreground">{room.bedType || "—"}</td>
                    <td className="py-3">
                      <span
                        className={[
                          "inline-flex items-center px-3 py-1 rounded-full text-xs font-bold",
                          room.active ? "bg-secondary text-primary" : "bg-muted text-muted-foreground",
                        ].join(" ")}
                      >
                        {room.active ? "Yes" : "No"}
                      </span>
                    </td>
                    <td className="py-3">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          className="rounded-full"
                          onClick={() => openEdit(room)}
                        >
                          <Pencil className="h-4 w-4 mr-2" />
                          Edit
                        </Button>

                        {deletingId === room.id ? (
                          <>
                            <Button
                              size="sm"
                              variant="destructive"
                              className="rounded-full"
                              onClick={() => handleDelete(room.id)}
                            >
                              Yes, delete
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="rounded-full"
                              onClick={() => setDeletingId(null)}
                            >
                              Cancel
                            </Button>
                          </>
                        ) : (
                          <Button
                            size="sm"
                            variant="destructive"
                            className="rounded-full"
                            onClick={() => setDeletingId(room.id)}
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            Delete
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </>
  );
};

export default ManageRoomsPage;