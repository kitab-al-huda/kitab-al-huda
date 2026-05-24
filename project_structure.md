# Kitab al-Huda Android Project Structure

## Overview

This document provides a hierarchical view of the Kitab al-Huda Android project directory structure. Kitab al-Huda is an Islamic application featuring Quran recitations, Hadith of the day, reciter management, and personalized playlists. The project follows modern Android development practices using Kotlin, Room database for persistence, Media3 for audio playback, and Navigation component for UI flow.

---

## 🌟 Zero-Rated Streaming: The Noble Idea

In countries like Morocco and other developing regions, many users lack access to standard high-speed mobile internet plans due to high costs. Instead, they rely on affordable "Social Media" subscription plans offered by ISPs. These plans grant unlimited zero-rated data access exclusively to applications like Facebook, Messenger, and WhatsApp.

To empower these users and facilitate easy access to sacred knowledge, **Kitab al-Huda** implements a highly innovative, community-oriented architecture: **Facebook Messenger as an Audio CDN**.

### The Vision & Architecture
- **Social Media Only Access**: By utilizing Messenger's attachment storage, the app's media streaming requests flow strictly through Facebook domains (`*.facebook.com`, `*.fbcdn.net`, `*.fbsbx.com`).
- **Zero-Data Playback**: Users with a social-only data plan can stream high-quality Quranic audios completely for free without consuming any general internet quota.
- **Messenger as a Database**: Audio files are posted to a designated Messenger thread, and their secure `message_id`s are collected. The app translates these IDs on-the-fly into temporary CDN URLs using the Facebook Graph API, presenting a seamless listening experience.
- **Seeded Reciters**: The legendary reciter **Mohamed Siddiq El-Minshawi** (reciter ID `6`) is the first to be seeded using this architecture, with 112 multi-part audios mapping to surahs 1-105.

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

## Directory Tree

```
Kitab al-Huda/
├── .gitignore
├── A7Lite.code-workspace
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── src/
│   │   ├── androidTest/
│   │   │   └── java/
│   │   │       └── com/
│   │   │           └── alfred/
│   │   │               └── kitabalhuda/
│   │   │                   └── PlaylistDatabaseTest.kt
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── alfred/
│   │   │   │           └── kitabalhuda/
│   │   │   │               ├── KitabAlHudaApplication.kt
│   │   │   │               ├── MainActivity.kt
│   │   │   │               ├── SplashActivity.kt
│   │   │   │               ├── database/
│   │   │   │               │   ├── AppDatabase.kt
│   │   │   │               │   ├── Converters.kt
│   │   │   │               │   ├── dao/
│   │   │   │               │   │   ├── AudioDao.kt
│   │   │   │               │   │   ├── HadithDao.kt
│   │   │   │               │   │   ├── ListeningHistoryDao.kt
│   │   │   │               │   │   ├── PlaylistDao.kt
│   │   │   │               │   │   ├── ReciteurDao.kt
│   │   │   │               │   │   └── SourateDao.kt
│   │   │   │               │   └── entity/
│   │   │   │               │       ├── AudioEntity.kt
│   │   │   │               │       ├── HadithEntity.kt
│   │   │   │               │       ├── ListeningHistoryEntity.kt
│   │   │   │               │       ├── PlaylistEntity.kt
│   │   │   │               │       ├── PlaylistItemEntity.kt
│   │   │   │               │       ├── ReciteurEntity.kt
│   │   │   │               │       └── SourateEntity.kt
│   │   │   │               ├── di/
│   │   │   │               │   └── ViewModelFactory.kt
│   │   │   │               ├── network/
│   │   │   │               │   ├── AttachmentResponse.kt
│   │   │   │               │   └── FacebookApiService.kt
│   │   │   │               ├── repository/
│   │   │   │               │   ├── AudioRepository.kt
│   │   │   │               │   ├── MessengerRepository.kt
│   │   │   │               │   ├── PlaylistRepository.kt
│   │   │   │               │   ├── ReciteurRepository.kt
│   │   │   │               │   └── SourateRepository.kt
│   │   │   │               ├── service/
│   │   │   │               │   └── AudioPlayerService.kt
│   │   │   │               ├── ui/
│   │   │   │               │   ├── discover/
│   │   │   │               │   │   └── DiscoverFragment.kt
│   │   │   │               │   ├── home/
│   │   │   │               │   │   ├── HomeViewModel.kt
│   │   │   │               │   │   └── ReciteurAdapter.kt
│   │   │   │               │   ├── library/
│   │   │   │               │   │   ├── AddToPlaylistBottomSheet.kt
│   │   │   │               │   │   ├── CreatePlaylistBottomSheet.kt
│   │   │   │               │   │   ├── EditPlaylistDialog.kt
│   │   │   │               │   │   ├── HistoryAdapter.kt
│   │   │   │               │   │   ├── HistoryFragment.kt
│   │   │   │               │   │   ├── HistoryViewModel.kt
│   │   │   │               │   │   ├── LibraryFragment.kt
│   │   │   │               │   │   ├── LibraryViewModel.kt
│   │   │   │               │   │   ├── PlaylistAdapter.kt
│   │   │   │               │   │   ├── PlaylistDetailAdapter.kt
│   │   │   │               │   │   ├── PlaylistDetailFragment.kt
│   │   │   │               │   │   ├── PlaylistDetailViewModel.kt
│   │   │   │               │   │   ├── PlaylistListFragment.kt
│   │   │   │               │   │   ├── PlaylistMiniAdapter.kt
│   │   │   │               │   │   └── PlaylistTouchHelperCallback.kt
│   │   │   │               │   ├── player/
│   │   │   │               │   │   ├── FullPlayerFragment.kt
│   │   │   │               │   │   ├── MiniPlayerFragment.kt
│   │   │   │               │   │   ├── PlaybackMode.kt
│   │   │   │               │   │   ├── PlayerViewModel.kt
│   │   │   │               │   │   └── SleepTimerBottomSheet.kt
│   │   │   │               │   ├── profile/
│   │   │   │               │   │   ├── ProfileFragment.kt
│   │   │   │               │   │   └── ProfileViewModel.kt
│   │   │   │               │   ├── quran/
│   │   │   │               │   │   ├── QuranFragment.kt
│   │   │   │               │   │   ├── ReciterSelectionBottomSheet.kt
│   │   │   │               │   │   ├── SourateAdapter.kt
│   │   │   │               │   │   └── SourateViewModel.kt
│   │   │   │               │   ├── search/
│   │   │   │               │   │   ├── SearchFragment.kt
│   │   │   │               │   │   └── SearchViewModel.kt
│   │   │   │               │   └── settings/
│   │   │   │               │       └── SettingsActivity.kt
│   │   │   │               ├── util/
│   │   │   │               │   └── ReciterPreferences.kt
│   │   │   │               └── utils/
│   │   │   │                   ├── CryptoUtils.kt
│   │   │   │                   ├── CsvHelper.kt
│   │   │   │                   ├── HadithManager.kt
│   │   │   │                   ├── JsonParser.kt
│   │   │   │                   ├── PreferenceManager.kt
│   │   │   │                   └── TimeOfDayManager.kt
│   │   │   └── res/
│   │   │       ├── color/
│   │   │       │   └── bottom_nav_item_color.xml
│   │   │       ├── drawable/
│   │   │       │   ├── badge_rating.xml
│   │   │       │   ├── badge_type.xml
│   │   │       │   ├── baseline_view_module_24.xml
│   │   │       │   ├── bg_bottom_sheet_handle.xml
│   │   │       │   ├── bg_bottom_sheet_m3.xml
│   │   │       │   ├── bg_card.xml
│   │   │       │   ├── bg_circle_number.xml
│   │   │       │   ├── bg_circle_play_button.xml
│   │   │       │   ├── bg_control_button.xml
│   │   │       │   ├── bg_play_button.xml
│   │   │       │   ├── bg_tag.xml
│   │   │       │   ├── gradient_bottom.xml
│   │   │       │   ├── ic_account_black_24dp.xml
│   │   │       │   ├── ic_add_24.xml
│   │   │       │   ├── ic_arrow_back.xml
│   │   │       │   ├── ic_audio_bars.xml
│   │   │       │   ├── ic_bookmark_add_24.xml
│   │   │       │   ├── ic_dashboard_24.xml
│   │   │       │   ├── ic_dashboard_black_24dp.xml
│   │   │       │   ├── ic_download_24.xml
│   │   │       │   ├── ic_fullscreen.xml
│   │   │       │   ├── ic_fullscreen_exit.xml
│   │   │       │   ├── ic_grid_view_24.xml
│   │   │       │   ├── ic_history_24.xml
│   │   │       │   ├── ic_home_black_24dp.xml
│   │   │       │   ├── ic_launcher_background.xml
│   │   │       │   ├── ic_launcher_foreground.xml
│   │   │       │   ├── ic_library_black_24dp.xml
│   │   │       │   ├── ic_list_view_24.xml
│   │   │       │   ├── ic_media_next.xml
│   │   │       │   ├── ic_media_pause.xml
│   │   │       │   ├── ic_media_play.xml
│   │   │       │   ├── ic_media_prev.xml
│   │   │       │   ├── ic_more_vert.xml
│   │   │       │   ├── ic_notifications_black_24dp.xml
│   │   │       │   ├── ic_pause.xml
│   │   │       │   ├── ic_play_once_stop.xml
│   │   │       │   ├── ic_play_sequential.xml
│   │   │       │   ├── ic_play.xml
│   │   │       │   ├── ic_quran_m3.xml
│   │   │       │   ├── ic_reciter.xml
│   │   │       │   ├── ic_repeat.xml
│   │   │       │   ├── ic_repeat_one.xml
│   │   │       │   ├── ic_search_24.xml
│   │   │       │   ├── ic_search_black_24dp.xml
│   │   │       │   ├── ic_settings_24.xml
│   │   │       │   ├── ic_settings.xml
│   │   │       │   ├── ic_share_24.xml
│   │   │       │   ├── ic_shuffle.xml
│   │   │       │   ├── ic_skip_next.xml
│   │   │       │   ├── ic_skip_previous.xml
│   │   │       │   ├── ic_sleep_timer.xml
│   │   │       │   ├── ic_sort_ascending_24.xml
│   │   │       │   ├── ic_sort_descending_24.xml
│   │   │       │   ├── ic_tune_24.xml
│   │   │       │   ├── shape_drag_handle.xml
│   │   │       │   ├── tab_indicator_default.xml
│   │   │       │   ├── tab_indicator_selected.xml
│   │   │       │   └── tab_selector.xml
│   │   │       ├── font/
│   │   │       │   ├── bismillah.ttf
│   │   │       │   └── quran_font.ttf
│   │   │       ├── layout/
│   │   │       │   ├── activity_main.xml
│   │   │       │   ├── activity_settings.xml
│   │   │       │   ├── activity_splash.xml
│   │   │       │   ├── bottom_sheet_add_to_playlist.xml
│   │   │       │   ├── bottom_sheet_create_playlist.xml
│   │   │       │   ├── bottom_sheet_sleep_timer.xml
│   │   │       │   ├── fragment_discover.xml
│   │   │       │   ├── fragment_full_player.xml
│   │   │       │   ├── fragment_history.xml
│   │   │       │   ├── fragment_library.xml
│   │   │       │   ├── fragment_mini_player.xml
│   │   │       │   ├── fragment_playlist_detail.xml
│   │   │       │   ├── fragment_playlist_list.xml
│   │   │       │   ├── fragment_profile.xml
│   │   │       │   ├── fragment_quran.xml
│   │   │       │   ├── fragment_search.xml
│   │   │       │   ├── item_history.xml
│   │   │       │   ├── item_playlist.xml
│   │   │       │   ├── item_playlist_mini.xml
│   │   │       │   ├── item_playlist_track.xml
│   │   │       │   ├── item_sourate.xml
│   │   │       │   └── item_suggestion.xml
│   │   │       ├── menu/
│   │   │       │   └── bottom_nav_menu.xml
│   │   │       ├── navigation/
│   │   │       │   └── mobile_navigation.xml
│   │   │       └── values/
│   │   │           ├── colors.xml
│   │   │           ├── strings.xml
│   │   │           └── themes.xml
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── alfred/
│   │                   └── kitabalhuda/
│   │                       ├── util/
│   │                       │   └── EncryptionUtilsTest.kt
│   │                       ├── ExampleUnitTest.kt
│   │                       └── SourateSearchTest.kt
│   └── build/
├── gradle/
└── local.properties
```

## Key Notes

- **Islamic Context**: The project is a specialized Islamic application.
- **Data Model**:
  - `SourateEntity`: Represents the 114 surahs of the Quran.
  - `ReciteurEntity`: Information about different Qaris (reciters).
  - `AudioEntity`: Linking specific surahs to specific reciters with URLs.
  - `HadithEntity`: Daily wisdom cards.
  - `ListeningHistoryEntity`: Tracks recently played tracks.
- **UI Architecture**:
  - **Discover**: Personalized dashboard with greeting and featured reciters.
  - **Quran**: Browsable list of all surahs with search and Mecca/Medina filters.
  - **Library**: Management of playlists and listening history.
  - **Player**: Media3 integrated player with mini-player and full-screen controls, includes sleep timer.
  - **Settings**: Global configuration (Language, Reciter preference).
- **Tech Stack**:
  - **Room Database**: Complex relationships between reciters, surahs, and playlists.
  - **Navigation Component**: Fragments-based navigation with a single activity architecture.
  - **Material 3**: Design system for a premium and modern look.
