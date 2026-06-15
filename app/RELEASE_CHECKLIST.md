# 🚀 Kitab al-Huda — Checklist de finalisation

> Dernière mise à jour : 15 juin 2026

---

## 🔴 Milestone 1 — Critique (bloquant release)

- [ ] **Toasts anglais → strings.xml**
  - `QuranFragment.kt:64` — `"Playing: ${sourate.nomArabe}"`
  - `AddToPlaylistBottomSheet.kt:68` — `"Added to ${playlist.name} (${audioIds.size} parts)"`
  - `AddToPlaylistBottomSheet.kt:70` — `"Added to ${playlist.name}"`
- [ ] **Permission `POST_NOTIFICATIONS`** — Ajouter dans `AndroidManifest.xml` (API 33+)
- [ ] **`android:exported="false"`** sur `MainActivity` et `AudioPlayerService` (sauf si justifié)
- [ ] **`fallbackToDestructiveMigration()`** — Remplacer par des migrations Room explicites + `exportSchema = true`
- [ ] **Lint `UnsafeOptInUsageError`** — Ajouter `@OptIn` sur `ResolvingDataSource.Factory` dans `AudioPlayerService.kt:47`

---

## 🟡 Milestone 2 — Haute priorité

- [ ] **`runBlocking` sur main thread** — `AudioPlayerService.kt:53` → `withContext(Dispatchers.IO)` ou `suspendCancellableCoroutine`
- [ ] **Icône personnalisée** — Remplacer icône Android par défaut + fond `#C5A059` (or) au lieu de `#3DDC84` (vert)
- [ ] **ProGuard rules obsolètes** — Nettoyer `proguard-rules.pro` :
  - Supprimer `com.google.android.exoplayer2.**` (Media3 remplace ExoPlayer v1)
  - Supprimer les keep rules pour packages inexistants (`model.**`, `entity.**`, `worker.**`)
  - Supprimer les doublons (`network.**` gardé 2×)
  - Supprimer ou restreindre le keep-all générique (l.162-166)
- [ ] **`compileSdk` / `targetSdk` mismatch** — Uniformiser (35/35 ou 34/34)
- [ ] **Backup rules** — Configurer `data_extraction_rules.xml` et `backup_rules.xml`
- [ ] **`android:text` Latin visible au runtime** (4 layouts) → remplacer par `tools:text`
  - `item_playlist_mini.xml:23` — `"Playlist Name"`
  - `fragment_full_player.xml:84,95` — `"00:00"`
  - `item_playlist_track.xml:46` — `"1"`
- [ ] **`"HD"` qualité vidéo par défaut** — `PreferenceManager.kt:36` (audio-only, supprimer ou renommer)
- [ ] **`"سورة"` hardcodé** — `PlayerViewModel.kt:350` → déplacer vers `strings.xml`
- [ ] **Vitesse non reconnue** — `SettingsActivity.kt:85` — `speed.toString() + "x"` affiche du Latin → utiliser `toArabicIndic()` ou format arabisé

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
