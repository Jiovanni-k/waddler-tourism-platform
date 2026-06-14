import AdminLayout from "../../components/admin/AdminLayout";
import PageHeader from "../../components/admin/PageHeader";
import thinkingPenguin from "../../assets/penguins/Thinking.png";

const AdminHotelsPage = () => (
  <AdminLayout>
    <PageHeader 
      category="CONSTRUCTION"
      title="Hotel Registry"
      subtitle="The full hotel directory is currently under construction. Stay tuned for advanced management tools!"
      penguin={thinkingPenguin}
    />
    <div className="bg-card rounded-[32px] shadow-soft p-16 text-center border-2 border-dashed border-border/50">
       <h3 className="text-xl font-black text-primary-deep">Coming Soon! 🐧</h3>
       <p className="text-muted-foreground font-bold">We're waddling as fast as we can to get this feature ready.</p>
    </div>
  </AdminLayout>
);
export default AdminHotelsPage;
