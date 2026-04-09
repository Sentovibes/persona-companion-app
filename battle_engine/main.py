"""
main.py – Entry point for the Persona Battle Engine CLI.

Usage:
    python -m battle_engine.main
    python -m battle_engine.main --enemies 3
    python -m battle_engine.main --persona "Agathion" --enemy "Beguiling Girl"

Data paths default to the app's asset folder (relative to project root).
Override with --persona-json and --enemy-json flags.
"""

import argparse
import random
import sys
from pathlib import Path

# Allow running as `python battle_engine/main.py` directly
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from battle_engine.data_loader import load_enemies, load_personas
from battle_engine.combatant   import enemy_to_combatant, persona_to_combatant
from battle_engine              import ui
from battle_engine.battle       import run_battle

# ── Default data paths (relative to project root) ────────────────────────────
DEFAULT_PERSONA_JSON = Path("app/src/main/assets/data/persona5/personas.json")
DEFAULT_ENEMY_JSON   = Path("app/src/main/assets/data/enemies/p5r_enemies.json")


def pick_persona(personas: dict, name: str | None) -> dict:
    if name and name in personas:
        return personas[name]
    # Interactive picker
    names = sorted(personas.keys())
    print(f"\n  {ui.CYAN}Available Personas (showing first 20):{ui.RESET}")
    for i, n in enumerate(names[:20]):
        print(f"    {i+1:>2}. {n}")
    print(f"    (or type a name)")
    raw = input(f"  {ui.YELLOW}▶ Choose Persona: {ui.RESET}").strip()
    if raw.isdigit():
        idx = int(raw) - 1
        if 0 <= idx < len(names):
            return personas[names[idx]]
    if raw in personas:
        return personas[raw]
    # Fuzzy fallback
    matches = [n for n in names if raw.lower() in n.lower()]
    if matches:
        return personas[matches[0]]
    print(f"  {ui.RED}Not found, using random.{ui.RESET}")
    return random.choice(list(personas.values()))


def pick_enemies(enemies: list[dict], name: str | None, count: int) -> list[dict]:
    if name:
        matches = [e for e in enemies if e["name"].lower() == name.lower()]
        if matches:
            return matches[:count]
    # Random non-boss enemies
    pool = [e for e in enemies if not e.get("isBoss") and not e.get("isMiniBoss")]
    return random.sample(pool, min(count, len(pool)))


def main():
    parser = argparse.ArgumentParser(description="Persona Battle Engine")
    parser.add_argument("--persona",      type=str, help="Persona name to use")
    parser.add_argument("--enemy",        type=str, help="Specific enemy name")
    parser.add_argument("--enemies",      type=int, default=1, choices=[1, 2, 3],
                        help="Number of enemies (1-3)")
    parser.add_argument("--persona-json", type=Path, default=DEFAULT_PERSONA_JSON)
    parser.add_argument("--enemy-json",   type=Path, default=DEFAULT_ENEMY_JSON)
    parser.add_argument("--player-name",  type=str, default="Joker")
    args = parser.parse_args()

    # ── Load data ─────────────────────────────────────────────────────────────
    try:
        personas = load_personas(args.persona_json)
    except FileNotFoundError:
        ui.slow_print(f"  {ui.RED}Persona JSON not found: {args.persona_json}{ui.RESET}")
        ui.slow_print(f"  {ui.YELLOW}Using built-in demo persona.{ui.RESET}")
        personas = _demo_personas()

    try:
        enemies_db = load_enemies(args.enemy_json)
    except FileNotFoundError:
        ui.slow_print(f"  {ui.RED}Enemy JSON not found: {args.enemy_json}{ui.RESET}")
        ui.slow_print(f"  {ui.YELLOW}Using built-in demo enemies.{ui.RESET}")
        enemies_db = _demo_enemies()

    # ── Build combatants ──────────────────────────────────────────────────────
    ui.clear()
    ui.banner("PERSONA BATTLE ENGINE")

    persona_data  = pick_persona(personas, args.persona)
    player        = persona_to_combatant(args.player_name, persona_data)

    enemy_dicts   = pick_enemies(enemies_db, args.enemy, args.enemies)
    enemy_combatants = [enemy_to_combatant(e) for e in enemy_dicts]

    ui.slow_print(f"\n  {ui.CYAN}Persona: {persona_data['name']}  "
                  f"(Lv.{persona_data['level']}){ui.RESET}", 0.02)
    ui.slow_print(f"  {ui.RED}Enemies: {', '.join(e['name'] for e in enemy_dicts)}{ui.RESET}\n", 0.02)

    input(f"  {ui.YELLOW}▶ Press Enter to start...{ui.RESET}")

    # ── Run battle ────────────────────────────────────────────────────────────
    result = run_battle(player, enemy_combatants)

    if result:
        ui.slow_print(f"\n  {ui.GREEN}You won! Well done, Phantom Thief.{ui.RESET}\n", 0.02)
    else:
        ui.slow_print(f"\n  {ui.RED}Defeated... try again.{ui.RESET}\n", 0.02)


# ── Built-in demo data (no JSON needed) ──────────────────────────────────────

def _demo_personas() -> dict:
    return {
        "Arsene": {
            "name": "Arsene", "arcana": "Fool", "level": 1,
            "stats": {"strength": 3, "magic": 5, "endurance": 3, "agility": 4, "luck": 3},
            "skills": ["Agi", "Zio", "Tarukaja", "Lunge"],
            "resists": {"Phys": "normal", "Fire": "normal", "Ice": "normal",
                        "Elec": "normal", "Wind": "normal", "Psy": "normal",
                        "Nuke": "normal", "Bless": "weak", "Curse": "resist",
                        "Almighty": "normal"},
        }
    }


def _demo_enemies() -> list[dict]:
    return [
        {
            "name": "Shadow", "persona": "Shadow", "arcana": "Fool",
            "level": 5, "hp": 120, "sp": 20,
            "stats": {"strength": 6, "magic": 6, "endurance": 5, "agility": 5, "luck": 4},
            "resists": {"Phys": "normal", "Fire": "weak", "Ice": "normal",
                        "Elec": "normal", "Wind": "normal", "Psy": "normal",
                        "Nuke": "normal", "Bless": "normal", "Curse": "normal",
                        "Almighty": "normal"},
            "skills": ["Agi"], "exp": 50, "isBoss": False, "isMiniBoss": False,
        }
    ]


if __name__ == "__main__":
    main()
