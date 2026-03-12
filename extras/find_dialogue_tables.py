#!/usr/bin/env python3
from bs4 import BeautifulSoup

soup = BeautifulSoup(open('../social_link_main_page.html', encoding='utf-8'), 'html.parser')

all_tables = soup.find_all('table')
print(f"Total tables: {len(all_tables)}\n")

# Look for tables with actual point values (numbers) and choices
for i, table in enumerate(all_tables):
    text = table.get_text()
    
    # More specific search - look for tables with "♪" (music note for points) or "+3" style points
    if ('♪' in text or '+3' in text or '+2' in text) and 'Rank' in text:
        print(f"=== Table {i+1} - Likely Social Link Dialogue Table ===")
        print(f"Classes: {table.get('class', [])}")
        
        rows = table.find_all('tr')
        print(f"Total rows: {len(rows)}")
        
        # Print first 10 rows
        for j, row in enumerate(rows[:10]):
            cells = row.find_all(['th', 'td'])
            cell_texts = [cell.get_text(strip=True)[:60] for cell in cells]
            print(f"  Row {j}: {cell_texts}")
        
        print()

# Also search for specific game sections
print("\n" + "="*60)
print("Searching for game-specific sections...")
print("="*60 + "\n")

# Look for headers mentioning Persona 3, 4, 5
headers = soup.find_all(['h2', 'h3', 'h4'])
for header in headers:
    text = header.get_text(strip=True)
    if any(game in text for game in ['Persona 3', 'Persona 4', 'Persona 5']):
        print(f"Found header: {text}")
