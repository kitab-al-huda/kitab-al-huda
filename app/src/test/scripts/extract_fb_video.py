import requests
import re
from urllib.parse import unquote, quote

def extract_facebook_video_urls(video_url):
    """
    Extracts direct video URLs for different qualities (HD and SD) from a Facebook video URL.

    Args:
        video_url (str): The original URL of the Facebook video.

    Returns:
        dict: A dictionary containing the 'hd_src' and 'sd_src' URLs if found.
              Returns an empty dictionary if no sources are found or an error occurs.
    """
    headers = {
        # Using a common user-agent can help avoid being blocked.
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }
    
    try:
        # 1. Construct the embed URL, similar to the JavaScript version.
        embed_url = f"https://www.facebook.com/plugins/video.php?href={quote(video_url)}"
        
        # 2. Fetch the HTML content of the embed page.
        response = requests.get(embed_url, headers=headers, timeout=10)
        response.raise_for_status()  # Raise an exception for bad status codes (4xx or 5xx)
        
        html_content = response.text
        
        # 3. Use regular expressions to find HD and SD video sources.
        # The regex patterns are adapted from the JavaScript code.
        hd_match = re.search(r'"hd_src":"(.*?)"', html_content)
        sd_match = re.search(r'"sd_src":"(.*?)"', html_content)
        
        sources = {}
        
        if hd_match:
            # Decode URL-encoded characters and fix escaped slashes.
            hd_url = unquote(hd_match.group(1)).replace('\\/', '/')
            sources['hd_src'] = hd_url
            
        if sd_match:
            # Decode URL-encoded characters and fix escaped slashes.
            sd_url = unquote(sd_match.group(1)).replace('\\/', '/')
            sources['sd_src'] = sd_url
            
        return sources

    except requests.exceptions.RequestException as e:
        print(f"An error occurred during the request: {e}")
        return {}
    except Exception as e:
        print(f"An unexpected error occurred: {e}")
        return {}

# --- Example Usage ---
if __name__ == "__main__":
    # Replace this with the Facebook video URL you want to process.
    # Note: This works best with public video URLs.
    sample_video_url = "https://web.facebook.com/villedechambly/videos/1706330509998494/" # Example public video
    
    video_links = extract_facebook_video_urls(sample_video_url)
    
    if video_links:
        print("Successfully extracted video URLs:")
        if 'hd_src' in video_links:
            print(f"  HD Quality: {video_links['hd_src']}")
        if 'sd_src' in video_links:
            print(f"  SD Quality: {video_links['sd_src']}")
    else:
        print("Could not extract video URLs. The video might be private, deleted, or the URL format may have changed.")

