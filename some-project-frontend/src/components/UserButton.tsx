"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { usePathname } from "next/navigation";
import { API_BASE } from "@/lib/config";
import { getToken } from "@/lib/auth";

type AuthUser = {
  displayName: string;
  email: string;
};

export default function UserButton() {
  // Holder data om innlogget bruker (null hvis ikke logget inn)
  const [me, setMe] = useState<AuthUser | null>(null);

  const pathname = usePathname();

  useEffect(() => {
    const token = getToken();
    if (!token) return;

    fetch(`${API_BASE}/api/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(async (r) => (r.ok ? r.json() : null)) 
      .then((data) => {
        if (data && data.displayName) setMe(data as AuthUser);
      })
      .catch(() => {
      });
  }, []);

  // Hvis ikke logget inn -> ikke vis knapp
  if (!me) return null;

  // Sjekk om bruker allerede er på egen profilside
  const isOnOwnPage = pathname === `/user/${me.displayName}`;

  // Hvis bruker er på egen profil, vis en "Hjem"-knapp.
  // Ellers vis en knapp som linker til "Min side".
  return isOnOwnPage ? (
    <Link href="/" className="btn" prefetch={false}>
      Hjem
    </Link>
  ) : (
    <Link
      href={`/user/${encodeURIComponent(me.displayName)}`}
      className="btn"
      prefetch={false}
    >
      Min side
    </Link>
  );
}
