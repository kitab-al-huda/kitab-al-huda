# 🤖 Kitab al-Huda Developer & AI Agent Guide (AGENTS.md)

Welcome, agent! This guide helps you navigate and work securely on **Kitab al-Huda (كتاب الهدى)** without violating core architecture constraints, security measures, or breaking current features.

---

## 🏗️ Crucial Guidelines & Non-Negotiable Rules

### 1. 🔒 Security & Secrets Management
- **NEVER** expose raw Facebook credentials in `strings.xml`, `BuildConfig`, or any kotlin file. They must remain encrypted in `.env`.
- Never commit the `.env` file or `encrypt.py`.
- If you modify credentials in `.env`, you must rebuild the obfuscated variables:
  1. Complete `.env` changes.
  2. Run the encryption script: `python3 encrypt.py`.
  3. Rebuild Gradle build config: `./gradlew :app:generateDebugBuildConfig`.
- **Build Signature**: The decryption key half `app_build_signature` is stored in `app/src/main/res/values/strings.xml`. Changing it without a matching `INTERNAL_BUILD_ID` in `.env` will cause decryption failures.
- For a deeper dive into the cryptography scheme, refer to [SECRETS_GUIDE.md](SECRETS_GUIDE.md).

### 2. 🎵 Media Player Architecture (Jetpack Media3)
- The app uses a zero-rated audio framework, converting custom `messenger://<msgId>` URIs to temporary CDN links on-the-fly.
- Do not bypass `ResolvingDataSource` when fetching media streams.
- The `AudioPlayerService` captures Android 12+ foreground start issues gracefully (`onForegroundServiceStartNotAllowedException`) — ensure any service adjustments respect this lifecycle constraint.

### 3. 🗄️ Database Changes (Room & Schema)
- When updating any entity (`SourateEntity`, `AudioEntity`, etc.) located in `app/src/main/java/com/alfred/kitabalhuda/database/entity/`, you **must**:
  1. Increment the version in `AppDatabase.kt`.
  2. Write appropriate Room DB Migrations in `AppDatabase.AppMigration`.
  3. Keep the schema exports updated.

---

## 🛠️ Build & Development Commands

### Common Tasks
| Command | Action |
|---------|--------|
| `./gradlew clean` | Cleans build artifacts |
| `./gradlew assembleDebug` | Builds Debug APK |
| `./gradlew :app:generateDebugBuildConfig` | Rebuilds build config (required after `.env` or `strings.xml` changes) |
| `./run_emulator_and_deploy.sh` | Builds APK, starts emulator, installs & launches |

### Testing & Linting
| Command | Action |
|---------|--------|
| `./gradlew testDebugUnitTest` | Runs all unit tests |
| `./gradlew :app:testDebugUnitTest --tests "com.alfred.kitabalhuda.SourateSearchTest"` | Runs specific test class |
| `./gradlew :app:testDebugUnitTest --tests "com.alfred.kitabalhuda.SourateSearchTest.testNormalizeArabicDiacritics"` | Runs specific test method |
| `./gradlew :app:lintDebug` | Runs static code lint |

### Secrets Management
| Command | Action |
|---------|--------|
| `python3 encrypt.py` | Encrypts `.env` raw secrets to obfuscated outputs (requires `pycryptodome`: `pip install pycryptodome`) |

---

## High-Level Architecture

### 1. Zero-Rated Audio CDN (Facebook Messenger as CDN)
To support users with social-only mobile data plans, audio files are hosted as attachments in a Facebook Messenger thread.
- **Lazy Loading with `ResolvingDataSource`**: The player UI passes a custom URI `messenger://<message_id>` to Jetpack Media3.
- `AudioPlayerService` constructs an ExoPlayer instance using a custom `ResolvingDataSource.Factory`. When a `messenger` URI is encountered during playback, it intercepts it, extracts the message ID, calls `MessengerRepository` to fetch the direct CDN URL via Facebook Graph API (performing a blocking coroutine execution), and swaps the data spec URI with the temporary CDN link.
- **Caching**: `MessengerRepository` uses a 120-item `LruCache` with a 45-minute expiration window to minimize Graph API requests.

### 2. Cryptographic Security (AES-256-CBC Key Splitting)
Sensitive Facebook API keys and credentials are encrypted using AES-256-CBC. The decryption key is split into two halves:
1. **Part 1** (`INTERNAL_BUILD_ID`): Stored in `.env` (gitignored), injected into `BuildConfig`.
2. **Part 2** (`app_build_signature`): Stored in `app/src/main/res/values/strings.xml` (bundled in APK).

At runtime, `CryptoUtils.decrypt()` combines `BuildConfig.INTERNAL_BUILD_ID` and the resource string `R.string.app_build_signature`, hashes them via SHA-256 to create a 256-bit key, and decrypts the obfuscated variables (`API_ENDPOINT_BASE`, `API_AUTH_SIGNATURE`) to instantiate Retrofit.

---

## 📂 Codebase Quick Mapping

- **Single Activity Pattern**: `MainActivity.kt` hosts Jetpack Navigation component and handles window insets.
- **Service Layer**: `AudioPlayerService.kt` manages background playback, media sessions, player states, notifications, custom sleep timers, and gradual fade-out logic.
- **Local Persistence (Room)**: Entities represent `SourateEntity`, `AudioEntity` (tracks mapping to surahs/parts), `ReciteurEntity`, `HadithEntity`, `PlaylistEntity`, `PlaylistItemEntity` (joins tracks to playlists with order), and `ListeningHistoryEntity`.
- **Data Repositories**: Located in `app/src/main/java/com/alfred/kitabalhuda/repository/` (`MessengerRepository`, `SourateRepository`, `ReciteurRepository`, `AudioRepository`, `PlaylistRepository`, etc.).
- **UI Architecture**: MVVM with XML Data Binding and LiveData. Views are divided by feature folders (`ui/home/`, `ui/discover/`, `ui/quran/`, `ui/player/`, `ui/library/`, `ui/search/`, `ui/settings/`, `ui/profile/`).
