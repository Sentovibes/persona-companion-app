#!/usr/bin/env python3
"""
Validate all Social Link JSON files
"""

import json
import os

def validate_social_link_file(filepath):
    """Validate a single Social Link JSON file"""
    print(f"\n{'='*60}")
    print(f"Validating: {os.path.basename(filepath)}")
    print('='*60)
    
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        arcana_count = len(data.keys())
        print(f"✓ Valid JSON")
        print(f"✓ Found {arcana_count} arcanas")
        
        total_ranks = 0
        total_choices = 0
        
        for arcana, ranks in data.items():
            # Skip exclusive flags
            if arcana in ["P4G Exclusive", "P5R Exclusive"]:
                continue
                
            rank_count = 0
            for rank_key, rank_data in ranks.items():
                # Skip flags
                if rank_key in ["P4G Exclusive", "P5R Exclusive"] or rank_key.startswith("Flag"):
                    continue
                    
                rank_count += 1
                
                # Count choices (excluding Next Rank and requirement fields)
                if isinstance(rank_data, dict):
                    choice_count = sum(1 for k in rank_data.keys() 
                                     if k not in ["Next Rank", "Requires", "Courage", "Knowledge", 
                                                 "Charm", "Diligence", "Understanding"])
                    total_choices += choice_count
            
            total_ranks += rank_count
            print(f"  • {arcana}: {rank_count} ranks")
        
        print(f"\n✓ Total: {total_ranks} ranks, {total_choices} dialogue choices")
        return True
        
    except json.JSONDecodeError as e:
        print(f"❌ JSON Error: {e}")
        return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

if __name__ == "__main__":
    base_path = "../app/src/main/assets/data/social-links"
    
    files = [
        "p3fes_social_links.json",
        "p3p_male_social_links.json",
        "p3p_femc_social_links.json",
        "p3r_social_links.json",
        "p4+p4g_social_links.json",
        "p5+p5r_social_links.json"
    ]
    
    print("="*60)
    print("SOCIAL LINKS VALIDATION")
    print("="*60)
    
    all_valid = True
    for filename in files:
        filepath = os.path.join(base_path, filename)
        if os.path.exists(filepath):
            if not validate_social_link_file(filepath):
                all_valid = False
        else:
            print(f"\n❌ File not found: {filename}")
            all_valid = False
    
    print("\n" + "="*60)
    if all_valid:
        print("✅ ALL FILES VALID!")
    else:
        print("❌ SOME FILES HAVE ERRORS")
    print("="*60)
