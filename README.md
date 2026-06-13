# Transit Line (İstanbul)

Minimalist, **offline-first** guide to Istanbul's metro and buses. No heavy map
engine — the metro network is drawn as a clean schematic on a Jetpack Compose
`Canvas`. No account, no ads, Material 3 Expressive, full Turkish/English support.

> Status: **Phase 1 foundation.** The whole app works fully offline from a
> bundled data seed. Live bus arrivals are intentionally **not** wired up yet —
> see [`docs/API_RESEARCH.md`](docs/API_RESEARCH.md) for the data-source research
> and the decisions awaiting approval.

## Design rules honored
- **No "AI slop", no glassmorphism.** Opaque, matte, high-contrast surfaces; depth
  comes from M3 tonal containers, never blur or translucency.
- **No bottom navigation bar.** Navigation is contextual: a top-left Metro/Bus
  segmented toggle, a top-right Settings button, and edge-to-edge modal bottom
  sheets for actions.
- **Material You (Android 12+)** is supported, but **metro line colors are always
  their official colors** — they are stored with the data and never recolored by
  the dynamic scheme.
- **Power Saving mode** flips the UI to true OLED black (`#000000`) and disables
  GPS instantly.
- **Offline is never an error** — just a polite "showing local data" banner.

## Architecture (folder structure)

```
app/src/main/
├── assets/seed/                 # metro.json, bus.json — the offline network seed
└── java/com/transitline/istanbul/
    ├── TransitLineApplication.kt # holds the manual DI container; seeds Room on launch
    ├── MainActivity.kt           # splash, edge-to-edge, locale, theme host
    ├── core/
    │   ├── design/               # M3 theme: Color (incl. OLED pitch-black), Type, Shape
    │   ├── connectivity/         # online/offline Flow (for the info banner)
    │   ├── location/             # platform LocationManager wrapper (no Play Services)
    │   └── util/                 # LocaleUtil (per-app TR/EN)
    ├── data/
    │   ├── local/                # Room: entities, DAOs, TransitDatabase, seed/
    │   ├── datastore/            # SettingsRepository (Preferences DataStore)
    │   ├── remote/               # LiveDataSource seam (stubbed; live data deferred)
    │   └── repository/           # Metro / Bus / Favorites / Backup(import-export)
    ├── domain/model/             # immutable domain models
    ├── di/AppContainer.kt        # manual DI (singletons)
    └── ui/
        ├── onboarding/           # one-time language + eye-care setup
        ├── home/                 # AppRoot, HomeScreen, RootViewModel
        ├── metro/                # MetroScreen + MetroCanvas (the schematic renderer)
        ├── bus/                  # vertical search, stop/line detail, directions
        ├── settings/             # power saving, dynamic color, a11y, GPS, import/export
        ├── components/           # shared (offline banner, mode toggle, line badge)
        └── util/                 # AppViewModelProvider (ViewModel factory)
```

**Stack:** Kotlin 2.0, Jetpack Compose (Material 3), Room + KSP, Preferences
DataStore, kotlinx.serialization, Coroutines/Flow. Manual DI (no framework) to
keep the build simple and the graph obvious. minSdk 26, targetSdk 35.

## Metro Canvas performance strategy

The map is one `Canvas` engineered to stay smooth at 90/120 Hz:

1. **Data is resolution-independent.** Stations carry normalized `(0..1)`
   coordinates; only a cheap affine map to pixels happens at draw time.
2. **Pan/zoom never recompose.** Scale/offset live in plain state that is read
   **only inside the draw lambda**, so a gesture invalidates the *draw* phase,
   not the composition — no tree recomposition per frame.
3. **No per-frame allocations / text measuring.** Label `TextLayoutResult`s are
   measured once and cached; glyphs are drawn from primitives. Offscreen nodes
   are culled, so per-frame work is O(visible nodes).
4. **No blur, no shadows, no layers** — flat fills only. This is both a
   performance win and the required matte M3 look.
5. **Immutable model** (`@Immutable MetroNetwork`) with O(1) lookup maps so hit
   testing on tap is a quick scan, not a graph walk.

Glyph language: lines have **round caps/joins**; normal stations are **dots**;
multi-line hubs are **rounded diamonds** (interchange); walking transfers are
**pills with three dots**. Tapping a station opens the contextual
"Buradan / Buraya" sheet.

## Building
Open in Android Studio (Koala+), or:
```bash
./gradlew :app:assembleDebug
```
Requires the Android SDK (compileSdk 35). The Gradle wrapper (8.14.3) is included.

## Data & attribution
Offline network is seeded from `assets/seed/*.json` (a curated, real subset of the
Istanbul rail network demonstrating dots, interchanges and a walking transfer).
Production data comes from the **İBB Open Data Portal** and **İETT** (free). The
seed is data, not code, so it can be regenerated from the full İBB GTFS feed
without touching the app.
