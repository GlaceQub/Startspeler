# MUI setup — quick plan & next steps (Kotlin/JS + kotlin-react)

This is a short, actionable checklist to add Material UI (MUI) to the StartspelerApp Kotlin/JS + kotlin-react frontend so the app looks professional fast.

## Summary (one line)
Use MUI v5 + Emotion for styling; use kotlin-react wrappers if you want typed Kotlin APIs, or use dynamic JS interop for the most up‑to‑date MUI API. Wrap the app in a ThemeProvider + CssBaseline and create a small design theme for consistent branding.

---

## Packages to add (NPM)
Add these to your Kotlin/JS project (via Gradle `npm(...)` or `package.json`):
- @mui/material
- @mui/icons-material
- @emotion/react
- @emotion/styled

Example (Gradle Kotlin/JS dependencies snippet)
```kotlin
dependencies {
  implementation(npm("@mui/material", "5.x.x"))
  implementation(npm("@mui/icons-material", "5.x.x"))
  implementation(npm("@emotion/react", "11.x.x"))
  implementation(npm("@emotion/styled", "11.x.x"))
  // kotlin-react already in your project
}
```

If you prefer typed Kotlin wrappers (optional)
- Consider kotlin-wrappers for React + MUI if compatible with your Kotlin and wrapper versions. Wrappers can make Kotlin code nicer, but may lag MUI releases.

---

## App root: ThemeProvider + CssBaseline (Kotlin example)

Use a single theme file and wrap your app:

Example (using kotlin-wrappers-style imports)
```kotlin
// ui/Theme.kt
import mui.material.styles.createTheme
import mui.material.styles.ThemeProvider
import mui.material.CssBaseline
import react.FC
import react.Props

val appTheme = createTheme(jsObject {
  palette = jsObject {
    primary = jsObject { main = "#1976d2" } // brand color
    secondary = jsObject { main = "#ff9800" }
  }
  typography = jsObject {
    fontFamily = "Inter, Roboto, sans-serif"
  }
})

val AppRoot = FC<Props> {
  ThemeProvider {
    this.theme = appTheme
    CssBaseline {}
    // App content...
  }
}
```

If you use dynamic interop (no wrappers) the pattern is the same — create the theme in JS interop and wrap your Kotlin root component.

---

## Example component (Menu card)
```kotlin
import mui.material.Card
import mui.material.CardContent
import mui.material.Button
import mui.material.Typography
import react.FC
import react.Props

val MenuItemCard = FC<Props> {
  Card {
    sx = jsObject { maxWidth = 360 }
    CardContent {
      Typography { variant = "h6"; +"IPA Beer" }
      Typography { variant = "body2"; +"Nice craft beer — 0.33l" }
      Button {
        variant = "contained"
        +"Add to order"
      }
    }
  }
}
```

---

## Fonts & index.html
- Load a professional font (e.g., Inter or Roboto) in `index.html`:
```html
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
```
- Set the theme typography to use that font.

---

## Dev / Build notes
- Ensure Gradle build runs `npmInstall` so MUI & emotion are available.
- Tree-shaking: import components individually to reduce bundle size.
- For production builds run:
  ```bash
  ./gradlew :jsApp:browserProductionWebpack
  ```
- Verify CSS-in-JS (emotion) is working in production build — no extra PostCSS steps are required for MUI/emotion.

---

## Performance & bundle size tips
- Avoid importing entire icon libraries; import specific icons:
  ```kotlin
  implementation(npm("@mui/icons-material/ShoppingCart", "latest"))
  ```
- Use lazy-loading (React.lazy / dynamic import) for heavy pages (admin dashboard).
- Prefer simple tables over `@mui/x-data-grid` unless you need advanced features (the DataGrid adds size).

---

## Accessibility & mobile UX
- Use MUI components for accessible controls (buttons, dialogs, lists).
- Ensure touch targets and spacing are large enough for PWA/mobile.
- Add snackbars for transient messages and aria labels on interactive elements.

---

## Recommended next steps (short checklist)
- [ ] Add NPM deps to `jsApp` Gradle config.
- [ ] Add a `ui/Theme.kt` with a brand theme and `AppRoot` wrapper.
- [ ] Replace a small part of the UI (menu list or single screen) with MUI components to validate look & feel.
- [ ] Add Google Font (Inter/Roboto) to `index.html`.
- [ ] Test dev build and production build (`browserProductionWebpack`) to check styles and bundle size.
- [ ] Optionally add kotlin-MUI wrappers if you prefer typed APIs and wrappers are compatible.

---

## If you want, I can
- generate the exact Gradle dependency lines and a `ui/Theme.kt` + one sample component and a small README snippet for StartspelerApp — ready to commit on a feature branch.
