import { Moon, Sun } from "lucide-react";
import { useLocation } from "react-router-dom";
import { useTheme } from "@/context/ThemeContext";

interface Props {
  className?: string;
  floating?: boolean;
}

export const ThemeToggle = ({ className = "", floating = false }: Props) => {
  const { isDark, setTheme } = useTheme();
  const { pathname } = useLocation();

  if (floating) {
    if (pathname === "/") return null;
    return (
      <button
        type="button"
        aria-label={isDark ? "Switch to light mode" : "Switch to dark mode"}
        title={isDark ? "Light mode" : "Dark mode"}
        onClick={() => setTheme(isDark ? "light" : "dark")}
        className="fixed bottom-6 right-6 z-50 h-11 w-11 rounded-full shadow-lg grid place-items-center transition-all bg-card border border-border text-primary hover:scale-110"
      >
        {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
      </button>
    );
  }

  return (
    <button
      type="button"
      aria-label={isDark ? "Switch to light mode" : "Switch to dark mode"}
      title={isDark ? "Light mode" : "Dark mode"}
      onClick={() => setTheme(isDark ? "light" : "dark")}
      className={`h-10 w-10 rounded-full grid place-items-center transition-colors bg-muted text-primary hover:bg-secondary ${className}`}
    >
      {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </button>
  );
};
