#!/usr/bin/env python3
import sqlite3
import json
import sys

DB_PATH = "/workspace/project/anime_real.db"

def get_anime_details(anime_id=None, anime_title=None):
    """
    Retrieve anime details by ID or title
    """
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    if anime_id:
        cursor.execute("SELECT * FROM animes WHERE postId = ?", (anime_id,))
    elif anime_title:
        cursor.execute("SELECT * FROM animes WHERE titleEn LIKE ?", (f"%{anime_title}%",))
    else:
        print("Error: Must provide either anime_id or anime_title")
        conn.close()
        return None
    
    anime_row = cursor.fetchone()
    
    if not anime_row:
        print(f"No anime found with the given {'ID' if anime_id else 'title'}")
        conn.close()
        return None
    
    anime = dict(anime_row)
    anime["genres"] = json.loads(anime["genres"]) if anime["genres"] else None
    
    # Get episodes
    cursor.execute("SELECT * FROM episodes WHERE animePostId = ? ORDER BY episodeNumber", (anime["postId"],))
    episode_rows = cursor.fetchall()
    
    episodes = []
    for row in episode_rows:
        episode = dict(row)
        episode["servers"] = json.loads(episode["servers"])
        episode["isFiller"] = bool(episode["isFiller"])
        episodes.append(episode)
    
    conn.close()
    
    return {
        "anime": anime,
        "episodes": episodes
    }

def list_all_animes():
    """
    List all animes in the database
    """
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    cursor.execute("SELECT postId, titleEn, type, year FROM animes ORDER BY titleEn")
    rows = cursor.fetchall()
    
    animes = []
    for row in rows:
        animes.append(dict(row))
    
    conn.close()
    return animes

def search_animes(query):
    """
    Search animes by title
    """
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    cursor.execute("""
    SELECT postId, titleEn, type, year FROM animes 
    WHERE titleEn LIKE ? OR titleJp LIKE ? OR titleAr LIKE ? OR description LIKE ?
    ORDER BY titleEn
    """, (f"%{query}%", f"%{query}%", f"%{query}%", f"%{query}%"))
    
    rows = cursor.fetchall()
    
    animes = []
    for row in rows:
        animes.append(dict(row))
    
    conn.close()
    return animes

def main():
    if len(sys.argv) < 2:
        print("Usage: python test_database.py [list|search|details] [query|anime_id]")
        return
    
    command = sys.argv[1]
    
    if command == "list":
        animes = list_all_animes()
        print(f"Found {len(animes)} animes:")
        for anime in animes:
            print(f"- {anime['titleEn']} ({anime['type']}, {anime['year']})")
    
    elif command == "search" and len(sys.argv) > 2:
        query = sys.argv[2]
        animes = search_animes(query)
        print(f"Found {len(animes)} animes matching '{query}':")
        for anime in animes:
            print(f"- {anime['titleEn']} ({anime['type']}, {anime['year']}) [ID: {anime['postId']}]")
    
    elif command == "details" and len(sys.argv) > 2:
        anime_id = sys.argv[2]
        anime_data = get_anime_details(anime_id=anime_id)
        
        if anime_data:
            anime = anime_data["anime"]
            episodes = anime_data["episodes"]
            
            print(f"\nAnime: {anime['titleEn']}")
            print(f"Japanese Title: {anime['titleJp']}")
            print(f"Arabic Title: {anime['titleAr']}")
            print(f"Type: {anime['type']}")
            print(f"Year: {anime['year']}")
            print(f"Status: {anime['status']}")
            print(f"Rating: {anime['rating']}")
            print(f"Episodes Count: {len(episodes)}")
            
            if anime["genres"]:
                print(f"Genres: {', '.join(anime['genres'])}")
            
            print(f"\nDescription: {anime['description'][:200]}...")
            
            print(f"\nEpisodes:")
            for episode in episodes[:5]:  # Show first 5 episodes
                print(f"- Episode {episode['episodeNumber']}: {episode['title']}")
                print(f"  Duration: {episode['duration']}")
                print(f"  Servers: {len(episode['servers'])}")
            
            if len(episodes) > 5:
                print(f"... and {len(episodes) - 5} more episodes")
    
    else:
        print("Invalid command or missing arguments")
        print("Usage: python test_database.py [list|search|details] [query|anime_id]")

if __name__ == "__main__":
    main()