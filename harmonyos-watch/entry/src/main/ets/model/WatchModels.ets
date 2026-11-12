export interface WatchGame {
  id: string;
  title: string;
  shortTitle: string;
  accentColor: string;
  seriesId: string;
}

export interface WatchClassroomItem {
  id: string;
  date: string;
  question: string;
  answer: string;
  isExam: boolean;
}

export interface WatchEnemyResistance {
  element: string;
  status: string;
}

export interface WatchEnemyItem {
  id: string;
  name: string;
  arcana: string;
  level: number;
  weaknesses: string[];
  resistances: WatchEnemyResistance[];
  area: string;
  isBoss: boolean;
}

export interface WatchDialogueChoice {
  text: string;
  points: number;
}

export interface WatchDialogue {
  question: string;
  choices: WatchDialogueChoice[];
}

export interface WatchSocialLinkRank {
  rankName: string;
  dialogues: WatchDialogue[];
}

export interface WatchSocialLink {
  id: string;
  arcana: string;
  characterName: string;
  ranks: WatchSocialLinkRank[];
}

export const WATCH_GAMES: WatchGame[] = [
  { id: 'p5r', title: 'Persona 5 Royal', shortTitle: 'P5R', accentColor: '#CC1A1A', seriesId: 'p5' },
  { id: 'p5', title: 'Persona 5', shortTitle: 'P5', accentColor: '#CC1A1A', seriesId: 'p5' },
  { id: 'p4g', title: 'Persona 4 Golden', shortTitle: 'P4G', accentColor: '#F5A800', seriesId: 'p4' },
  { id: 'p4', title: 'Persona 4', shortTitle: 'P4', accentColor: '#F5A800', seriesId: 'p4' },
  { id: 'p3r', title: 'Persona 3 Reload', shortTitle: 'P3R', accentColor: '#1A6FCC', seriesId: 'p3' },
  { id: 'p3p', title: 'Persona 3 Portable', shortTitle: 'P3P', accentColor: '#1A6FCC', seriesId: 'p3' },
  { id: 'p3fes', title: 'Persona 3 FES', shortTitle: 'P3FES', accentColor: '#1A6FCC', seriesId: 'p3' }
];
