"use client";

import useSWRInfinite from "swr/infinite";
import { useEffect, useMemo, useRef, useState } from "react";
import styles from "./page.module.css";
import { API_BASE } from "@/lib/config";
import { getToken } from "@/lib/auth";
import Link from "next/link";
import Image from "next/image";
import { Avatar } from "@/components/Avatar";

/* ============================
   Types
============================ */
type Post = {
  id: string;
  author: string;
  authorAvatarUrl?: string | null;
  content: string;
  imageUrl?: string | null;
  createdAt: string;
  likeCount: number;
  commentCount: number;
  likedByMe: boolean;
};

type FeedMode = "global" | "user" | "home" | "popular-day" | "popular-week";
type Page = { posts: Post[]; nextCursor: { createdAt: string; id: string } | null };

type Comment = {
  id: string;
  author: string;
  authorAvatarUrl?: string | null;
  content: string;
  createdAt: string;
};
type CommentsPage = { comments: Comment[]; nextCursor: { createdAt: string; id: string } | null };

type Me = { id?: string | null; displayName?: string | null } | null;

/* ============================
   Fetch helpers
============================ */
const fetcher = async (url: string) => {
  const token = getToken();
  const res = await fetch(url, {
    headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) },
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${text.slice(0, 200)}`);
  return JSON.parse(text);
};

async function authedFetch(url: string, opts: RequestInit = {}) {
  const token = getToken();
  return fetch(url, {
    ...opts,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(opts.headers || {}),
    },
  });
}

const PAGE_SIZE = 10;

/* ============================
   SWR Key Builders
============================ */
function makeGetKey(displayName?: string, mode: FeedMode = "global") {
  const dn = (displayName ?? "").trim();
  return (pageIndex: number, prev: Page | null) => {
    if (prev && !prev.nextCursor) return null;

    const cursor =
      pageIndex === 0
        ? ""
        : `&cursorCreatedAt=${encodeURIComponent(prev!.nextCursor!.createdAt)}&cursorId=${prev!.nextCursor!.id}`;

    let base: string;
    if (mode === "home") base = `${API_BASE}/api/home`;
    else if (mode === "user" && dn.length > 0) base = `${API_BASE}/api/users/${encodeURIComponent(dn)}/posts`;
    else if (mode === "popular-day") base = `${API_BASE}/api/popular?range=day`;
    else if (mode === "popular-week") base = `${API_BASE}/api/popular?range=week`;
    else base = `${API_BASE}/api/posts`;

    return `${base}${base.includes("?") ? "&" : "?"}limit=${PAGE_SIZE}${cursor}`;
  };
}

function makeCommentsGetKey(postId: string) {
  return (pageIndex: number, prev: CommentsPage | null) => {
    if (prev && !prev.nextCursor) return null;
    const cursor =
      pageIndex === 0
        ? ""
        : `&cursorCreatedAt=${encodeURIComponent(prev!.nextCursor!.createdAt)}&cursorId=${prev!.nextCursor!.id}`;
    return `${API_BASE}/api/posts/${postId}/comments?limit=20${cursor}`;
  };
}

/* ============================
   Composer
============================ */
export function Composer() {
  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = e.currentTarget;
    const content = (form.elements.namedItem("content") as HTMLTextAreaElement).value.trim();
    if (!content) return;

    const token = getToken();
    const res = await fetch(`${API_BASE}/api/posts`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ author: "anonymous", content, imageUrl: null }),
    });

    if (res.ok) form.reset();
    else alert("Du må være innlogget for å publisere.");
  }

  return (
    <form onSubmit={onSubmit} className="card" style={{ display: "grid", gap: ".5rem", marginBottom: "1rem" }}>
      <textarea name="content" placeholder="Del noe..." rows={3} />
      <button className="btn">Publiser</button>
    </form>
  );
}

/* ============================
   Comments Drawer
============================ */
function Comments({ postId, onClose }: { postId: string; onClose: () => void }) {
  const { data, size, setSize, isLoading, isValidating, mutate } = useSWRInfinite<CommentsPage>(
    makeCommentsGetKey(postId),
    fetcher,
    { revalidateOnFocus: false, initialSize: 1 }
  );

  const comments = useMemo(() => (data ? data.flatMap((d) => d.comments) : []), [data]);

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!getToken()) {
      alert("Du må være innlogget for å kommentere.");
      return;
    }
    const form = e.currentTarget;
    const content = (form.elements.namedItem("content") as HTMLTextAreaElement).value.trim();
    if (!content) return;

    const temp: Comment = {
      id: `temp-${Date.now()}`,
      author: "Deg",
      authorAvatarUrl: null,
      content,
      createdAt: new Date().toISOString(),
    };

    mutate((pages) => {
      if (!pages || pages.length === 0) return pages;
      const clone = structuredClone(pages);
      clone[0].comments = [temp, ...clone[0].comments];
      return clone;
    }, false);

    const res = await authedFetch(`${API_BASE}/api/posts/${postId}/comments`, {
      method: "POST",
      body: JSON.stringify({ content }),
    });

    if (res.ok) {
      const real = await res.json();
      mutate((pages) => {
        if (!pages || pages.length === 0) return pages;
        const clone = structuredClone(pages);
        clone[0].comments = [real, ...clone[0].comments.filter((c) => c.id !== temp.id)];
        return clone;
      }, false);
    } else {
      mutate();
      alert("Kunne ikke publisere kommentaren.");
    }
    form.reset();
  }

  return (
    <div
      className="card"
      style={{
        position: "fixed",
        left: 0,
        right: 0,
        bottom: 0,
        maxHeight: "60vh",
        overflow: "auto",
        margin: "0 auto",
        width: "min(720px, 95vw)",
        boxShadow: "var(--shadow-md)",
        borderTopLeftRadius: "var(--radius)",
        borderTopRightRadius: "var(--radius)",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: ".5rem" }}>
        <strong>Kommentarer</strong>
        <button className="btn-ghost" onClick={onClose}>Lukk</button>
      </div>

      <form onSubmit={onSubmit} style={{ display: "grid", gap: ".5rem", marginBottom: ".75rem" }}>
        <textarea name="content" rows={2} placeholder="Skriv en kommentar…" />
        <button className="btn">Kommenter</button>
      </form>

      {comments.map((c) => (
        <div
          key={c.id}
          style={{
            display: "grid",
            gridTemplateColumns: "36px 1fr",
            gap: ".6rem",
            padding: ".6rem 0",
            borderTop: "1px solid var(--color-border)",
          }}
        >
          <Avatar src={c.authorAvatarUrl ?? null} displayName={c.author} size={32} />
          <div>
            <div style={{ fontWeight: 600 }}>{c.author}</div>
            <div style={{ whiteSpace: "pre-wrap" }}>{c.content}</div>
            <time style={{ fontSize: ".8rem", color: "var(--color-muted)" }}>
              {new Date(c.createdAt).toLocaleString()}
            </time>
          </div>
        </div>
      ))}

      <div style={{ textAlign: "center", color: "var(--color-muted)", padding: ".5rem" }}>
        {isLoading || isValidating ? (
          "Laster…"
        ) : data && !data[data.length - 1]?.nextCursor ? (
          "Ingen flere kommentarer"
        ) : (
          <button className="btn-ghost" onClick={() => setSize((s) => s + 1)}>
            Last inn flere
          </button>
        )}
      </div>
    </div>
  );
}

/* ============================
   Feed
============================ */
export default function Feed({ displayName, mode = "global" }: { displayName?: string; mode?: FeedMode }) {
  const getKey = makeGetKey(displayName, mode);
  const [openFor, setOpenFor] = useState<string | null>(null);
  const [me, setMe] = useState<Me>(null);

  // Load current user once and on "auth-changed"
  useEffect(() => {
    const t = getToken();
    if (!t) { setMe(null); return; }
    let cancelled = false;
    (async () => {
      try {
        const r = await fetch(`${API_BASE}/api/auth/me`, { headers: { Authorization: `Bearer ${t}` } });
        if (!cancelled) setMe(r.ok ? await r.json() : null);
      } catch {
        if (!cancelled) setMe(null);
      }
    })();

    const onAuth = () => {
      const nt = getToken();
      if (!nt) { setMe(null); return; }
      fetch(`${API_BASE}/api/auth/me`, { headers: { Authorization: `Bearer ${nt}` } })
        .then(r => r.ok ? r.json() : null)
        .then(setMe)
        .catch(() => setMe(null));
    };
    window.addEventListener("auth-changed", onAuth);
    return () => {
      cancelled = true;
      window.removeEventListener("auth-changed", onAuth);
    };
  }, []);

  const { data, size, setSize, isLoading, isValidating, error, mutate } = useSWRInfinite<Page>(getKey, fetcher, {
    revalidateOnFocus: false,
    initialSize: 1,
  });

  const posts = useMemo(() => (data ? data.flatMap((d) => d.posts) : []), [data]);

  // End-of-list detection
  const isEnd = useMemo(() => {
    if (!data || data.length === 0) return false;
    return !data[data.length - 1]?.nextCursor;
  }, [data]);

  // Infinite scroll — flicker-free
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const pagingRef = useRef(false);

  useEffect(() => {
    if (!isValidating) pagingRef.current = false;
  }, [isValidating]);

  useEffect(() => {
    const el = sentinelRef.current;
    if (!el) return;

    const io = new IntersectionObserver(
      (entries) => {
        for (const e of entries) {
          if (!e.isIntersecting) continue;
          if (isValidating || isEnd) return;
          if (pagingRef.current) return;
          pagingRef.current = true;
          setSize((s) => s + 1);
        }
      },
      { rootMargin: "600px" }
    );

    if (!isEnd) io.observe(el);
    return () => io.disconnect();
  }, [setSize, isValidating, isEnd]);

  // SSE updates
  useEffect(() => {
    const es = new EventSource(`${API_BASE}/api/stream/posts`);

    const postHandler = (evt: MessageEvent) => {
      if (!evt.data) return;
      try {
        const post: Post = JSON.parse(evt.data);
        if (displayName && post.author !== displayName) return;

        mutate((pages) => {
          if (!pages || pages.length === 0) return pages;
          const clone = structuredClone(pages);

          // Update in place if exists
          let found = false;
          for (const page of clone) {
            const idx = page.posts.findIndex((p) => p.id === post.id);
            if (idx !== -1) {
              page.posts[idx] = post;
              found = true;
              break;
            }
          }

          // If new and mode allows, insert into first page (sorted)
          if (!found && mode !== "home" && !mode.startsWith("popular")) {
            const page0 = clone[0];
            page0.posts = [...page0.posts, post]
              .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
              .slice(0, PAGE_SIZE);
          }
          return clone;
        }, false);
      } catch (e) { console.warn("Bad SSE payload:", e); }
    };

    const deletedHandler = (evt: MessageEvent) => {
      const postId = evt.data?.toString();
      if (!postId) return;

      mutate((pages) => {
        if (!pages) return pages;
        const clone = structuredClone(pages);
        for (const page of clone) {
          page.posts = page.posts.filter((p) => p.id !== postId);
        }
        return clone;
      }, false);
    };

    es.addEventListener("post", postHandler);
    es.addEventListener("postDeleted", deletedHandler);
    es.onerror = (e) => console.warn("SSE error", e);
    return () => es.close();
  }, [mutate, displayName, mode]);

  /* ============================
     Like / Delete handlers
  ============================ */
  function toggleLikeOptimistic(postId: string, like: boolean) {
    mutate((pages) => {
      if (!pages) return pages;
      const clone = structuredClone(pages);
      for (const page of clone) {
        page.posts = page.posts.map((p) =>
          p.id === postId
            ? {
                ...p,
                likedByMe: like,
                likeCount: Math.max(0, p.likeCount + (like ? 1 : -1)),
              }
            : p
        );
      }
      return clone;
    }, false);
  }

  let likeInFlight: Record<string, boolean> = {};

  async function onLikeClick(p: Post) {
    if (!getToken()) { alert("Du må være innlogget for å like."); return; }
    if (likeInFlight[p.id]) return;
    likeInFlight[p.id] = true;

    const like = !p.likedByMe;
    toggleLikeOptimistic(p.id, like);

    try {
      const res = await authedFetch(`${API_BASE}/api/posts/${p.id}/likes`, {
        method: like ? "POST" : "DELETE",
      });
      if (!res.ok) {
        toggleLikeOptimistic(p.id, !like);
        console.error("Like failed", await res.text());
        return;
      }
      const fresh: Post = await res.json();
      mutate((pages) => {
        if (!pages) return pages;
        const clone = structuredClone(pages);
        for (const page of clone) {
          page.posts = page.posts.map((old) => (old.id === fresh.id ? fresh : old));
        }
        return clone;
      }, false);
    } catch (e) {
      toggleLikeOptimistic(p.id, !like);
      console.error(e);
      alert("Nettverksfeil ved oppdatering av liker.");
    } finally {
      likeInFlight[p.id] = false;
    }
  }

  function removePostOptimistic(postId: string) {
    mutate((pages) => {
      if (!pages) return pages;
      const clone = structuredClone(pages);
      for (const page of clone) {
        page.posts = page.posts.filter((p) => p.id !== postId);
      }
      return clone;
    }, false);
  }

  async function deletePost(postId: string) {
    if (!getToken()) { alert("Du må være innlogget for å slette."); return; }
    if (!confirm("Slett dette innlegget? Dette kan ikke angres.")) return;

    const previous = data;
    removePostOptimistic(postId);

    try {
      const res = await authedFetch(`${API_BASE}/api/posts/${postId}`, { method: "DELETE" });
      if (!res.ok) {
        throw new Error(await res.text());
      }
    } catch (e) {
      mutate(previous, false);
      console.error("Delete failed", e);
      alert("Kunne ikke slette innlegget.");
    }
  }

  function openComments(id: string) {
    setOpenFor(id);
  }

  // Status states
  if (error) {
    return (
      <div style={{ color: "crimson", padding: "1rem" }}>
        Kunne ikke laste inn feed. Sjekk nettverksfanen i nettleserkonsollen.
      </div>
    );
  }
  if (!data && isLoading) return <div style={{ padding: "1rem" }}>Laster…</div>;
  if (data && posts.length === 0)
    return (
      <div style={{ padding: "1rem", color: "var(--color-muted)" }}>
        {displayName ? `Ingen innlegg av ${displayName} ennå` : "Ingen innlegg ennå"}
      </div>
    );

  // Render
  return (
    <>
      {posts.map((p) => {
        const canDelete =
          !!me?.displayName &&
          me.displayName.trim().toLowerCase() === p.author.trim().toLowerCase();

        return (
          <article key={p.id} className={styles.post}>
            <header className={styles.postHeader}>
              <Link
                href={`/user/${encodeURIComponent(p.author)}`}
                className={styles.postAvatar}
                aria-label={`Vis profil for ${p.author}`}
                prefetch={false}
              >
                <Avatar src={p.authorAvatarUrl ?? null} displayName={p.author} size={40} />
              </Link>

              <Link
                href={`/user/${encodeURIComponent(p.author)}`}
                className={styles.postAuthor}
                title={`Vis innlegg fra ${p.author}`}
                prefetch={false}
              >
                {p.author}
              </Link>

              <time className={styles.postMeta} dateTime={p.createdAt}>
                {new Date(p.createdAt).toLocaleString()}
              </time>
            </header>

            <div className={styles.postContent}>{p.content}</div>

            {p.imageUrl && (
              <Image
                className={`${styles.postMedia} ${styles.postMediaCover}`}
                src={p.imageUrl}
                alt="Innleggsbilde"
                width={1280}
                height={720}
              />
            )}

            <div className={styles.postActions}>
              <button className="btn-ghost" onClick={() => onLikeClick(p)} aria-pressed={p.likedByMe}>
                {p.likedByMe ? "💚" : "❤️"} {p.likeCount}
              </button>
              <button className="btn-ghost" onClick={() => openComments(p.id)}>
                💬 {p.commentCount}
              </button>

              {canDelete && (
                <button className="btn-ghost" onClick={() => deletePost(p.id)} title="Slett innlegg">
                  🗑️ Slett
                </button>
              )}
            </div>
          </article>
        );
      })}

      {/* Sentinel for infinite scroll (height 0 when end) */}
      <div ref={sentinelRef} style={{ height: isEnd ? 0 : 1 }} />

      {/* Footer status */}
      <div style={{ textAlign: "center", padding: "0.75rem", color: "var(--color-muted)" }}>
        {isValidating ? "Laster…" : isEnd ? "Det finnes ikke flere innlegg" : ""}
      </div>

      {openFor && <Comments postId={openFor} onClose={() => setOpenFor(null)} />}
    </>
  );
}
