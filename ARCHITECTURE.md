# OrbitCast Android — Architecture Requirements Document

**Status:** Implemented (v1 client)
**Author:** venvennnn
**Date:** 15 August 2026
**Scope:** Native Android client for the OrbitCast pipeline (control plane only)

This file is the product constitution. The Kotlin sources follow it.

---

## 1. Context

OrbitCast is a self-updating podcast pipeline: a topic prompt in, a live RSS feed out. Seven services on Zerops — `web`, `api`, `worker`, `cron`, Postgres, Valkey, object storage — with only `web` and `api` internet-facing. The deliberate design choice at the centre of the product is that **there is no OrbitCast player**. Output is plain RSS, and the existing podcast ecosystem is the frontend.

This document scopes a native Android app that respects that choice. The app is a **control plane**: it creates, tunes, and monitors feeds. It does not play audio.

A secondary goal is that this is the author's first Android project, so the architecture is deliberately biased toward the smallest thing that teaches the platform properly.

---

## 2. Goals

- **G1** — Create and edit feeds (topic prompt, cadence, voice) from a phone.
- **G2** — Monitor the pipeline: per-episode stage progression, failures, and skip decisions.
- **G3** — Make the skip log visible and legible — it is the product's honesty thesis and currently has no good surface.
- **G4** — Get the RSS URL into a podcast app in one tap.
- **G5** — Seed a feed from something the user is already reading, via the Android share sheet.
- **G6** — Teach the author modern Android (Compose, networking, state, intents, push) without platform combat.

## 3. Non-goals

- **NG1 — Audio playback of any kind.** No ExoPlayer/Media3, no `MediaSessionService`, no download manager, no lock-screen controls. Playback is delegated to the user's existing podcast app.
- **NG2 — Offline-first sync.** No local database, no conflict resolution. Network-required, cache-in-memory.
- **NG3 — Feature parity with the web dashboard.** Anything better served by a browser tab stays in the browser tab.
- **NG4 — Multi-user / team features.** Single-operator app against the author's own account.
- **NG5 — iOS or cross-platform.** Native Kotlin, single target.
- **NG6 — Tablet / foldable layouts.** Phone portrait only for v1.

---

## 4. Decisions

### D1 — Control plane, not player

Playback is handed off via `ACTION_VIEW` on the RSS URL, AntennaPod's `pcast://` scheme, or clipboard copy. There is no play button.

### D2 — Share sheet and push are core scope

Share-sheet seeding (G5) is registered on `MainActivity`. Push channels **Episodes** and **Skips** are created at process start. FCM emission itself is backend work (B7).

### D3 — Token auth, resolved backend-first

The app stores a bearer token in an `EncryptedFile` sealed by a Keystore `MasterKey`, and attaches it with an OkHttp interceptor. A 401 clears the token and navigates to Settings. Non-secrets (API base URL, channel toggles) live in DataStore.

The live API (`https://api-13d-8000.sea1.zerops.app`) does not yet validate tokens (B1). The interceptor still attaches a token when one is saved; a blank token is omitted so the open API remains usable.

### D4 — Polling, not streaming

While a feed detail screen is resumed, the client polls `GET /feeds/{id}` every 3 seconds. Polling stops on `ON_PAUSE`. The dedicated `GET /feeds/{id}/status` (B4) is not required; the detail payload is small.

### D5 — Deliberately flat architecture

Single Activity, Jetpack Compose, Navigation Compose, Retrofit + `kotlinx.serialization`, `ViewModel` + `StateFlow`, one repository (`FeedRepository`). Single Gradle module. No Room. No Hilt. No use-case layer. Manual constructor wiring via `AppContainer`.

### D6 — Read-only before write

List / detail / episode / skip timeline are the primary surfaces. Create and share-seed are present because they exist on the live API. Edit / pause / delete call the B5 endpoints and degrade with an explicit message when the API returns 404/405.

### D7 — Push via FCM, treating skips as first-class events

Two notification channels, separately toggleable. Notification taps deep-link `orbitcast://feeds/{id}`. Adding `google-services.json` and a `FirebaseMessagingService` is the remaining M5 client work once B7 lands.

---

## 5. Screens

| # | Screen | Milestone |
|---|---|---|
| S1 | Feed list | M1 |
| S2 | Feed detail (timeline with skips inline) | M2 |
| S3 | Episode detail | M2 |
| S4 | Create / edit feed | M3 |
| S5 | Share-sheet target → S4 | M4 |
| S6 | Settings | M1 |

Skip entries render in the episode timeline — muted card, block icon, one-line reason from `episodes.error` (which is where the worker persists `{skip, reason}`).

Voice is omitted from S4: the live API has no voice field (edge-tts is server-side). Cadence and recap match `schedule_minutes` / `recap_previous`.

---

## 6. Backend prerequisites

| ID | Requirement | Live API (2026-08-15) |
|---|---|---|
| B1 | Token issuance + bearer validation | Not shipped. Client interceptor is ready. |
| B2 | `GET /feeds` | Shipped. |
| B3 | `GET /feeds/{id}` with episodes + skips | Shipped. Skips are episodes with `status=skipped`. |
| B4 | `GET /feeds/{id}/status` | Not shipped. Client polls B3. |
| B5 | `POST /feeds`, `PATCH`, `DELETE` | `POST` shipped. PATCH/DELETE not shipped. |
| B6 | Manual trigger | Shipped as `POST /feeds/{id}/refresh`. |
| B7 | Device token + FCM on publish/skip | Not shipped. Channels exist on the client. |

Default base URL: `https://api-13d-8000.sea1.zerops.app`.

---

## 7. Open questions — resolved against the live pipeline

1. **Do feed edits trigger regeneration?** No edit endpoint exists. Confirm copy says edits apply to the *next* scheduled run only.
2. **Does the skip reason persist?** Yes. The worker writes `result.reason` into `episodes.error` and `episodes.description`. S2 reads that string.
3. **One token per device?** Irrelevant until B1. The client stores one token per install.
4. **Regenerate this episode?** Not exposed. Failed and skipped rows can `POST /episodes/{id}/retry`. Completed episodes are not regenerated from the phone.

---

## 8. Definition of done (v1)

A feed can be created on the phone from a shared article, its generation watched to completion, its RSS handed to AntennaPod in one tap, and — once B7 lands — a notification can arrive saying either that an episode published or that nothing episode-worthy happened, with the skip visible in the timeline when the app is opened.
