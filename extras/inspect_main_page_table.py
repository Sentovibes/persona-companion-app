#!/usr/bin/env python3
from bs4 import BeautifulSoup

soup = BeautifulSoup(open('../social_link_main_page.html', encoding='utf-8'), 'html.parser')

# Find the table with rank data
all_tables = soup.find_all('table')
print(f"Total tables: {len(all_tables)}\n")

for i, table in enumerate(all_tables):
    text = table.get_text()
    if 'Rank' in text and ('Choice' in text or 'Response' in text or 'Points' in text):
        print(f"=== Table {i+1} with Rank Data ===")
        print(f"Classes: {table.get('class', [])}")
        
        rows = table.find_all('tr')
        print(f"Total rows: {len(rows)}\n")
        
        # Print first 15 rows
        for j, row in enumerate(rows[:15]):
            cells = row.find_all(['th', 'td'])
            cell_texts = [cell.get_text(strip=True)[:80] for cell in cells]
            print(f"Row {j}: {cell_texts}")
        
        print("\n" + "="*60 + "\n")
