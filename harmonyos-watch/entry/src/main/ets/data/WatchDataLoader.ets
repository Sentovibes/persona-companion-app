import { common } from '@kit.AbilityKit';
import { util } from '@kit.ArkTS';
import {
  WatchClassroomItem,
  WatchDialogue,
  WatchDialogueChoice,
  WatchEnemyItem,
  WatchEnemyResistance,
  WatchSocialLink,
  WatchSocialLinkRank
} from '../model/WatchModels';

const P3_ELEMENTS: string[] = ['Slash', 'Strike', 'Pierce', 'Fire', 'Ice', 'Elec', 'Wind', 'Light', 'Dark', 'Almighty'];
const P4_ELEMENTS: string[] = ['Phys', 'Fire', 'Ice', 'Elec', 'Wind', 'Light', 'Dark', 'Almighty'];
const P5_ELEMENTS: string[] = ['Phys', 'Gun', 'Fire', 'Ice', 'Elec', 'Wind', 'Psy', 'Nuke', 'Bless', 'Curse'];

function readRawJsonText(context: common.Context, relPath: string): Promise<string> {
  return context.resourceManager.getRawFileContent(relPath).then((bytes: Uint8Array) => {
    const decoder = util.TextDecoder.create('utf-8');
    return decoder.decodeToString(bytes);
  });
}

function getElementsForGame(gameId: string): string[] {
  if (gameId.startsWith('p3')) {
    return P3_ELEMENTS;
  }
  if (gameId.startsWith('p5')) {
    return P5_ELEMENTS;
  }
  return P4_ELEMENTS;
}

export async function loadWatchClassroom(context: common.Context, gameId: string): Promise<WatchClassroomItem[]> {
  let file = 'data/classroom/p5_classroom_answers.json';
  if (gameId.startsWith('p3')) {
    file = 'data/classroom/p3_classroom_answers.json';
  } else if (gameId.startsWith('p4')) {
    file = 'data/classroom/p4_classroom_answers.json';
  }

  try {
    const text = await readRawJsonText(context, file);
    const root = JSON.parse(text) as Record<string, Record<string, Record<string, Object[]>>>;
    const isRoyalOrGolden = gameId === 'p5r' || gameId === 'p4g';
    const items: WatchClassroomItem[] = [];
    let idx = 0;

    const months = Object.keys(root);
    for (let m = 0; m < months.length; m++) {
      const monthObj = root[months[m]];
      if (!monthObj) {
        continue;
      }
      const types = Object.keys(monthObj);
      for (let t = 0; t < types.length; t++) {
        const typeName = types[t];
        const lowerType = typeName.toLowerCase();
        const isExam = lowerType.includes('exam') || lowerType.includes('midterm') || lowerType.includes('final');
        const datesObj = monthObj[typeName];
        if (!datesObj) {
          continue;
        }
        const dates = Object.keys(datesObj);
        for (let d = 0; d < dates.length; d++) {
          const dateKey = dates[d];
          const qArr = datesObj[dateKey];
          if (!Array.isArray(qArr)) {
            continue;
          }
          for (let i = 0; i < qArr.length; i++) {
            const qObj = qArr[i] as Record<string, Object>;
            const p5rExcl = Boolean(qObj['P5R Exclusive']);
            const p4gExcl = Boolean(qObj['P4G Exclusive']);
            if ((p5rExcl || p4gExcl) && !isRoyalOrGolden) {
              continue;
            }
            const question = String(qObj['Question'] ?? '');
            const answer = String(qObj['Answer'] ?? '');
            if (answer.length > 0) {
              items.push({
                id: `${idx++}_${dateKey}`,
                date: dateKey,
                question: question.length > 0 ? question : `Question ${dateKey}`,
                answer: answer,
                isExam: isExam
              });
            }
          }
        }
      }
    }
    return items;
  } catch {
    return [];
  }
}

export async function loadWatchEnemies(context: common.Context, gameId: string): Promise<WatchEnemyItem[]> {
  const fileMap: Record<string, string> = {
    'p3fes': 'data/enemies/p3fes_enemies.json',
    'p3p': 'data/enemies/p3p_enemies.json',
    'p3r': 'data/enemies/p3r_enemies.json',
    'p4': 'data/enemies/p4_enemies.json',
    'p4g': 'data/enemies/p4g_enemies.json',
    'p5': 'data/enemies/p5_enemies.json',
    'p5r': 'data/enemies/p5r_enemies.json'
  };
  const file = fileMap[gameId];
  if (!file) {
    return [];
  }

  const elements = getElementsForGame(gameId);

  try {
    const text = await readRawJsonText(context, file);
    const arr = JSON.parse(text) as Record<string, Object>[];
    const items: WatchEnemyItem[] = [];

    for (let i = 0; i < arr.length; i++) {
      const obj = arr[i];
      const name = String(obj['name'] ?? '');
      if (name.length === 0) {
        continue;
      }
      const arcana = String(obj['arcana'] ?? '');
      const level = Number(obj['level'] ?? 1);
      const area = String(obj['area'] ?? '');
      const resists = String(obj['resists'] ?? '');
      const isBoss = Boolean(obj['isBoss']) || area.toLowerCase() === 'boss';

      const weaknesses: string[] = [];
      const resistances: WatchEnemyResistance[] = [];

      for (let eIdx = 0; eIdx < resists.length && eIdx < elements.length; eIdx++) {
        const code = resists.charAt(eIdx);
        const elem = elements[eIdx];
        if (code === 'w' || code === 'W') {
          weaknesses.push(elem);
          resistances.push({ element: elem, status: 'Weak' });
        } else if (code === 'r' || code === 's' || code === 'S') {
          resistances.push({ element: elem, status: 'Resist' });
        } else if (code === 'n' || code === 'N' || code === '_') {
          resistances.push({ element: elem, status: 'Null' });
        } else if (code === 'p' || code === 'P' || code === 'R') {
          resistances.push({ element: elem, status: 'Repel' });
        } else if (code === 'd' || code === 'D' || code === 'a' || code === 'A') {
          resistances.push({ element: elem, status: 'Drain' });
        }
      }

      items.push({
        id: `${i}_${name}`,
        name: name,
        arcana: arcana,
        level: level,
        weaknesses: weaknesses,
        resistances: resistances,
        area: area,
        isBoss: isBoss
      });
    }

    items.sort((a, b) => (a.level - b.level) || a.name.localeCompare(b.name));
    return items;
  } catch {
    return [];
  }
}

export async function loadWatchSocialLinks(context: common.Context, gameId: string): Promise<WatchSocialLink[]> {
  const fileMap: Record<string, string> = {
    'p3fes': 'data/social-links/p3fes_social_links.json',
    'p3p': 'data/social-links/p3p_male_social_links.json',
    'p3r': 'data/social-links/p3r_social_links.json',
    'p4': 'data/social-links/p4+p4g_social_links.json',
    'p4g': 'data/social-links/p4+p4g_social_links.json',
    'p5': 'data/social-links/p5+p5r_social_links.json',
    'p5r': 'data/social-links/p5+p5r_social_links.json'
  };
  const file = fileMap[gameId];
  if (!file) {
    return [];
  }

  try {
    const text = await readRawJsonText(context, file);
    const root = JSON.parse(text) as Record<string, Record<string, Object>>;
    const links: WatchSocialLink[] = [];

    const arcanas = Object.keys(root);
    for (let i = 0; i < arcanas.length; i++) {
      const arcana = arcanas[i];
      if (arcana.startsWith('EXTRA:')) {
        continue;
      }
      const data = root[arcana];
      if (!data) {
        continue;
      }
      if (Boolean(data['P4G Exclusive']) && gameId === 'p4') {
        continue;
      }
      if (Boolean(data['P5R Exclusive']) && gameId === 'p5') {
        continue;
      }

      const ranks: WatchSocialLinkRank[] = [];
      const prog = data['Rank Up Progression'] as Record<string, Record<string, Object>> | undefined;
      if (prog) {
        const rankKeys = Object.keys(prog);
        for (let r = 0; r < rankKeys.length; r++) {
          const rankName = rankKeys[r];
          const rObj = prog[rankName];
          const dArr = rObj?.['Dialogues'] as Record<string, Object>[] | undefined;
          const parsedDialogues: WatchDialogue[] = [];

          if (Array.isArray(dArr)) {
            for (let d = 0; d < dArr.length; d++) {
              const dObj = dArr[d];
              const q = String(dObj['Question'] ?? '');
              const cArr = dObj['Choices'] as Record<string, Object>[] | undefined;
              const bestChoices: WatchDialogueChoice[] = [];
              if (Array.isArray(cArr)) {
                for (let c = 0; c < cArr.length; c++) {
                  const cObj = cArr[c];
                  const ans = String(cObj['Answer'] ?? '');
                  const pts = Number(cObj['Points'] ?? 0);
                  if (ans.length > 0) {
                    bestChoices.push({ text: ans, points: pts });
                  }
                }
              }
              if (bestChoices.length > 0) {
                parsedDialogues.push({ question: q, choices: bestChoices });
              }
            }
          }

          ranks.push({
            rankName: rankName,
            dialogues: parsedDialogues
          });
        }
      }

      links.push({
        id: `${gameId}_${arcana}`,
        arcana: arcana,
        characterName: arcana,
        ranks: ranks
      });
    }

    return links;
  } catch {
    return [];
  }
}
