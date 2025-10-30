"use client";

import { useEffect, useMemo, useRef, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { API_BASE } from "@/lib/config";
import { Avatar } from "@/components/Avatar";

type UserHit = {
  displayName: string;
  avatarUrl?: string | null; 
};

export default function SearchUsers() {
  const [q, setQ] = useState("");
  const [hits, setHits] = useState<UserHit[]>([]);
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);

  const router = useRouter();
  const boxRef = useRef<HTMLDivElement | null>(null);
  const listboxId = "search-users-listbox";

  const runSearch = useCallback((query: string, signal?: AbortSignal) => {
    return fetch(`${API_BASE}/api/users/search?q=${encodeURIComponent(query)}`, { signal });
  }, []);

  useEffect(() => {
    const trimmed = q.trim();
    if (trimmed.length < 2) {
      setHits([]);
      setOpen(false);
      return;
    }

    const ctrl = new AbortController();
    const t = setTimeout(async () => {
      try {
        const res = await runSearch(trimmed, ctrl.signal);
        if (!res.ok) throw new Error();
        const data: UserHit[] = await res.json();
        setHits(data);
        setActive(0);
        setOpen(data.length > 0);
      } catch (err: any) {
        if (err?.name === "AbortError") return; 
        setHits([]);
        setOpen(false);
      }
    }, 200); 

    return () => {
      clearTimeout(t);
      ctrl.abort();
    };
  }, [q, runSearch]);

  // Lukk liste ved klikk utenfor
  useEffect(() => {
    function onDocClick(e: MouseEvent) {
      if (!boxRef.current) return;
      if (!boxRef.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("click", onDocClick);
    return () => document.removeEventListener("click", onDocClick);
  }, []);

  const goTo = (name: string) => {
    setOpen(false);
    setQ("");
    router.push(`/user/${encodeURIComponent(name)}`);
  };

  // Tastaturnavigasjon i resultater + fallback til direkte søk på Enter
  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (!open || hits.length === 0) {
      if (e.key === "Enter" && q.trim()) {
        goTo(q.trim());
      }
      return;
    }
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActive((a) => Math.min(a + 1, hits.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActive((a) => Math.max(a - 1, 0));
    } else if (e.key === "Enter") {
      e.preventDefault();
      goTo(hits[active].displayName);
    } else if (e.key === "Escape") {
      setOpen(false);
    }
  }

  // Vis maks 8 treff
  const results = useMemo(() => hits.slice(0, 8), [hits]);

  return (
    <div className="header-search" ref={boxRef}>
      <input
        type="search"
        inputMode="search"
        placeholder="Søk etter brukere…"
        aria-label="Søk etter brukere"
        className="input header-search-input"
        value={q}
        onChange={(e) => setQ(e.target.value)}
        onFocus={() => results.length > 0 && setOpen(true)}
        onKeyDown={onKeyDown}
        role="combobox"
        aria-expanded={open}
        aria-controls={open ? listboxId : undefined}
        aria-activedescendant={open ? `${listboxId}-opt-${active}` : undefined}
        aria-autocomplete="list"
        autoCorrect="off"
        autoCapitalize="off"
        spellCheck={false}
      />

      {open && results.length > 0 && (
        <div className="search-popover" role="listbox" id={listboxId}>
          {results.map((u, i) => (
            <button
              type="button"
              key={`${u.displayName}-${i}`}
              className={`search-item ${i === active ? "is-active" : ""}`}
              onMouseEnter={() => setActive(i)}
              onClick={() => goTo(u.displayName)}
              role="option"
              id={`${listboxId}-opt-${i}`}
              aria-selected={i === active}
            >
              <Avatar
                src={u.avatarUrl ?? null}   
                displayName={u.displayName}
                size={24}
                className="search-avatar"
              />
              <span className="search-name">{u.displayName}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
