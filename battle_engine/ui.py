"""
ui.py – Persona 5-style terminal UI using colorama.
"""

import os
import time
from colorama import Fore, Back, Style, init

init(autoreset=True)

# ── Palette ──────────────────────────────────────────────────────────────────
RED    = Fore.RED    + Style.BRIGHT
YELLOW = Fore.YELLOW + Style.BRIGHT
WHITE  = Fore.WHITE  + Style.BRIGHT
CYAN   = Fore.CYAN   + Style.BRIGHT
GREEN  = Fore.GREEN  + Style.BRIGHT
MAGENTA= Fore.MAGENTA+ Style.BRIGHT
DIM    = Style.DIM
RESET  = Style.RESET_ALL

AFFINITY_COLOR = {
    "weak":   Fore.RED    + Style.BRIGHT,
    "resist": Fore.CYAN   + Style.BRIGHT,
    "null":   Fore.WHITE  + Style.BRIGHT,
    "drain":  Fore.GREEN  + Style.BRIGHT,
    "repel":  Fore.MAGENTA+ Style.BRIGHT,
    "normal": Fore.WHITE,
}

AFFINITY_LABEL = {
    "weak":   "WEAK!",
    "resist": "Resist",
    "null":   "Null",
    "drain":  "Drain",
    "repel":  "Repel",
    "normal": "",
}


def clear():
    os.system("cls" if os.name == "nt" else "clear")


def divider(char="─", width=60, color=RED):
    print(color + char * width + RESET)


def banner(text: str):
    divider()
    print(RED + f"  ★  {text.upper()}  ★" + RESET)
    divider()


def slow_print(text: str, delay: float = 0.02):
    for ch in text:
        print(ch, end="", flush=True)
        time.sleep(delay)
    print()


def hp_bar(current: int, maximum: int, width: int = 20) -> str:
    ratio = current / maximum if maximum else 0
    filled = int(ratio * width)
    bar_color = GREEN if ratio > 0.5 else (YELLOW if ratio > 0.25 else RED)
    bar = bar_color + "█" * filled + DIM + "░" * (width - filled) + RESET
    return f"[{bar}{RESET}] {current:>4}/{maximum}"


def sp_bar(current: int, maximum: int, width: int = 10) -> str:
    if maximum == 0:
        return ""
    ratio = current / maximum
    filled = int(ratio * width)
    bar = Fore.BLUE + Style.BRIGHT + "█" * filled + DIM + "░" * (width - filled) + RESET
    return f"[{bar}{RESET}] {current:>3}/{maximum}"


def print_combatant_status(c, indent: int = 0):
    pad = " " * indent
    name_str = (RED if c.is_enemy else CYAN) + f"{c.name}" + RESET
    persona_str = YELLOW + f"[{c.persona}]" + RESET if c.persona != c.name else ""
    print(f"{pad}{name_str} {persona_str}  Lv.{c.level}")
    print(f"{pad}  HP {hp_bar(c.hp, c.max_hp)}")
    if c.max_sp > 0:
        print(f"{pad}  SP {sp_bar(c.sp, c.max_sp)}")
    if c.buffs:
        buff_str = "  ".join(
            f"{GREEN}↑{k.upper()}{RESET}({v})" for k, v in c.buffs.items()
        )
        print(f"{pad}  Buffs: {buff_str}")


def print_battle_status(player, enemies: list):
    divider("═")
    print_combatant_status(player)
    divider("─", 40, Fore.WHITE)
    for i, e in enumerate(enemies):
        status = (RED + " [DOWN]" + RESET) if e.is_downed else ""
        print(f"  {WHITE}{i+1}.{RESET} ", end="")
        print_combatant_status(e, indent=0)
        if e.is_downed:
            print(f"      {RED}▼ DOWNED{RESET}")
    divider("═")


def print_hit_result(affinity: str, damage: int, target_name: str):
    color = AFFINITY_COLOR.get(affinity, Fore.WHITE)
    label = AFFINITY_LABEL.get(affinity, "")

    if affinity == "weak":
        slow_print(f"  {color}▶▶ {label}  {target_name} takes {damage} dmg!{RESET}", 0.015)
    elif affinity in ("null", "drain", "repel"):
        print(f"  {color}▶ {label}!  {target_name}{RESET}")
    elif affinity == "resist":
        print(f"  {color}▶ {label}  {target_name} takes {damage} dmg.{RESET}")
    else:
        print(f"  {Fore.WHITE}▶ {target_name} takes {damage} dmg.{RESET}")


def print_one_more():
    slow_print(f"\n  {RED}★ ONE MORE! ★{RESET}\n", 0.03)


def print_all_out_prompt():
    divider("*", 60, RED)
    slow_print(f"  {RED}ALL ENEMIES DOWNED!  ★ ALL-OUT ATTACK? ★{RESET}", 0.02)
    divider("*", 60, RED)


def print_all_out_attack(player_name: str):
    slow_print(f"\n  {RED}★★★  {player_name.upper()} GOES ALL OUT!  ★★★{RESET}\n", 0.02)


def print_skill_menu(skills: list[str], skill_db, sp: int):
    print(f"\n  {CYAN}── SKILLS ──{RESET}")
    for i, name in enumerate(skills):
        sk = skill_db(name)
        cost_str = f"SP:{sk['cost']}" if not sk.get("hp_cost") else f"HP:{sk['cost']}%"
        affordable = sp >= sk["cost"] or sk.get("hp_cost")
        color = WHITE if affordable else DIM
        print(f"  {color}{i+1}. {name:<20} {sk['element']:<10} {cost_str}{RESET}")


def print_action_menu():
    print(f"\n  {YELLOW}┌─ ACTION ──────────────────┐{RESET}")
    print(f"  {YELLOW}│{RESET}  1. Attack (Melee)         {YELLOW}│{RESET}")
    print(f"  {YELLOW}│{RESET}  2. Skill                  {YELLOW}│{RESET}")
    print(f"  {YELLOW}│{RESET}  3. Guard                  {YELLOW}│{RESET}")
    print(f"  {YELLOW}│{RESET}  4. Analyze                {YELLOW}│{RESET}")
    print(f"  {YELLOW}└───────────────────────────┘{RESET}")


def print_analyze(enemy):
    divider("─", 50, CYAN)
    print(f"  {CYAN}ANALYZE: {enemy.name} [{enemy.persona}]{RESET}  Lv.{enemy.level}")
    print(f"  STR:{enemy.strength} MAG:{enemy.magic} END:{enemy.endurance} "
          f"AGI:{enemy.agility} LCK:{enemy.luck}")
    print(f"  {'ELEM':<10}", end="")
    for el, aff in enemy.resists.items():
        color = AFFINITY_COLOR.get(aff, Fore.WHITE)
        label = {"normal": "-", "weak": "Wk", "resist": "Rs",
                 "null": "Nu", "drain": "Dr", "repel": "Rp"}.get(aff, "?")
        print(f"  {color}{el[:2]}:{label}{RESET}", end="")
    print()
    divider("─", 50, CYAN)


def prompt_choice(prompt: str, valid: range | list) -> int:
    while True:
        try:
            val = int(input(f"  {YELLOW}▶ {prompt}{RESET} "))
            if val in valid:
                return val
            print(f"  {RED}Invalid choice.{RESET}")
        except (ValueError, EOFError):
            print(f"  {RED}Enter a number.{RESET}")


def print_victory(exp: int):
    banner("VICTORY!")
    print(f"  {YELLOW}EXP gained: {exp}{RESET}\n")


def print_game_over():
    banner("GAME OVER")
    slow_print(f"  {RED}Your Persona has been defeated...{RESET}\n", 0.03)
