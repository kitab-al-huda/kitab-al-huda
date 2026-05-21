#!/usr/bin/env python3
import requests
import json
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
import sqlite3
import os
from datetime import datetime

# Constants from the app with updated token
BASE_URL = "https://graph.facebook.com/v22.0/"
PAGE_ID = "547656128434943"
ACCESS_TOKEN = "EAAKEBzaTQCwBO9VY7trZBIUD9AyqdFipvn7A1r4ZA7Tcde9SIDlS4S72bZA7XtMoZBxSIANBZCpmKJa7VQaZB5QOgALhSxz8jBUn4opzOYnjoS7jo4RBxtkDCAIBxZAIFMUeOOxCdnMjOHjxvNhlN7xQQfNDRL6QtyIPMAtrmut9CKrXhctg4CAkGXDaHGZC0wQekFynPXMh"
DB_PATH = "/workspace/project/anime_real.db"

# Function to get page posts (similar to getPagePosts in the app)
def get_page_posts(page_id: str, access_token: str, limit: int = 25, after: Optional[str] = None) -> Dict[str, Any]:
    url = f"{BASE_URL}{page_id}/feed"
    params = {
        "access_token": access_token,
        "limit": limit,
        "fields": "id,message,full_picture,created_time"
    }
    
    if after:
        params["after"] = after
    
    response = requests.get(url, params=params)
    print(f"API Response Status: {response.status_code}")
    if response.status_code != 200:
        print(f"Error response: {response.text}")
        return {}
    return response.json()

# Function to get post comments (similar to getPostComments in the app)
def get_post_comments(post_id: str, access_token: str, limit: int = 100, after: Optional[str] = None) -> Dict[str, Any]:
    url = f"{BASE_URL}{post_id}/comments"
    params = {
        "access_token": access_token,
        "limit": limit,
        "fields": "id,message,created_time"
    }
    
    if after:
        params["after"] = after
    
    response = requests.get(url, params=params)
    print(f"Comments API Response Status: {response.status_code}")
    if response.status_code != 200:
        print(f"Error response: {response.text}")
        return {}
    return response.json()

# Function to try parsing JSON (similar to tryParseJson in the app)
def try_parse_json(json_str: str):
    try:
        data = json.loads(json_str)
        return data
    except json.JSONDecodeError:
        return None

# Function to create the database schema
def create_database():
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Create animes table
    cursor.execute('''
    CREATE TABLE animes (
        postId TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        titleEn TEXT NOT NULL,
        titleJp TEXT,
        titleAr TEXT,
        description TEXT NOT NULL,
        imageUrl TEXT NOT NULL,
        imageId TEXT NOT NULL,
        episodes INTEGER,
        year INTEGER,
        rating TEXT,
        genres TEXT,
        studio TEXT,
        rank INTEGER,
        status TEXT,
        type TEXT,
        season TEXT,
        updatedAt TEXT NOT NULL,
        timestamp INTEGER NOT NULL
    )
    ''')
    
    # Create episodes table
    cursor.execute('''
    CREATE TABLE episodes (
        commentId TEXT PRIMARY KEY,
        animePostId TEXT NOT NULL,
        episodeNumber TEXT NOT NULL,
        title TEXT NOT NULL,
        addedBy TEXT NOT NULL,
        duration TEXT NOT NULL,
        isFiller INTEGER NOT NULL,
        servers TEXT NOT NULL,
        releaseDate TEXT,
        timestamp INTEGER NOT NULL,
        FOREIGN KEY (animePostId) REFERENCES animes (postId) ON DELETE CASCADE
    )
    ''')
    
    conn.commit()
    conn.close()
    
    print("Database created successfully")

# Function to insert anime data from Facebook
def insert_anime_data(post_id, anime_post):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    try:
        anime_data = anime_post["data"]
        title = anime_data["title"]
        info = anime_data["info"]
        image = anime_data["image"]
        
        cursor.execute('''
        INSERT INTO animes (
            postId, title, titleEn, titleJp, titleAr, description, imageUrl, imageId,
            episodes, year, rating, genres, studio, rank, status, type, season,
            updatedAt, timestamp
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            post_id,
            title["en"],
            title["en"],
            title.get("jp", ""),
            title.get("ar", ""),
            anime_data["description"],
            image["url"],
            image.get("id", ""),
            info.get("episodes"),
            info.get("year"),
            info.get("rating"),
            json.dumps(info.get("genres", [])),
            info.get("studio"),
            info.get("rank"),
            info.get("status"),
            info.get("type"),
            info.get("season"),
            anime_data.get("updatedAt", ""),
            int(datetime.now().timestamp() * 1000)
        ))
        
        conn.commit()
        print(f"Inserted anime: {title['en']}")
        return True
    except Exception as e:
        print(f"Error inserting anime: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()

# Function to insert episode data from Facebook
def insert_episode_data(comment_id, post_id, episode_comment):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    try:
        cursor.execute('''
        INSERT INTO episodes (
            commentId, animePostId, episodeNumber, title, addedBy,
            duration, isFiller, servers, releaseDate, timestamp
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            comment_id,
            post_id,
            episode_comment["episodeNumber"],
            episode_comment["title"],
            episode_comment["addedBy"],
            episode_comment["duration"],
            1 if episode_comment.get("isFiller", False) else 0,
            json.dumps(episode_comment.get("servers", [])),
            episode_comment.get("releaseDate", ""),
            int(datetime.now().timestamp() * 1000)
        ))
        
        conn.commit()
        print(f"Inserted episode: {episode_comment['episodeNumber']} - {episode_comment['title']}")
        return True
    except Exception as e:
        print(f"Error inserting episode: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()

# Function to retrieve anime with episodes
def get_anime_with_episodes(post_id):
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    # Get anime data
    cursor.execute("SELECT * FROM animes WHERE postId = ?", (post_id,))
    anime_row = cursor.fetchone()
    
    if not anime_row:
        conn.close()
        return None
    
    # Convert row to dict
    anime = dict(anime_row)
    
    # Parse JSON fields
    anime["genres"] = json.loads(anime["genres"]) if anime["genres"] else None
    
    # Get episodes data
    cursor.execute("SELECT * FROM episodes WHERE animePostId = ? ORDER BY episodeNumber", (post_id,))
    episode_rows = cursor.fetchall()
    
    episodes = []
    for row in episode_rows:
        episode = dict(row)
        # Parse JSON fields
        episode["servers"] = json.loads(episode["servers"])
        episode["isFiller"] = bool(episode["isFiller"])
        episodes.append(episode)
    
    conn.close()
    
    return {
        "anime": anime,
        "episodes": episodes
    }

# Function to get all animes
def get_all_animes():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    cursor.execute("SELECT * FROM animes ORDER BY timestamp DESC")
    rows = cursor.fetchall()
    
    animes = []
    for row in rows:
        anime = dict(row)
        # Parse JSON fields
        anime["genres"] = json.loads(anime["genres"]) if anime["genres"] else None
        animes.append(anime)
    
    conn.close()
    return animes

# Main function to retrieve data from Facebook
def main():
    print("Starting data retrieval from Facebook Graph API...")
    
    # Create database
    create_database()
    
    all_posts = []
    next_page_cursor = None
    
    # Get all posts from the page
    while True:
        posts_response = get_page_posts(PAGE_ID, ACCESS_TOKEN, after=next_page_cursor)
        
        if not posts_response or "data" not in posts_response:
            print("Error retrieving posts or no data returned")
            break
        
        print(f"Retrieved {len(posts_response['data'])} posts")
        
        # Process each post to find anime data
        for post in posts_response["data"]:
            if "message" in post and post["message"]:
                try:
                    anime_post = try_parse_json(post["message"])
                    
                    if anime_post and isinstance(anime_post, dict) and anime_post.get("type") == "anime":
                        all_posts.append((post["id"], anime_post))
                        print(f"Found anime: {anime_post.get('data', {}).get('title', {}).get('en', 'Unknown')}")
                except Exception as e:
                    print(f"Error parsing post: {e}")
        
        # Check if there's a next page
        if "paging" in posts_response and "cursors" in posts_response["paging"] and "after" in posts_response["paging"]["cursors"]:
            next_page_cursor = posts_response["paging"]["cursors"]["after"]
            print(f"Moving to next page with cursor: {next_page_cursor}")
        else:
            break
    
    print(f"Total anime posts found: {len(all_posts)}")
    
    # Insert anime data into database
    for post_id, anime_post in all_posts:
        insert_anime_data(post_id, anime_post)
        
        # Get episodes for this anime
        print(f"\nRetrieving episodes for post {post_id}")
        all_episodes = []
        next_page_cursor = None
        
        while True:
            comments_response = get_post_comments(post_id, ACCESS_TOKEN, after=next_page_cursor)
            
            if not comments_response or "data" not in comments_response:
                print("Error retrieving comments or no data returned")
                break
            
            print(f"Retrieved {len(comments_response['data'])} comments")
            
            # Process each comment to find episode data
            for comment in comments_response["data"]:
                if "message" in comment and comment["message"]:
                    try:
                        episode_comment = try_parse_json(comment["message"])
                        
                        if episode_comment and isinstance(episode_comment, dict) and episode_comment.get("type") == "episode":
                            all_episodes.append((comment["id"], episode_comment))
                            print(f"Found episode: {episode_comment.get('episodeNumber', 'Unknown')} - {episode_comment.get('title', 'Unknown')}")
                            insert_episode_data(comment["id"], post_id, episode_comment)
                    except Exception as e:
                        print(f"Error parsing comment: {e}")
            
            # Check if there's a next page
            if "paging" in comments_response and "cursors" in comments_response["paging"] and "after" in comments_response["paging"]["cursors"]:
                next_page_cursor = comments_response["paging"]["cursors"]["after"]
                print(f"Moving to next page with cursor: {next_page_cursor}")
            else:
                break
        
        print(f"Total episodes found for this anime: {len(all_episodes)}")
    
    # Display summary of data retrieved
    animes = get_all_animes()
    print(f"\nTotal animes in database: {len(animes)}")
    
    for anime in animes:
        anime_with_episodes = get_anime_with_episodes(anime["postId"])
        episodes_count = len(anime_with_episodes["episodes"]) if anime_with_episodes else 0
        print(f"- {anime['titleEn']} ({anime['type']}, {anime['year']}): {episodes_count} episodes")
    
    print("\nData retrieval complete!")

if __name__ == "__main__":
    main()