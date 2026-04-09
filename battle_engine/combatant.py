"""
combatant.py – Combatant dataclass used for both player Personas and enemies.
"""

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class Combatant:
    name: str
    persona: str                        # persona / shadow name shown in battle
    level: int
    max_hp: int
    max_sp: int
    hp: int
    sp: int
    strength: int
    magic: int
    endurance: int
    agility: int
    luck: int
    resists: dict[str, str]             # {element: affinity}
    skills: list[str]
    is_enemy: bool = False
    is_boss: bool = False
    is_downed: bool = False             # knocked down (used for One More tracking)
    buffs: dict[str, int] = field(default_factory=dict)   # {stat: turns_remaining}

    # ------------------------------------------------------------------ #
    @property
    def alive(self) -> bool:
        return self.hp > 0

    def take_damage(self, amount: int) -> int:
        """Apply damage, clamp to 0. Returns actual damage dealt."""
        actual = min(self.hp, max(0, amount))
        self.hp -= actual
        return actual

    def heal(self, amount: int) -> int:
        """Restore HP, clamp to max. Returns actual healed."""
        actual = min(self.max_hp - self.hp, max(0, amount))
        self.hp += actual
        return actual

    def use_sp(self, cost: int) -> bool:
        """Deduct SP cost. Returns False if not enough SP."""
        if self.sp < cost:
            return False
        self.sp -= cost
        return True

    def get_affinity(self, element: str) -> str:
        return self.resists.get(element, "normal")

    def tick_buffs(self):
        """Decrement buff timers at end of turn."""
        expired = [k for k, v in self.buffs.items() if v <= 1]
        for k in expired:
            del self.buffs[k]
        for k in self.buffs:
            self.buffs[k] -= 1

    def atk_multiplier(self) -> float:
        return 1.5 if "atk" in self.buffs else 1.0

    def def_multiplier(self) -> float:
        return 0.75 if "def" in self.buffs else 1.0


# ------------------------------------------------------------------ #
# Factory helpers
# ------------------------------------------------------------------ #

def enemy_to_combatant(enemy: dict) -> "Combatant":
    stats = enemy.get("stats", {})
    hp = enemy["hp"]
    sp = enemy.get("sp", 0)
    return Combatant(
        name=enemy["name"],
        persona=enemy.get("persona", enemy["name"]),
        level=enemy["level"],
        max_hp=hp, hp=hp,
        max_sp=sp, sp=sp,
        strength=stats.get("strength", 5),
        magic=stats.get("magic", 5),
        endurance=stats.get("endurance", 5),
        agility=stats.get("agility", 5),
        luck=stats.get("luck", 5),
        resists=enemy["resists"],
        skills=enemy["skills"],
        is_enemy=True,
        is_boss=enemy.get("isBoss", False) or enemy.get("isMiniBoss", False),
    )


def persona_to_combatant(
    player_name: str,
    persona: dict,
    hp: Optional[int] = None,
    sp: Optional[int] = None,
) -> "Combatant":
    stats = persona["stats"]
    end = stats.get("endurance", 10)
    mag = stats.get("magic", 10)
    base_hp = hp if hp is not None else 80 + end * 8
    base_sp = sp if sp is not None else 40 + mag * 4
    return Combatant(
        name=player_name,
        persona=persona["name"],
        level=persona["level"],
        max_hp=base_hp, hp=base_hp,
        max_sp=base_sp, sp=base_sp,
        strength=stats.get("strength", 10),
        magic=stats.get("magic", 10),
        endurance=end,
        agility=stats.get("agility", 10),
        luck=stats.get("luck", 10),
        resists=persona["resists"],
        skills=persona["skills"],
        is_enemy=False,
    )
