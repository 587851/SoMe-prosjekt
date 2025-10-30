"use client";

import { useMemo, useState } from "react";
import { API_BASE } from "@/lib/config";        
import { saveToken } from "@/lib/auth";         
import { useRouter, useSearchParams } from "next/navigation";

export default function Login() {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const router = useRouter();
  const params = useSearchParams();

  // "next"-parameter: hvor brukeren skal sendes etter innlogging
  // hvis param ikke finnes eller ikke starter med "/", send til "/"
  const next = useMemo(() => {
    const n = params.get("next");
    return n && n.startsWith("/") ? n : "/";
  }, [params]);

  // håndter innsending av login-skjema
  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();  
    setErr(null);

    // kall backend sitt /api/auth/login endepunkt
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) { 
      setErr(data?.message || "Login failed"); 
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
          <h2>Logg inn</h2>
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
            autoComplete="current-password"
          />
          {err && <div style={{ color: "crimson" }}>{err}</div>}
          <button className="btn">Logg inn</button>
        </form>
      </section>
    </main>
  );
}
