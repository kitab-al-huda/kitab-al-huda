# 🌟 Kitab al-Huda (كتاب الهدى)

**Kitab al-Huda** is a premium, modern Islamic Android application designed to provide seamless access to Quran recitations, daily Hadiths, and personal spiritual management. Built using cutting-edge Android development practices, it features a beautiful Material 3 design, a robust local persistence layer, and a highly innovative data-saving streaming architecture.

---

## 🚀 The Noble Idea: Zero-Rated Audio CDN

In many developing regions (such as Morocco), standard mobile internet plans can be prohibitively expensive. However, mobile operators often provide affordable **"Social Media" subscription plans** that grant unlimited access exclusively to social networking services like Facebook, Messenger, and WhatsApp.

To bridge this digital divide and make sacred knowledge accessible to everyone, **Kitab al-Huda** implements an innovative **Messenger-as-a-CDN** architecture:

* **Zero-Data Playback**: By hosting high-quality Quranic audio files within secure Facebook Messenger attachment streams, the media requests flow strictly through zero-rated Facebook domains (`*.facebook.com`, `*.fbcdn.net`, `*.fbsbx.com`).
* **Messenger as a Media Database**: Audio files are uploaded to a designated Messenger thread. The application retrieves these secure `message_id`s and translates them on-the-fly into temporary CDN URLs using the Facebook Graph API.
* **Cost-Free Streaming**: Users with a social-only data plan can stream surahs without consuming any general internet quota.
* **Minshawi Seed**: The legendary Qari **Mohamed Siddiq El-Minshawi** (reciter ID `6`) is fully seeded with 112 multi-part audios mapping to Surahs 1 to 105.

```
                      ┌──────────────────────────────────────────────┐
                      │          App (User on Social-Only)           │
                      └──────────────────────┬───────────────────────┘
                                             │
                             1. Get URI      │ 3. Stream CDN Url
                         (messenger://msgId) │ (zero-rated data)
                                             ▼
       ┌─────────────────────────────────────┴───────────────────────────────────┐
       │                       Facebook Graph API / CDN                          │
       │  (Resolves message_id securely via Retrofit to temporary CDN video_url) │
       └─────────────────────────────────────────────────────────────────────────┘
```

---

## ✨ Features

* **📖 Holy Quran**: Complete list of the 114 Surahs with deep search, revelation type filters (Meccan/Medinan), and seamless recitation playback.
* **🎧 Premium Audio Player**: Powered by **Jetpack Media3**, featuring a full-screen player, a persistent mini-player, background playback, and system media session integration.
* **💤 Sleep Timer**: Automatically stop audio playback after a set duration.
* **🕌 Discover Dashboard**: Personalized home screen with dynamic Islamic greetings, daily Hadith of the day cards, and featured reciters.
* **📚 My Library**: Fully featured personal playlists (create, edit, reorder, delete) and listening history persistent across app sessions.
* **🌐 Multilingual Support**: Fully localized in **Arabic**, **French**, and **English** with native RTL layout support.
* **🔒 Obfuscated Security**: Cryptographically secured API endpoints and tokens using custom AES-256 multi-part decryption keys.

---

## 🛠️ Technology Stack

* **Language**: [Kotlin](https://kotlinlang.org/) - 100% Type-safe & modern.
* **UI & Architecture**: MVVM Architecture, Single Activity Pattern, Navigation Component, View Binding, and Material 3 Design System.
* **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) - Offline caching, playlists, surahs metadata, and listening history with complex relations.
* **Audio Engine**: [Jetpack Media3 (ExoPlayer & MediaSession)](https://developer.android.com/guide/topics/media/media3) - Premium, high-performance audio engine.
* **Network & API**: [Retrofit 2](https://square.github.io/retrofit/) & [Gson](https://github.com/google/gson) - Type-safe HTTP client to query Facebook Graph API.
* **Security & Cryptography**: Custom multi-part AES-256-CBC decryption combined with Kotlin Proguard/R8 obfuscation.

---

## 🔑 Installation & Configuration of Secrets

To build and run the application, you must configure the Facebook Messenger API credentials. For security and anti-reverse engineering purposes, these secrets are split and encrypted at build time:

1. **Part 1 of the Key (`INTERNAL_BUILD_ID`)** is stored locally in `.env`.
2. **Part 2 of the Key (`app_build_signature`)** is placed inside the application's XML resources (`strings.xml`).
3. An automated Python script (`encrypt.py`) combines both parts, encrypts the raw credentials using AES-256, and writes them back to `.env` as obfuscated variables (`API_ENDPOINT_BASE`, `API_SERVICE_IDENTIFIER`, and `API_AUTH_SIGNATURE`).
4. Gradle reads `.env` at compile-time and injects the encrypted values into `BuildConfig`.

### Step-by-Step Setup

1. **Clone the repository** and navigate to the project root:
    ```bash
    git clone https://github.com/kitab-al-huda/kitab-al-huda.git
    cd kitab-al-huda
    ```

2. **Create your `.env` file** from the template:
   ```bash
   cp .env.example .env
   ```

3. **Fill in your raw Facebook credentials** and choose your keys:
   * Open `.env` and fill in `FACEBOOK_BASE_URL`, `FACEBOOK_PAGE_ID`, and `FACEBOOK_ACCESS_TOKEN`.
   * Modify the `INTERNAL_BUILD_ID` in `.env` with your first secret key part.
   * Open `app/src/main/res/values/strings.xml` and update `<string name="app_build_signature">` with your second secret key part.

4. **Run the automated encryption script** (utilizes a local Python virtual environment):
   ```bash
   # Create a virtual environment (if not already done)
   python3 -m venv .venv

   # Install dependencies (requires uv or pip)
   .venv/bin/pip install pycryptodome
   # OR with uv: uv pip install pycryptodome

   # Run the script to automatically encrypt and update .env
   .venv/bin/python encrypt.py
   ```

For an exhaustive walkthrough of this security system, please read the [Secrets Guide](SECRETS_GUIDE.md).

---

## 📦 How to Build and Run

After configuring your secrets:

1. **Generate Build Configurations**:
   ```bash
   ./gradlew :app:generateDebugBuildConfig
   ```

2. **Install and Run on Device / Emulator**:
   ```bash
   ./gradlew :app:installDebug
   ```
   *Alternatively, open the project in **Android Studio** and click **Run ▶**.*

---

## 📂 Project Structure

```
Kitab al-Huda/
├── .env                         # Central local secrets (Gitignored)
├── .env.example                 # Secrets template file
├── encrypt.py                   # Automated AES-256 local encryption script
├── SECRETS_GUIDE.md             # In-depth guide to configure application secrets
├── build.gradle.kts             # Top-level build file
├── settings.gradle.kts          # Gradle project settings
├── gradlew                      # Gradle wrapper (Unix)
├── gradle/
│   └── libs.versions.toml       # Version catalog
├── app/
│   ├── build.gradle.kts         # Application build and BuildConfig injections
│   ├── proguard-rules.pro       # Strict Proguard/R8 obfuscation rules
│   └── src/main/java/com/alfred/kitabalhuda/
│       ├── KitabAlHudaApplication.kt   # Application class
│       ├── MainActivity.kt             # Main entry point (Navigation)
│       ├── SplashActivity.kt           # Splash screen
│       ├── database/                   # Room Database, DAOs & Entities
│       │   ├── AppDatabase.kt
│       │   ├── Converters.kt
│       │   ├── dao/                    # Data Access Objects
│       │   └── entity/                 # Room entity classes
│       ├── di/                         # Dependency Injection (ViewModelFactory)
│       ├── network/                    # Retrofit API Services & response models
│       ├── repository/                 # Repository Pattern (data abstraction)
│       ├── service/                    # Jetpack Media3 AudioPlayerService
│       ├── ui/                         # Fragments, ViewModels & Adapters (by feature)
│       ├── util/                       # Utility helpers (ReciterPreferences)
│       └── utils/                      # Cryptography & resource managers (CryptoUtils)
```

---

## 🌍 Project Ecosystem

This project is part of the **Kitab al-Huda** ecosystem:

| Repo | Description | Link |
|------|-------------|------|
| 📱 **kitab-al-huda** | Android app (this repo) | [GitHub](https://github.com/kitab-al-huda/kitab-al-huda) |
| 🌐 **kitab-al-huda.github.io** | Site vitrine + Privacy Policy | [GitHub](https://github.com/kitab-al-huda/kitab-al-huda.github.io) — [Site](https://kitab-al-huda.github.io) |
| 🔧 **kitab-al-huda-tools** | Scripts Python d'upload Messenger | [GitHub](https://github.com/kitab-al-huda/kitab-al-huda-tools) |

---

## 📜 License & Intentions

This project is built with the sole intention of sharing beneficial Islamic knowledge and making Quran recitations accessible to everyone under strict economic and structural constraints.

*"The best among you are those who learn the Qur'an and teach it." (Sahih al-Bukhari)*
