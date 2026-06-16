# 🚀 Kitab al-Huda — Checklist de finalisation

> Dernière mise à jour : 16 juin 2026

---

## 🛒 Milestone — Préparation Play Store

| # | Tâche | Détail | Statut |
|---|-------|--------|--------|
| # | Tâche | Détail | Statut |
|---|-------|--------|--------|
| 4.1 | Créer un keystore `.jks` | `keytool` → `release.jks` à la racine | ✅ |
| 4.2 | Configurer `signingConfigs` dans `app/build.gradle.kts` | `rootProject.file()` + `gradle.properties` gitignoré | ✅ |
| 4.3 | Update `targetSdk = 35` | Dans `app/build.gradle.kts` | ✅ |
| 4.4 | Rédiger une Privacy Policy | `site/privacy.html` — couvre : aucune donnée, API Meta, audio streaming | ✅ |
| 4.5 | Ajouter lien privacy dans l'app | Item "سياسة الخصوصية" dans Settings, lien cliquable | ❌ |
| 4.6 | Tester un build release signé | `./gradlew :app:bundleRelease` → `app-release.aab` (11 MB) | ✅ |
| 4.7 | Sauvegarder `mapping.txt` | `app/build/outputs/mapping/release/mapping.txt` → Play Console | ✅ |

### Play Console — Fiche & Questionnaire

| # | Tâche | Statut |
|---|-------|--------|
| 4.8 | Compléter "Data Safety" (aucune donnée, pas de tracking) | ❌ |
| 4.9 | Questionnaire "Content Rating" | ❌ |
| 4.10 | Description + screenshots en arabe | ❌ |
| 4.11 | Upload AAB + mapping.txt en production | ❌ |

### Post-publication

| # | Tâche | Statut |
|---|-------|--------|
| 4.12 | Tester installation depuis Play Store | ❌ |
| 4.13 | Surveiller crashs (Android Vitals) | ❌ |
| 4.14 | Hotfix si besoin (version bump + rebuild signé) | ❌ |
