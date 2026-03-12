#!/usr/bin/env python3
from bs4 import BeautifulSoup

soup = BeautifulSoup(open('../debug_page.html', encoding='utf-8'), 'html.parser')
tables = soup.find_all('table')

print(f"Found {len(tables)} tables\n")

for i, table in enumerate(tables):
    print(f"=== Table {i+1} ===")
    print(f"Classes: {table.get('class', [])}")
    
    # Get first row
    first_row = table.find('tr')
    if first_row:
        headers = [th.get_text(strip=True) for th in first_row.find_all(['th', 'td'])]
        print(f"Headers: {headers}")
    
    # Count rows
    rows = table.find_all('tr')
    print(f"Total rows: {len(rows)}")
    print()
