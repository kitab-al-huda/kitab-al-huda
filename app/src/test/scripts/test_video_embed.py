#!/usr/bin/env python3
import re
import sqlite3
import json
import requests
from urllib.parse import urlparse, parse_qs

# Chemin vers la base de données
DB_PATH = "quran_real.db"

def get_all_video_urls():
    """Récupère toutes les URLs vidéo des serveurs dans la base de données"""
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    cursor.execute("SELECT commentId, servers FROM surahs")
    rows = cursor.fetchall()

    video_urls = []
    for row in rows:
        servers = json.loads(row["servers"])
        for server in servers:
            video_urls.append({
                "surah_id": row["commentId"],
                "server_name": server.get("name", "Unknown"),
                "url": server.get("url", "")
            })

    conn.close()
    return video_urls

def convert_facebook_to_embed(url):
    """Convertit une URL Facebook en URL embarquée"""
    # Vérifier si c'est une URL Facebook
    if "facebook.com" in url or "fb.watch" in url:
        # Cas 1: URL de type fb.watch
        if "fb.watch" in url:
            try:
                # Récupérer la page pour obtenir l'URL réelle
                response = requests.get(url)
                if response.status_code == 200:
                    # Extraire l'URL réelle depuis la redirection
                    real_url = response.url
                    # Extraire l'ID de la vidéo
                    video_id = None
                    if "/videos/" in real_url:
                        video_id = re.search(r'/videos/(\d+)', real_url).group(1)
                    elif "/watch/" in real_url:
                        video_id = re.search(r'v=(\d+)', real_url).group(1)

                    if video_id:
                        return f"https://www.facebook.com/plugins/video.php?href=https://www.facebook.com/watch/?v={video_id}&show_text=false&width=560&height=315&appId"
            except Exception as e:
                print(f"Erreur lors de la conversion de l'URL fb.watch: {e}")
                return url

        # Cas 2: URL de type facebook.com/watch
        elif "/watch/" in url or "/watch?" in url:
            try:
                # Extraire l'ID de la vidéo
                parsed_url = urlparse(url)
                if parsed_url.query:
                    query_params = parse_qs(parsed_url.query)
                    video_id = query_params.get('v', [None])[0]
                    if video_id:
                        return f"https://www.facebook.com/plugins/video.php?href=https://www.facebook.com/watch/?v={video_id}&show_text=false&width=560&height=315&appId"
            except Exception as e:
                print(f"Erreur lors de la conversion de l'URL facebook.com/watch: {e}")
                return url

        # Cas 3: URL de type facebook.com/username/videos/id
        elif "/videos/" in url:
            try:
                video_id = re.search(r'/videos/(\d+)', url).group(1)
                return f"https://www.facebook.com/plugins/video.php?href={url}&show_text=false&width=560&height=315&appId"
            except Exception as e:
                print(f"Erreur lors de la conversion de l'URL facebook.com/videos: {e}")
                return url

    # Si ce n'est pas une URL Facebook ou si la conversion a échoué, retourner l'URL d'origine
    return url

def convert_youtube_to_embed(url):
    """Convertit une URL YouTube en URL embarquée"""
    youtube_regex = r'(https?://)?(www\.)?(youtube|youtu|youtube-nocookie)\.(com|be)/(watch\?v=|embed/|v/|.+\?v=)?([^&=%\?]{11})'

    youtube_match = re.match(youtube_regex, url)
    if youtube_match:
        youtube_id = youtube_match.group(6)
        return f'https://www.youtube.com/embed/{youtube_id}'

    return url

def convert_to_embed_url(url):
    """Convertit n'importe quelle URL vidéo en URL embarquée"""
    if "facebook.com" in url or "fb.watch" in url:
        return convert_facebook_to_embed(url)
    elif "youtube.com" in url or "youtu.be" in url:
        return convert_youtube_to_embed(url)
    return url

def main():
    """Fonction principale pour tester la conversion des URLs"""
    print("Récupération des URLs vidéo depuis la base de données...")

    try:
        video_urls = get_all_video_urls()
        print(f"Nombre total d'URLs vidéo trouvées: {len(video_urls)}")

        # Tester la conversion pour quelques URLs
        for i, video in enumerate(video_urls[:5]):  # Limiter à 5 exemples
            original_url = video["url"]
            embed_url = convert_to_embed_url(original_url)

            print(f"\nExemple {i+1}:")
            print(f"Serveur: {video['server_name']}")
            print(f"URL originale: {original_url}")
            print(f"URL embarquée: {embed_url}")

        # Tester avec des exemples spécifiques
        test_urls = [
            "https://www.facebook.com/watch?v=123456789",
            "https://fb.watch/abcdef123/",
            "https://www.facebook.com/username/videos/123456789",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ"
        ]

        print("\n\nTests avec des exemples spécifiques:")
        for url in test_urls:
            embed_url = convert_to_embed_url(url)
            print(f"Original: {url}")
            print(f"Embarquée: {embed_url}\n")

        print("\nHTML pour tester l'intégration:")
        for url in test_urls:
            embed_url = convert_to_embed_url(url)
            html = f'<iframe src="{embed_url}" width="560" height="315" style="border:none;overflow:hidden" scrolling="no" frameborder="0" allowfullscreen="true" allow="autoplay; clipboard-write; encrypted-media; picture-in-picture; web-share"></iframe>'
            print(f"{html}\n")

    except Exception as e:
        print(f"Erreur: {e}")
        print("La base de données n'existe peut-être pas encore. Exécutez d'abord test_facebook_api_real.py pour créer la base de données.")

if __name__ == "__main__":
    main()
