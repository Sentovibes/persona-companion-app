"""
Enemy and Boss Data Scraper for Persona Games
Scrapes boss data from Megami Tensei Wiki
"""

import requests
from bs4 import BeautifulSoup
import json
import time
import re

class PersonaBossScraper:
    def __init__(self):
        self.base_url = "https://megamitensei.fandom.com"
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
    
    def scrape_boss_page(self, url):
        """Scrape a boss list page"""
        print(f"Scraping: {url}")
        try:
            response = requests.get(url, headers=self.headers)
            response.raise_for_status()
            soup = BeautifulSoup(response.content, 'html.parser')
            
            bosses = []
            
            # Find all tables with boss data
            tables = soup.find_all('table', class_='wikitable')
            
            for table in tables:
                rows = table.find_all('tr')[1:]  # Skip header
                
                for row in rows:
                    cols = row.find_all(['td', 'th'])
                    if len(cols) < 2:
                        continue
                    
                    # Extract boss name (usually first column)
                    name_cell = cols[0]
                    name = self.clean_text(name_cell.get_text())
                    
                    if not name or len(name) < 2:
                        continue
                    
                    # Try to extract level and HP
                    level = 0
                    hp = 0
                    
                    for col in cols:
                        text = self.clean_text(col.get_text())
                        # Look for level
                        if 'lv' in text.lower() or 'level' in text.lower():
                            level = self.parse_number(text)
                        # Look for HP
                        if 'hp' in text.lower():
                            hp = self.parse_number(text)
                    
                    boss = {
                        "name": name,
                        "arcana": "Boss",
                        "level": level if level > 0 else 99,
                        "hp": hp if hp > 0 else 9999,
                        "sp": 999,
                        "stats": {
                            "strength": 99,
                            "magic": 99,
                            "endurance": 99,
                            "agility": 99,
                            "luck": 99
                        },
                        "resists": "----------",
                        "skills": [],
                        "area": "Story Boss",
                        "exp": 0,
                        "drops": {
                            "gem": "-",
                            "item": "-"
                        }
                    }
                    
                    bosses.append(boss)
            
            return bosses
        except Exception as e:
            print(f"  Error: {e}")
            return []
    
    def clean_text(self, text):
        """Clean up text from HTML"""
        return text.strip().replace('\n', ' ').replace('\xa0', ' ').replace('  ', ' ')
    
    def parse_number(self, text):
        """Extract number from text"""
        text = self.clean_text(text)
        # Remove commas and extract digits
        text = text.replace(',', '')
        match = re.search(r'\d+', text)
        return int(match.group()) if match else 0
    
    def save_to_json(self, data, filename):
        """Save data to JSON file"""
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"  Saved: {filename} ({len(data)} bosses)")

def main():
    scraper = PersonaBossScraper()
    
    # Boss pages on the wiki
    boss_pages = {
        'p3_bosses': 'https://megamitensei.fandom.com/wiki/List_of_Persona_3_Bosses',
        'p4_bosses': 'https://megamitensei.fandom.com/wiki/List_of_Persona_4_Bosses',
        'p5_bosses': 'https://megamitensei.fandom.com/wiki/List_of_Persona_5_Bosses',
    }
    
    print("="*60)
    print("Persona Boss Data Scraper")
    print("="*60)
    print()
    
    for game_id, url in boss_pages.items():
        print(f"Processing {game_id}...")
        bosses = scraper.scrape_boss_page(url)
        
        if bosses:
            scraper.save_to_json(bosses, f"extras/{game_id}.json")
        else:
            print(f"  No bosses found for {game_id}")
        
        print()
        time.sleep(2)  # Be nice to the server
    
    print("="*60)
    print("Scraping complete!")
    print("="*60)

if __name__ == "__main__":
    main()

