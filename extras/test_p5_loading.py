#!/usr/bin/env python3
"""
Test if P5 Social Links file can be loaded and parsed
"""

import json

filepath = "../app/src/main/assets/data/social-links/p5+p5r_social_links.json"

print("Testing P5/P5R Social Links file...")
print("="*60)

try:
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    print(f"✓ File loaded successfully")
    print(f"✓ Found {len(data.keys())} arcanas")
    
    # Check first few arcanas
    for i, (arcana, ranks) in enumerate(data.items()):
        if i >= 3:
            break
        print(f"\n{arcana}:")
        print(f"  Ranks: {len(ranks)}")
        
        # Check for exclusive flags
        if "P5R Exclusive" in ranks:
            print(f"  P5R Exclusive: {ranks['P5R Exclusive']}")
    
    # Check if there are any P5R exclusive arcanas
    p5r_exclusive = []
    for arcana, ranks in data.items():
        if isinstance(ranks, dict) and ranks.get("P5R Exclusive") == True:
            p5r_exclusive.append(arcana)
    
    if p5r_exclusive:
        print(f"\n✓ P5R Exclusive arcanas: {', '.join(p5r_exclusive)}")
    
    print("\n" + "="*60)
    print("✓ File is valid and ready to use")
    
except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()
