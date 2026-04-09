// Finds duplicate persona images (same persona, different filename variants)
// and removes the older/lower-quality one, keeping the canonical name.
import fs from 'fs';
import path from 'path';
import crypto from 'crypto';

const IMAGE_DIR = '../app/src/main/assets/images/personas_shared';

function hashFile(filepath) {
  return crypto.createHash('md5').update(fs.readFileSync(filepath)).digest('hex');
}

const files = fs.readdirSync(IMAGE_DIR).filter(f => f.endsWith('.png'));
const hashMap = new Map(); // hash -> [files]
const dupes = [];

for (const file of files) {
  const fullPath = path.join(IMAGE_DIR, file);
  const hash = hashFile(fullPath);
  if (!hashMap.has(hash)) hashMap.set(hash, []);
  hashMap.get(hash).push(file);
}

for (const [hash, group] of hashMap) {
  if (group.length > 1) {
    dupes.push(group);
    console.log(`Duplicate group (${hash.slice(0,8)}):`);
    group.forEach(f => console.log(`  ${f}`));
  }
}

if (dupes.length === 0) {
  console.log('No exact duplicates found.');
} else {
  console.log(`\nFound ${dupes.length} duplicate groups.`);
  console.log('Review above and delete the unwanted variants manually,');
  console.log('or run with --auto-delete to keep the shortest filename in each group.');

  if (process.argv.includes('--auto-delete')) {
    for (const group of dupes) {
      // Keep shortest name (most canonical), delete the rest
      const sorted = [...group].sort((a, b) => a.length - b.length);
      const keep = sorted[0];
      const remove = sorted.slice(1);
      console.log(`\nKeeping: ${keep}`);
      for (const f of remove) {
        fs.unlinkSync(path.join(IMAGE_DIR, f));
        console.log(`  Deleted: ${f}`);
      }
    }
  }
}
