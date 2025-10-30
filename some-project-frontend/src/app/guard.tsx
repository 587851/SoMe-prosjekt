"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { getToken, clearToken } from "@/lib/auth";
import { API_BASE } from "@/lib/config";

/**
 * Beskytter innhold som krever innlogging.
 * - Sjekker lokalt token
 * - Verifiserer token mot /api/auth/me
 * - Redirecter til /login med ?next=<nåværende sti> hvis ikke autentisert
 */
export default function Guard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const [ready, setReady] = useState(false); // render først når auth er bekreftet

  useEffect(() => {
    const token = getToken();

    // Ingen token -> send til login med "next" så vi kan hoppe tilbake etterpå
    if (!token) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }

    // Verifiser token mot backend
    (async () => {
      try {
        const res = await fetch(`${API_BASE}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        // "me" returnerer enten bruker-DTO eller null; sjekk at vi har gyldig epost
        const me = await res.json().catch(() => null);
        if (!res.ok || !me?.email) {
          clearToken();
          router.replace(`/login?next=${encodeURIComponent(pathname)}`);
          return;
        }

        // Alt ok -> slipp gjennom
        setReady(true);
      } catch {
        // Nettverksfeil eller liknende -> oppførsel som ikke-innlogget
        clearToken();
        router.replace(`/login?next=${encodeURIComponent(pathname)}`);
      }
    })();
  }, [router, pathname]);

  // Vent med å rendre barna til auth er bekreftet
  if (!ready) return null; 
  return <>{children}</>;
}
