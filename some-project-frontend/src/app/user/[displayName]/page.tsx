"use client";

import { useEffect, useState, useMemo } from "react";
import { useParams } from "next/navigation";
import styles from "@/app/page.module.css";
import Guard from "@/app/guard";              
import Feed, { Composer } from "@/app/feed"; 
import ProfileCard from "@/components/ProfileCard";
import { API_BASE } from "@/lib/config";      
import { getToken } from "@/lib/auth";       

type Me = { id: string; displayName?: string | null } | null;

export default function UserPage() {

  const { displayName } = useParams<{ displayName: string }>();
  const dn = (displayName ?? "").trim();

  const [me, setMe] = useState<Me>(null);

  // Hent innlogget bruker når komponenten mountes
  useEffect(() => {
    const token = getToken();
    if (!token) { 
      setMe(null); 
      return; 
    }

    (async () => {
      try {
        const r = await fetch(`${API_BASE}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setMe(r.ok ? await r.json() : null);
      } catch {
        setMe(null);
      }
    })();
  }, []);

  // sjekk om dette er brukeren sin egen side 
  const isMyPage = useMemo(() => {
    const myName = (me?.displayName ?? "").trim().toLowerCase();
    return dn.length > 0 && myName.length > 0 && myName === dn.toLowerCase();
  }, [me, dn]);

  // Hvis displayName mangler i URL
  if (!dn) {
    return (
      <main className={styles.feedLayout}>
        <section className={styles.feed}>Bruker ikke funnet.</section>
      </main>
    );
  }

  return (
    <Guard>
      <main className={styles.feedLayout}>
        <section className={styles.feed}>
          <ProfileCard displayName={dn} />
          {/* Composer vises bare dersom man ser på sin egen profil */}
          {isMyPage && <Composer />}
          {/* Feed for brukeren, key gjør at feed re-rendres ved bytte av bruker */}
          <Feed key={`user:${dn}`} displayName={dn} mode="user" />
        </section>
      </main>
    </Guard>
  );
}
