#!/usr/bin/env python3
"""
Test parsing of Social Link files to see what choices are extracted
"""

import json

def test_parse_file(filepath, game_name):
    print(f"\n{'='*60}")
    print(f"Testing: {game_name}")
    print('='*60)
    
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Test first non-Fool arcana
    for arcana, ranks in data.items():
        if arcana == "Fool":
            continue
            
        print(f"\nArcana: {arcana}")
        
        # Check first few ranks
        rank_count = 0
        for rank_key, rank_data in ranks.items():
            if rank_key in ["P4G Exclusive", "P5R Exclusive"] or rank_key.startswith("Flag"):
                continue
                
            rank_count += 1
            if rank_count > 3:  # Only show first 3 ranks
                break
                
            print(f"\n  {rank_key}:")
            
            if not isinstance(rank_data, dict):
                print(f"    ERROR: rank_data is not a dict, it's {type(rank_data)}")
                continue
            
            # Count choices
            choice_count = 0
            for key, value in rank_data.items():
                # Skip known non-choice fields
                if key in ["Next Rank", "Requires", "Requirements"]:
                    continue
                if any(stat in key for stat in ["Courage", "Knowledge", "Charm", "Diligence", "Understanding", "Guts", "Proficiency", "Kindness"]):
                    continue
                    
                choice_count += 1
                print(f"    Choice: '{key}' = {value}")
            
            if choice_count == 0:
                print(f"    (No choices found)")
        
        break  # Only test first arcana

if __name__ == "__main__":
    files = [
        ("../app/src/main/assets/data/social-links/p3p_male_social_links.json", "P3P Male"),
        ("../app/src/main/assets/data/social-links/p4+p4g_social_links.json", "P4/P4G"),
        ("../app/src/main/assets/data/social-links/p5+p5r_social_links.json", "P5/P5R")
    ]
    
    for filepath, game_name in files:
        test_parse_file(filepath, game_name)
