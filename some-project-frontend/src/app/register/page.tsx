"use client"; 

import { useMemo, useState } from "react";
import { API_BASE } from "@/lib/config";        
import { saveToken } from "@/lib/auth";        
import { useRouter, useSearchParams } from "next/navigation";

export default function Register() {

  const [email, setEmail] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const router = useRouter();
  const params = useSearchParams();

  // "next"-parameter brukes til å sende brukeren videre etter registrering
  // hvis ikke satt (eller ugyldig), send til "/"
  const next = useMemo(() => {
    const n = params.get("next");
    return n && n.startsWith("/") ? n : "/";
  }, [params]);

  // håndter innsending av register-skjema
  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); 
    setErr(null);

    // kall backend sitt /api/auth/register-endepunkt
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password, displayName }),
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
      setErr(data?.message || "Register failed");
      return;
    }

    saveToken(data.token);

    router.replace(next);
  };

  return (
    <main className="center-layout">
      <section className="center-col">
        <form
          onSubmit={onSubmit}
          className="card auth-card"
          style={{ display: "grid", gap: ".75rem" }}
        >
          <h2>Opprett bruker</h2>
          <input
            className="input"
            placeholder="Visningsnavn"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
          />
          <input
            className="input"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
          <input
            className="input"
            placeholder="Passord"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
          />
          {err && <div style={{ color: "crimson" }}>{err}</div>}
          <button className="btn">Registrer</button>
        </form>
      </section>
    </main>
  );
}
