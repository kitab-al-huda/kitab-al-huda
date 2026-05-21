# Scripts de test pour Kitab al-Huda

Ces scripts permettent de tester la récupération des données d'animes et d'épisodes depuis l'API Facebook Graph, de la même façon que l'application Kitab al-Huda.

## test_facebook_api_real.py

Ce script récupère les données d'animes et d'épisodes depuis l'API Facebook Graph et les stocke dans une base de données SQLite locale.

### Utilisation

```bash
python3 test_facebook_api_real.py
```

Le script va :
1. Créer une base de données SQLite nommée `anime_real.db`
2. Récupérer tous les posts de la page Facebook
3. Extraire les données d'animes des posts
4. Récupérer les commentaires pour chaque post d'anime
5. Extraire les données d'épisodes des commentaires
6. Stocker toutes les données dans la base de données

## test_database.py

Ce script permet de tester la récupération des données depuis la base de données créée par `test_facebook_api_real.py`.

### Utilisation

```bash
# Lister tous les animes
python3 test_database.py list

# Rechercher des animes par titre
python3 test_database.py search "Demon Slayer"

# Afficher les détails d'un anime spécifique
python3 test_database.py details 547656128434943_122124313256749556
```

## Mise à jour du token d'accès

Le token d'accès Facebook expire après un certain temps. Pour mettre à jour le token, modifiez la constante `ACCESS_TOKEN` dans les fichiers suivants :
- `test_facebook_api_real.py`
- `/workspace/project/Kitab al-Huda/app/src/main/java/com/alfred/kitabalhuda/network/FacebookGraphApiService.kt`