const fs = require("fs");
const p5rEnemies = require("../web/data/enemies/p5r_enemies.json");

const PERSONALITY_MAP = {
  // Timid
  "Pixie": "Timid", "Agathion": "Timid", "Mandrake": "Timid", "Silky": "Timid", "Saki Mitama": "Timid",
  "Hua Po": "Timid", "Apsaras": "Timid", "Kodama": "Timid", "Koropokguru": "Timid", "Kushi Mitama": "Timid",
  "Angel": "Timid", "Clotho": "Timid", "Sarasvati": "Timid", "Parvati": "Timid", "Hariti": "Timid",
  "Titania": "Timid", "Scathach": "Timid", "Cybele": "Timid", "Koh-i-Noor": "Timid", "Queen Mab": "Timid",
  "Fortuna": "Timid", "Kikuri-Hime": "Timid", "Lachesis": "Timid", "Atropos": "Timid", "Norn": "Timid",
  "Bicorn": "Timid", "Makami": "Timid", "Choronzon": "Timid", "Mithra": "Timid", "Kushinada": "Timid",
  "Leanan Sidhe": "Timid", "High Pixie": "Timid", "Daisoujou": "Timid", "Succubus": "Timid",

  // Upbeat
  "Jack-o'-Lantern": "Upbeat", "Mokoi": "Upbeat", "Suzaku": "Upbeat", "Sudama": "Upbeat",
  "Thunderbird": "Upbeat", "Byakhee": "Upbeat", "Lilim": "Upbeat", "Dionysus": "Upbeat",
  "Mara": "Upbeat", "Baal": "Upbeat", "Black Frost": "Upbeat", "Cu Chulainn": "Upbeat",
  "Thor": "Upbeat", "Shiva": "Upbeat", "Orlov": "Upbeat", "Emperor's Amulet": "Upbeat",
  "Decarabia": "Upbeat", "Matador": "Upbeat", "Hell Biker": "Upbeat", "Baphomet": "Upbeat",
  "Barong": "Upbeat", "Garuda": "Upbeat", "Hanuman": "Upbeat", "Yatagarasu": "Upbeat",
  "Quetzalcoatl": "Upbeat", "Seiryu": "Upbeat", "Genbu": "Upbeat", "Byakko": "Upbeat",
  "Nigi Mitama": "Upbeat", "Ara Mitama": "Upbeat", "Koppa Tengu": "Upbeat", "Sandman": "Upbeat",
  "Hope Diamond": "Upbeat", "Crystal Skull": "Upbeat", "Macabre": "Upbeat", "Kaiwan": "Upbeat",

  // Gloomy
  "Incubus": "Gloomy", "Slime": "Gloomy", "Berith": "Gloomy", "Onmoraki": "Gloomy",
  "Inugami": "Gloomy", "Nekomata": "Gloomy", "Lamia": "Gloomy", "Pisaca": "Gloomy",
  "Nue": "Gloomy", "Legion": "Gloomy", "Hecatoncheires": "Gloomy", "Anubis": "Gloomy",
  "Pazuzu": "Gloomy", "Chernobog": "Gloomy", "Thanatos": "Gloomy", "Nebiros": "Gloomy",
  "Belial": "Gloomy", "Beelzebub": "Gloomy", "Regent": "Gloomy", "Stone of Scone": "Gloomy",
  "Black Ooze": "Gloomy", "Mothman": "Gloomy", "Anzu": "Gloomy",
  "Loa": "Gloomy", "Skadi": "Gloomy", "Mada": "Gloomy", "Abaddon": "Gloomy",
  "Belphegor": "Gloomy", "Bishamonten": "Gloomy", "Kumbhanda": "Gloomy", "Take-Minakata": "Gloomy",

  // Irritable
  "Kelpie": "Irritable", "Archangel": "Irritable", "Eligor": "Irritable", "Orobas": "Irritable",
  "Orthrus": "Irritable", "Rakshasa": "Irritable", "Oni": "Irritable", "Kurama Tengu": "Irritable",
  "Kin-Ki": "Irritable", "Shiki-Ouji": "Irritable", "Girimehkala": "Irritable", "Valkyrie": "Irritable",
  "Ose": "Irritable", "Oberon": "Irritable", "Cerberus": "Irritable", "King Frost": "Irritable",
  "Siegfried": "Irritable", "Surt": "Irritable", "Futsunushi": "Irritable", "Odin": "Irritable",
  "Asura": "Irritable", "Chi You": "Irritable", "Sui-Ki": "Irritable", "Fuu-Ki": "Irritable",
  "Zouchouten": "Irritable", "Koumokuten": "Irritable", "Jikokuten": "Irritable", "Yaksini": "Irritable",
  "Dakini": "Irritable", "Ganesha": "Irritable", "Throne": "Irritable", "Dominion": "Irritable",
  "Melchizedek": "Irritable", "Moloch": "Irritable", "Sandalphon": "Irritable", "Metatron": "Irritable",
  "Michael": "Irritable", "Gabriel": "Irritable", "Uriel": "Irritable", "Raphael": "Irritable"
};

const shadowsList = [];
for (const enemy of p5rEnemies) {
  if (enemy.isBoss) continue;
  const pName = enemy.persona_name || enemy.name;
  const personality = PERSONALITY_MAP[pName] || PERSONALITY_MAP[enemy.name] || (
    enemy.arcana === "Lovers" || enemy.arcana === "Priestess" || enemy.arcana === "Empress" ? "Timid" :
    enemy.arcana === "Magician" || enemy.arcana === "Chariot" || enemy.arcana === "Sun" ? "Upbeat" :
    enemy.arcana === "Death" || enemy.arcana === "Moon" || enemy.arcana === "Hanged Man" ? "Gloomy" :
    "Irritable"
  );

  shadowsList.push({
    name: enemy.name,
    persona_name: pName,
    arcana: enemy.arcana || "Unknown",
    level: enemy.level || 1,
    hp: enemy.hp || 0,
    weakness: enemy.resists ? enemy.resists : "",
    area: enemy.area || "Palace / Mementos",
    personality: personality
  });
}

shadowsList.sort((a,b) => a.level - b.level);

const data = {
  p5: {
    personality_matrix: {
      "Upbeat": {
        "likes": "Funny",
        "neutral": "Serious",
        "hates": "Vague",
        "color": "#FFB74D",
        "description": "High energy and cheerful. Loves clever, funny, and witty remarks. Dislikes indecisive or vague answers.",
        "best_type": "Funny / Joke",
        "ok_type": "Serious",
        "bad_type": "Vague / Ambiguous"
      },
      "Timid": {
        "likes": "Kind",
        "neutral": "Vague",
        "hates": "Funny",
        "color": "#81C784",
        "description": "Easily frightened and cautious. Responds best to kindness, empathy, and gentleness. Never joke or tease them.",
        "best_type": "Kind / Gentle",
        "ok_type": "Vague / Ambiguous",
        "bad_type": "Funny / Joke"
      },
      "Gloomy": {
        "likes": "Vague",
        "neutral": "Serious",
        "hates": "Kind",
        "color": "#64B5F6",
        "description": "Melancholy and cynical. Prefers mysterious, casual, or vague replies. Dislikes overly sweet sympathy.",
        "best_type": "Vague / Ambiguous",
        "ok_type": "Serious",
        "bad_type": "Kind / Gentle"
      },
      "Irritable": {
        "likes": "Serious",
        "neutral": "Vague",
        "hates": "Kind",
        "color": "#E57373",
        "description": "Aggressive and impatient. Demands direct, serious, and no-nonsense responses. Hates soft, timid excuses.",
        "best_type": "Serious / Direct",
        "ok_type": "Vague / Ambiguous",
        "bad_type": "Kind / Gentle"
      }
    },
    sun_confidant_perks: [
      { "rank": 1, "name": "Speechcraft", "effect": "Unlocks politician speeches for Charm stat boost on Sundays." },
      { "rank": 2, "name": "Diplomacy", "effect": "Occasionally enables demanding more money or rare items during hold-up negotiations." },
      { "rank": 3, "name": "Fundraising", "effect": "Enables repeatedly demanding money from downed Shadows during a single negotiation." },
      { "rank": 5, "name": "Manipulation", "effect": "Enemies will sometimes offer rare and high-value items during negotiations." },
      { "rank": 8, "name": "Mind Control", "effect": "Allows skipping negotiation entirely and recruiting downed Shadows with a single speech prompt." },
      { "rank": 10, "name": "Charismatic Speech", "effect": "Allows recruiting Shadows that are HIGHER level than Joker!" }
    ],
    mechanics: [
      { "title": "1. Red Aura / Disaster Shadows", "desc": "Disaster Shadows (glowing red aura) cannot be negotiated with during hold-ups. Defeating them causes a chain explosion that damages other enemies!" },
      { "title": "2. Chariot Rank 7 (Insta-Kill)", "desc": "Ryuji's Insta-Kill perk in Mementos/Palaces automatically defeats shadows 10+ levels below Joker and instantly recruits their Persona without any dialogue!" },
      { "title": "3. Already Owned Personas", "desc": "If you negotiate with a Persona already in your stock, they skip conversation immediately and grant bonus EXP to that Persona." }
    ],
    shadows: shadowsList
  },
  p3: {
    major_arcana: [
      { "num": "0", "name": "0. Fool", "effect": "Increases all EXP gained by +20% for the rest of Tartarus exploration." },
      { "num": "I", "name": "I. Magician", "effect": "Levels up a random skill on your equipped Persona to its next tier." },
      { "num": "II", "name": "II. Priestess", "effect": "Increases All-Out Attack damage dealt by all party members." },
      { "num": "III", "name": "III. Empress", "effect": "Increases status ailment recovery and evasion rate." },
      { "num": "IV", "name": "IV. Emperor", "effect": "Grants permanent stat increases to your equipped Persona." },
      { "num": "V", "name": "V. Hierophant", "effect": "Guarantees extra card pick opportunities in future Shuffle Times." },
      { "num": "VI", "name": "VI. Lovers", "effect": "Greatly increases item and treasure chest discovery rates." },
      { "num": "VII", "name": "VII. Chariot", "effect": "Permanently increases the Protagonist's Max HP by +10." },
      { "num": "VIII", "name": "VIII. Justice", "effect": "Enables picking 1 additional card in every Shuffle Time." },
      { "num": "IX", "name": "IX. Hermit", "effect": "Permanently increases the Protagonist's Max SP by +5." },
      { "num": "X", "name": "X. Fortune", "effect": "Increases Critical Hit rate for all party members." },
      { "num": "XI", "name": "XI. Strength", "effect": "Increases Physical damage dealt by the entire party." },
      { "num": "XII", "name": "XII. Hanged Man", "effect": "Increases defense and reduces physical damage taken." },
      { "num": "XIII", "name": "XIII. Death", "effect": "Risk / Reward card: Increases spawn chance of rare shadows & Reaper." },
      { "num": "XIV", "name": "XIV. Temperance", "effect": "Grants +50% EXP to benched / inactive party members." },
      { "num": "XV", "name": "XV. Devil", "effect": "Doubles all Yen / Money dropped from battles." },
      { "num": "XVI", "name": "XVI. Tower", "effect": "Fills the Theurgy gauge for all active party members." },
      { "num": "XVII", "name": "XVII. Star", "effect": "Increases Magic damage dealt by the entire party." },
      { "num": "XVIII", "name": "XVIII. Moon", "effect": "Fully restores HP and SP for all party members after battle." },
      { "num": "XIX", "name": "XIX. Sun", "effect": "Removes the Protagonist level cap on Velvet Room fusions for today!" },
      { "num": "XX", "name": "XX. Judgement", "effect": "Grants massive bonus EXP to the next fused Persona." },
      { "num": "XXI", "name": "XXI. Aeon", "effect": "Triggers Arcana Burst, unlocking permanent extra picks in Shuffle Time!" }
    ],
    minor_arcana: [
      { "suit": "Swords (⚔️)", "title": "Skill Cards & Weapons", "desc": "Grants physical/elemental/support skill cards or equipment upgrades." },
      { "suit": "Cups (🏆)", "title": "Recovery & Healing", "desc": "Restores a percentage of HP and SP to all party members immediately." },
      { "suit": "Coins (🪙)", "title": "Bonus Money / Yen", "desc": "Grants substantial amounts of bonus Yen scaled by dungeon floor rank." },
      { "suit": "Wands (🪄)", "title": "Bonus EXP Multiplier", "desc": "Increases battle EXP reward by up to +100%." }
    ],
    mechanics: [
      { "title": "How to Trigger Shuffle Time", "desc": "Shuffle Time is guaranteed when you finish a battle with an All-Out Attack, or when defeating the final enemy using the Protagonist's attack." },
      { "title": "Arcana Burst", "desc": "Collecting the required quota of Major Arcana cards in a single Tartarus exploration triggers an Arcana Burst, permanently granting +1 additional card pick for every future Shuffle Time until leaving Tartarus." },
      { "title": "Monad Doors & Passages", "desc": "Defeating Monad Shadows guarantees rare Major Arcana cards with potent exploration perks." }
    ]
  },
  p4: {
    sweep_guide: [
      { "title": "What is an All-Clear / Sweep Bonus?", "desc": "If you choose every single card shown in Shuffle Time (using cards with +1 / +2 draw perks to clear penalty cards), you trigger an All-Clear Sweep Bonus!" },
      { "title": "Sweep Bonus Rewards", "desc": "1. Guarantees a Shuffle Time after the very next battle.\n2. Gives +3 starting card picks on the next Shuffle Time.\n3. Converts all cards into higher ranks with positive effects!" },
      { "title": "How to Achieve Sweeps Reliably", "desc": "Always pick cards that grant extra draws (like Justice, Hierophant, or Devil) before picking penalty cards (like half money/no exp) so you can pick every card on screen." }
    ],
    major_arcana: [
      { "name": "Magician", "effect": "Levels up one random skill of the equipped Persona to its next tier." },
      { "name": "Emperor", "effect": "Levels up the equipped Persona by 1 level." },
      { "name": "Priestess", "effect": "Flips one face-down card face-up and grants +1 draw." },
      { "name": "Hierophant", "effect": "Grants +1 additional card draw in the current Shuffle Time." },
      { "name": "Lovers", "effect": "Allows picking 2 cards of your choice without penalty." },
      { "name": "Chariot", "effect": "Grants +1 Agility or Endurance to equipped Persona." },
      { "name": "Justice", "effect": "Grants +2 additional card draws in the current Shuffle Time." },
      { "name": "Hermit", "effect": "Reveals dungeon map for the current floor." },
      { "name": "Fortune", "effect": "Grants +1 Luck to equipped Persona." },
      { "name": "Strength", "effect": "Grants +1 Strength to equipped Persona." },
      { "name": "Hanged Man", "effect": "Decreases 1 stat on equipped Persona, but increases another by +2." },
      { "name": "Death", "effect": "Opens a cursed chest — triggers the Reaper encounter if 21 chests were opened!" },
      { "name": "Temperance", "effect": "Grants bonus chests on the current dungeon floor." },
      { "name": "Devil", "effect": "Grants +3 extra card draws, but forfeits Yen or EXP reward from this battle." },
      { "name": "Tower", "effect": "Destroys all chest keys, but grants +3 card draws." },
      { "name": "Star", "effect": "Grants +1 Magic to equipped Persona." },
      { "name": "Moon", "effect": "Halves battle EXP, but grants +1 card draw." },
      { "name": "Sun", "effect": "Grants +1 card draw and transforms a penalty card into a bonus card." },
      { "name": "Judgement", "effect": "Replaces all remaining cards with random new cards." },
      { "name": "Fool", "effect": "Changes all remaining cards into 1 single powerful card." }
    ],
    minor_arcana: [
      { "suit": "Swords (⚔️)", "title": "Skill Cards", "desc": "Grants skill cards of rank 1 to 10." },
      { "suit": "Cups (🏆)", "title": "HP & SP Recovery", "desc": "Restores HP and SP to the entire party." },
      { "suit": "Coins (🪙)", "title": "Yen / Money", "desc": "Grants bonus Yen." },
      { "suit": "Wands (🪄)", "title": "Bonus EXP", "desc": "Increases battle EXP by 20% to 100%." }
    ]
  }
};

fs.writeFileSync("./web/data/negotiation/negotiation_data.json", JSON.stringify(data, null, 2));
console.log("Saved game-separated negotiation_data.json successfully!");
