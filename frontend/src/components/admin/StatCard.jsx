import React from "react";
import { ArrowUpRight, ArrowDownRight } from "lucide-react";

const StatCard = ({ title, value, icon: Icon, trend, variant = "default", color = "purple" }) => {
  const bgColors = {
    purple: "bg-secondary/40",
    gold: "bg-accent/15",
    green: "bg-success/15",
    gray: "bg-muted/30"
  };
  const textColors = {
    purple: "text-primary",
    gold: "text-accent",
    green: "text-success",
    gray: "text-muted-foreground"
  };

  if (variant === "simple") {
    return (
      <div className="bg-card rounded-3xl p-8 shadow-soft flex flex-col gap-4 border border-border/30">
        <span className={`${bgColors[color] || bgColors.purple} ${textColors[color] || textColors.purple} text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full w-fit shadow-sm`}>
          {title}
        </span>
        <div className="text-4xl font-black text-primary-deep tracking-tight">
          {value}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-card rounded-3xl p-6 shadow-soft flex flex-col gap-5 border border-border/30 hover:border-primary/20 transition-all group">
      <div className="flex justify-between items-start">
        <div className="w-12 h-12 rounded-2xl bg-secondary flex items-center justify-center text-primary group-hover:scale-110 transition-transform shadow-sm">
          {Icon && <Icon size={24} />}
        </div>
        {trend && (
          <div className={`flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-black tracking-tighter ${trend.startsWith("+") ? "bg-success/15 text-success" : "bg-destructive/15 text-destructive"}`}>
            {trend.startsWith("+") ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
            {trend}
          </div>
        )}
      </div>

      <div>
        <div className="text-xs font-black text-muted-foreground uppercase tracking-widest mb-1">
          {title}
        </div>
        <div className="text-3xl font-black text-primary-deep tracking-tight">
          {value}
        </div>
      </div>
    </div>
  );
};

export default StatCard;
