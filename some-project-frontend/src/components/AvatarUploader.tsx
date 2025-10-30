"use client";
import { useState } from "react";
import { API_BASE } from "@/lib/config";
import { getToken } from "@/lib/auth";

/**
 * AvatarUploader
 *
 * - Lar brukeren velge en bildefil fra disk
 * - Sender filen til backend via multipart/form-data
 * - Viser status ("Uploading…"), feil og forhåndsvisning av ny avatar
 */
export default function AvatarUploader() {
  const [url, setUrl] = useState<string | null>(null);   
  const [busy, setBusy] = useState(false);               
  const [err, setErr] = useState<string | null>(null);   

  async function onChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    setErr(null);
    setBusy(true);

    try {
      const token = getToken();
      const form = new FormData();
      form.append("file", file);

      const res = await fetch(`${API_BASE}/api/files/avatar`, {
        method: "POST",
        headers: token ? { Authorization: `Bearer ${token}` } : {}, 
        body: form,
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data?.message || "Upload failed");

      setUrl(data.avatarPath);

      // Oppdater UI
      window.dispatchEvent(new Event("auth-changed"));
    } catch (e: any) {
      setErr(e.message ?? "Upload failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div style={{ display: "grid", gap: ".5rem" }}>
      <input type="file" accept="image/*" onChange={onChange} disabled={busy} />
      {busy && <div>Uploading…</div>}
      {err && <div style={{ color: "crimson" }}>{err}</div>}
      {url && (
        <img
          src={`${API_BASE}${url}`} 
          alt="avatar"
          width={80}
          height={80}
          style={{ borderRadius: "50%", objectFit: "cover" }}
        />
      )}
    </div>
  );
}
