#!/usr/bin/env python3
from bs4 import BeautifulSoup

soup = BeautifulSoup(open('../debug_page.html', encoding='utf-8'), 'html.parser')

# Get the collapsible table (Social Link table)
table = soup.find('table', class_='mw-collapsible')

if table:
    print("Found Social Link table!")
    rows = table.find_all('tr')
    print(f"Total rows: {len(rows)}\n")
    
    # Print first 10 rows to see structure
    for i, row in enumerate(rows[:10]):
        cells = row.find_all(['th', 'td'])
        cell_texts = [cell.get_text(strip=True)[:50] for cell in cells]
        print(f"Row {i}: {cell_texts}")
else:
    print("Table not found!")
