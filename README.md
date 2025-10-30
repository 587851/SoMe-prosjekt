
# SoMe – Fullstack Social Media Project

Dette er et fullstack SoMe-prosjekt bygget med **Spring Boot + PostgreSQL** i backend og **Next.js + TypeScript** i frontend.  
Brukere kan registrere seg, logge inn, publisere innlegg, like, kommentere, følge andre, og se en live feed med SSE (Server-Sent Events).

---

## Demo & Skjermbilder

![Login-skjerm](docs/screens/login.png)  
![Feed med innlegg](docs/screens/feed.png)  

🎥 [Se demo-video på YouTube](https://youtu.be/sett-inn-din-demo-link)

---

## Struktur

Prosjektet er delt i to hoveddeler:

```

/backend   → Spring Boot REST API
/frontend  → Next.js 13 frontend
README.md  → Denne filen (root intro)

````

- [Backend README](https://github.com/587851/SoMe-prosjekt/blob/master/some-project-backend/README.md)  
- [Frontend README](./frontend/README.md)  

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





