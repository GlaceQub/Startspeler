name=StartspelerApp/POC-Local-Zero-Cost-STEP-GUIDE.md url=https://github.com/GlaceQub/Startspeler/blob/dev/StartspelerApp/POC-Local-Zero-Cost-STEP-GUIDE.md
# StartspelerApp — POC Local Zero‑Cost (Summary + Step‑Guide based on current StartspelerApp files)

This file combines the zero‑cost local POC summary with a concrete step‑by‑step development plan that matches the minimal scaffold already present under `StartspelerApp/` in your repo. It also includes additional guidance for a production MySQL backend, JWT auth verification in the Ktor backend, runtime config, helper scripts, Flyway migrations, a dev proxy snippet and MUI note so teammates can run the project reproducibly.

Quick summary (one line)
- Frontend: Kotlin/JS PWA (StartspelerApp/jsApp) — develop on `localhost`, build static bundle for Netlify.  
- Shared: KMP shared module (StartspelerApp/shared) for db.tables, HTTP and auth logic.  
- Backend: Ktor JVM running locally (or in Docker) — put DB access behind repositories so DB can be swapped between Postgres (dev) and MySQL (prod).
- DB/Auth: Supabase (cloud free project for fastest start) for dev auth and Postgres testing; add local MySQL tests and Flyway migrations for production parity.

What is already in `StartspelerApp/` (key files & paths)
- StartspelerApp/settings.gradle.kts
- StartspelerApp/gradle.properties
- StartspelerApp/build.gradle.kts
- StartspelerApp/.gitignore
- StartspelerApp/README.md
- StartspelerApp/shared/ (auth + http abstractions)
- StartspelerApp/jsApp/ (Kotlin/JS frontend scaffold)

Immediate goals (what to get working today)
1. Run frontend dev server and confirm UI loads on `localhost`.
2. Run backend locally (either a temporary stub that returns health + orders, or scaffold a simple Ktor module) and confirm API reachable.
3. Connect frontend to backend via dev proxy (no CORS issues).
4. Use Supabase cloud (free) for Auth & DB for fastest setup; prepare local MySQL + Flyway validation so you have production parity.

Step-by-step development guide

A. Quick local dev (fastest path; use Supabase cloud for auth & dev DB)
1. Create a free Supabase project and create a test table or use the UI:
   - Note `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
2. Configure runtime for dev (recommended runtime config approach)
   - Create `StartspelerApp/public/config.example.json` and have teammates copy to `config.json` (not committed). See example below.
3. Run frontend dev server:
   - From repo root:
    ./gradlew :jsApp:jsBrowserDevelopmentRun
   - Open: http://localhost:3000
4. Provide a lightweight backend (if you don't have one yet)
   - Option: create a simple Ktor stub locally (see "Backend quick scaffold" below).
   - Or point frontend to cloud-only Supabase flows for POC (Supabase JS via interop).
5. For local device testing on same Wi‑Fi:
   - Bind dev servers to all interfaces:
     - Ktor host = "0.0.0.0" (if you run Ktor locally)
     - Kotlin/JS dev server host = "0.0.0.0" (configure in `jsApp` runTask)
   - Find laptop LAN IP and open `http://<lan-ip>:3000` on device.

B. If you want to run fully local (MySQL and/or supabase CLI)
1. Install Docker and supabase CLI (optional if you use Supabase cloud).
2. For MySQL local testing (production parity) run docker-compose.mysql.yml (included below).
3. Use Flyway to run migrations against the local MySQL to validate schema compatibility.

C. Add runtime config (so you can swap BACKEND_URL for ngrok without rebuilding)
- Example file: `StartspelerApp/public/config.example.json`
```json
{
  "backendUrl": "http://localhost:8080",
  "supabaseUrl": "https://your-project.supabase.co",
  "supabaseAnonKey": "public-anon-key"
}
```
- At app startup (in Main.kt), fetch `/config.json` and use its values. This avoids rebuilding the frontend whenever a demo tunnel URL changes.

D. Backend quick scaffold (recommended structure)
- Create a `backend/` module (can be `StartspelerApp/backend` or top-level `backend/`) with:
  - Gradle Kotlin JVM or Ktor template.
  - Minimal dependencies: Ktor server Netty, Ktor auth, kotlinx-serialization, ktor client, HikariCP, Exposed (or your ORM), Flyway, MySQL & Postgres drivers.
  - Minimal code (pseudo):
```kotlin
// Application.kt
fun main() {
  embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
    install(CORS) { allowHost("localhost:3000") } // dev
    routing {
      get("/health") { call.respondText("OK") }
      post("/orders") { /* validate JWT, store order to DB */ }
      webSocket("/ws") { /* if using WS */ }
    }
  }.start(wait=true)
}
```
- Start with simple endpoints:
  - GET /health
  - POST /orders (accepts order body, returns created order)
  - GET /orders?status=new (worker UI polling or via realtime)
- Verify local from terminal:
  curl http://localhost:8080/health

E. Dev proxy to avoid CORS (recommended)
- Add a devServer proxy in `StartspelerApp/jsApp/build.gradle.kts` runTask:
```kotlin
runTask {
  devServer = devServer?.copy(host = "0.0.0.0", port = 3000,
    proxy = mapOf("/api" to "http://localhost:8080"))
}
```
- Then have frontend call `/api/orders` and the proxy forwards to your backend.

F. Remote demo (only when needed) — keep this optional
- Run backend locally and expose with ngrok:
  ngrok http 8080
- Use the ngrok HTTPS URL in runtime `config.json` for demo. If Netlify is used for the frontend, update `config.json` hosted there to point to the ngrok URL (no rebuild required).

G. Build & deploy frontend to Netlify (free)
1. In Netlify:
  - Build command: `./gradlew :jsApp:jsBrowserProductionWebpack`
   - Publish directory: `jsApp/build/distributions`
2. Use runtime `config.json` on Netlify (upload /config.json or expose it as a static file) to change backend URL for demos without a rebuild.

---

## Additional items added for production parity and team reproducibility

H. Production DB = MySQL (what this changes)
- Decision: Production DB is MySQL; dev can still use Supabase Postgres for speed but you must ensure schema + SQL are compatible.
- Actions to take:
  - Keep DB access behind repository/DAO interfaces so you can swap drivers by configuration.
  - Use Flyway for migrations and maintain DB-specific migrations if needed (Flyway supports database-specific SQL files).
  - Add `docker-compose.mysql.yml` (example below) for local MySQL testing and a script to run Flyway against it.
  - Avoid Postgres-only SQL in your app layer and migrations (no reliance on RLS, Postgres-only functions, or heavy JSONB features unless you provide MySQL equivalents).

Example `docker-compose.mysql.yml` (add to repo):
```yaml
version: "3.8"
services:
  mysql:
    image: mysql:8.0
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: startspeler
      MYSQL_USER: startspeler
      MYSQL_PASSWORD: startspelerpass
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql
  adminer:
    image: adminer
    restart: unless-stopped
    ports:
      - "8081:8080"
volumes:
  db_data:
```

I. Flyway migrations guidance
- Add Flyway to the backend and place migrations in `backend/src/main/resources/db/migration`.
- Keep migrations portable. If you must use DB-specific SQL, use Flyway naming conventions for database-specific scripts (e.g., `V1__init__mysql.sql`).
- On app startup run Flyway against the configured DB URL to ensure migrations are applied before the app starts.

J. JWT auth verification in Ktor (Supabase Auth strategy)
- Pattern:
  1. Frontend logs in via Supabase Auth and receives an access token.
  2. Frontend includes Authorization: Bearer <token> in requests to Ktor.
  3. Backend validates the JWT signature and claims using JWKS from the provider (Supabase) and caches keys.
  4. Backend extracts the provider user id (sub) and maps it to a local user row in DB to enforce roles/permissions.

- Minimal Ktor snippet (conceptual):
```kotlin
install(Authentication) {
  jwt("auth-jwt") {
    verifier(makeJwkProvider(jwksUrl))
    validate { credential ->
      if (credential.payload.getClaim("sub").asString() != null) JWTPrincipal(credential.payload) else null
    }
    challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized) }
  }
}

routing {
  authenticate("auth-jwt") {
    post("/api/orders") {
      val principal = call.principal<JWTPrincipal>()!!
      val providerId = principal.payload.getClaim("sub").asString()
      // map providerId to local user and proceed
    }
  }
}
```
- Keep SUPABASE_SERVICE_ROLE_KEY on the server only (for admin tasks) and never in frontend or public configs.

K. Realtime approach (MySQL production)
- Supabase Realtime is Postgres-specific. For production on MySQL implement server-side realtime:
  - Simple: Ktor WebSockets that broadcast new orders to connected bar dashboards (server keeps WS sessions per staff client).
  - Scalable: publish order events to Redis pub/sub and have backend instances relay to WS clients.
- Verify JWT token on WS connect and map the session to a user id to control which clients receive which events.

L. MUI and frontend styling note
- Use MUI (Material UI v5) for a professional look. Add NPM deps: `@mui/material`, `@mui/icons-material`, `@emotion/react`, `@emotion/styled`.
- Wrap app root in ThemeProvider + CssBaseline; create a small theme with your brand colors.
- Note: use kotlin-wrappers for convenience if compatible, or dynamic interop.

M. Dev & CI validation for DB compatibility
- Add a CI/local script (e.g., `scripts/validate-migrations.sh`) that runs Flyway migrations against both a Postgres test DB (or Supabase) and a local MySQL container. Fail the check if migrations error.

---

## Practical sprint (prioritized next tasks)
1. (Immediate) Make frontend fetch `config.json` at startup and add `public/config.example.json`.
2. (Immediate) Add devServer proxy in `jsApp/build.gradle.kts`.
3. (Short) Add `backend/` Ktor minimal module with `/health` and `/orders` endpoints; ensure it binds to `0.0.0.0`.
4. (Short) Wire frontend `Main.kt` to call backend endpoints via `backendUrl` from `config.json`.
5. (Short) Use Supabase cloud for Auth and simple order storage during rapid dev; create minimal DB schema (orders, order_items, menu) and Flyway migrations.
6. (Short) Add `docker-compose.mysql.yml` and a migration validation script; run migrations against local MySQL to find issues early.
7. (Mid) Replace Supabase Realtime usage with Ktor WebSockets or Redis pub/sub to support MySQL in production.
8. (Mid) Add MUI theme & a sample screen using MUI components (menu list + cart) for professional polish.
9. (Mid) Add scripts: `StartspelerApp/scripts/dev-local.sh` and `StartspelerApp/scripts/demo-ngrok.sh`.
10. (Stretch) Add CI job to run migration validation and build the frontend to catch breakages early.

What to test (short list)
- Dev UI loads at `http://localhost:3000`.
- Backend responds at `http://localhost:8080/health`.
- Place an order from the UI → backend receives and stores it (Supabase or local DB).
- Bar dashboard updates (polling or realtime) and worker can change order status.
- Local device (phone) can reach the dev frontend via LAN IP.
- Flyway migrations run successfully against local MySQL container.

Helpful commands (copy/paste)
- Dev frontend:
  ./gradlew :jsApp:jsBrowserDevelopmentRun
- Build frontend production:
  ./gradlew :jsApp:jsBrowserProductionWebpack
- Run backend (stub or real):
  ./gradlew :backend:run
- Start local MySQL (docker-compose):
  docker-compose -f docker-compose.mysql.yml up -d
- Run Flyway (example):
  ./gradlew :backend:flywayMigrate -Dflyway.url=jdbc:mysql://localhost:3306/startspeler -Dflyway.user=startspeler -Dflyway.password=startspelerpass
- Start ngrok:
  ngrok http 8080

Security reminders
- Do NOT commit `SUPABASE_SERVICE_ROLE_KEY`. Keep server keys in env vars or local secret files.
- CORS: restrict to dev origins (`http://localhost:3000`) during dev; configure production origin only for deployed frontend.
- For demos with ngrok: be aware the URL is public; do not expose admin or destructive operations.

One-time maintainer checklist (do this once and commit the results)
- [ ] Commit Gradle wrapper files so teammates use the same Gradle.
- [ ] Add `StartspelerApp/public/config.example.json` and update README to instruct copying to `config.json`.
- [ ] Add `StartspelerApp/scripts/dev-local.sh` and `StartspelerApp/scripts/demo-ngrok.sh`.
- [ ] Add `docker-compose.mysql.yml` and `db/migrations` skeleton (Flyway SQL files).
- [ ] Add `backend/` minimal Ktor scaffold if not already present.
- [ ] Add CI job or validation script to run Flyway migrations against Postgres & MySQL.

If you want, I can add the helper files and a backend scaffold on a feature branch and open a PR. Tell me which files you want me to create and I will add them.