"use client";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { API_BASE } from "@/lib/config";
import { getToken } from "@/lib/auth";
import { Avatar } from "@/components/Avatar";

type Profile = {
  id: string;
  displayName?: string | null;
  email?: string | null;
  avatarUrl?: string | null;
  bio?: string | null;
};
type Me = { id: string; displayName?: string | null; email?: string | null } | null;

type FollowStats = {
  displayName: string;
  avatarUrl?: string | null;
  followers: number;
  following: number;
  followingByMe: boolean;
};

export default function ProfileCard({ displayName }: { displayName: string }) {
  const [profile, setProfile] = useState<Profile | null>(null);   // profilen som vises
  const [me, setMe] = useState<Me>(null);                         // innlogget bruker (kan være null)
  const [edit, setEdit] = useState(false);                        // redigerer bio?
  const [bioDraft, setBioDraft] = useState("");                   // lokal bio-buffer
  const [stats, setStats] = useState<FollowStats | null>(null);   // følge-statistikk
  const [followBusy, setFollowBusy] = useState(false);            // lås for follow/unfollow

  const token = getToken();
  const router = useRouter();

  /** Hent offentlig profil */
  const loadProfile = useCallback(async (name: string, signal?: AbortSignal) => {
    try {
      const r = await fetch(`${API_BASE}/api/users/${encodeURIComponent(name)}`, { signal });
      if (!r.ok) {
        console.error("loadProfile failed", r.status);
        setProfile(null);
        return;
      }
      const p = await r.json();
      setProfile(p);
      setBioDraft((p?.bio ?? "").toString());
    } catch (e) {
      if ((e as any)?.name !== "AbortError") {
        console.error("loadProfile error", e);
        setProfile(null);
      }
    }
  }, []);

  /** Hent følge-statistikk (avhengig av innlogget bruker for “followingByMe”) */
  const loadFollowStats = useCallback(async (name: string, signal?: AbortSignal) => {
    try {
      const headers: HeadersInit = {};
      const t = getToken();
      if (t) headers["Authorization"] = `Bearer ${t}`;
      const r = await fetch(`${API_BASE}/api/users/${encodeURIComponent(name)}/follow-stats`, { headers, signal });
      setStats(r.ok ? await r.json() : null);
    } catch (e) {
      if ((e as any)?.name !== "AbortError") setStats(null);
    }
  }, []);

  /** Hent innlogget bruker */
  const loadMe = useCallback(async (signal?: AbortSignal) => {
    const t = getToken();
    if (!t) {
      setMe(null);
      return;
    }
    try {
      const r = await fetch(`${API_BASE}/api/auth/me`, {
        headers: { Authorization: `Bearer ${t}` },
        signal,
      });
      setMe(r.ok ? await r.json() : null);
    } catch (e) {
      if ((e as any)?.name !== "AbortError") setMe(null);
    }
  }, []);

  // Når displayName endres -> hent profil + stats
  useEffect(() => {
    const ctrl = new AbortController();
    loadProfile(displayName, ctrl.signal);
    loadFollowStats(displayName, ctrl.signal);
    return () => ctrl.abort();
  }, [displayName, loadProfile, loadFollowStats]);

  // Når token endres (eller ved mount) -> hent bruker
  useEffect(() => {
    const ctrl = new AbortController();
    loadMe(ctrl.signal);
    return () => ctrl.abort();
  }, [token, loadMe]);

  // Er dette brukeren sin egen profil?
  const isMe = useMemo(() => {
    if (!me || !profile) return false;
    if (me.id && profile.id) return me.id === profile.id;
    const a = (me.displayName ?? "").trim().toLowerCase();
    const b = (profile.displayName ?? "").trim().toLowerCase();
    return a.length > 0 && a === b;
  }, [me, profile]);

  /** Lagre bio for brukeren */
  async function saveBio() {
    if (!token) return;
    const res = await fetch(`${API_BASE}/api/users/me`, {
      method: "PUT",
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify({ bio: bioDraft }),
    });
    if (res.ok) {
      const p = await res.json();
      setProfile(p);
      setEdit(false);
      // informer resten av appen (HeaderAuth/IfLoggedIn) om at brukerdata endret seg
      window.dispatchEvent(new Event("auth:changed"));
    } else {
      alert("Kunne ikke lagre bio.");
    }
  }

  /** Endre avatar for brukeren */
  async function onAvatarChange(e: React.ChangeEvent<HTMLInputElement>) {
    const input = e.currentTarget;
    const file = input.files?.[0];
    if (!file || !token) return;

    try {
      const form = new FormData();
      form.append("file", file);

      const res = await fetch(`${API_BASE}/api/files/avatar`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: form,
      });

      // Backend returnerer JSON: { "avatarPath": "/files/avatars/<uuid>/avatar_..." }
      if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`Opplasting feilet (${res.status}): ${text.slice(0, 200)}`);
      }

      let url: string | null = null;
      const ct = res.headers.get("content-type") || "";

      if (ct.includes("application/json")) {
        const data: any = await res.json().catch(() => ({}));
        url =
          data?.avatarPath ??
          data?.avatarUrl ??
          data?.url ??
          data?.path ??
          (typeof data === "string" ? data : null) ??
          null;
      } else {
        const text = await res.text().catch(() => "");
        url = text && (/^\/|^https?:\/\//i.test(text) ? text : null);
      }

      if (url) {
        const busted = `${url}${url.includes("?") ? "&" : "?"}t=${Date.now()}`;
        setProfile((p) => (p ? { ...p, avatarUrl: busted } : p));
      } else {
        await loadProfile(displayName);
      }

      router.refresh();
      window.dispatchEvent(new Event("auth:changed"));
    } catch (err) {
      console.error("Feil ved opplasting av avatar:", err);
      alert(err instanceof Error ? err.message : "Opplasting av avatar feilet.");
    } finally {
      e.currentTarget.value = "";
    }
  }
  /** Følg/avfølg med optimistisk UI-oppdatering */
  async function toggleFollow() {
    if (!token || !stats || isMe || followBusy) return;
    setFollowBusy(true);

    const wantFollow = !stats.followingByMe;

    // Optimistisk oppdatering av statistikk
    setStats((s) =>
      s
        ? {
            ...s,
            followingByMe: wantFollow,
            followers: s.followers + (wantFollow ? 1 : -1),
          }
        : s
    );

    try {
      const res = await fetch(`${API_BASE}/api/users/${encodeURIComponent(displayName)}/follow`, {
        method: wantFollow ? "POST" : "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      if (!res.ok) throw new Error(await res.text());
    } catch (e) {
      setStats((s) =>
        s
          ? {
              ...s,
              followingByMe: !wantFollow,
              followers: s.followers - (wantFollow ? 1 : -1),
            }
          : s
      );
      alert("Kunne ikke oppdatere følge-status.");
      console.error(e);
    } finally {
      setFollowBusy(false);
    }
  }

  if (!profile) return null;

  const name = profile.displayName ?? "(uten navn)";
  const email = profile.email ?? "";

  return (
    <section className="card" style={{ display: "grid", gap: ".5rem", marginBottom: "1rem" }}>
      {/* Topp-rad: avatar + navn/email + høyresideknapper */}
      <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
        <Avatar src={profile.avatarUrl} displayName={name} size={64} />

        <div style={{ lineHeight: 1.25, minWidth: 0 }}>
          <div style={{ fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis" }}>{name}</div>
          {!!email && (
            <div
              style={{
                color: "var(--color-muted)",
                fontSize: ".9rem",
                overflow: "hidden",
                textOverflow: "ellipsis",
              }}
            >
              {email}
            </div>
          )}
        </div>

        {/* Høyreside-kontroller */}
        <div style={{ marginLeft: "auto", display: "flex", gap: ".5rem", alignItems: "center" }}>
          {/* Følgere */}
          {stats && (
            <button
              className="btn btn-outline"
              disabled
              aria-disabled="true"
              title="Følgere"
              style={{ pointerEvents: "none" }}
            >
              {stats.followers} følgere
            </button>
          )}

          {/* Egen profil: bytt bilde + rediger bio */}
          {isMe && (
            <>
              <label className="btn btn-ghost" style={{ cursor: "pointer" }}>
                Bytt bilde
                <input type="file" accept="image/*" onChange={onAvatarChange} style={{ display: "none" }} />
              </label>

              {!edit ? (
                <button className="btn" onClick={() => setEdit(true)}>
                  Rediger
                </button>
              ) : (
                <>
                  <button className="btn" onClick={saveBio}>
                    Lagre
                  </button>
                  <button
                    className="btn btn-outline"
                    onClick={() => {
                      setEdit(false);
                      setBioDraft(profile.bio ?? "");
                    }}
                  >
                    Avbryt
                  </button>
                </>
              )}
            </>
          )}

          {/* Ikke egen profil: følg/avfølg */}
          {!isMe && stats && (
            <button
              className={stats.followingByMe ? "btn btn-outline" : "btn"}
              onClick={toggleFollow}
              disabled={followBusy || !token}
            >
              {stats.followingByMe ? "Følger" : "Følg"}
            </button>
          )}
        </div>
      </div>

      {/* Bio: vis eller rediger */}
      {!edit ? (
        <p style={{ whiteSpace: "pre-wrap", marginTop: ".25rem" }}>
          {(profile.bio ?? "").trim() ? (
            profile.bio
          ) : (
            <span style={{ color: "var(--color-muted)" }}>
              {isMe ? "Legg til en kort beskrivelse om deg selv…" : "Ingen beskrivelse lagt til."}
            </span>
          )}
        </p>
      ) : (
        <textarea
          className="input"
          rows={3}
          maxLength={280}
          value={bioDraft}
          onChange={(e) => setBioDraft(e.target.value)}
          placeholder="Skriv en kort beskrivelse (maks 280 tegn)…"
        />
      )}
    </section>
  );
}
