const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');

test('docs web app entry files exist', () => {
    assert.ok(fs.existsSync('docs/index.html'), 'index.html exists');
    assert.ok(fs.existsSync('docs/app.js'), 'app.js exists');
    assert.ok(fs.existsSync('docs/styles.css'), 'styles.css exists');
});

test('docs dataset integrity', () => {
    assert.ok(fs.existsSync('docs/data/persona5/royal_personas.json'), 'P5R personas exist');
    assert.ok(fs.existsSync('docs/data/skills/p5r_skills.json'), 'P5R skills exist');
});
