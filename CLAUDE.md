# Kitab al-Huda — Context for AI Assistants

## What is this project?

**Kitab al-Huda** (كتاب الهدى) is a premium Islamic Android application providing Quran recitations, daily Hadith, and spiritual management. It is built with Kotlin, Room, Jetpack Media3, and follows modern Android best practices (MVVM, Single Activity, Navigation Component, Material 3).

The app is distinguished by an innovative **zero-rated audio streaming architecture** that leverages Facebook Messenger as a CDN, allowing users with social-only mobile data plans to stream Quranic audio without consuming general internet quota.

---

## Architecture Overview

### Zero-Rated Messenger-as-CDN

Users in regions with expensive standard data plans (e.g. Morocco) often rely on cheap "Social Media" plans. The app hosts high-quality audio files as attachments in a Facebook Messenger thread, retrieves their `message_id`s, and translates them on-the-fly into temporary CDN URLs via the Facebook Graph API. Streaming flows exclusively through zero-rated Facebook domains (`*.facebook.com`, `*.fbcdn.net`, `*.fbsbx.com`).

**Seeded reciters:**
- **Mohamed Siddiq El-Minshawi** (ID `6`) — 112 multi-part audios, Surahs 1-105.
- **Mishary Rashid Al-Afasy** (ID `7`) — zero-rated streaming support added later.

### Security: AES-256 Multi-Part Obfuscation

Sensitive API credentials (Facebook base URL, page ID, access token) are encrypted at build time using a custom AES-256-CBC scheme. The decryption key is split into two parts:

1. **Part 1** (`INTERNAL_BUILD_ID`) — stored in `.env` (gitignored).
2. **Part 2** (`app_build_signature`) — stored in `app/src/main/res/values/strings.xml` (bundled in APK).

An automated Python script (`encrypt.py`) combines both parts, encrypts raw credentials, and writes them back into `.env` as obfuscated variables. At runtime, `CryptoUtils.decrypt()` recombines the key parts to recover cleartext in memory.

**Key files:**
- `.env` — Gitignored. Contains raw secrets (plaintext input for `encrypt.py`) plus encrypted variables (output from `encrypt.py`).
- `.env.example` — Template for new developers.
- `encrypt.py` — Gitignored. Python script that detects the build signature, reads raw secrets, encrypts, and rewrites `.env`.
- `app/build.gradle.kts` — `loadEnvFile()` reads `.env` and injects values into `BuildConfig`.
- `utils/CryptoUtils.kt` — Runtime decryption.
- `repository/MessengerRepository.kt` — Retrofit client using decrypted credentials.

### Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | MVVM, Data Binding, Material 3, Navigation Component |
| Database | Room (entities, DAOs, relations) |
| Audio | Jetpack Media3 (ExoPlayer + MediaSession) |
| Network | Retrofit 2 + Gson |
| Security | Custom AES-256-CBC, Proguard/R8 obfuscation |
| Build | Gradle with Kotlin DSL, BuildConfig injection |

---

## Project Structure

```
app/src/main/java/com/alfred/kitabalhuda/
├── database/       # Room Database, DAOs, Entities
├── di/             # ViewModelFactory (DI)
├── network/        # Retrofit API service, response models
├── repository/     # Data abstraction layer
├── service/        # Jetpack Media3 AudioPlayerService
├── ui/             # Fragments, ViewModels, Adapters (by feature)
│   ├── discover/
│   ├── home/
│   ├── library/
│   ├── player/
│   ├── profile/
│   ├── quran/
│   ├── search/
│   └── settings/
└── utils/          # CryptoUtils, CsvHelper, etc.
```

---

## Important Notes for AI Assistants

1. **Never commit `.env`** — It is in `.gitignore` and contains the first half of the decryption key plus encrypted secrets.
2. **Never commit `encrypt.py`** — It is gitignored and contains the full encryption logic.
3. **Two `strings.xml` files** — Both `res/values/strings.xml` and `res/values-en/strings.xml` must contain the **same** `app_build_signature` value.
4. **Build before running** — Secrets are injected at build time via `BuildConfig`. If you change `.env` or `strings.xml`, you must rebuild (`./gradlew :app:generateDebugBuildConfig`).
5. **Token expiry** — Facebook Long-Lived Page Access Token expires ~60 days. Renewal requires re-running `encrypt.py` with the new token.
6. **.venv required** — `encrypt.py` must be run inside the project's Python virtual environment: `.venv/bin/python encrypt.py`.

---

## Key External Resources

- `README.md` — Full project description, setup guide, and high-level overview.
- `SECRETS_GUIDE.md` — Exhaustive step-by-step guide for configuring and rotating secrets.
- `project_structure.md` — Complete directory tree and file inventory.
- `.env.example` — Template for `.env` setup.
- `gradle.properties` — Standard Gradle configuration.

---

*Last updated: 2026-05-23*
