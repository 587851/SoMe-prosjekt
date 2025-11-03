# SoMe – Fullstack sosiale medier prosjekt

Dette er et fullstack SoMe-prosjekt bygget med **Spring Boot + PostgreSQL** i backend og **Next.js + TypeScript** i frontend.  
Brukere kan registrere seg, logge inn, publisere innlegg, like, kommentere, følge andre, og se en live feed med innlegg.

---

## Demo & Skjermbilder

### Innloggingsskjerm
![Login](https://github.com/587851/SoMe-prosjekt/blob/master/LoggInn.png?raw=true)

### Registreringsskjerm
![Registrering](https://github.com/587851/SoMe-prosjekt/blob/master/Registrer.png?raw=true)

### Forside med innlegg fra de brukeren følger
![Forside med innlegg](https://github.com/587851/SoMe-prosjekt/blob/master/forside.png?raw=true)  

### Profilside til brukeren
![Profil til bruker](https://github.com/587851/SoMe-prosjekt/blob/master/Profil.png?raw=true)

### Kommentarer på en post
![Kommentarer på post](https://github.com/587851/SoMe-prosjekt/blob/master/Kommentar.png?raw=true)

### Søkefunksjon
![Søkefunksjon](https://github.com/587851/SoMe-prosjekt/blob/master/S%C3%B8k.png?raw=true)

## Video presentasjon
[Se demo-video på YouTube](https://youtu.be/6QxjA5EGPOY)

---

## Struktur

Prosjektet er delt i to hoveddeler:

```

/backend   → Spring Boot REST API
/frontend  → Next.js 13 frontend
README.md  → Denne filen (root intro)

````

- [Backend README](https://github.com/587851/SoMe-prosjekt/blob/master/some-project-backend/README.md)  
- [Frontend README](https://github.com/587851/SoMe-prosjekt/blob/master/some-project-frontend/README.md)  

---


## Kom i gang (kortversjon)


1. **Start database i Docker**
   ```bash
   docker run --name pg-social \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_DB=social \
     -p 5432:5432 \
     -d postgres:16


2. **Kjør backend**

   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Kjør frontend**

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

* Backend kjører som standard på: [http://localhost:8080](http://localhost:8080)
* Frontend kjører på: [http://localhost:3000](http://localhost:3000)

---

## Teknologier

* **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), JPA/Hibernate, PostgreSQL
* **Frontend:** Next.js 13 (App Router), React, TypeScript, SWR, CSS (custom)
* **Database:** PostgreSQL (Docker)
* **Realtime:** SSE (Server-Sent Events)





