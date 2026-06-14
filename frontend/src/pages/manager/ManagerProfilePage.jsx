import { useEffect, useState } from "react";
import { Building2, Calendar, KeyRound, Mail, Save, User } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { updateMe, changePassword } from "../../api/authApi";
import workingPenguin from "../../assets/penguins/Working.png";

const ManagerProfilePage = () => {
  const { user } = useAuth();
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    username: "",
    birth: "",
  });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [saving, setSaving] = useState(false);
  const [passwordSaving, setPasswordSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");

  useEffect(() => {
    if (!user) return;
    setForm({
      firstName: user.firstName ?? "",
      lastName: user.lastName ?? "",
      email: user.email ?? "",
      username: user.username ?? "",
      birth: user.birthDate ?? "",
    });
  }, [user]);

  const setField = (key) => (event) => {
    setForm((current) => ({ ...current, [key]: event.target.value }));
  };

  const setPasswordField = (key) => (event) => {
    setPasswordForm((current) => ({ ...current, [key]: event.target.value }));
  };

  const handleSave = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");

    try {
      await updateMe({ firstName: form.firstName, lastName: form.lastName });
      localStorage.setItem(
        "waddler_user",
        JSON.stringify({ ...user, firstName: form.firstName, lastName: form.lastName }),
      );
      setMessage("Manager profile changes saved.");
    } catch (error) {
      console.error("Failed to update manager profile:", error);
      setMessage("Failed to save profile changes.");
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSave = async (event) => {
    event.preventDefault();
    setPasswordMessage("");

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordMessage("New password and confirmation do not match.");
      return;
    }

    setPasswordSaving(true);
    try {
      await changePassword(passwordForm);
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setPasswordMessage("Password updated.");
    } catch (error) {
      console.error("Failed to update manager password:", error);
      setPasswordMessage("Failed to update password.");
    } finally {
      setPasswordSaving(false);
    }
  };

  const fullName = `${form.firstName} ${form.lastName}`.trim() || "Hotel Manager";
  const initials = `${form.firstName?.[0] || "M"}${form.lastName?.[0] || ""}`;

  return (
    <div className="space-y-6">
      <section className="rounded-3xl bg-gradient-hero p-8 md:p-10 grid md:grid-cols-[1fr_auto] gap-6 items-center overflow-hidden shadow-soft">
        <div className="space-y-3">
          <span className="inline-flex items-center gap-2 text-xs font-black uppercase tracking-widest text-primary bg-card px-3 py-1.5 rounded-full">
            <Building2 size={14} />
            Manager Account
          </span>
          <h1 className="text-3xl md:text-5xl font-bold text-primary-deep leading-tight">Manager Profile</h1>
          <p className="text-muted-foreground max-w-xl">
            Manage your hotel manager account information and sign-in details.
          </p>
        </div>
        <img src={workingPenguin} alt="" className="w-36 md:w-48 drop-shadow-xl justify-self-end hidden sm:block" />
      </section>

      <div className="grid lg:grid-cols-[260px_1fr] gap-6">
        <aside className="bg-card rounded-3xl shadow-soft p-6 text-center space-y-4 h-fit border border-border/30">
          <div className="h-24 w-24 mx-auto rounded-3xl bg-secondary text-primary grid place-items-center text-3xl font-black shadow-sm">
            {initials}
          </div>
          <div>
            <p className="font-extrabold text-lg text-primary-deep">{fullName}</p>
            <p className="text-sm text-muted-foreground font-bold break-all">{form.email || "No email available"}</p>
          </div>
          <span className="inline-flex items-center gap-2 bg-primary/15 text-primary text-xs font-black px-3 py-1.5 rounded-full uppercase tracking-widest">
            <Building2 size={14} />
            Hotel Manager
          </span>
        </aside>

        <div className="space-y-5">
          <form onSubmit={handleSave} className="bg-card rounded-3xl shadow-soft p-6 sm:p-8 space-y-6 border border-border/30">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-2xl bg-secondary text-primary grid place-items-center">
                <User size={22} />
              </div>
              <h2 className="font-extrabold text-lg text-primary-deep">Personal information</h2>
            </div>

            {message && (
              <div className="rounded-2xl bg-muted/30 px-4 py-3 text-sm font-bold text-primary-deep">
                {message}
              </div>
            )}

            <div className="grid sm:grid-cols-2 gap-4">
              <label className="space-y-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">First name</span>
                <input value={form.firstName} onChange={setField("firstName")} className="w-full h-12 px-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
              </label>
              <label className="space-y-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">Last name</span>
                <input value={form.lastName} onChange={setField("lastName")} className="w-full h-12 px-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
              </label>
              <label className="space-y-2 sm:col-span-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">Email</span>
                <div className="relative">
                  <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
                  <input type="email" value={form.email} onChange={setField("email")} className="w-full h-12 pl-11 pr-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
                </div>
              </label>
              <label className="space-y-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">Username</span>
                <input value={form.username} onChange={setField("username")} className="w-full h-12 px-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
              </label>
              <label className="space-y-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">Birth date</span>
                <div className="relative">
                  <Calendar size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" />
                  <input type="date" value={form.birth} onChange={setField("birth")} className="w-full h-12 pl-11 pr-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
                </div>
              </label>
            </div>

            <div className="flex justify-end">
              <button type="submit" disabled={saving} className="rounded-full bg-primary text-primary-foreground hover:opacity-90 font-bold px-8 h-12 inline-flex items-center gap-2 disabled:opacity-60">
                <Save size={18} />
                {saving ? "Saving..." : "Save changes"}
              </button>
            </div>
          </form>

          <form onSubmit={handlePasswordSave} className="bg-card rounded-3xl shadow-soft p-6 sm:p-8 space-y-5 border border-border/30">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-2xl bg-accent/15 text-accent grid place-items-center">
                <KeyRound size={22} />
              </div>
              <h2 className="font-extrabold text-lg text-primary-deep">Change password</h2>
            </div>

            {passwordMessage && (
              <div className="rounded-2xl bg-muted/30 px-4 py-3 text-sm font-bold text-primary-deep">
                {passwordMessage}
              </div>
            )}

            <div className="grid sm:grid-cols-3 gap-4">
              <label className="space-y-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">Current password</span>
                <input type="password" value={passwordForm.currentPassword} onChange={setPasswordField("currentPassword")} className="w-full h-12 px-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
              </label>
              <label className="space-y-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">New password</span>
                <input type="password" value={passwordForm.newPassword} onChange={setPasswordField("newPassword")} className="w-full h-12 px-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
              </label>
              <label className="space-y-2">
                <span className="text-xs font-black text-muted-foreground uppercase tracking-widest">Confirm password</span>
                <input type="password" value={passwordForm.confirmPassword} onChange={setPasswordField("confirmPassword")} className="w-full h-12 px-4 rounded-2xl bg-white border-2 border-border/30 focus:border-primary outline-none font-bold" />
              </label>
            </div>

            <div className="flex justify-end">
              <button type="submit" disabled={passwordSaving} className="rounded-full bg-primary text-primary-foreground hover:opacity-90 font-bold px-8 h-12 disabled:opacity-60">
                {passwordSaving ? "Updating..." : "Update password"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ManagerProfilePage;
