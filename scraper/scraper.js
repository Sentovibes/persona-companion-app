/**
 * Persona image scraper — megatenwiki.com
 *
 * Cloudflare blocks all plain HTTP. Strategy:
 *   1. Open real Chrome once → CF auto-solves in ~5s → extract cf_clearance cookie
 *   2. Close browser, use that cookie for all API + image downloads via Node https
 *   3. Cookies cached 1hr so re-runs skip the browser step
 */

import { chromium } from 'playwright-extra';
import StealthPlugin from 'puppeteer-extra-plugin-stealth';
import https from 'https';
import fs from 'fs';
import path from 'path';
import crypto from 'crypto';

chromium.use(StealthPlugin());

// ─── config ──────────────────────────────────────────────────────────────────
const OUTPUT_DIR  = path.resolve('../app/src/main/assets/images/personas_shared');
const P5R_DATA    = path.resolve('../app/src/main/assets/data/persona5/royal_personas.json');
const DONE_LOG    = path.resolve('./done.json');
const FAILED_LOG  = path.resolve('./failed.json');
const COOKIE_FILE = path.resolve('./cf_cookies.json');
const PROFILE_DIR = path.resolve('./chrome_profile');
const CHROME_EXE  = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const WIKI        = 'https://megatenwiki.com';
const DELAY_MS    = 900;

// ─── terminal colors ──────────────────────────────────────────────────────────
const C = { reset:'\x1b[0m', green:'\x1b[32m', red:'\x1b[31m', yellow:'\x1b[33m', cyan:'\x1b[36m', gray:'\x1b[90m', bold:'\x1b[1m' };
const tag = (col, lbl, msg) => process.stdout.write(`${col}[${lbl}]${C.reset} ${msg}\n`);

function printHeader(total, ok, fail) {
  console.clear();
  const pct = total > 0 ? Math.round((ok / total) * 100) : 0;
  const bar = C.green + '█'.repeat(Math.round(pct/2)) + C.gray + '░'.repeat(50 - Math.round(pct/2)) + C.reset;
  console.log(`${C.bold}${C.cyan}━━━ megatenwiki.com Persona Scraper ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${C.reset}`);
  console.log(`  [${bar}] ${C.bold}${pct}%${C.reset}  ${C.green}${ok} ok${C.reset}  ${C.red}${fail} failed${C.reset}  of ${total}`);
  console.log(`  Images → ${C.cyan}${OUTPUT_DIR}${C.reset}`);
  console.log(`${C.gray}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${C.reset}`);
}

const sleep = ms => new Promise(r => setTimeout(r, ms));

function toFilename(name) {
  return name.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_\-]/g, '') + '.png';
}

// ─── MediaWiki MD5 image path (fallback if imageinfo API blocked) ─────────────
function mwImageUrl(filename) {
  const f = filename.replace(/^File:/i, '').replace(/ /g, '_');
  const h = crypto.createHash('md5').update(f).digest('hex');
  return `${WIKI}/images/${h[0]}/${h.slice(0,2)}/${encodeURIComponent(f)}`;
}

// ─── HTTPS with CF cookies ────────────────────────────────────────────────────
function httpsGet(url, cookies, redirects = 0) {
  if (redirects > 5) return Promise.reject(new Error('too many redirects'));
  return new Promise((resolve, reject) => {
    const req = https.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
        'Cookie': cookies,
        'Referer': WIKI + '/',
        'Accept': '*/*',
        'Accept-Language': 'en-US,en;q=0.9',
      }
    }, res => {
      if ([301,302,303,307,308].includes(res.statusCode) && res.headers.location) {
        const next = res.headers.location.startsWith('htt += 50;
  else if (t.includes('p5s') || t.includes('strikers')) s += 45;
  else if (t.includes('p5'))                            s += 40;
  else if (t.includes('p4g') || t.includes('golden'))   s += 35;
  else if (t.includes('p4'))                            s += 30;
  else if (t.includes('p3r') || t.includes('reload'))   s += 25;
  else if (t.includes('p3fes') || t.includes('fes'))    s += 20;
  else if (t.includes('p3p') || t.includes('portable')) s += 15;
  else if (t.includes('p3'))                            s += 10;
  if (t.includes('thumb'))  s -= 50;
  if (t.includes('icon'))   s -= 30;
  if (t.includes('sprite')) s -= 20;
  if (t.includes('boss'))   s -= 10;
  return s;
}

async function getPageImages(personaName) {
  const allImages = [];
  let imcontinue = null;
  do {
    const params = { action: 'query', titles: personaName, prop: 'images', imlimit: '50' };
    if (imcontinue) params.imcontinue = imcontinue;
    const data = await apiGet(params);
    const page = Object.values(data?.query?.pages || {})[0];
    allImages.push(...(page?.images || []).map(i => i.title));
    imcontinue = data?.continue?.imcontinue || null;
  } while (imcontinue);
  return allImages;
}

async function resolveImageUrl(fileTitle) {
  const data = await apiGet({ action: 'query', titles: fileTitle, prop: 'imageinfo', iiprop: 'url' });
  const url = Object.values(data?.query?.pages || {})[0]?.imageinfo?.[0]?.url;
  return url || null; // keep full URL including query params
}

async function scrapePersona(name, done, failed) {
  if (done.has(name)) { tag(C.gray, 'SKIP', name); return 'skip'; }

  tag(C.cyan, 'FETCH', name);
  const destPath = path.join(OUTPUT_DIR, toFilename(name));

  try {
    const fileTitles = await getPageImages(name);
    if (!fileTitles.length) {
      tag(C.yellow, 'MISS', `${name} — no images on wiki page`);
      failed.set(name, 'no images on page');
      return 'fail';
    }

    const scored = fileTitles
      .map(t => ({ title: t, score: scoreTitle(t) }))
      .sort((a, b) => b.score - a.score);

    tag(C.gray, 'CAND', `${scored.length} imgs, best: ${scored[0].title} (${scored[0].score})`);

    for (const candidate of scored.slice(0, 5)) {
      try {
        const imgUrl = await resolveImageUrl(candidate.title);
        if (!imgUrl) continue;

        tag(C.gray, 'DL  ', imgUrl.split('/').slice(-1)[0]);
        const buf = await httpGet(imgUrl);

        if (buf.length < 5000) { tag(C.yellow, 'TINY', `${buf.length}b, skipping`); continue; }

        // verify it's actually an image (PNG, JPG, GIF, or WebP)
        const magic = buf.slice(0, 12).toString('hex');
        const isImage = magic.startsWith('89504e47')                          // PNG
                     || magic.startsWith('ffd8ff')                            // JPG
                     || magic.startsWith('47494638')                          // GIF
                     || (magic.startsWith('52494646') && buf.slice(8,12).toString('ascii') === 'WEBP'); // WebP
        if (!isImage) {
          tag(C.yellow, 'WARN', `not an image (${magic.slice(0,8)}), skipping`);
          continue;
        }

        fs.writeFileSync(destPath, buf);
        tag(C.green, ' OK ', `${name}  →  ${toFilename(name)}  (${Math.round(buf.length/1024)}KB)`);
        done.add(name);
        return 'ok';
      } catch (e) {
        tag(C.yellow, 'WARN', `${candidate.title} — ${e.message}`);
      }
    }

    tag(C.red, 'FAIL', `${name} — all candidates failed`);
    failed.set(name, 'all candidates failed');
    return 'fail';

  } catch (e) {
    tag(C.red, 'ERR ', `${name} — ${e.message}`);
    failed.set(name, e.message);
    return 'fail';
  }
}

async function main() {
  const personas = Object.keys(JSON.parse(fs.readFileSync(P5R_DATA, 'utf8')));
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });

  const done   = new Set(fs.existsSync(DONE_LOG)   ? JSON.parse(fs.readFileSync(DONE_LOG))   : []);
  const failed = new Map(Object.entries(fs.existsSync(FAILED_LOG) ? JSON.parse(fs.readFileSync(FAILED_LOG)) : {}));

  const total = personas.length;
  let ok = 0, skip = 0, fail = 0;

  printHeader(total, done.size, failed.size);
  console.log(`\n  ${total} personas  |  ${done.size} done  |  ${failed.size} previously failed\n`);

  for (const name of personas) {
    const result = await scrapePersona(name, done, failed);
    if (result === 'ok')        ok++;
    else if (result === 'skip') skip++;
    else                        fail++;

    if ((ok + fail) % 10 === 0 && (ok + fail) > 0) {
      fs.writeFileSync(DONE_LOG,   JSON.stringify([...done], null, 2));
      fs.writeFileSync(FAILED_LOG, JSON.stringify(Object.fromEntries(failed), null, 2));
      printHeader(total, done.size, failed.size);
    }

    await sleep(DELAY_MS);
  }

  fs.writeFileSync(DONE_LOG,   JSON.stringify([...done], null, 2));
  fs.writeFileSync(FAILED_LOG, JSON.stringify(Object.fromEntries(failed), null, 2));

  console.log(`\n${C.bold}${C.cyan}━━━ Done ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${C.reset}`);
  console.log(`  ${C.green}Downloaded:${C.reset} ${ok}  ${C.gray}Skipped:${C.reset} ${skip}  ${C.red}Failed:${C.reset} ${fail}`);
  console.log(`  Images → ${C.cyan}${OUTPUT_DIR}${C.reset}`);
  if (fail > 0) console.log(`  Failures → ${C.cyan}${FAILED_LOG}${C.reset}`);
  console.log(`${C.gray}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${C.reset}\n`);
}

main().catch(console.error);
