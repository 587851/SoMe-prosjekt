"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import LogoutBtn from "./LogoutBtn";
import { getToken } from "@/lib/auth";

/**
 * HeaderAuth
 *
 * Viser enten:
 *  - Logout-knapp hvis bruker er innlogget
 *  - "Logg inn" og "Registrer" linker hvis ikke
 *
 * Holder seg oppdatert når:
 *  - token i localStorage endres (fra en annen fane)
 *  - eller når vi dispatch-er custom "auth:changed"-event i appen
 */
export default function HeaderAuth() {
  const [loggedIn, setLoggedIn] = useState<boolean | null>(null);

  useEffect(() => {
    // sjekker token i localStorage
    const sync = () => setLoggedIn(Boolean(getToken()));
    sync();

    // lytter på custom event fra resten av appen
    const onAuthChanged = () => sync();
    window.addEventListener("auth:changed", onAuthChanged);

    // lytter på storage-event (f.eks. logg ut i annen fane)
    const onStorage = (e: StorageEvent) => {
      if (e.key === "token" || e.key === null) sync();
    };
    window.addEventListener("storage", onStorage);

    return () => {
      window.removeEventListener("auth:changed", onAuthChanged);
      window.removeEventListener("storage", onStorage);
    };
  }, []);

  // mens vi ikke vet auth-status → ikke render noe
  if (loggedIn === null) return null; 

  return loggedIn ? (
    <LogoutBtn />
  ) : (
    <div style={{ display: "flex", gap: ".4rem" }}>
      <Link href="/login" className="btn-ghost btn-ghost-sm">Logg inn</Link>
      <Link href="/register" className="btn-ghost btn-ghost-sm">Registrer</Link>
    </div>
  );
}
