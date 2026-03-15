import json
import math

def calculate_normal_fusion(demon_a, demon_b, fusion_chart, demon_db, is_p5=False):
    # 1. Extract base data
    arcana_a, lvl_a = demon_a['race'], demon_a['lvl']
    arcana_b, lvl_b = demon_b['race'], demon_b['lvl']

    # 2. Find Arcana indices
    races = fusion_chart['races']
    table = fusion_chart['table']
    
    try:
        idx_a = races.index(arcana_a)
        idx_b = races.index(arcana_b)
    except ValueError:
        return "Invalid Arcana"

    # 3. Look up the resulting Arcana
    if is_p5:
        # P5 uses a triangular matrix: table[max][min]
        max_idx = max(idx_a, idx_b)
        min_idx = min(idx_a, idx_b)
        result_arcana = table[max_idx][min_idx]
    else:
        # P3/P4 use a square matrix: table[row][col]
        result_arcana = table[idx_a][idx_b]

    # If the chart returns "-", the fusion is impossible
    if result_arcana == "-":
        return "Incompatible"

    # 4. Calculate the Target Level
    target_lvl = math.floor((lvl_a + lvl_b) / 2) + 1

    # 5. Find the correct Demon
    # Filter for the resulting arcana and ignore Special Fusion/DLC demons if necessary
    possible_demons = [
        d for name, d in demon_db.items() 
        if d['race'] == result_arcana and not d.get('isSpecial', False)
    ]
    
    # Sort by base level ascending
    possible_demons.sort(key=lambda x: x['lvl'])

    for demon in possible_demons:
        # The result must be >= target level AND cannot be one of the ingredients
        if demon['lvl'] >= target_lvl and demon['name'] not in (demon_a['name'], demon_b['name']):
            return demon['name']
    
    # Edge case: If the target level exceeds the highest demon in that Arcana,
    # the game falls back to the highest available demon in that Arcana.
    if possible_demons:
        # Iterate backwards to find the highest valid one
        for demon in reversed(possible_demons):
             if demon['name'] not in (demon_a['name'], demon_b['name']):
                 return demon['name']

    return "No valid demon found"