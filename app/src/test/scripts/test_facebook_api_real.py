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
ACCESS_TOKEN = os.environ.get("FACEBOOK_PAGE_ACCESS_TOKEN", "YOUR_ACCESS_TOKEN_HERE")
DB_PATH = "quran_real.db"

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

    # Create reciters table
    cursor.execute('''
    CREATE TABLE reciters (
        postId TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        nameEn TEXT NOT NULL,
        nameAr TEXT,
        description TEXT NOT NULL,
        imageUrl TEXT NOT NULL,
        imageId TEXT NOT NULL,
        surahsCount INTEGER,
        updatedAt TEXT NOT NULL,
        timestamp INTEGER NOT NULL
    )
    ''')

    # Create surahs table
    cursor.execute('''
    CREATE TABLE surahs (
        commentId TEXT PRIMARY KEY,
        reciterPostId TEXT NOT NULL,
        surahNumber TEXT NOT NULL,
        title TEXT NOT NULL,
        addedBy TEXT NOT NULL,
        duration TEXT NOT NULL,
        servers TEXT NOT NULL,
        releaseDate TEXT,
        timestamp INTEGER NOT NULL,
        FOREIGN KEY (reciterPostId) REFERENCES reciters (postId) ON DELETE CASCADE
    )
    ''')

    conn.commit()
    conn.close()

    print("Database created successfully")

# Function to insert reciter data from Facebook
def insert_reciter_data(post_id, reciter_post):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    try:
        reciter_data = reciter_post["data"]
        name_info = reciter_data["name"]
        info = reciter_data.get("info", {})
        image = reciter_data["image"]

        cursor.execute('''
        INSERT INTO reciters (
            postId, name, nameEn, nameAr, description, imageUrl, imageId,
            surahsCount, updatedAt, timestamp
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            post_id,
            name_info["en"],
            name_info["en"],
            name_info.get("ar", ""),
            reciter_data["description"],
            image["url"],
            image.get("id", ""),
            info.get("surahsCount"),
            reciter_data.get("updatedAt", ""),
            int(datetime.now().timestamp() * 1000)
        ))

        conn.commit()
        print(f"Inserted reciter: {name_info['en']}")
        return True
    except Exception as e:
        print(f"Error inserting reciter: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()

# Function to insert surah data from Facebook
def insert_surah_data(comment_id, post_id, surah_comment):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    try:
        cursor.execute('''
        INSERT INTO surahs (
            commentId, reciterPostId, surahNumber, title, addedBy,
            duration, servers, releaseDate, timestamp
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            comment_id,
            post_id,
            surah_comment["surahNumber"],
            surah_comment["title"],
            surah_comment["addedBy"],
            surah_comment["duration"],
            json.dumps(surah_comment.get("servers", [])),
            surah_comment.get("releaseDate", ""),
            int(datetime.now().timestamp() * 1000)
        ))

        conn.commit()
        print(f"Inserted surah: {surah_comment['surahNumber']} - {surah_comment['title']}")
        return True
    except Exception as e:
        print(f"Error inserting surah: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()

# Function to retrieve reciter with surahs
def get_reciter_with_surahs(post_id):
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    # Get reciter data
    cursor.execute("SELECT * FROM reciters WHERE postId = ?", (post_id,))
    reciter_row = cursor.fetchone()

    if not reciter_row:
        conn.close()
        return None

    # Convert row to dict
    reciter = dict(reciter_row)

    # Get surahs data
    cursor.execute("SELECT * FROM surahs WHERE reciterPostId = ? ORDER BY CAST(surahNumber AS INTEGER)", (post_id,))
    surah_rows = cursor.fetchall()

    surahs = []
    for row in surah_rows:
        surah = dict(row)
        surah["servers"] = json.loads(surah["servers"])
        surahs.append(surah)

    conn.close()

    return {
        "reciter": reciter,
        "surahs": surahs
    }

# Function to get all reciters
def get_all_reciters():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM reciters ORDER BY timestamp DESC")
    rows = cursor.fetchall()

    reciters = []
    for row in rows:
        reciters.append(dict(row))

    conn.close()
    return reciters

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

        # Process each post to find reciter data
        for post in posts_response["data"]:
            if "message" in post and post["message"]:
                try:
                    reciter_post = try_parse_json(post["message"])

                    if reciter_post and isinstance(reciter_post, dict) and reciter_post.get("type") in ("reciter", "anime"):  # "anime" is legacy format fallback
                        all_posts.append((post["id"], reciter_post))
                        print(f"Found reciter: {reciter_post.get('data', {}).get('name', {}).get('en', 'Unknown')}")
                except Exception as e:
                    print(f"Error parsing post: {e}")

        # Check if there's a next page
        if "paging" in posts_response and "cursors" in posts_response["paging"] and "after" in posts_response["paging"]["cursors"]:
            next_page_cursor = posts_response["paging"]["cursors"]["after"]
            print(f"Moving to next page with cursor: {next_page_cursor}")
        else:
            break

    print(f"Total reciter posts found: {len(all_posts)}")

    # Insert reciter data into database
    for post_id, reciter_post in all_posts:
        insert_reciter_data(post_id, reciter_post)

        # Get surahs for this reciter
        print(f"\nRetrieving surahs for post {post_id}")
        all_surahs = []
        next_page_cursor = None

        while True:
            comments_response = get_post_comments(post_id, ACCESS_TOKEN, after=next_page_cursor)

            if not comments_response or "data" not in comments_response:
                print("Error retrieving comments or no data returned")
                break

            print(f"Retrieved {len(comments_response['data'])} comments")

            # Process each comment to find surah data
            for comment in comments_response["data"]:
                if "message" in comment and comment["message"]:
                    try:
                        surah_comment = try_parse_json(comment["message"])

                        if surah_comment and isinstance(surah_comment, dict) and surah_comment.get("type") in ("surah", "episode"):
                            all_surahs.append((comment["id"], surah_comment))
                            print(f"Found surah: {surah_comment.get('surahNumber', 'Unknown')} - {surah_comment.get('title', 'Unknown')}")
                            insert_surah_data(comment["id"], post_id, surah_comment)
                    except Exception as e:
                        print(f"Error parsing comment: {e}")

            # Check if there's a next page
            if "paging" in comments_response and "cursors" in comments_response["paging"] and "after" in comments_response["paging"]["cursors"]:
                next_page_cursor = comments_response["paging"]["cursors"]["after"]
                print(f"Moving to next page with cursor: {next_page_cursor}")
            else:
                break

        print(f"Total surahs found for this reciter: {len(all_surahs)}")

    # Display summary of data retrieved
    reciters = get_all_reciters()
    print(f"\nTotal reciters in database: {len(reciters)}")

    for reciter in reciters:
        reciter_with_surahs = get_reciter_with_surahs(reciter["postId"])
        surahs_count = len(reciter_with_surahs["surahs"]) if reciter_with_surahs else 0
        print(f"- {reciter['nameEn']}: {surahs_count} surahs")

    print("\nData retrieval complete!")

if __name__ == "__main__":
    main()
