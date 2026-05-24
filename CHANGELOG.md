# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

- None.

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
