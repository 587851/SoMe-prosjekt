
# SoMe Prosjekt Backend

Dette er backend-delen av **SomeProject**, bygget med **Spring Boot 3**, **Spring Security (JWT)**, **Spring Data JPA** og **PostgreSQL**.  
Den tilbyr et REST-API for **autentisering, brukerprofiler, poster, kommentarer, likes, follow-relasjoner, SSE-streaming og populære innlegg**.

---

## Oppstart

### Forutsetninger
- Java 17+
- Maven 3+
- PostgreSQL (eller annen database som støtter JPA/Hibernate)

### Konfigurasjon
Applikasjonen bruker Spring Boot sin standard `application.properties` eller `application.yml`.  
Disse miljøvariablene må defineres:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/someproject
spring.datasource.username=youruser
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=your-very-long-secret-key
app.jwt.expiresMinutes=60

# Uploads
app.upload.dir=./uploads
````

### Kjør applikasjonen

Med Maven:

```bash
mvn spring-boot:run
```

Eller bygg JAR og kjør:

```bash
mvn clean package
java -jar target/someproject-backend-0.0.1-SNAPSHOT.jar
```

Serveren starter som standard på:
👉 [http://localhost:8080](http://localhost:8080)

---

## Moduler

* `config/` → CORS, statiske filer, upload-config
* `domain/` → JPA-entiteter (User, Post, Comment, PostLike, UserFollow)
* `repo/` → Spring Data JPA repositories
* `security/` → JWT-basert autentisering med Spring Security
* `service/` → Forretningslogikk (auth, posts, follow, popular)
* `sse/` → SSE-hub for realtime events (likes, kommentarer, nye poster, slettinger)
* `web/` → REST-controllers
* `web/dto/` → DTO-klasser for request/response (gruppert i `auth/`, `user/`, `post/`, `comment/`, `popular/`, `common/`)

---

## Autentisering

JWT brukes i `Authorization: Bearer <token>` header.
Nye tokens får man via:

* `POST /api/auth/register`
* `POST /api/auth/login`

**Eksempel LoginRequest:**

```json
{
  "email": "user@example.com",
  "password": "secret"
}
```

**Eksempel LoginResponse:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "f7a4e1ac-0a1c-4a71-923b-899f03e4e321",
    "email": "user@example.com",
    "displayName": "testuser"
  }
}
```

---

## API Endepunkter

### Auth

* `POST /api/auth/register` → registrer bruker
* `POST /api/auth/login` → logg inn
* `GET /api/auth/me` → hent innlogget bruker

### Users

* `GET /api/users/search?q=abc` → søk etter brukere
* `GET /api/users/{displayName}` → hent brukerprofil
* `PUT /api/users/me` → oppdater min bio
* `GET /api/users/{displayName}/follow-stats` → følgere/følger-statistikk

### Follow

* `POST /api/users/{displayName}/follow` → følg en bruker
* `DELETE /api/users/{displayName}/follow` → slutt å følge
* `GET /api/home` → home-feed med innlegg fra følgede brukere

### Posts

* `GET /api/posts` → hent global feed
* `POST /api/posts` → opprett nytt innlegg
* `DELETE /api/posts/{postId}` → slett innlegg
* `GET /api/users/{displayName}/posts` → hent poster av en bruker
* `POST /api/posts/{postId}/likes` → like et innlegg
* `DELETE /api/posts/{postId}/likes` → unlike
* `POST /api/posts/{postId}/comments` → legg til kommentar
* `GET /api/posts/{postId}/comments` → hent kommentarer
* `GET /api/stream/posts` → SSE-stream av post-hendelser

### Popular

* `GET /api/popular?range=day|week&limit=10` → hent populære innlegg
  (støtter keyset pagination med `cursorScore`, `cursorCreatedAt`, `cursorId`)

### Files

* `POST /api/files/avatar` → last opp avatar (multipart/form-data)

---

## SSE (Server-Sent Events)

Klienter kan abonnere på realtidsoppdateringer:

```http
GET /api/stream/posts
Accept: text/event-stream
```

Hendelser:

* `post` → nytt eller oppdatert innlegg
* `postDeleted` → innlegg slettet

---

## DTO-struktur

```
dto/
├─ common/      → CursorDto
├─ auth/        → AuthUserDto, LoginRequest, LoginResponse, RegisterRequest
├─ user/        → UserProfileDto, UserSearchDto, FollowStatsDto
├─ post/        → PostDto, PostsPageDto, CreatePostRequest
├─ comment/     → CommentDto, CommentsPageDto, CreateCommentRequest
├─ popular/     → PopularCursorDto, PopularPostsPageDto
```

## Kjør lokalt med database i Docker

Du trenger **ikke** Dockerfile for backend-applikasjonen dersom du kjører Spring Boot lokalt.  
Det eneste du trenger i Docker er databasen (Postgres).

Start Postgres-containeren:

```bash
docker run --name pg-social \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=social \
  -p 5432:5432 -d postgres:16


