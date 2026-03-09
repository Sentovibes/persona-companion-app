"""
Generate list of wiki URLs for manual image collection
Opens URLs in browser for easy manual downloading
"""
import json
import webbrowser
import time

def generate_enemy_urls():
    """Generate wiki URLs for all bosses"""
    games = {
        'P3R': '../app/src/main/assets/data/enemies/p3r_enemies.json',
        'P4G': '../app/src/main/assets/data/enemies/p4g_enemies.json',
        'P5R': '../app/src/main/assets/data/enemies/p5r_enemies.json',
    }
    
    all_urls = []
    
    for game, filepath in games.items():
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                enemies = json.load(f)
            
            # Filter to bosses only
            bosses = [e for e in enemies if e.get('isBoss') or e.get('isMiniBoss')]
            
            print(f"\n{game} Bosses: {len(bosses)}")
            
            for boss in bosses:
                name = boss.get('name')
                if not name:
                    continue
                
                search_name = name.replace(" ", "_")
                url = f"https://megamitensei.fandom.com/wiki/{search_name}"
                all_urls.append((game, name, url))
                
        except Exception as e:
            print(f"Error processing {game}: {e}")
    
    return all_urls

def save_url_list(urls):
    """Save URLs to a text file"""
    with open('enemy_wiki_urls.txt', 'w', encoding='utf-8') as f:
        f.write("# Enemy Wiki URLs for Manual Image Collection\n")
        f.write("# Right-click infobox image -> Save Image As...\n")
        f.write("# Save to: images/enemies/[game]/[name].png\n\n")
        
        current_game = None
        for game, name, url in urls:
            if game != current_game:
                f.write(f"\n# === {game} ===\n")
                current_game = game
            f.write(f"{name}|{url}\n")
    
    print(f"\nSaved {len(urls)} URLs to enemy_wiki_urls.txt")

def open_urls_in_browser(urls, limit=10):
    """Open URLs in browser (limited to avoid overwhelming)"""
    print(f"\nOpening first {limit} URLs in browser...")
    print("Right-click infobox image -> Copy Image Address")
    print("Or: Right-click -> Save Image As...")
    
    for i, (game, name, url) in enumerate(urls[:limit], 1):
        print(f"[{i}] Opening: {name}")
        webbrowser.open(url)
        time.sleep(2)  # Delay between opens

if __name__ == '__main__':
    print("Wiki URL Generator")
    print("=" * 50)
    
    urls = generate_enemy_urls()
    save_url_list(urls)
    
    print(f"\nTotal URLs: {len(urls)}")
    print("\nOptions:")
    print("  1. Open URLs in browser (first 10)")
    print("  2. Just save list to file")
    print("  3. Exit")
    
    choice = input("\nChoice: ")
    
    if choice == '1':
        open_urls_in_browser(urls, 10)
        print("\nContinue opening more? (y/n)")
        if input().lower() == 'y':
            open_urls_in_browser(urls[10:20], 10)
    
    print("\n[DONE] Check enemy_wiki_urls.txt for full list")
