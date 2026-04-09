import { chromium } from 'playwright-extra';
import StealthPlugin from 'puppeteer-extra-plugin-stealth';
chromium.use(StealthPlugin());

const browser = await chromium.launch({
  headless: false,
  executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
  args: ['--no-sandbox'],
});

const context = await browser.newContext({
  userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
  viewport: { width: 1280, height: 900 },
});

const page = await context.newPage();

console.log('Navigating to main page...');
await page.goto('https://www.megatenwiki.com/wiki/Main_Page', { waitUntil: 'domcontentloaded', timeout: 30000 });
await new Promise(r => setTimeout(r, 4000));

const title = await page.title();
console.log('Main page title:', title);

console.log('\nNavigating to Abaddon gallery...');
await page.goto('https://www.megatenwiki.com/wiki/Gallery:Abaddon', { waitUntil: 'domcontentloaded', timeout: 20000 });
await new Promise(r => setTimeout(r, 3000));

const title2 = await page.title();
console.log('Gallery page title:', title2);

const bodyText = await page.evaluate(() => document.body?.innerText?.slice(0, 500));
console.log('Body text preview:\n', bodyText);

const imgs = await page.evaluate(() =>
  Array.from(document.querySelectorAll('img')).slice(0, 10).map(i => ({ src: i.src, w: i.width, h: i.height, alt: i.alt }))
);
console.log('\nFirst 10 imgs:', JSON.stringify(imgs, null, 2));

// Keep browser open so you can see it
console.log('\nBrowser staying open for 30s so you can inspect...');
await new Promise(r => setTimeout(r, 30000));
await browser.close();
