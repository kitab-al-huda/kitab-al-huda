# 🚀 Kitab al-Huda — Checklist de finalisation

> Dernière mise à jour : 15 juin 2026

---

## ✅ Milestone 1 — Critique (Terminé — commit `bd33ffe`)

- [x] **Toasts anglais → strings.xml**
  - `QuranFragment.kt:64` → `getString(R.string.playing_surah)`
  - `AddToPlaylistBottomSheet.kt:68,70` → `getString(R.string.added_to_playlist*)` + `toArabicIndic()`
- [x] **Permission `POST_NOTIFICATIONS`** — Ajoutée dans `AndroidManifest.xml`
- [x] **`android:exported="false"`** sur `MainActivity` et `AudioPlayerService`
- [x] **Room schema export** — `exportSchema = true` + `kapt schemaLocation` + schéma v7 exporté
- [x] **Lint `UnsafeOptInUsageError`** — `@SuppressLint` + `@OptIn` sur `AudioPlayerService` → **0 erreur lint**

---

## 🟡 Milestone 2 — Haute priorité (Terminé ✅)

> Toutes les tâches ci-dessous ont été traitées. Reste uniquement les tâches non applicables (ex: icône déjà correcte).

- [x] **`runBlocking` + timeout** — `AudioPlayerService.kt:56` → `withTimeout(10s)` + `withContext(Dispatchers.IO)`
- [x] ~~Icône personnalisée~~ — Déjà correcte (fond `#C5A059`, icône déjà en place)
- [x] **ProGuard rules nettoyées** — Supprimé `exoplayer2.**`, `model.**`, `entity.**`, `worker.**`, catch-all, doublons
- [x] **SDK mismatch** — Conservé `compileSdk=35` / `targetSdk=34` (stabilité Google Play)
- [x] **Backup rules configurées** — `data_extraction_rules.xml` + `backup_rules.xml` avec DB + prefs
- [x] **`android:text` → `tools:text`** — `item_playlist_mini.xml`, `fragment_full_player.xml`, `item_playlist_track.xml`
- [x] **`"HD"` qualité vidéo supprimé** — `PreferenceManager.kt` — méthodes `save/getPreferredVideoQuality` retirées
- [x] **`"سورة"` → `strings.xml`** — `PlayerViewModel.kt:350` → `getString(R.string.surah_fallback)`
- [x] **Vitesse arabisée** — `SettingsActivity.kt:85` → `speed.formatSpeedArabic()` (chiffres + `٫` + `×`)

---

## 🟢 Milestone 3 — Priorité normale

### Dépendances
- [ ] Kotlin 1.9.23 → 2.0+
- [ ] Media3 1.3.1 → 1.5.x+
- [ ] Room 2.6.1 → 2.7.x+
- [ ] Retrofit 2.9.0 → 2.11.x+
- [ ] Coroutines 1.7.3 → 1.9.x+
- [ ] Java 8 target → Java 17 (`VERSION_1_8` → `VERSION_17`)

### Code quality
- [ ] **`e.printStackTrace()`** → `Log.e(TAG, ...)` dans :
  - `AudioPlayerService.kt:126,134`
  - `PlayerViewModel.kt:74`
  - `CsvHelper.kt:44`
  - `HadithManager.kt:37`
- [ ] **Timeout sur `resolveAudioUrl`** — Ajouter délai max dans `MessengerRepository`
- [ ] **Date format** — `HistoryAdapter.kt:27` — `SimpleDateFormat("MMM dd, HH:mm")` → `DateTimeFormatter` avec locale arabe forcée
- [ ] **Reciteur hardcodé** — `"مشاري بن راشد العفاسي"` dans `DiscoverFragment.kt:77` et `QuranFragment.kt:74` → `strings.xml` ou `ReciterPreferences`
- [ ] **`@Suppress("UNUSED_PARAMETER")`** — `PlayerViewModel.kt:400,451` — Supprimer le paramètre inutilisé `surahName` ou l'utiliser

### Fragments vides / Placeholder
- [ ] `ProfileFragment` + `ProfileViewModel` — Cacher du nav graph ou implémenter
- [ ] `SearchFragment` + `SearchViewModel` — Cacher du nav graph ou implémenter
- [ ] `DownloadsFragment` — Contenu placeholder actuel ("قريباً…"), à implémenter
- [ ] `PlayStore` icon — Créer `playstore.png` (512×512)

### Divers
- [ ] **Commentaires français** — Traduire en anglais dans tout le codebase
- [ ] **Cache non-monotonic** — `MessengerRepository.kt:104,107` — Remplacer `System.currentTimeMillis()` par `SystemClock.elapsedRealtime()` pour l'expiration du cache
- [ ] **Validation URL CDN** — `MessengerRepository.kt:80-83` — Vérifier que l'URL résolue est valide avant utilisation
- [ ] **Foreground service type** — Vérifier si `foregroundServiceType` doit être déclaré pour Android 14+ (API 34)

---

## 📋 Étapes de build & validation

```bash
# Build
./gradlew assembleDebug

# Tests
./gradlew testDebugUnitTest

# Lint
./gradlew :app:lintDebug

# Déploiement
./run_emulator_and_deploy.sh
```

## 🔐 Rappel secrets

```bash
# Après modification de .env ou strings.xml
python3 encrypt.py
./gradlew :app:generateDebugBuildConfig
```

> **Règle d'or** : Ne jamais committer `.env` ni `encrypt.py`.
