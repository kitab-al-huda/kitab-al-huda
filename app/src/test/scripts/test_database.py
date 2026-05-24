#!/usr/bin/env python3
import sqlite3
import json
import sys

DB_PATH = "quran_real.db"

def get_reciter_details(reciter_id=None, reciter_name=None):
    """
    Retrieve reciter details by ID or name
    """
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    if reciter_id:
        cursor.execute("SELECT * FROM reciters WHERE postId = ?", (reciter_id,))
    elif reciter_name:
        cursor.execute("SELECT * FROM reciters WHERE nameEn LIKE ?", (f"%{reciter_name}%",))
    else:
        print("Error: Must provide either reciter_id or reciter_name")
        conn.close()
        return None

    reciter_row = cursor.fetchone()

    if not reciter_row:
        print(f"No reciter found with the given {'ID' if reciter_id else 'name'}")
        conn.close()
        return None

    reciter = dict(reciter_row)

    # Get surahs
    cursor.execute("SELECT * FROM surahs WHERE reciterPostId = ? ORDER BY CAST(surahNumber AS INTEGER)", (reciter["postId"],))
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

def list_all_reciters():
    """
    List all reciters in the database
    """
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    cursor.execute("SELECT postId, nameEn, surahsCount FROM reciters ORDER BY nameEn")
    rows = cursor.fetchall()

    reciters = []
    for row in rows:
        reciters.append(dict(row))

    conn.close()
    return reciters

def search_reciters(query):
    """
    Search reciters by name or description
    """
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    cursor.execute("""
    SELECT postId, nameEn, surahsCount FROM reciters
    WHERE nameEn LIKE ? OR nameAr LIKE ? OR description LIKE ?
    ORDER BY nameEn
    """, (f"%{query}%", f"%{query}%", f"%{query}%"))

    rows = cursor.fetchall()

    reciters = []
    for row in rows:
        reciters.append(dict(row))

    conn.close()
    return reciters

def main():
    if len(sys.argv) < 2:
        print("Usage: python test_database.py [list|search|details] [query|reciter_id]")
        return

    command = sys.argv[1]

    if command == "list":
        reciters = list_all_reciters()
        print(f"Found {len(reciters)} reciters:")
        for reciter in reciters:
            print(f"- {reciter['nameEn']} (Surahs: {reciter['surahsCount']})")

    elif command == "search" and len(sys.argv) > 2:
        query = sys.argv[2]
        reciters = search_reciters(query)
        print(f"Found {len(reciters)} reciters matching '{query}':")
        for reciter in reciters:
            print(f"- {reciter['nameEn']} (Surahs: {reciter['surahsCount']}) [ID: {reciter['postId']}]")

    elif command == "details" and len(sys.argv) > 2:
        reciter_id = sys.argv[2]
        reciter_data = get_reciter_details(reciter_id=reciter_id)

        if reciter_data:
            reciter = reciter_data["reciter"]
            surahs = reciter_data["surahs"]

            print(f"\nReciter: {reciter['nameEn']}")
            print(f"Arabic Name: {reciter['nameAr']}")
            print(f"Surahs Count: {len(surahs)}")

            print(f"\nDescription: {reciter['description'][:200]}...")

            print(f"\nSurahs:")
            for surah in surahs[:5]:  # Show first 5 surahs
                print(f"- Surah {surah['surahNumber']}: {surah['title']}")
                print(f"  Duration: {surah['duration']}")
                print(f"  Servers: {len(surah['servers'])}")

            if len(surahs) > 5:
                print(f"... and {len(surahs) - 5} more surahs")

    else:
        print("Invalid command or missing arguments")
        print("Usage: python test_database.py [list|search|details] [query|reciter_id]")

if __name__ == "__main__":
    main()
