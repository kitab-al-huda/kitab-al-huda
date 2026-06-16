# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Removed
- **Old Profile & Search screens**: Deleted `ProfileFragment`, `ProfileViewModel`, `SearchFragment`, and `SearchViewModel` — unused fragments not in navigation graph.

### Changed
- **Build config**: Updated `.gitignore`, `app/build.gradle.kts` (Kotlin 2.0, Java 17, dependency bumps).
- **Release checklist**: Moved Play Store preparation tasks to `RELEASE_CHECKLIST.md`.

---

## [1.3.0] — 2026-05-31

### Added
- **System Media Integration Fix**: Implemented `onGetLibraryRoot` callback in `AudioPlayerService`'s session. This allows external media controllers like Android System UI (`com.android.systemui`) to connect successfully, enabling seamless lock screen media controls, quick settings panel integration, and background audio resumption support.
- **Close Icon Resource**: Added `ic_close.xml` vector drawable representing a standard Material Design close ("X") icon.

### Changed
- **Modal Player Refactoring (BottomSheet)**: Refactored `FullPlayerFragment` to extend `BottomSheetDialogFragment` instead of a manual fragment layout transaction. This places the player overlay inside its own system dialog window, natively resolving all priority back-press conflicts with the underlying `NavController` back stack and enabling beautiful swipe-to-dismiss gesture support.
- **Improved Player UI Layout**: Updated the player's top-left button to use the new close icon (`ic_close.xml`) instead of the back arrow to match its modal bottom-sheet nature.
- **Lifecycle & Fragment Safety**: Refactored `AddToPlaylistBottomSheet`, `CreatePlaylistBottomSheet`, `EditPlaylistDialog`, and `ReciterSelectionBottomSheet` to utilize the modern `FragmentResult` API and argument factories (`newInstance(bundle)`). This ensures robust data passing and completely prevents state loss or crashes on device rotation and process death.
- **Robust Player Debouncing**: Integrated a 1000ms click-debounce cooldown on the mini-player to prevent duplicate fragment transactions from opening multiple players on rapid clicks.
- **Code Hardening**: Cleaned up Media3 listener leakage on view destruction in `PlaylistDetailFragment` and modernized `HadithManager` utilizing Kotlin's `use` block for safe resource closure.

---

## [1.2.0] — 2026-05-23

### Added
- **Multi-part Surah Playback**: Enhanced playback engine to handle surahs split into multiple audio segments seamlessly.
- **Zero-Rated Streaming for Al-Afasy**: Extended the Messenger-as-CDN architecture to support reciter ID `7` (Mishary Rashid Al-Afasy).
- **Playback Mode Enhancements**: New `PlaybackMode` enum with updated UI controls, including a "Play current & stop" mode and dedicated icon.
- **Sleep Timer**: Added a sleep timer to automatically stop audio playback after a configurable duration.
- **Improved Navigation**: Single Activity + Navigation Component with a persistent mini-player.

### Changed
- **Security Hardening**: Introduced `.env`-based secrets management with automated AES-256 encryption via `encrypt.py`. Replaced hardcoded API configuration with BuildConfig injection.
- **Database Schema Expansion**: Room database now supports playlists, listening history, and multi-part audio entities.
- **UI/UX**: Material 3 design system applied across all screens. Added RTL support for Arabic.

---

## [1.1.0] — Seed Release (El-Minshawi)

### Added
- **Zero-Rated Audio CDN**: Innovative Facebook Messenger attachment streaming for social-only data plans.
- **El-Minshawi Seed**: 112 multi-part audios mapping to Surahs 1–105 for reciter ID `6` (Mohamed Siddiq El-Minshawi).
- **Offline Persistence**: Room database with complex relations for surahs, reciters, audios, and playlists.
- **Media3 Player**: Full-screen and mini-player with background playback and system media session integration.

---

## [1.0.0] — Initial Release

### Added
- Base application scaffold with single activity architecture.
- Quran surah listing, search, and filter (Meccan/Medinan).
- Reciter management and selection.
- Basic playlist creation and management.
- Daily Hadith feature.
- Multilingual support (English, French, Arabic).

---

*Format based on [Keep a Changelog](https://keepachangelog.com/).*
