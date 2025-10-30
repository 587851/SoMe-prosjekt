// src/app/page.tsx
"use client";

import { useState } from "react";
import styles from "@/app/page.module.css";
import Guard from "./guard";          
import Feed, { Composer } from "./feed";

// Faner brukeren kan velge mellom
type Tab = "following" | "all" | "popular-day" | "popular-week";

export default function HomePage() {
  // Holder styr på hvilken fane som er aktiv
  const [tab, setTab] = useState<Tab>("following");

  // Oversett fanen til riktig Feed-modus
  const mode =
    tab === "following" ? "home" :
    tab === "all" ? "global" :
    tab === "popular-day" ? "popular-day" :
    "popular-week";

  return (
    <Guard>
      <main className={styles.feedLayout}>
        <section className={styles.feed}>
          {/* Composer: skjema for å lage nytt innlegg, vises alltid på forsiden */}
          <Composer />

          {/* Fane-knapper for å bytte feed-type */}
          <div
            style={{ display: "flex", gap: ".5rem", marginBottom: ".75rem" }}
            role="tablist"
            aria-label="Feed-filter"
          >
            <button
              role="tab"
              aria-selected={tab === "following"}
              className={`btn ${tab === "following" ? "" : "btn-outline"}`}
              onClick={() => setTab("following")}
            >
              Følger
            </button>
            <button
              role="tab"
              aria-selected={tab === "all"}
              className={`btn ${tab === "all" ? "" : "btn-outline"}`}
              onClick={() => setTab("all")}
            >
              Alle
            </button>
            <button
              role="tab"
              aria-selected={tab === "popular-day"}
              className={`btn ${tab === "popular-day" ? "" : "btn-outline"}`}
              onClick={() => setTab("popular-day")}
              title="Mest populære siste 24 timer"
            >
              Populært (24t)
            </button>
            <button
              role="tab"
              aria-selected={tab === "popular-week"}
              className={`btn ${tab === "popular-week" ? "" : "btn-outline"}`}
              onClick={() => setTab("popular-week")}
              title="Mest populære siste 7 dager"
            >
              Populært (7d)
            </button>
          </div>

          {/* Render feed for valgt fane.
              key= gjør at SWR henter på nytt når man bytter fane */}
          <Feed key={`feed:${tab}`} mode={mode as any} />
        </section>
      </main>
    </Guard>
  );
}
