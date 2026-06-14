import { useState, useEffect } from "react";
import { 
  Search, 
  UserPlus,
  Shield,
  User as UserIcon,
  MoreHorizontal,
  Mail,
  Calendar,
  Filter,
  X,
  CheckCircle2,
  XCircle
} from "lucide-react";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import { adminGetAllUsers, adminUpdateUser } from "../../api/adminApi";
import curiousPenguin from "../../assets/penguins/Curious.png";

const EMPTY_INVITE = {
  firstName: "",
  lastName: "",
  email: "",
  role: "USER"
};

const ROLE_OPTIONS = [
  { value: "ALL", label: "All roles" },
  { value: "USER", label: "Users" },
  { value: "HOTEL_MANAGER", label: "Hotel managers" },
  { value: "ADMIN", label: "Admins" },
  { value: "PENDING_INVITE", label: "Pending invites" }
];

const formatRole = (role) => {
  if (role === "PENDING_INVITE") return "Pending invite";
  return role?.replace("_", " ") || "User";
};

const AdminUsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [inviteForm, setInviteForm] = useState(EMPTY_INVITE);
  const [inviteError, setInviteError] = useState("");
  const [openMenuId, setOpenMenuId] = useState(null);
  const [actingId, setActingId] = useState(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const res = await adminGetAllUsers();
      const raw = res.data;
      setUsers(Array.isArray(raw) ? raw : Array.isArray(raw?.content) ? raw.content : []);
    } catch (err) {
      console.error(err);
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  const handleInviteSubmit = (event) => {
    event.preventDefault();
    const email = inviteForm.email.trim().toLowerCase();
    if (!inviteForm.firstName.trim() || !email) {
      setInviteError("First name and email are required.");
      return;
    }
    if (users.some((user) => user.email?.toLowerCase() === email)) {
      setInviteError("A user with this email already exists.");
      return;
    }

    const invitedUser = {
      id: `invite-${Date.now()}`,
      firstName: inviteForm.firstName.trim(),
      lastName: inviteForm.lastName.trim(),
      email,
      role: inviteForm.role,
      active: false,
      invited: true,
      createdAt: new Date().toISOString()
    };

    setUsers((prev) => [invitedUser, ...prev]);
    setInviteForm(EMPTY_INVITE);
    setInviteError("");
    setShowInviteModal(false);
  };

  const handleUserPatch = async (user, patch) => {
    setActingId(user.id);
    setOpenMenuId(null);
    setUsers((prev) => prev.map((item) => item.id === user.id ? { ...item, ...patch } : item));

    if (String(user.id).startsWith("invite-")) {
      setActingId(null);
      return;
    }

    try {
      await adminUpdateUser(user.id, patch);
    } catch (err) {
      console.error("User update failed:", err);
      setUsers((prev) => prev.map((item) => item.id === user.id ? user : item));
    } finally {
      setActingId(null);
    }
  };

  const normalizedSearch = searchQuery.trim().toLowerCase();
  const filteredUsers = users.filter((u) => {
    const matchesSearch =
      !normalizedSearch ||
      `${u.firstName || ""} ${u.lastName || ""}`.toLowerCase().includes(normalizedSearch) ||
      u.email?.toLowerCase().includes(normalizedSearch);

    const roleKey = u.invited ? "PENDING_INVITE" : u.role;
    const matchesRole = roleFilter === "ALL" || roleKey === roleFilter;

    return matchesSearch && matchesRole;
  });

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
        category="PEOPLE"
        title="Manage users"
        subtitle="Promote managers, deactivate accounts, and review user activity in real-time."
        penguin={curiousPenguin}
        action={
          <button
            type="button"
            onClick={() => setShowInviteModal(true)}
            className="btn bg-primary text-primary-foreground hover:opacity-90 shadow-soft px-6 py-2.5 rounded-full font-bold flex items-center gap-2 transition-all"
          >
            <UserPlus size={18} />
            Invite new user
          </button>
        }
      />

      {showInviteModal && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <form
            onSubmit={handleInviteSubmit}
            className="bg-card rounded-[32px] w-full max-w-lg shadow-glow p-8 border border-white/20 animate-in zoom-in duration-300"
          >
            <div className="flex items-start justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-black text-primary-deep">Invite user</h2>
                <p className="text-sm font-bold text-muted-foreground mt-1">
                  Add a pending invite to this admin view.
                </p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setShowInviteModal(false);
                  setInviteError("");
                }}
                className="p-2 text-muted-foreground hover:text-destructive hover:bg-muted/20 rounded-xl transition-all"
              >
                <X size={20} />
              </button>
            </div>

            {inviteError && (
              <div className="mb-4 rounded-2xl bg-destructive/10 text-destructive px-4 py-3 text-sm font-bold">
                {inviteError}
              </div>
            )}

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
              <label className="text-xs font-black text-muted-foreground uppercase tracking-widest">
                First name
                <input
                  type="text"
                  value={inviteForm.firstName}
                  onChange={(e) => setInviteForm((form) => ({ ...form, firstName: e.target.value }))}
                  className="mt-2 w-full px-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm normal-case tracking-normal text-primary-deep"
                />
              </label>
              <label className="text-xs font-black text-muted-foreground uppercase tracking-widest">
                Last name
                <input
                  type="text"
                  value={inviteForm.lastName}
                  onChange={(e) => setInviteForm((form) => ({ ...form, lastName: e.target.value }))}
                  className="mt-2 w-full px-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm normal-case tracking-normal text-primary-deep"
                />
              </label>
            </div>

            <label className="block text-xs font-black text-muted-foreground uppercase tracking-widest mb-4">
              Email
              <input
                type="email"
                value={inviteForm.email}
                onChange={(e) => setInviteForm((form) => ({ ...form, email: e.target.value }))}
                className="mt-2 w-full px-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm normal-case tracking-normal text-primary-deep"
              />
            </label>

            <label className="block text-xs font-black text-muted-foreground uppercase tracking-widest mb-8">
              Role
              <select
                value={inviteForm.role}
                onChange={(e) => setInviteForm((form) => ({ ...form, role: e.target.value }))}
                className="mt-2 w-full px-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm normal-case tracking-normal text-primary-deep"
              >
                <option value="USER">User</option>
                <option value="HOTEL_MANAGER">Hotel manager</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>

            <div className="flex flex-col sm:flex-row gap-3">
              <button
                type="button"
                onClick={() => setShowInviteModal(false)}
                className="flex-1 py-3 bg-muted text-muted-foreground font-bold rounded-xl hover:bg-muted/80 transition-all"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex-1 py-3 bg-primary text-primary-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft"
              >
                Send invite
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-card rounded-[32px] shadow-soft overflow-hidden border border-border/30">
        <div className="p-6 border-b border-border/30 flex flex-col md:flex-row gap-4 items-center justify-between bg-muted/5">
          <div className="relative w-full md:w-96">
            <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input 
              type="text" 
              placeholder="Search by name or email..." 
              className="w-full pl-12 pr-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm transition-all"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          
          <label className="flex items-center gap-2 w-full md:w-auto px-5 py-3 bg-white border-2 border-border/30 rounded-2xl font-black text-sm text-primary-deep shadow-sm">
            <Filter size={18} className="text-primary" />
            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              className="bg-transparent outline-none font-black text-sm flex-1 md:flex-none cursor-pointer"
            >
              {ROLE_OPTIONS.map((option) => (
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
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">User Profile</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Role & Permissions</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Joined Date</th>
                <th className="p-6 text-xs font-black text-muted-foreground uppercase tracking-widest">Status</th>
                <th className="p-6 text-right text-xs font-black text-muted-foreground uppercase tracking-widest">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/30">
              {filteredUsers.map((u) => (
                <tr key={u.id} className="hover:bg-muted/5 transition-colors group">
                  <td className="p-6">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-2xl bg-secondary text-primary flex items-center justify-center text-sm font-black shadow-sm group-hover:scale-105 transition-transform">
                        {u.firstName?.[0] || "U"}{u.lastName?.[0] || ""}
                      </div>
                      <div>
                        <div className="font-black text-primary-deep text-base">{u.firstName} {u.lastName}</div>
                        <div className="flex items-center gap-1.5 text-xs text-muted-foreground font-bold">
                          <Mail size={12} />
                          {u.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="p-6">
                    <div className={`
                      inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-black tracking-wide
                      ${u.role === "HOTEL_MANAGER" ? "bg-accent/15 text-accent" : "bg-primary/15 text-primary"}
                    `}>
                      {u.role === "HOTEL_MANAGER" ? <Shield size={14} /> : <UserIcon size={14} />}
                      {formatRole(u.role)}
                    </div>
                  </td>
                  <td className="p-6">
                    <div className="flex items-center gap-2 text-sm font-bold text-muted-foreground">
                      <Calendar size={14} />
                      {u.createdAt?.split('T')[0] || "May 6, 2026"}
                    </div>
                  </td>
                  <td className="p-6">
                    <span className={`
                      inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest
                      ${u.invited ? "bg-accent/15 text-accent" : u.active ? "bg-success/15 text-success" : "bg-destructive/15 text-destructive"}
                    `}>
                      <span className="w-1.5 h-1.5 rounded-full bg-current" />
                      {u.invited ? "Pending invite" : u.active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="p-6 text-right relative">
                    <button
                      type="button"
                      disabled={actingId === u.id}
                      onClick={() => setOpenMenuId((id) => id === u.id ? null : u.id)}
                      className="p-2 text-muted-foreground hover:bg-muted/20 rounded-xl transition-all disabled:opacity-50"
                    >
                      <MoreHorizontal size={20} />
                    </button>
                    {openMenuId === u.id && (
                      <div className="absolute right-6 top-14 z-20 w-56 bg-card border border-border/40 rounded-2xl shadow-soft p-2 text-left">
                        <button
                          type="button"
                          onClick={() => handleUserPatch(u, { active: !u.active, invited: false })}
                          className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-primary-deep hover:bg-muted/20 transition-all"
                        >
                          {u.active ? <XCircle size={16} className="text-destructive" /> : <CheckCircle2 size={16} className="text-success" />}
                          {u.active ? "Deactivate user" : "Activate user"}
                        </button>
                        <button
                          type="button"
                          onClick={() => handleUserPatch(u, { role: u.role === "HOTEL_MANAGER" ? "USER" : "HOTEL_MANAGER" })}
                          className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-primary-deep hover:bg-muted/20 transition-all"
                        >
                          <Shield size={16} className="text-accent" />
                          {u.role === "HOTEL_MANAGER" ? "Make regular user" : "Make hotel manager"}
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {filteredUsers.length === 0 && (
            <div className="p-20 text-center flex flex-col items-center">
              <div className="w-16 h-16 bg-muted/20 rounded-full flex items-center justify-center text-muted-foreground mb-4">
                 <Search size={32} />
              </div>
              <h3 className="text-xl font-black text-primary-deep mb-1">No users found</h3>
              <p className="text-muted-foreground font-bold text-sm">We couldn't find any users matching your search.</p>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminUsersPage;
