// src/lib/auth.ts
const AUTH_EVENT = "auth:changed";

function emitAuthChanged() {
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(AUTH_EVENT));
  }
}

export function saveToken(token: string) {
  localStorage.setItem("token", token);
  emitAuthChanged();
  window.dispatchEvent(new Event("auth-changed"));       
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("token");
}

export function clearToken() {
  localStorage.removeItem("token");
  emitAuthChanged();
  window.dispatchEvent(new Event("auth-changed"));      
}

export function onAuthChanged(cb: () => void) {
  const handler = () => cb();
  window.addEventListener(AUTH_EVENT, handler);
  return () => window.removeEventListener(AUTH_EVENT, handler);
}


