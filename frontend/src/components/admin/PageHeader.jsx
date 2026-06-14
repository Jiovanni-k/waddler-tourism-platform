import { Sparkles } from "lucide-react";

const PageHeader = ({ category, title, subtitle, action, penguin }) => (
  <section className="mb-10">
    <div className="rounded-3xl bg-gradient-hero p-8 md:p-10 grid md:grid-cols-[1fr_auto] gap-6 items-center overflow-hidden shadow-soft border border-white/50">
      <div className="space-y-3">
        {category && (
          <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-primary bg-card px-3 py-1.5 rounded-full shadow-sm">
            <Sparkles className="h-3 w-3" /> {category}
          </span>
        )}
        <h1 className="text-3xl md:text-5xl font-black text-primary-deep leading-tight tracking-tight">{title}</h1>
        {subtitle && <p className="text-muted-foreground max-w-xl font-medium text-lg leading-relaxed">{subtitle}</p>}
        {action && <div className="pt-4">{action}</div>}
      </div>
      {penguin && (
        <div className="hidden md:block">
          <img
            src={penguin}
            alt=""
            className="w-40 md:w-52 drop-shadow-2xl justify-self-end transform-gpu"
            style={{ animation: "float 4s ease-in-out infinite" }}
          />
        </div>
      )}
    </div>
  </section>
);

export default PageHeader;
