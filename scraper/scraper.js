import https from 'https';
import fs from 'fs';
import path from 'path';

const OUTPUT_DIR = path.resolve('../app/src/main/assets/images/personas_shared');
const P5R_DATA   = path.resolve('../app/src/main/assets/data/persona5/royal_personas.json');
const FAILED_LOG = path.resolve('./failed.json');
const DONE_LOG   = path.resolve('./done.json');
const FANDOM_API = 'https://megamitensei.fandom.com/api.php';
const DELAY_MS   = 800;

const C = {
  reset:'\x1b[0m', green:'\x1b[32m', red:'\x1b[31m',
  yellow:'\x1b[33m', cyan:'\x1b[36m', gray:'\x1b[90m', bold:'\x1b[1m',
};

function tag(color, label, msg) {
  process.stdout.write(`${color}[${label}]${C.reset} ${msg}\n`);
}

function printHeader(total, doneCount, failCount) {
  console.clear();
  console.log(`${C.bold}${C.cyan}━━━ Persona Image Scraper ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${C.reset}`);
  console.log(`  Images → ${C.cyan}${OUTPUT_DIR}${C.reset}`);
  console.log(`  Done   → ${C.gray}${DONE_LOG}${C.reset}`);
  console.log(`  Failed → ${C.gray}${FAILED_LOG}${C.reset}`);
  console.log(`${C.gray}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${C.reset}`);
  const pct = total > 0 ? Math.round((doneCount / total) * 100) : 0;
  const filled = Math.round((pct / 100) * 50);
  const bar = C.green + '█'.repeat(filled) + C.gray + '░'.repeat(50 - filled) + C.reset;
  console.log(`  [${bar}] ${C.bold}${pct}%${C.reset}  ${C.green}${doneCount} ok${C.reset}  ${C.red}${failCount} failed${C.reset}  of ${total}`);
  console.log(`${C.gray}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${C.reset}`);
}

function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

function toFilename(name) {
  return name.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_\-]/g, '') + '.png';
}

function httpGet(url) {
  return new Promise((resolve, reject) => {
    const req = https.get(url, { headers: { 'User-Agent': 'Mozilla/5.0 PersonaScraper/1.0' } }, res => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return httpGet(res.headers.location).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) return reject(new Error(`HTTP ${res.statusCode}`));
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve(Buffer.concat(chunks)));
    });
    req.on('error', reject);
    req.setTimeout(15000, () => { req.destroy(); reject(new Error('timeout')); });
  });
}

async function apiGet(params) {
  const url = new URL(FANDOM_API);
  url.searchParams.set('format', 'json');
  for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);
  const buf = await httpGet(url.toString());
  return JSON.parse(buf.toString());
}

function scoreTitle(title) {
  const t = title.toLowerCase();
  let s = 0;
  if (t.includes('portrait')) s += 100;
  if (t.includes('model'))    s += 80;
  if (t.includes('artwork'))  s += 60;
  if      (t.includes('p5r') || t.includes('royal'))    s += 50;
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
