# StartspelerApp — POC Local Zero‑Cost (Summary + Step‑Guide based on current StartspelerApp files)

This file combines the zero‑cost local POC summary with a concrete step‑by‑step development plan that matches the minimal scaffold already present under `StartspelerApp/` in your repo.

Quick summary (one line)
- Frontend: Kotlin/JS PWA (StartspelerApp/jsApp) — develop on `localhost`, build static bundle for Netlify.  
- Shared: KMP shared module (StartspelerApp/shared) for models, HTTP and auth logic.  
- Backend: not included yet in the scaffold — run Ktor locally (or add `StartspelerApp/backend`) and verify JWTs against Supabase.  
- DB/Auth: Supabase (cloud free project for fastest start) or `supabase` CLI for fully local mode.

What is already in `StartspelerApp/` (key files & paths)
- StartspelerApp/settings.gradle.kts
- StartspelerApp/gradle.properties
- StartspelerApp/build.gradle.kts
- StartspelerApp/.gitignore
- StartspelerApp/README.md
- StartspelerApp/shared/
  - build.gradle.kts
  - src/commonMain/kotlin/com/example/auth/AuthTokens.kt
  - src/commonMain/kotlin/com/example/auth/TokenStorage.kt
  - src/jsMain/kotlin/com/example/auth/TokenStorageJs.kt
  - src/commonMain/kotlin/com/example/network/HttpClientProvider.kt
  - src/jsMain/kotlin/com/example/network/HttpClientProviderJs.kt
  - src/commonMain/kotlin/com/example/auth/SupabaseAuthRepository.kt
- StartspelerApp/jsApp/
  - build.gradle.kts
  - src/main/resources/index.html
  - src/main/kotlin/com/example/js/Main.kt

Immediate goals (what to get working today)
1. Run frontend dev server and confirm UI loads on `localhost`.
2. Run backend locally (either a temporary stub that returns health + orders, or scaffold a simple Ktor module) and confirm API reachable.
3. Connect frontend to backend via dev proxy (no CORS issues).
4. Use Supabase cloud (free) for Auth & DB for fastest setup; optionally prepare local supabase later.

Step-by-step development guide

A. Quick local dev (fastest path; use Supabase cloud)
1. Create a free Supabase project and create a test table or use the UI:
   - Note `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
2. Configure runtime for dev (recommended runtime config approach)
   - Create `StartspelerApp/public/config.example.json` (see example below) and update code to fetch `config.json` at startup (or use environment variables in dev).
3. Run frontend dev server:
   - From repo root:
     ./gradlew :jsApp:browserDevelopmentRun
   - Open: http://localhost:3000
4. Provide a lightweight backend (if you don't have one yet)
   - Option: create a simple Ktor stub locally (see "Backend quick scaffold" later).
   - Or point frontend to cloud-only Supabase flows for POC (Supabase JS via interop).
5. For local device testing on same Wi‑Fi:
   - Bind dev servers to all interfaces:
     - Ktor host = "0.0.0.0" (if you run Ktor locally)
     - Kotlin/JS dev server host = "0.0.0.0" (configure in `jsApp` runTask)
   - Find laptop LAN IP and open `http://<lan-ip>:3000` on device.

B. If you want to run fully local (supabase CLI)
1. Install Docker and supabase CLI:
   - https://supabase.com/docs/guides/cli
2. In a folder:
   supabase init
   supabase start
3. The CLI prints local URLs & keys — use those in `config.json` or env for backend/frontend.

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
  - Minimal dependencies: Ktor server Netty, Ktor auth, kotlinx-serialization, ktor client.
  - Minimal code (pseudo):
```kotlin
// Application.kt
fun main() {
  embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
    install(CORS) { allowHost("localhost:3000") } // dev
    routing {
      get("/health") { call.respondText("OK") }
      post("/orders") { /* validate JWT, store order to Supabase / DB */ }
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
   - Build command: `./gradlew :jsApp:browserProductionWebpack`
   - Publish directory: `jsApp/build/distributions`
2. Use runtime `config.json` on Netlify (upload /config.json or expose it as a static file) to change backend URL for demos without a rebuild.

Prioritized next tasks (practical sprint for a student POC)
1. (Immediate) Make frontend fetch `config.json` at startup and add `public/config.example.json`.
2. (Immediate) Add devServer proxy in `jsApp/build.gradle.kts`.
3. (Short) Add `backend/` Ktor minimal module with `/health` and `/orders` endpoints; ensure it binds to `0.0.0.0`.
4. (Short) Wire frontend `Main.kt` to call backend endpoints via `backendUrl` from `config.json`.
5. (Short) Use Supabase cloud for Auth and simple order storage; create minimal DB schema (orders, order_items, menu).
6. (Short) Test device flow on LAN (phone -> dev frontend hosted on laptop).
7. (Mid) Add realtime delivery: start with Supabase Realtime (listen on order rows) or add basic Ktor WebSocket broadcast for the bar dashboard.
8. (Mid) Add printer approach: simple "Print" button in bar dashboard that opens printable order page (browser print) or later add a small print agent.
9. (Mid) Create README demo script to run backend + start ngrok (optional) for remote reviewers.
10. (Stretch) Create CI job to build frontend and optionally deploy to Netlify automatically on push to `main`.

What to test (short list)
- Dev UI loads at `http://localhost:3000`.
- Backend responds at `http://localhost:8080/health`.
- Place an order from the UI → backend receives and stores it (Supabase or local DB).
- Bar dashboard updates (polling or realtime) and worker can change order status.
- Local device (phone) can reach the dev frontend via LAN IP.
- For remote demo: ngrok forwarded URL responds from the hosted frontend.

Helpful commands (copy/paste)
- Dev frontend:
  ./gradlew :jsApp:browserDevelopmentRun
- Build frontend production:
  ./gradlew :jsApp:browserProductionWebpack
- Run backend (stub or real):
  ./gradlew :backend:run
- Start supabase locally:
  supabase init
  supabase start
- Start ngrok:
  ngrok http 8080

Security reminders
- Do NOT commit `SUPABASE_SERVICE_ROLE_KEY`. Keep server keys in env vars or local secret files.
- CORS: restrict to dev origins (`http://localhost:3000`) during dev; configure production origin only for deployed frontend.
- For demos with ngrok: be aware the URL is public; do not expose admin or destructive operations.