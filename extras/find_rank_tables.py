#!/usr/bin/env python3
from bs4 import BeautifulSoup

soup = BeautifulSoup(open('../debug_page.html', encoding='utf-8'), 'html.parser')

# Search for text containing "Rank" or "Choice" or "Points"
print("Searching for Social Link rank data...\n")

# Look for headers containing these keywords
headers = soup.find_all(['h2', 'h3', 'h4'])
for header in headers:
    text = header.get_text(strip=True)
    if any(keyword in text.lower() for keyword in ['rank', 'social link', 'confidant']):
        print(f"Found header: {text}")

print("\n" + "="*60)
print("Searching for tables with 'Rank' in content...")
print("="*60 + "\n")

all_tables = soup.find_all('table')
for i, table in enumerate(all_tables):
    text = table.get_text()
    if 'Rank' in text and ('Choice' in text or 'Response' in text or 'Points' in text):
        print(f"Table {i+1} might be a Social Link table!")
        print(f"  Classes: {table.get('class', [])}")
        rows = table.find_all('tr')
        if rows:
            first_row = rows[0]
            headers = [cell.get_text(strip=True) for cell in first_row.find_all(['th', 'td'])]
            print(f"  Headers: {headers}")
        print()
