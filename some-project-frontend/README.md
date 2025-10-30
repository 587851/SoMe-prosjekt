# SomeProject Frontend

Dette er frontend-delen av **SomeProject**, bygget med **Next.js 13+** og **TypeScript**.
Den tilbyr et responsivt brukergrensesnitt for innlogging, registrering, poster, kommentarer, likes, profiler, follow-relasjoner og SSE-strømming av oppdateringer fra backend.

## Oppstart

### Forutsetninger

* Node.js 18+
* npm eller yarn

### Konfigurasjon

Applikasjonen bruker en enkel konfigurasjonsfil for API-base-url:

```ts
// src/lib/config.ts
export const API_BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080";
```

👉 Sett `NEXT_PUBLIC_API_BASE` i `.env.local` for å peke til backend-serveren din:

```env
NEXT_PUBLIC_API_BASE=http://localhost:8080
```

### Kjør utviklingsserveren

Med npm:

```bash
npm install
npm run dev
```

Med yarn:

```bash
yarn
yarn dev
```

Serveren starter på:
👉 [http://localhost:3000](http://localhost:3000)

---

## Mapper og moduler

```
src/
├─ app/                  -> App Router sider (Next.js 13+)
│  ├─ page.tsx           -> Forside (feed med tabs: following, all, popular)
│  ├─ layout.tsx         -> Root layout (header, theme, auth, search)
│  ├─ feed.tsx           -> Feed-komponenten (innlegg, likes, kommentarer, SSE)
│  ├─ guard.tsx          -> Route-guard (beskytter sider som krever login)
│  ├─ globals.css        -> Globale styles og tema (lys/mørk)
│  ├─ page.module.css    -> Feed/post-stiler
│  ├─ login/page.tsx     -> Login-side
│  ├─ register/page.tsx  -> Registreringsside
│  └─ user/[displayName]/page.tsx -> Brukerprofiler med feed
│
├─ components/           -> UI-komponenter
│  ├─ Avatar.tsx
│  ├─ AvatarUploader.tsx
│  ├─ HeaderAuth.tsx
│  ├─ IfLoggedIn.tsx
│  ├─ LogoutBtn.tsx
│  ├─ ProfileCard.tsx
│  ├─ SearchUsers.tsx
│  ├─ ThemeToggle.tsx
│  └─ UserButton.tsx
│
└─ lib/                  -> Hjelpefunksjoner
   ├─ auth.ts            -> JWT token lagring/henting i localStorage
   └─ config.ts          -> API_BASE-konfigurasjon

```

---

## Autentisering

Frontend bruker JWT utstedt av backend (`/api/auth/login` og `/api/auth/register`).

* Token lagres i `localStorage` (`saveToken`, `getToken`, `clearToken` i `lib/auth.ts`).
* Komponenter som `Guard`, `HeaderAuth` og `IfLoggedIn` bruker token for å beskytte sider eller vise riktig UI.

---

## Viktige sider / funksjoner

### Auth

* `/login` -> Innlogging
* `/register` -> Opprett ny bruker

### Feed

* `/` -> Forside (tabs: "Følger", "Alle", "Populært (24t)", "Populært (7d)")
* Uendelig scroll med SWR + IntersectionObserver
* Live oppdateringer via SSE (`/api/stream/posts`)

### Brukere

* `/user/[displayName]` -> Brukerprofil med feed, bio, avatar
* `ProfileCard` lar eier redigere bio og bytte avatar
* Følg / Avfølg direkte fra profilen

### Kommentarer

* Drawer som åpner nederst for hvert innlegg
* Optimistisk oppdatering ved posting

---

## SSE (Server-Sent Events)

Frontend abonnerer på live feed-oppdateringer:

```ts
const es = new EventSource(`${API_BASE}/api/stream/posts`);
es.addEventListener("post", handler);
es.addEventListener("postDeleted", handler);
```

Hendelser:

* `post` -> nytt eller oppdatert innlegg
* `postDeleted` -> innlegg slettet

---


## DTO-struktur

Frontend forventer samme DTO-er som backend leverer, f.eks.:

* **AuthUserDto**: `{ id, email, displayName }`
* **PostDto**: `{ id, author, content, createdAt, likeCount, commentCount, likedByMe }`
* **CommentDto**: `{ id, author, content, createdAt }`
* **UserProfileDto**: `{ id, displayName, email, avatarUrl, bio }`
* **FollowStatsDto**: `{ displayName, followers, following, followingByMe }`

---


