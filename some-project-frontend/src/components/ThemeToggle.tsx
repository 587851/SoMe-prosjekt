"use client";

import { useEffect, useState } from "react";

export default function ThemeToggle() {
  const [dark, setDark] = useState(false);

  useEffect(() => {
    document.documentElement.classList.toggle("dark", dark);
  }, [dark]);

  return (
    <button
      className="theme-toggle"
      onClick={() => setDark((v) => !v)}
      aria-pressed={dark}
      aria-label={dark ? "Bytt til lyst modus" : "Bytt til mørkt modus"}
      title={dark ? "Bytt til lyst modus" : "Bytt til mørkt modus"}
    >
      <span className="icon sun" aria-hidden="true">☀️</span>
      <span className="icon moon" aria-hidden="true">🌙</span>
    </button>
  );
}
