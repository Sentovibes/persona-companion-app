"""
battle.py – Core turn-based battle loop with One More / All-Out Attack system.
"""

import random
from typing import Optional

from .combatant import Combatant
from .data_loader import get_skill, ELEMENT_ORDER
from . import ui


# ── Damage formula ────────────────────────────────────────────────────────────

def calc_damage(attacker: Combatant, skill: dict, target: Combatant) -> tuple[int, str]:
    """
    Returns (damage, affinity_str).
    Affinity: normal / weak / resist / null / drain / repel
    """
    element = skill["element"]
    power   = skill.get("power", 50)

    if element in ("Heal", "Buff"):
        return 0, "normal"

    affinity = target.get_affinity(element)
    if affinity in ("null", "drain", "repel"):
        return 0, affinity

    # Stat used: magic for elemental, strength for phys
    stat = attacker.magic if element not in ("Phys",) else attacker.strength
    base = (stat * power) / 50

    # Affinity modifier
    mod = {"weak": 1.5, "resist": 0.5, "normal": 1.0}.get(affinity, 1.0)

    # Attacker buff
    base *= attacker.atk_multiplier()
    # Defender buff
    base /= target.def_multiplier()

    # Small random variance ±10%
    variance = random.uniform(0.9, 1.1)
    damage = max(1, int(base * mod * variance))

    return damage, affinity


def apply_skill(attacker: Combatant, skill_name: str, targets: list[Combatant],
                extra_skills=None) -> list[tuple[Combatant, int, str]]:
    """
    Execute a skill against one or more targets.
    Returns list of (target, damage, affinity).
    """
    skill = get_skill(skill_name, extra_skills)
    results = []

    if skill["element"] == "Heal":
        healed = attacker.heal(skill["power"])
        ui.slow_print(f"  {ui.GREEN}{attacker.name} restores {healed} HP!{ui.RESET}", 0.01)
        return results

    if skill["element"] == "Buff":
        buff_map = {"Tarukaja": "atk", "Rakukaja": "def", "Sukukaja": "agi"}
        buff_key = buff_map.get(skill_name, "atk")
        attacker.buffs[buff_key] = 3
        ui.slow_print(f"  {ui.GREEN}{attacker.name}'s {buff_key.upper()} raised!{ui.RESET}", 0.01)
        return results

    for target in targets:
        if not target.alive:
            continue
        damage, affinity = calc_damage(attacker, skill, target)

        if affinity == "drain":
            attacker.heal(damage)
            ui.slow_print(f"  {ui.GREEN}{target.name} drains the attack! {attacker.name} healed!{ui.RESET}", 0.01)
        elif affinity == "repel":
            attacker.take_damage(damage)
            ui.slow_print(f"  {ui.MAGENTA}{target.name} repels the attack! {attacker.name} takes {damage} dmg!{ui.RESET}", 0.01)
        else:
            actual = target.take_damage(damage)
            ui.print_hit_result(affinity, actual, target.name)

        results.append((target, damage, affinity))

    return results


# ── One More logic ────────────────────────────────────────────────────────────

def check_one_more(results: list[tuple[Combatant, int, str]]) -> bool:
    """True if any living enemy was hit on a weakness."""
    return any(aff == "weak" for _, _, aff in results)


def all_enemies_downed(enemies: list[Combatant]) -> bool:
    return all(e.is_downed or not e.alive for e in enemies)


# ── Enemy AI ──────────────────────────────────────────────────────────────────

def enemy_turn(enemy: Combatant, player: Combatant, extra_skills=None):
    if not enemy.alive or enemy.is_downed:
        return

    ui.slow_print(f"\n  {ui.RED}▶ {enemy.name} acts!{ui.RESET}", 0.015)

    # Pick a random skill or melee
    if enemy.skills:
        skill_name = random.choice(enemy.skills)
        skill = get_skill(skill_name, extra_skills)
        # Only use if enemy has SP (or it's a phys skill)
        if skill.get("hp_cost") or enemy.sp >= skill["cost"]:
            if not skill.get("hp_cost"):
                enemy.sp -= skill["cost"]
            ui.slow_print(f"  {ui.RED}{enemy.name} uses {skill_name}!{ui.RESET}", 0.015)
            apply_skill(enemy, skill_name, [player], extra_skills)
            return

    # Fallback: melee
    melee_dmg = max(1, int(enemy.strength * random.uniform(0.8, 1.2)))
    actual = player.take_damage(melee_dmg)
    ui.slow_print(f"  {ui.RED}{enemy.name} attacks! {player.name} takes {actual} dmg.{ui.RESET}", 0.015)


# ── Player turn ───────────────────────────────────────────────────────────────

def player_turn(player: Combatant, enemies: list[Combatant],
                extra_skills=None) -> bool:
    """
    Handle one player action. Returns True if player gets a One More.
    """
    alive_enemies = [e for e in enemies if e.alive]

    ui.print_battle_status(player, enemies)
    ui.print_action_menu()
    action = ui.prompt_choice("Choose action:", range(1, 5))

    # ── 1. Melee ──────────────────────────────────────────────────────────────
    if action == 1:
        target = _pick_target(alive_enemies)
        melee_dmg = max(1, int(player.strength * random.uniform(0.9, 1.1)))
        affinity  = target.get_affinity("Phys")
        if affinity in ("null", "drain", "repel"):
            ui.print_hit_result(affinity, 0, target.name)
            return False
        mod = 1.5 if affinity == "weak" else (0.5 if affinity == "resist" else 1.0)
        actual = target.take_damage(int(melee_dmg * mod))
        ui.print_hit_result(affinity, actual, target.name)
        if affinity == "weak" and target.alive:
            target.is_downed = True
        return affinity == "weak"

    # ── 2. Skill ──────────────────────────────────────────────────────────────
    elif action == 2:
        ui.print_skill_menu(player.skills, lambda n: get_skill(n, extra_skills), player.sp)
        sk_idx = ui.prompt_choice("Choose skill (0=back):", range(0, len(player.skills) + 1))
        if sk_idx == 0:
            return player_turn(player, enemies, extra_skills)  # re-prompt

        skill_name = player.skills[sk_idx - 1]
        skill = get_skill(skill_name, extra_skills)

        # SP / HP cost check
        if skill.get("hp_cost"):
            hp_cost = max(1, int(player.max_hp * skill["cost"] / 100))
            player.take_damage(hp_cost)
        else:
            if not player.use_sp(skill["cost"]):
                ui.slow_print(f"  {ui.RED}Not enough SP!{ui.RESET}", 0.01)
                return player_turn(player, enemies, extra_skills)

        ui.slow_print(f"\n  {ui.CYAN}{player.name} uses {skill_name}!{ui.RESET}", 0.015)

        # Target selection
        if skill["target"] == "all":
            targets = alive_enemies
        elif skill["target"] == "self":
            targets = [player]
        else:
            targets = [_pick_target(alive_enemies)]

        results = apply_skill(player, skill_name, targets, extra_skills)

        # Mark downed on weakness
        one_more = False
        for target, _, aff in results:
            if aff == "weak" and target.alive:
                target.is_downed = True
                one_more = True

        return one_more

    # ── 3. Guard ──────────────────────────────────────────────────────────────
    elif action == 3:
        player.buffs["def"] = 1
        ui.slow_print(f"  {ui.GREEN}{player.name} guards! DEF raised this turn.{ui.RESET}", 0.015)
        return False

    # ── 4. Analyze ────────────────────────────────────────────────────────────
    elif action == 4:
        target = _pick_target(alive_enemies)
        ui.print_analyze(target)
        return player_turn(player, enemies, extra_skills)  # analyze doesn't cost a turn

    return False


def _pick_target(alive_enemies: list[Combatant]) -> Combatant:
    if len(alive_enemies) == 1:
        return alive_enemies[0]
    print(f"\n  {ui.CYAN}Target:{ui.RESET}")
    for i, e in enumerate(alive_enemies):
        print(f"    {i+1}. {e.name} (HP {e.hp}/{e.max_hp})")
    idx = ui.prompt_choice("Choose target:", range(1, len(alive_enemies) + 1))
    return alive_enemies[idx - 1]


# ── All-Out Attack ────────────────────────────────────────────────────────────

def all_out_attack(player: Combatant, enemies: list[Combatant]):
    ui.print_all_out_attack(player.name)
    for e in enemies:
        if e.alive:
            dmg = max(1, int((player.strength + player.magic) * random.uniform(1.5, 2.5)))
            actual = e.take_damage(dmg)
            ui.slow_print(f"  {ui.RED}{e.name} takes {actual} dmg!{ui.RESET}", 0.01)


# ── Main battle loop ──────────────────────────────────────────────────────────

def run_battle(player: Combatant, enemies: list[Combatant],
               extra_skills=None) -> bool:
    """
    Run a full battle. Returns True if player wins, False on game over.
    """
    ui.banner(f"BATTLE START")
    total_exp = sum(e.exp if hasattr(e, "exp") else 0 for e in enemies)

    turn = 1
    while True:
        ui.slow_print(f"\n  {ui.YELLOW}── Turn {turn} ──{ui.RESET}", 0.01)

        # ── Player phase ──────────────────────────────────────────────────────
        extra_turns = 1  # player always gets at least 1 action
        while extra_turns > 0:
            extra_turns -= 1

            if not player.alive:
                ui.print_game_over()
                return False

            alive = [e for e in enemies if e.alive]
            if not alive:
                break

            got_one_more = player_turn(player, enemies, extra_skills)

            # Check kills
            for e in enemies:
                if not e.alive:
                    e.is_downed = False  # dead, not just downed

            alive = [e for e in enemies if e.alive]
            if not alive:
                break

            # All-Out Attack check
            if all_enemies_downed(alive):
                ui.print_all_out_prompt()
                choice = ui.prompt_choice("1=All-Out Attack  2=Keep going:", [1, 2])
                if choice == 1:
                    all_out_attack(player, alive)
                    # Reset downed after AOA
                    for e in alive:
                        e.is_downed = False
                    alive = [e for e in enemies if e.alive]
                    if not alive:
                        break
                else:
                    # Reset downed flags so enemies can act
                    for e in alive:
                        e.is_downed = False

            elif got_one_more:
                ui.print_one_more()
                extra_turns += 1  # grant extra action

        # ── Victory check ─────────────────────────────────────────────────────
        if not any(e.alive for e in enemies):
            ui.print_victory(total_exp)
            return True

        # ── Enemy phase ───────────────────────────────────────────────────────
        for e in enemies:
            if e.alive and not e.is_downed:
                enemy_turn(e, player, extra_skills)
                if not player.alive:
                    ui.print_game_over()
                    return False

        # Reset downed flags at end of round
        for e in enemies:
            if e.alive:
                e.is_downed = False

        # Tick buffs
        player.tick_buffs()
        for e in enemies:
            e.tick_buffs()

        turn += 1
