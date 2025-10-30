# StartspelerApp — KMP PWA starter scaffold

This folder contains a minimal Kotlin Multiplatform starter for a PWA frontend (Kotlin/JS) plus a shared module.

Structure:
- shared: KMP shared module with common auth + HTTP abstractions
- jsApp: Kotlin/JS browser app that depends on shared

Quick run (local):
1. Ensure JDK 17+ and Node.js (v16+) are installed.
2. From repository root run: `./gradlew :jsApp:browserDevelopmentRun`

Notes:
- This scaffold is minimal and intended to be developed further in feature branches.
- The Supabase URL and ANON key are read from environment variables in the sample; update Main.kt for local testing.
