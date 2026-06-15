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

## 🟢 Milestone 3 — Priorité normale (Terminé ✅)

> Reste : fragments vides non implémentés (ne sont pas dans le nav graph), icône PlayStore (manuel).

### Dépendances
- [x] Kotlin 1.9.23 → **2.0.21**
- [x] Media3 1.3.1 → **1.5.1**
- [x] Room 2.6.1 → **2.7.1**
- [x] Retrofit 2.9.0 → **2.11.0**
- [x] Coroutines 1.7.3 → **1.9.0**
- [x] Java 8 → **Java 17** + desugaring `java.time`

### Code quality
- [x] **`e.printStackTrace()`** → `Log.e(TAG, ...)` — 5 occurrences dans 4 fichiers
- [x] **Timeout sur `resolveAudioUrl`** — Déjà fait dans MS2 (withTimeout 10s)
- [x] **Date format** — `HistoryAdapter.kt` → `DateTimeFormatter` + `Locale("ar")` + desugaring
- [x] **Reciteur hardcodé** → `strings.xml` (`reciter_default_name` + dialog strings)
- [x] **`@Suppress("UNUSED_PARAMETER")`** — `surahName` retiré de `playSurah` et `playSurahWithReciter` + tous les appelants mis à jour

### Fragments vides / Placeholder
- [ ] `ProfileFragment` + `ProfileViewModel` — Pas dans nav graph (sans effet)
- [ ] `SearchFragment` + `SearchViewModel` — Pas dans nav graph (sans effet)
- [ ] `DownloadsFragment` — Placeholder existant "قريباً"
- [ ] `PlayStore` icon — À créer manuellement (512×512)

### Divers
- [ ] ~~Commentaires français~~ — Vaste chantier optionnel
- [x] **Cache non-monotonic** → `SystemClock.elapsedRealtime()` dans `MessengerRepository`
- [x] **Validation URL CDN** — Vérification `startsWith("http")` dans `MessengerRepository`
- [x] **Foreground service type** — Déjà configuré (`mediaPlayback` + `POST_NOTIFICATIONS`)

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
