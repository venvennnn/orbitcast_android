# OrbitCast Android

Control plane for the OrbitCast pipeline. Create, tune, and watch feeds from a phone. **It does not play audio.** Output is RSS; AntennaPod (or any podcast app) is the player.

This is a first-Android-project codebase: one module, no Hilt, no Room, no player stack. Read [ARCHITECTURE.md](ARCHITECTURE.md) before changing shape.

## What it does

- **Feed list** — status dot, cadence, last run, next run, pull to refresh
- **Feed detail** — prompt header, live stage chips (3s poll while visible), skips inline in the timeline
- **Episode detail** — script, show notes, duration, size, error trace, share
- **Create / share-seed** — share an article from another app to prefill the prompt
- **RSS handoff** — copy, `ACTION_VIEW`, or AntennaPod `pcast://`
- **Settings** — API base URL, Keystore-backed token, notification channel toggles

Default API: `https://api-13d-8000.sea1.zerops.app` (the live Zerops deployment). Auth is not enforced there yet; the interceptor is ready for when it is.

## Open in Android Studio

1. Android Studio (Panda / Quail, or any version that ships AGP 8.13).
2. File → Open → this directory.
3. Let Gradle sync. JDK 17+.
4. Run the `app` configuration on a phone or emulator (API 26+).

There is no play button. That is intentional (NG1).

## First launch

1. Open **Settings** (gear on the feed list).
2. Confirm the API base URL.
3. Optionally paste a bearer token and tap **Test connection**. The call is `GET /health` then `GET /feeds` with the interceptor attached.
4. Back on the list, pull to refresh. Tap a feed. Overflow → **Open in AntennaPod** (or Copy RSS).

Share an article from Chrome / Firefox: the share sheet lists OrbitCast and opens the create form with the URL as seed context.

## Layout

```
app/src/main/java/app/orbitcast/
  MainActivity.kt          single activity, share + deep-link entry
  AppContainer.kt          manual wiring (no Hilt)
  data/api/                Retrofit, auth interceptor, models
  data/session/            DataStore + EncryptedFile token
  data/repo/               FeedRepository
  ui/feeds|feed|episode|editor|settings
  notify/                  Episodes + Skips channels
  util/                    Format, RSS intents
```

## What is not here yet

| Gap | Why |
|---|---|
| Real token validation | Backend B1 — API is still open |
| PATCH / DELETE feed | Backend B5 — UI calls them and explains the 404 |
| Voice picker | API has no voice field |
| FCM | Backend B7 + a `google-services.json`. Channels and `orbitcast://feeds/{id}` deep links are in place |

## Tests

```
./gradlew test
```

Covers cadence / relative time / skip one-liners, RSS URL shaping, and the auth interceptor (attach / omit / 401).
