"use client";
import { useEffect, useState, useCallback } from "react";
import { API_BASE } from "@/lib/config";
import { getToken } from "@/lib/auth";

type AuthUser = { id: string; email: string; displayName: string } | null;

/**
 * IfLoggedIn
 * - Renderer barna kun hvis bruker er innlogget.
 * - Leser token fra localStorage og verifiserer via /api/auth/me.
 * - Lytter på "auth:changed" for å re-fetche ved login/logout/avatar-endring.
 */
export default function IfLoggedIn({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | undefined>(undefined); // undefined = laster, null = ikke innlogget
  
  const fetchUser = useCallback(async (signal?: AbortSignal) => {
    const token = getToken();
    if (!token) {
      setUser(null);
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
        signal,
      });
      setUser(res.ok ? await res.json() : null);
    } catch {
      // Nettverksfeil: behandle som ikke innlogget
      setUser(null);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetchUser(controller.signal);

    const handler = () => fetchUser(controller.signal);
    window.addEventListener("auth:changed", handler);

    return () => {
      window.removeEventListener("auth:changed", handler);
      controller.abort(); 
    };
  }, [fetchUser]);

  // Mens vi ikke vet noe enda: render ingenting 
  if (user === undefined) return null;

  // Ikke innlogget: ikke render barna
  if (!user) return null;

  // Innlogget
  return <>{children}</>;
}
