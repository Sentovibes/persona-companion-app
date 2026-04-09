"""
data_loader.py – Loads Persona / Enemy / Skill data from the app's JSON format.
Resists string index map (10 chars):
  0=Phys  1=Fire  2=Ice  3=Elec  4=Wind  5=Psy  6=Nuke  7=Bless  8=Curse  9=Almighty
Affinity codes: '-'=Normal  'w'=Weak  's'=Resist  'd'=Drain  'r'=Repel  'x'=Null
"""

import json
from pathlib import Path
from typing import Optional

ELEMENT_ORDER = ["Phys", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse", "Almighty"]
AFFINITY_MAP  = {"-": "normal", "w": "weak", "s": "resist", "d": "drain", "r": "repel", "x": "null"}

# ---------------------------------------------------------------------------
# Skill catalogue (built-in minimal set + loaded from JSON if available)
# ---------------------------------------------------------------------------
BUILTIN_SKILLS: dict[str, dict] = {
    # name -> {element, power, cost, target, description}
    "Agi":        {"element": "Fire",  "power": 50,  "cost": 4,  "target": "single", "desc": "Light Fire dmg"},
    "Maragi":     {"element": "Fire",  "power": 50,  "cost": 10, "target": "all",    "desc": "Light Fire dmg (all)"},
    "Agidyne":    {"element": "Fire",  "power": 110, "cost": 12, "target": "single", "desc": "Heavy Fire dmg"},
    "Maragidyne": {"element": "Fire",  "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Fire dmg (all)"},
    "Bufu":       {"element": "Ice",   "power": 50,  "cost": 4,  "target": "single", "desc": "Light Ice dmg"},
    "Mabufu":     {"element": "Ice",   "power": 50,  "cost": 10, "target": "all",    "desc": "Light Ice dmg (all)"},
    "Bufudyne":   {"element": "Ice",   "power": 110, "cost": 12, "target": "single", "desc": "Heavy Ice dmg"},
    "Mabufudyne": {"element": "Ice",   "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Ice dmg (all)"},
    "Zio":        {"element": "Elec",  "power": 50,  "cost": 4,  "target": "single", "desc": "Light Elec dmg"},
    "Mazio":      {"element": "Elec",  "power": 50,  "cost": 10, "target": "all",    "desc": "Light Elec dmg (all)"},
    "Ziodyne":    {"element": "Elec",  "power": 110, "cost": 12, "target": "single", "desc": "Heavy Elec dmg"},
    "Maziodyne":  {"element": "Elec",  "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Elec dmg (all)"},
    "Garu":       {"element": "Wind",  "power": 50,  "cost": 4,  "target": "single", "desc": "Light Wind dmg"},
    "Magaru":     {"element": "Wind",  "power": 50,  "cost": 10, "target": "all",    "desc": "Light Wind dmg (all)"},
    "Garudyne":   {"element": "Wind",  "power": 110, "cost": 12, "target": "single", "desc": "Heavy Wind dmg"},
    "Magarudyne": {"element": "Wind",  "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Wind dmg (all)"},
    "Psiodyne":   {"element": "Psy",   "power": 110, "cost": 12, "target": "single", "desc": "Heavy Psy dmg"},
    "Mapsiodyne": {"element": "Psy",   "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Psy dmg (all)"},
    "Freidyne":   {"element": "Nuke",  "power": 110, "cost": 12, "target": "single", "desc": "Heavy Nuke dmg"},
    "Mafreidyne": {"element": "Nuke",  "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Nuke dmg (all)"},
    "Kouga":      {"element": "Bless", "power": 50,  "cost": 4,  "target": "single", "desc": "Light Bless dmg"},
    "Makougaon":  {"element": "Bless", "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Bless dmg (all)"},
    "Eiga":       {"element": "Curse", "power": 50,  "cost": 4,  "target": "single", "desc": "Light Curse dmg"},
    "Maeigaon":   {"element": "Curse", "power": 110, "cost": 22, "target": "all",    "desc": "Heavy Curse dmg (all)"},
    "Megidolaon": {"element": "Almighty","power":150,"cost": 30, "target": "all",    "desc": "Severe Almighty dmg"},
    "Lunge":      {"element": "Phys",  "power": 80,  "cost": 10, "target": "single", "desc": "Medium Phys dmg (HP cost)", "hp_cost": True},
    "Deathbound": {"element": "Phys",  "power": 100, "cost": 18, "target": "all",    "desc": "Heavy Phys dmg (all)", "hp_cost": True},
    "Dia":        {"element": "Heal",  "power": 40,  "cost": 3,  "target": "self",   "desc": "Restore slight HP"},
    "Diarama":    {"element": "Heal",  "power": 80,  "cost": 6,  "target": "self",   "desc": "Restore moderate HP"},
    "Mediarama":  {"element": "Heal",  "power": 80,  "cost": 12, "target": "ally",   "desc": "Restore moderate HP (all)"},
    "Tarukaja":   {"element": "Buff",  "power": 0,   "cost": 6,  "target": "self",   "desc": "Raise ATK 1 turn"},
    "Rakukaja":   {"element": "Buff",  "power": 0,   "cost": 6,  "target": "self",   "desc": "Raise DEF 1 turn"},
    "Sukukaja":   {"element": "Buff",  "power": 0,   "cost": 6,  "target": "self",   "desc": "Raise AGI 1 turn"},
}


def parse_resists(resists_str: str) -> dict[str, str]:
    """Convert a 10-char resists string to {element: affinity} dict."""
    result = {}
    for i, ch in enumerate(resists_str[:10]):
        element = ELEMENT_ORDER[i]
        result[element] = AFFINITY_MAP.get(ch, "normal")
    return result


def load_enemies(json_path: str | Path) -> list[dict]:
    """Load enemy list from the app's enemies JSON (array format)."""
    with open(json_path, encoding="utf-8") as f:
        raw = json.load(f)
    enemies = []
    for e in raw:
        enemies.append({
            "name":       e["name"],
            "persona":    e.get("persona_name", ""),
            "arcana":     e.get("arcana", ""),
            "level":      e.get("level", 1),
            "hp":         e.get("hp", 100),
            "sp":         e.get("sp", 0),
            "stats":      e.get("stats", {}),
            "resists":    parse_resists(e.get("resists", "----------")),
            "skills":     e.get("skills", []),
            "exp":        e.get("exp", 0),
            "isBoss":     e.get("isBoss", False),
            "isMiniBoss": e.get("isMiniBoss", False),
        })
    return enemies


def load_personas(json_path: str | Path) -> dict[str, dict]:
    """Load persona dict from the app's personas JSON (object format)."""
    with open(json_path, encoding="utf-8") as f:
        raw = json.load(f)
    personas = {}
    for name, p in raw.items():
        stats = p.get("stats", [0, 0, 0, 0, 0])
        personas[name] = {
            "name":    name,
            "arcana":  p.get("arcana", ""),
            "level":   p.get("level", 1),
            "stats": {
                "strength":  stats[0],
                "magic":     stats[1],
                "endurance": stats[2],
                "agility":   stats[3],
                "luck":      stats[4],
            },
            "skills":  list(p.get("skills", {}).keys()),
            "resists": parse_resists(p.get("resists", "----------")),
        }
    return personas


def get_skill(name: str, extra_skills: Optional[dict] = None) -> dict:
    """Look up a skill by name; falls back to a generic Phys attack."""
    if extra_skills and name in extra_skills:
        return extra_skills[name]
    if name in BUILTIN_SKILLS:
        return BUILTIN_SKILLS[name]
    # Unknown skill – treat as light Phys
    return {"element": "Phys", "power": 50, "cost": 0, "target": "single", "desc": name}
