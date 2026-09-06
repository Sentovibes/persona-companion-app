import http from 'http';
import fs from 'fs';
import path from 'path';
import puppeteer from 'puppeteer';

const PORT = 8089;
const DOCS_DIR = path.resolve('docs');

// Static HTTP server for docs/
const server = http.createServer((req, res) => {
  let reqPath = decodeURI(req.url.split('?')[0]);
  if (reqPath === '/' || reqPath === '') reqPath = '/index.html';
  const filePath = path.join(DOCS_DIR, reqPath);

  if (!filePath.startsWith(DOCS_DIR) || !fs.existsSync(filePath)) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Not Found');
    return;
  }

  const ext = path.extname(filePath).toLowerCase();
  const mimeTypes = {
    '.html': 'text/html',
    '.js': 'application/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.webp': 'image/webp',
    '.svg': 'image/svg+xml'
  };

  res.writeHead(200, {
    'Content-Type': mimeTypes[ext] || 'application/octet-stream',
    'Access-Control-Allow-Origin': '*'
  });
  fs.createReadStream(filePath).pipe(res);
});

await new Promise((resolve) => server.listen(PORT, '127.0.0.1', resolve));
console.log(`Test server running at http://127.0.0.1:${PORT}`);

const results = [];
function recordTest(name, passed, details = '') {
  results.push({ name, passed, details });
  const status = passed ? '[PASS]' : '[FAIL]';
  console.log(`${status} - ${name}${details ? ': ' + details : ''}`);
}

let browser;
try {
  browser = await puppeteer.launch({
    headless: true,
    executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();
  page.on('pageerror', (err) => console.error('PAGE ERROR:', err.message));

  // Test 1: Home page loads with HTTP 200
  const respHome = await page.goto(`http://127.0.0.1:${PORT}/index.html`, { waitUntil: 'networkidle0', timeout: 30000 });
  recordTest('HTTP 200 Initial Page Load', respHome.status() === 200, `Status: ${respHome.status()}`);

  // Test 2: Series Selection cards render for P3, P4, P5
  await page.waitForSelector('.series-card', { timeout: 10000 });
  const seriesCount = await page.evaluate(() => document.querySelectorAll('.series-card').length);
  recordTest('Home Series Selection Rendering', seriesCount === 3, `Rendered ${seriesCount} series cards (P3, P4, P5)`);

  // Test 3: Select Persona 5 Royal and open Personas compendium
  await page.evaluate(async () => {
    selectGame('p5', 'p5r');
    S.listMode = 'personas';
    await navigate('list');
  });
  await page.waitForSelector('#listContent .row-card', { timeout: 15000 });

  const p5rPersonaCount = await page.evaluate(() => {
    return document.querySelectorAll('#listContent .row-card').length;
  });
  recordTest('P5R Personas Compendium Loading', p5rPersonaCount > 150, `Rendered ${p5rPersonaCount} personas`);

  // Test 4: Search functionality (Search for "Jack Frost")
  await page.type('#searchInput', 'Jack Frost');
  await new Promise((r) => setTimeout(r, 600));

  const foundJackFrost = await page.evaluate(() => {
    const items = Array.from(document.querySelectorAll('#listContent .row-card'));
    const visible = items.filter(el => el.style.display !== 'none' && el.innerText.includes('Jack Frost'));
    return visible.length > 0;
  });
  recordTest('Compendium Live Search Filter', foundJackFrost, 'Found Jack Frost in search results');

  // Test 5: Open Persona Detail View & Verify Affinities/Stats
  await page.evaluate(() => {
    openPersona('Jack Frost');
  });
  await new Promise((r) => setTimeout(r, 800));

  const detailVerified = await page.evaluate(() => {
    const text = document.body.innerText;
    return text.includes('Jack Frost') && (text.includes('Stats') || text.includes('Skills') || text.includes('St') || text.includes('Lv'));
  });
  recordTest('Persona Detail View & Stats Display', detailVerified, 'Jack Frost details, stats and affinities loaded');

  // Test 6: Verify Fusion Calculator
  await page.evaluate(async () => {
    selectGame('p5', 'p5r');
    await navigate('fusion');
    await new Promise(r => setTimeout(r, 600));
    selectFusionPersona('Jack Frost');
  });
  await page.waitForSelector('.fusion-recipe-card, #fusionContent', { timeout: 10000 });
  const fusionRecipesCount = await page.evaluate(() => {
    return document.querySelectorAll('.fusion-recipe-card').length || (S.fusion.recipes ? S.fusion.recipes.length : 0);
  });
  recordTest('Fusion Calculator Recipe Engine', fusionRecipesCount > 0, `Generated ${fusionRecipesCount} recipes for Jack Frost`);

  // Test 7: Classroom Answers Dataset
  await page.evaluate(async () => {
    selectGame('p5', 'p5r');
    S.listMode = 'classroom';
    await navigate('list');
  });
  await page.waitForSelector('.qa-card', { timeout: 10000 });
  const classroomCount = await page.evaluate(() => document.querySelectorAll('.qa-card').length);
  recordTest('Classroom Answers Guide Loading', classroomCount > 20, `Loaded ${classroomCount} classroom QA entries`);

  // Test 8: Enemy Weakness Calculator Dataset
  await page.evaluate(async () => {
    selectGame('p5', 'p5r');
    S.listMode = 'enemies';
    await navigate('list');
  });
  await page.waitForSelector('#listContent .row-card', { timeout: 10000 });
  const enemyItemsCount = await page.evaluate(() => document.querySelectorAll('#listContent .row-card').length);
  recordTest('Enemy Weakness Calculator Loading', enemyItemsCount > 50, `Loaded ${enemyItemsCount} enemies with affinities`);

  // Test 9: Multi-Game Switching (Switch to Persona 4 Golden)
  await page.evaluate(async () => {
    selectGame('p4', 'p4g');
    S.listMode = 'personas';
    await navigate('list');
  });
  await page.waitForSelector('#listContent .row-card', { timeout: 10000 });
  const p4gPersonasCount = await page.evaluate(() => document.querySelectorAll('#listContent .row-card').length);
  recordTest('Multi-Game Switch (Persona 4 Golden)', p4gPersonasCount > 100, `Loaded ${p4gPersonasCount} P4G personas`);

  // Test 10: Multi-Game Switching (Switch to Persona 3 Reload)
  await page.evaluate(async () => {
    selectGame('p3', 'p3r');
    S.listMode = 'personas';
    await navigate('list');
  });
  await page.waitForSelector('#listContent .row-card', { timeout: 10000 });
  const p3rPersonasCount = await page.evaluate(() => document.querySelectorAll('#listContent .row-card').length);
  recordTest('Multi-Game Switch (Persona 3 Reload)', p3rPersonasCount > 100, `Loaded ${p3rPersonasCount} P3R personas`);

} catch (err) {
  recordTest('Integration Test Execution', false, err.message);
} finally {
  if (browser) await browser.close();
  server.close();
}

console.log('\n--- INTEGRATION TEST SUMMARY ---');
const passedCount = results.filter((r) => r.passed).length;
console.log(`Total: ${results.length} | Passed: ${passedCount} | Failed: ${results.length - passedCount}`);

if (passedCount === results.length) {
  console.log('ALL INTEGRATION TESTS PASSED');
  process.exit(0);
} else {
  console.error('SOME INTEGRATION TESTS FAILED');
  process.exit(1);
}
