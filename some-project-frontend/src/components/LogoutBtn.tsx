"use client";

import { clearToken } from "@/lib/auth";
import { useRouter } from "next/navigation";

/**
 * LogoutBtn
 * - Sletter JWT-token fra localStorage via clearToken().
 * - Navigerer brukeren tilbake til /login.
 * - Sender et custom event ("auth:changed") slik at andre komponenter (IfLoggedIn, HeaderAuth, osv.)
 *   kan oppdatere seg når innloggingsstatus endres.
 */
export default function LogoutBtn() {
  const router = useRouter();

  function handleLogout() {
    clearToken();
    window.dispatchEvent(new Event("auth:changed")); 
    router.replace("/login");
  }

  return (
    <button
      type="button"
      className="btn-ghost btn-ghost-sm"
      onClick={handleLogout}
      aria-label="Logg ut"
      title="Logg ut"
    >
      <svg
        width="18"
        height="18"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          d="M12 3v8m6.364-4.364a8.5 8.5 0 1 1-12.728 0"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
        />
      </svg>
      <span className="hide-sm">Logg ut</span>
    </button>
  );
}
