# Kitab al-Huda Android Project Structure

## Overview

This document provides a hierarchical view of the Kitab al-Huda Android project directory structure. Kitab al-Huda is an Islamic application featuring Quran recitations, Hadith of the day, reciter management, and personalized playlists. The project follows modern Android development practices using Kotlin, Room database for persistence, Media3 for audio playback, and Navigation component for UI flow.

## Directory Tree

```
Kitab al-Huda/
├── .env
├── .env.example
├── .gitignore
├── AGENTS.md
├── CHANGELOG.md
├── README.md
├── SECRETS_GUIDE.md
├── build.gradle.kts
├── encrypt.py
├── gradle.properties
├── gradlew
├── gradlew.bat
├── KitabAlHuda.code-workspace
├── local.properties
├── project_structure.md
├── run_emulator_and_deploy.sh
├── settings.gradle.kts
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── build/ (generated)
│   └── src/
│       ├── androidTest/
│       │   └── java/
│       │       └── com/
│       │           └── alfred/
│       │               └── kitabalhuda/
│       │                   ├── ExampleInstrumentedTest.kt
│       │                   └── PlaylistDatabaseTest.kt
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── alfred/
│       │   │           └── kitabalhuda/
│       │   │               ├── KitabAlHudaApplication.kt
│       │   │               ├── MainActivity.kt
│       │   │               ├── SplashActivity.kt
│       │   │               ├── database/
│       │   │               │   ├── AppDatabase.kt
│       │   │               │   ├── Converters.kt
│       │   │               │   ├── dao/
│       │   │               │   │   ├── AudioDao.kt
│       │   │               │   │   ├── HadithDao.kt
│       │   │               │   │   ├── ListeningHistoryDao.kt
│       │   │               │   │   ├── PlaylistDao.kt
│       │   │               │   │   ├── ReciteurDao.kt
│       │   │               │   │   └── SourateDao.kt
│       │   │               │   └── entity/
│       │   │               │       ├── AudioEntity.kt
│       │   │               │       ├── HadithEntity.kt
│       │   │               │       ├── ListeningHistoryEntity.kt
│       │   │               │       ├── PlaylistEntity.kt
│       │   │               │       ├── PlaylistItemEntity.kt
│       │   │               │       ├── ReciteurEntity.kt
│       │   │               │       └── SourateEntity.kt
│       │   │               ├── di/
│       │   │               │   └── ViewModelFactory.kt
│       │   │               ├── network/
│       │   │               │   ├── AttachmentResponse.kt
│       │   │               │   └── FacebookApiService.kt
│       │   │               ├── repository/
│       │   │               │   ├── AudioRepository.kt
│       │   │               │   ├── MessengerRepository.kt
│       │   │               │   ├── PlaylistRepository.kt
│       │   │               │   ├── ReciteurRepository.kt
│       │   │               │   └── SourateRepository.kt
│       │   │               ├── service/
│       │   │               │   └── AudioPlayerService.kt
│       │   │               ├── ui/
│       │   │               │   ├── discover/
│       │   │               │   │   └── DiscoverFragment.kt
│       │   │               │   ├── home/
│       │   │               │   │   ├── HomeViewModel.kt
│       │   │               │   │   └── ReciteurAdapter.kt
│       │   │               │   ├── library/
│       │   │               │   │   ├── AddToPlaylistBottomSheet.kt
│       │   │               │   │   ├── CreatePlaylistBottomSheet.kt
│       │   │               │   │   ├── EditPlaylistDialog.kt
│       │   │               │   │   ├── HistoryAdapter.kt
│       │   │               │   │   ├── HistoryFragment.kt
│       │   │               │   │   ├── HistoryViewModel.kt
│       │   │               │   │   ├── LibraryFragment.kt
│       │   │               │   │   ├── LibraryViewModel.kt
│       │   │               │   │   ├── PlaylistAdapter.kt
│       │   │               │   │   ├── PlaylistDetailAdapter.kt
│       │   │               │   │   ├── PlaylistDetailFragment.kt
│       │   │               │   │   ├── PlaylistDetailViewModel.kt
│       │   │               │   │   ├── PlaylistListFragment.kt
│       │   │               │   │   ├── PlaylistMiniAdapter.kt
│       │   │               │   │   └── PlaylistTouchHelperCallback.kt
│       │   │               │   ├── player/
│       │   │               │   │   ├── FullPlayerFragment.kt
│       │   │               │   │   ├── MiniPlayerFragment.kt
│       │   │               │   │   ├── PlaybackMode.kt
│       │   │               │   │   ├── PlayerViewModel.kt
│       │   │               │   │   └── SleepTimerBottomSheet.kt
│       │   │               │   ├── profile/
│       │   │               │   │   ├── ProfileFragment.kt
│       │   │               │   │   └── ProfileViewModel.kt
│       │   │               │   ├── quran/
│       │   │               │   │   ├── QuranFragment.kt
│       │   │               │   │   ├── ReciterSelectionBottomSheet.kt
│       │   │               │   │   ├── SourateAdapter.kt
│       │   │               │   │   └── SourateViewModel.kt
│       │   │               │   ├── search/
│       │   │               │   │   ├── SearchFragment.kt
│       │   │               │   │   └── SearchViewModel.kt
│       │   │               │   └── settings/
│       │   │               │       └── SettingsActivity.kt
│   │   │               └── utils/
│   │   │                   ├── CryptoUtils.kt
│   │   │                   ├── CsvHelper.kt
│   │   │                   ├── HadithManager.kt
│   │   │                   ├── JsonParser.kt
│   │   │                   ├── PreferenceManager.kt
│   │   │                   ├── ReciterPreferences.kt
│   │   │                   └── TimeOfDayManager.kt
│       │   └── res/
│       │       ├── color/
│       │       │   └── bottom_nav_item_color.xml
│       │       ├── drawable/
│       │       │   ├── badge_rating.xml
│       │       │   ├── badge_type.xml
│       │       │   ├── baseline_view_module_24.xml
│       │       │   ├── bg_bottom_sheet_handle.xml
│       │       │   ├── bg_bottom_sheet_m3.xml
│       │       │   ├── bg_card.xml
│       │       │   ├── bg_circle_number.xml
│       │       │   ├── bg_circle_play_button.xml
│       │       │   ├── bg_control_button.xml
│       │       │   ├── bg_play_button.xml
│       │       │   ├── bg_tag.xml
│       │       │   ├── gradient_bottom.xml
│       │       │   ├── ic_add_24.xml
│       │       │   ├── ic_arrow_back.xml
│       │       │   ├── ic_audio_bars.xml
│       │       │   ├── ic_close.xml
│       │       │   ├── ic_drag_handle.xml
│       │       │   ├── ic_grid_view_24.xml
│       │       │   ├── ic_history_24.xml
│       │       │   ├── ic_home_black_24dp.xml
│       │       │   ├── ic_launcher_background.xml
│       │       │   ├── ic_launcher_foreground.xml
│       │       │   ├── ic_library_black_24dp.xml
│       │       │   ├── ic_list_view_24.xml
│       │       │   ├── ic_media_next.xml
│       │       │   ├── ic_media_pause.xml
│       │       │   ├── ic_media_play.xml
│       │       │   ├── ic_media_prev.xml
│       │       │   ├── ic_more_vert.xml
│       │       │   ├── ic_play.xml
│       │       │   ├── ic_play_once_stop.xml
│       │       │   ├── ic_play_sequential.xml
│       │       │   ├── ic_quran_m3.xml
│       │       │   ├── ic_reciter.xml
│       │       │   ├── ic_repeat.xml
│       │       │   ├── ic_repeat_one.xml
│       │       │   ├── ic_shuffle.xml
│       │       │   ├── ic_sleep_timer.xml
│       │       │   ├── ic_sort_ascending_24.xml
│       │       │   ├── ic_tune_24.xml
│       │       │   ├── shape_drag_handle.xml
│       │       │   ├── tab_indicator_default.xml
│       │       │   ├── tab_indicator_selected.xml
│       │       │   └── tab_selector.xml
│       │       ├── layout/
│       │       │   ├── activity_main.xml
│       │       │   ├── activity_settings.xml
│       │       │   ├── activity_splash.xml
│       │       │   ├── bottom_sheet_add_to_playlist.xml
│       │       │   ├── bottom_sheet_create_playlist.xml
│       │       │   ├── bottom_sheet_select_reciter.xml
│       │       │   ├── bottom_sheet_sleep_timer.xml
│       │       │   ├── countdown_view.xml
│       │       │   ├── dialog_edit_playlist.xml
│       │       │   ├── fragment_discover.xml
│       │       │   ├── fragment_full_player.xml
│       │       │   ├── fragment_history.xml
│       │       │   ├── fragment_library.xml
│       │       │   ├── fragment_mini_player.xml
│       │       │   ├── fragment_playlist_detail.xml
│       │       │   ├── fragment_playlist_list.xml
│       │       │   ├── fragment_profile.xml
│       │       │   ├── fragment_quran.xml
│       │       │   ├── fragment_search.xml
│       │       │   ├── item_history.xml
│       │       │   ├── item_playlist.xml
│       │       │   ├── item_playlist_mini.xml
│       │       │   ├── item_playlist_track.xml
│       │       │   ├── item_reciter_selection.xml
│       │       │   ├── item_reciteur.xml
│       │       │   ├── item_sourate.xml
│       │       │   └── item_suggestion.xml
│       │       ├── menu/
│       │       │   ├── bottom_nav_menu.xml
│       │       │   └── home_menu.xml
│       │       ├── mipmap-anydpi-v26/
│       │       │   ├── ic_launcher.xml
│       │       │   └── ic_launcher_round.xml
│       │       ├── mipmap-hdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-mdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xhdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxhdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxxhdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── navigation/
│       │       │   └── mobile_navigation.xml
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── dimens.xml
│       │       │   ├── ic_launcher_background.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       ├── values-night/
│       │       │   └── themes.xml
│       │       └── xml/
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       └── test/
│           ├── java/
│           │   └── com/
│           │       └── alfred/
│           │           └── kitabalhuda/
│           │               ├── ExampleUnitTest.kt
│           │               ├── SourateSearchTest.kt
│           │               └── util/
│           │                   └── EncryptionUtilsTest.kt
│           └── scripts/
│               ├── README.md
│               ├── test_database.py
│               ├── test_facebook_api_real.py
│               └── view.html
└── gradle/
    ├── libs.versions.toml
    └── wrapper/
        ├── gradle-wrapper.jar
        └── gradle-wrapper.properties
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
- **Font resources** (`res/font/`): Previously existed with `bismillah.ttf` and `quran_font.ttf`, but have been removed from the project.
- **Test Scripts**: Python scripts for testing database and Facebook API live under `app/src/test/scripts/`.
- **Zero-Rated CDN Architecture**: See [README.md](README.md) for the full architecture overview.
