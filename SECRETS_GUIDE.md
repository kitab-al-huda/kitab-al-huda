# Guide : Configurer les secrets Facebook dans Kitab Al-Huda

> Ce guide explique **étape par étape** quoi faire après avoir obtenu ton
> `access_token`, `page_id` et `base_url` Facebook.
> 
> Dans cette version, tous les secrets sont stockés de manière chiffrée et tous les noms des variables cryptographiques et d'API ont été renommés de manière extrêmement générique pour masquer leur rôle à tout rétro-ingénieur.

---

## Architecture de sécurité

```
.env (local, jamais commité)
 ├── INTERNAL_BUILD_ID          ← moitié 1 de la clé AES (identifiant générique de build)
 ├── API_ENDPOINT_BASE          ← URL Base chiffrée en AES
 ├── API_SERVICE_IDENTIFIER     ← Page ID chiffré en AES
 └── API_AUTH_SIGNATURE         ← Access Token chiffré en AES

res/values/strings.xml (dans l'APK)
 └── app_build_signature        ← moitié 2 de la clé AES (signature générique de build)

encrypt.py (script local, jamais commité)
 └── lit les secrets en clair → détecte automatiquement la signature de build → chiffre → écrit dans .env

app/build.gradle.kts
 └── loadEnvFile() → lit .env → injecte dans BuildConfig

BuildConfig (généré au build)
 └── fournit les valeurs chiffrées au code Kotlin sous des noms génériques

CryptoUtils.kt (runtime)
 └── combine INTERNAL_BUILD_ID + app_build_signature → déchiffre → secrets en clair en mémoire

MessengerRepository.kt (runtime)
 └── instancie Retrofit avec les secrets déchiffrés
```

---

## Flux complet (chiffrement → déchiffrement)

```
Valeurs brutes (token, pageId, baseUrl) dans .env
    │
    ▼  encrypt.py (AES-256-CBC, clé = INTERNAL_BUILD_ID + app_build_signature, IV=0)
Valeurs chiffrées (Base64)
    │
    ▼  stockées automatiquement dans .env (API_ENDPOINT_BASE, etc.)
    │
    ▼  lues par Gradle (loadEnvFile())
    │
    ▼  injectées dans BuildConfig au build
    │
    ▼  décryptées par CryptoUtils.decrypt() au lancement de l'app
    │
    ▼  utilisées par Retrofit pour appeler l'API Graph Facebook
    │
    ▼  URL CDN retournée → mise en cache (LRU, TTL 45 min)
    │
    ▼  lue par ExoPlayer / MediaPlayer dans l'UI
```

---

### Étape 1 — Ouvrir `.env` et renseigner les secrets en clair

Ouvre `.env` à la racine du projet et modifie la section des valeurs en clair avec tes secrets de production réels :

```ini
# --- Valeurs en clair (lues par encrypt.py → jamais dans le code) ---
# ✏️  Mets ici tes vraies valeurs, puis lance : python encrypt.py
FACEBOOK_BASE_URL=https://graph.facebook.com/v22.0/
FACEBOOK_PAGE_ID=TON_VRAI_PAGE_ID
FACEBOOK_ACCESS_TOKEN=TON_VRAI_TOKEN
```

> ⚠️ Ces valeurs ne sont pas injectées dans l'application. Elles sont stockées localement dans `.env` (exclu de Git) et lues uniquement par `encrypt.py` pour générer automatiquement les versions chiffrées ci-dessous dans le même fichier.

---

### Étape 2 — Choisir de vraies clés de chiffrement

**Dans `.env`**, change la ligne `INTERNAL_BUILD_ID` (partie 1) :

```ini
INTERNAL_BUILD_ID=ChoisisUnMotDePasseLong1
```

**Dans `app/src/main/res/values/strings.xml`** (ligne ~101) :

```xml
<string name="app_build_signature">ChoisisUnMotDePasseLong2</string>
```


> 💡 Exemples de bonnes clés : `K1tab@Huda#2026` et `S3cret$P4rt2!`
> N'importe quoi de long, aléatoire et difficile à deviner.
> 
> ⚠️ Le fichier `strings.xml` doit contenir la valeur correcte pour `app_build_signature`.

---

### Étape 3 — Lancer `encrypt.py` pour chiffrer automatiquement

```bash
cd /home/alfredo/Programmation/android_apps_projet/kitab-al-huda

# Lancer le script de chiffrement automatique dans le venv
.venv/bin/python encrypt.py
```

Le script va s'exécuter **instantanément sans te demander aucune saisie** :
1. Il détecte automatiquement `app_build_signature` dans `app/src/main/res/values/strings.xml`.
2. Il lit directement les secrets bruts que tu as saisis à l'**Étape 1**.
3. Il les chiffre avec AES-256 et réécrit directement les clés `API_ENDPOINT_BASE`, `API_SERVICE_IDENTIFIER` et `API_AUTH_SIGNATURE` dans ton fichier `.env`.

---

### Étape 4 — Vérifier `.env` après le script

Le script met à jour automatiquement la section des valeurs chiffrées dans ton fichier `.env` :

```ini
API_ENDPOINT_BASE=<nouvelle valeur Base64>
API_SERVICE_IDENTIFIER=<nouvelle valeur Base64>
API_AUTH_SIGNATURE=<nouvelle valeur Base64>
```

> ❌ Si les valeurs ne changent pas ou si la génération échoue → vérifie que tu as bien configuré le fichier `strings.xml` avec la bonne clé `app_build_signature` et que tes secrets bruts dans le `.env` sont corrects.

---

### Étape 5 — Vérifier le build Gradle

```bash
./gradlew :app:generateDebugBuildConfig
```

Résultat attendu :

```
BUILD SUCCESSFUL
```

**Erreurs possibles :**

| Erreur | Cause | Solution |
|--------|-------|----------|
| `Fichier .env introuvable` | `.env` absent de la racine | Vérifier que `.env` est bien à la racine du projet |
| `NullPointerException` dans `build.gradle.kts` | Clé manquante dans `.env` | Vérifier que toutes les clés chiffrées (`API_...`) sont présentes |
| Espaces ou guillemets dans les valeurs | Format incorrect dans `.env` | Le format doit être `CLE=VALEUR` sans guillemets ni espaces |

---

### Étape 6 — Lancer l'app

```bash
./gradlew :app:installDebug
```

Ou depuis Android Studio → **Run ▶**.

---

## Ce qu'il ne faut JAMAIS faire

| ❌ Interdit | ✅ À la place |
|------------|--------------|
| Écrire le token directement dans le code | Le mettre dans `.env` en clair, puis lancer `encrypt.py` |
| Commiter `.env` | `.env` est dans `.gitignore` — c'est bon |
| Utiliser `mykey1` / `mykey2` en production | Choisir de vraies clés longues et aléatoires |
| Mettre les deux parties de la clé dans `.env` | Part 1 (`INTERNAL_BUILD_ID`) dans `.env`, Part 2 (`app_build_signature`) dans `strings.xml` |
| Partager `.env` par email/Slack | Partager uniquement `.env.example` |
| Modifier `app_build_signature` sans rechiffrer | Relancer `encrypt.py` après tout changement de clé |

---

## Points de vigilance récurrents

### Renouvellement du token Facebook
Le Long-Lived Page Access Token expire après **~60 jours**.
Quand il expire, répéter les **Étapes 1 → 5** avec le nouveau token.

### Changement de clés
Si tu changes `INTERNAL_BUILD_ID` ou `app_build_signature`,
tu dois **rechiffrer toutes les valeurs** (relancer `encrypt.py`).

### Environnement virtuel Python
Toujours utiliser l'environnement virtuel pour lancer `encrypt.py` :

```bash
.venv/bin/python encrypt.py
```

---

## Fichiers impliqués

| Fichier | Rôle | Commité dans Git ? |
|---------|------|-------------------|
| `.env` | Secrets chiffrés + clé part 1 | ❌ Non |
| `.env.example` | Template vide pour les nouveaux devs | ✅ Oui |
| `encrypt.py` | Script de chiffrement automatique | ❌ Non |
| `gradle.properties` | Config Gradle uniquement | ✅ Oui |
| `local.properties` | Chemin SDK Android | ❌ Non |
| `app/build.gradle.kts` | Lit `.env` → injecte dans BuildConfig | ✅ Oui |
| `res/values/strings.xml` | Clé part 2 (dans l'APK) | ✅ Oui |

| `utils/CryptoUtils.kt` | Déchiffrement au runtime | ✅ Oui |
| `repository/MessengerRepository.kt` | Utilise les valeurs déchiffrées | ✅ Oui |

---

*Dernière mise à jour : 2026-05-22*
