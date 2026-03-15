"""Create separate P5 and P5R fusion charts from tool data."""
import json
from pathlib import Path

TOOL = Path('C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app')
OUR_WEB = Path('web/data')

def load(path):
    with open(path, encoding='utf-8') as f:
        return json.load(f)

t = TOOL / 'p5/data'
p5_chart = load(t/'fusion-chart.json')
p5r_chart = load(t/'roy-fusion-chart.json')

# Save P5 base chart
with open(OUR_WEB/'fusion-charts/p5-base-fusion-chart.json', 'w', encoding='utf-8') as f:
    json.dump(p5_chart, f, indent=2, ensure_ascii=False)
print('Created p5-base-fusion-chart.json with', len(p5_chart['races']), 'races')

# Save P5R chart (overwrite existing)
with open(OUR_WEB/'fusion-charts/p5-fusion-chart.json', 'w', encoding='utf-8') as f:
    json.dump(p5r_chart, f, indent=2, ensure_ascii=False)
print('Updated p5-fusion-chart.json with', len(p5r_chart['races']), 'races (P5R)')
