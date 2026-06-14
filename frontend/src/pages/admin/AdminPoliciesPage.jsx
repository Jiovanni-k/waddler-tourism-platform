import { useState } from "react";
import { 
  Plus, 
  FileText, 
  Settings, 
  Trash2,
  Clock,
  X
} from "lucide-react";
import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import planningPenguin from "../../assets/penguins/Planning.png";

const INITIAL_POLICIES = [
  { id: 1, title: "Terms of Service", lastUpdated: "2026-05-01", status: "Active", version: "2.4" },
  { id: 2, title: "Privacy Policy", lastUpdated: "2026-04-15", status: "Active", version: "1.9" },
  { id: 3, title: "Hotel Partner Agreement", lastUpdated: "2026-05-05", status: "Draft", version: "3.0-RC" },
  { id: 4, title: "Cancellation Policy", lastUpdated: "2025-12-20", status: "Active", version: "1.2" }
];

const EMPTY_POLICY_FORM = {
  title: "",
  status: "Draft",
  version: "1.0"
};

const AdminPoliciesPage = () => {
  const [policies, setPolicies] = useState(INITIAL_POLICIES);
  const [policyForm, setPolicyForm] = useState(EMPTY_POLICY_FORM);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingPolicyId, setEditingPolicyId] = useState(null);
  const [deletePolicy, setDeletePolicy] = useState(null);
  const [formError, setFormError] = useState("");

  const isEditing = editingPolicyId !== null;

  const openCreateForm = () => {
    setEditingPolicyId(null);
    setPolicyForm(EMPTY_POLICY_FORM);
    setFormError("");
    setIsFormOpen(true);
  };

  const openEditForm = (policy) => {
    setEditingPolicyId(policy.id);
    setPolicyForm({
      title: policy.title,
      status: policy.status,
      version: policy.version
    });
    setFormError("");
    setIsFormOpen(true);
  };

  const closeForm = () => {
    setEditingPolicyId(null);
    setPolicyForm(EMPTY_POLICY_FORM);
    setFormError("");
    setIsFormOpen(false);
  };

  const handleSavePolicy = (event) => {
    event.preventDefault();
    const title = policyForm.title.trim();
    const version = policyForm.version.trim();

    if (!title || !version) {
      setFormError("Policy title and version are required.");
      return;
    }

    const today = new Date().toISOString().slice(0, 10);

    if (isEditing) {
      setPolicies((prev) => prev.map((policy) =>
        policy.id === editingPolicyId
          ? { ...policy, title, status: policyForm.status, version, lastUpdated: today }
          : policy
      ));
    } else {
      setPolicies((prev) => [
        {
          id: Date.now(),
          title,
          status: policyForm.status,
          version,
          lastUpdated: today
        },
        ...prev
      ]);
    }

    closeForm();
  };

  const confirmDelete = () => {
    if (!deletePolicy) return;
    setPolicies((prev) => prev.filter((policy) => policy.id !== deletePolicy.id));
    setDeletePolicy(null);
  };

  return (
    <AdminLayout>
      <PageHeader
        category="LEGAL"
        title="App Policies"
        subtitle="Manage the legal framework and community guidelines of the Waddler ecosystem."
        penguin={planningPenguin}
        action={
          <button
            type="button"
            onClick={openCreateForm}
            className="btn bg-primary text-primary-foreground hover:opacity-90 shadow-soft px-6 py-2.5 rounded-full font-bold flex items-center gap-2 transition-all"
          >
            <Plus size={18} />
            Create new policy
          </button>
        }
      />

      {isFormOpen && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <form
            onSubmit={handleSavePolicy}
            className="bg-card rounded-[32px] w-full max-w-lg shadow-glow p-8 border border-white/20 animate-in zoom-in duration-300"
          >
            <div className="flex items-start justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-black text-primary-deep">
                  {isEditing ? "Edit policy" : "Create policy"}
                </h2>
                <p className="text-sm font-bold text-muted-foreground mt-1">
                  {isEditing ? "Update the policy card details." : "Add a new policy card to the admin page."}
                </p>
              </div>
              <button
                type="button"
                onClick={closeForm}
                className="p-2 text-muted-foreground hover:text-destructive hover:bg-muted/20 rounded-xl transition-all"
              >
                <X size={20} />
              </button>
            </div>

            {formError && (
              <div className="mb-4 rounded-2xl bg-destructive/10 text-destructive px-4 py-3 text-sm font-bold">
                {formError}
              </div>
            )}

            <label className="block text-xs font-black text-muted-foreground uppercase tracking-widest mb-4">
              Policy title
              <input
                type="text"
                value={policyForm.title}
                onChange={(event) => setPolicyForm((form) => ({ ...form, title: event.target.value }))}
                className="mt-2 w-full px-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm normal-case tracking-normal text-primary-deep"
              />
            </label>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
              <label className="block text-xs font-black text-muted-foreground uppercase tracking-widest">
                Status
                <select
                  value={policyForm.status}
                  onChange={(event) => setPolicyForm((form) => ({ ...form, status: event.target.value }))}
                  className="mt-2 w-full px-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm normal-case tracking-normal text-primary-deep"
                >
                  <option value="Draft">Draft</option>
                  <option value="Active">Active</option>
                </select>
              </label>

              <label className="block text-xs font-black text-muted-foreground uppercase tracking-widest">
                Version
                <input
                  type="text"
                  value={policyForm.version}
                  onChange={(event) => setPolicyForm((form) => ({ ...form, version: event.target.value }))}
                  className="mt-2 w-full px-4 py-3 bg-white border-2 border-border/30 rounded-2xl focus:border-primary outline-none font-bold text-sm normal-case tracking-normal text-primary-deep"
                />
              </label>
            </div>

            <div className="flex flex-col sm:flex-row gap-3">
              <button
                type="button"
                onClick={closeForm}
                className="flex-1 py-3 bg-muted text-muted-foreground font-bold rounded-xl hover:bg-muted/80 transition-all"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex-1 py-3 bg-primary text-primary-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft"
              >
                {isEditing ? "Save changes" : "Create policy"}
              </button>
            </div>
          </form>
        </div>
      )}

      {deletePolicy && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-card rounded-[32px] w-full max-w-md shadow-glow p-8 border border-white/20 animate-in zoom-in duration-300">
            <div className="w-16 h-16 bg-destructive/10 rounded-2xl flex items-center justify-center mb-6 text-destructive">
              <Trash2 size={32} />
            </div>
            <h2 className="text-2xl font-black text-primary-deep mb-2">Delete policy?</h2>
            <p className="text-muted-foreground font-bold text-sm mb-6">
              This will remove "{deletePolicy.title}" from the policies page.
            </p>

            <div className="flex flex-col sm:flex-row gap-3">
              <button
                type="button"
                onClick={() => setDeletePolicy(null)}
                className="flex-1 py-3 bg-muted text-muted-foreground font-bold rounded-xl hover:bg-muted/80 transition-all"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={confirmDelete}
                className="flex-1 py-3 bg-destructive text-destructive-foreground font-bold rounded-xl hover:opacity-90 transition-all shadow-soft"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
        {policies.map(p => (
          <article key={p.id} className="bg-card rounded-[32px] shadow-soft p-8 border border-border/30 hover:border-primary/30 transition-all group">
            <div className="flex justify-between items-start mb-6">
              <div className="w-14 h-14 bg-secondary rounded-2xl flex items-center justify-center text-primary shadow-sm group-hover:scale-105 transition-transform">
                <FileText size={28} />
              </div>
              <span className={`
                px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest
                ${p.status === "Active" ? "bg-success/15 text-success" : "bg-primary/15 text-primary"}
              `}>
                {p.status}
              </span>
            </div>

            <h3 className="text-xl font-black text-primary-deep mb-2">{p.title}</h3>
            <div className="flex items-center gap-4 text-xs font-bold text-muted-foreground mb-8">
              <div className="flex items-center gap-1.5">
                <Clock size={14} className="text-primary" />
                Updated {p.lastUpdated}
              </div>
              <div className="flex items-center gap-1.5">
                <Settings size={14} className="text-primary" />
                v{p.version}
              </div>
            </div>

            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => openEditForm(p)}
                className="flex-1 py-3 bg-muted/30 text-primary-deep font-bold rounded-xl hover:bg-muted/50 transition-all text-sm"
              >
                Edit
              </button>
              <button
                type="button"
                onClick={() => setDeletePolicy(p)}
                className="px-4 py-3 bg-white border-2 border-border/30 rounded-xl text-muted-foreground hover:text-destructive hover:border-destructive/30 transition-all"
              >
                <Trash2 size={18} />
              </button>
            </div>
          </article>
        ))}
      </div>
    </AdminLayout>
  );
};

export default AdminPoliciesPage;
