# Kitab al-Huda Android Project Structure

## Overview

This document provides a hierarchical view of the Kitab al-Huda Android project directory structure. Kitab al-Huda is an Islamic application featuring Quran recitations, Hadith of the day, reciter management, and personalized playlists. The project follows modern Android development practices using Kotlin, Room database for persistence, Media3 for audio playback, and Navigation component for UI flow.

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
│   │   │                   └── ExampleInstrumentedTest.kt
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
│   │   │   │               ├── repository/
│   │   │   │               │   ├── ReciteurRepository.kt
│   │   │   │               │   └── SourateRepository.kt
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
│   │   │   │               │   │   ├── PlayerViewModel.kt
│   │   │   │               │   │   └── SleepTimerBottomSheet.kt
│   │   │   │               │   ├── profile/
│   │   │   │               │   │   ├── ProfileFragment.kt
│   │   │   │               │   │   └── ProfileViewModel.kt
│   │   │   │               │   ├── quran/
│   │   │   │               │   │   ├── QuranFragment.kt
│   │   │   │               │   │   ├── SourateAdapter.kt
│   │   │   │               │   │   └── SourateViewModel.kt
│   │   │   │               │   ├── search/
│   │   │   │               │   │   ├── SearchFragment.kt
│   │   │   │               │   │   └── SearchViewModel.kt
│   │   │   │               │   └── settings/
│   │   │   │               │       └── SettingsActivity.kt
│   │   │   │               └── utils/
│   │   │   │                   ├── HadithManager.kt
│   │   │   │                   ├── PreferenceManager.kt
│   │   │   │                   └── TimeOfDayManager.kt
│   │   │   └── res/
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
│   │                       └── ExampleUnitTest.kt
│   └── build/
├── gradle/
└── local.properties
```

## Key Notes

- **Islamic Context**: The project has transitioned from an Anime-based template to a specialized Islamic application.
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
