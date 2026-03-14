/* ── Data ──────────────────────────────────────────────────────────────────── */
const SERIES = [
    {
        id: 'p3', title: 'Persona 3', color: '#1A6FCC',
        games: [
            { id: 'p3fes', title: 'Persona 3 FES',      tabs: ['personas','enemies','classroom'] },
            { id: 'p3p',   title: 'Persona 3 Portable', tabs: ['personas','enemies','classroom'] },
            { id: 'p3r',   title: 'Persona 3 Reload',   tabs: ['personas','enemies','classroom'] },
        ]
    },
    {
        id: 'p4', title: 'Persona 4', color: '#F5A800',
        games: [
            { id: 'p4',  title: 'Persona 4',        tabs: ['personas','enemies','classroom'] },
            { id: 'p4g', title: 'Persona 4 Golden', tabs: ['personas','enemies','classroom'] },
        ]
    },
    {
        id: 'p5', title: 'Persona 5', color: '#CC1A1A',
        games: [
            { id: 'p5',  title: 'Persona 5',       tabs: ['personas','enemies','classroom'] },
            { id: 'p5r', title: 'Persona 5 Royal', tabs: ['personas','enemies','classroom'] },
        ]
    },
];

const DATA_PATHS = {
    personas: {
        p3fes:'./data/persona3/personas.json', p3p:'./data/persona3/portable_personas.json',
        p3r:'./data/persona3/reload_personas.json', p4:'./data/persona4/personas.json',
        p4g:'./data/persona4/golden_personas.json', p5:'./data/persona5/personas.json',
        p5r:'./data/persona5/royal_personas.json'
    },
    enemies: {
        p3fes:'./data/enemies/p3fes_enemies.json', p3p:'./data/enemies/p3p_enemies.json',
        p3r:'./data/enemies/p3r_enemies.json', p4:'./data/enemies/p4_enemies.json',
        p4g:'./data/enemies/p4g_enemies.json', p5:'./data/enemies/p5_enemies.json',
        p5r:'./data/enemies/p5r_enemies.json'
    },
    classroom: {
        p3fes:'./data/classroom/p3_classroom_answers.json', p3p:'./data/classroom/p3_classroom_answers.json',
        p3r:'./data/classroom/p3_classroom_answers.json', p4:'./data/classroom/p4_classroom_answers.json',
        p4g:'./data/classroom/p4_classroom_answers.json', p5:'./data/classroom/p5_classroom_answers.json',
        p5r:'./data/classroom/p5_classroom_answers.json'
    }
};

const ELEMENTS = {
    p3: ['Slash','Strike','Pierce','Fire','Ice','Elec','Wind','Light','Dark','Almighty'],
    p4: ['Phys','Fire','Ice','Elec','Wind','Light','Dark','Almighty'],
    p5: ['Phys','Gun','Fire','Ice','Elec','Wind','Psy','Nuke','Bless','Curse']
};

/* ── State ─────────────────────────────────────────────────────────────────── */
const S = {
    screen: 'home',
    series: null, game: null, tab: 'personas',
    rawData: {}, sort: 'arcana', enemyTab: 'enemies',
    query: '', detail: null, favorites: new Set()
};

/* ── Boot ──────────────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    buildHome();
    S.favorites = new Set(JSON.parse(localStorage.getItem('favs') || '[]'));
});

/* ── Navigation ────────────────────────────────────────────────────────────── */
function navigate(to, payload) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById('screen-' + to).classList.add('active');
    S.screen = to;
    if (to === 'home')   buildHome();
    if (to === 'game')   buildGameScreen(payload);
    if (to === 'list')   buildListScreen(payload);
    if (to === 'detail') buildDetailScreen(payload);
}

function goBackFromList() {
    navigate('game', S.series);
}

/* ── Home ──────────────────────────────────────────────────────────────────── */
function buildHome() {
    const el = document.getElementById('seriesList');
    el.innerHTML = SERIES.map(s => `
        <div class="series-card" style="background:linear-gradient(90deg,${s.color},${s.color}99)"
             onclick="navigate('game','${s.id}')">
            <div class="series-card-bg-num">${s.id.replace('p','')}</div>
            <div class="series-card-text">
                <div class="series-card-title">${s.title}</div>
                <div class="series-card-sub">${s.games.length} game${s.games.length>1?'s':''}</div>
            </div>
        </div>`).join('');
}

/* ── Game Selection ────────────────────────────────────────────────────────── */
function buildGameScreen(seriesId) {
    const series = SERIES.find(s => s.id === seriesId);
    if (!series) return;
    S.series = seriesId;
    document.getElementById('gameScreenTitle').textContent = series.title;
    const el = document.getElementById('gameList');
    el.innerHTML = series.games.map(g => `
        <button class="game-btn" onclick="selectGame('${seriesId}','${g.id}')">
            <span class="game-btn-dot" style="background:${series.color}"></span>
            ${g.title}
        </button>`).join('');
}

function selectGame(seriesId, gameId) {
    S.game = gameId;
    S.series = seriesId;
    S.tab = 'personas';
    S.sort = 'arcana';
    S.enemyTab = 'enemies';
    S.query = '';
    navigate('list', { seriesId, gameId, tab: 'personas' });
}

/* ── List Screen ───────────────────────────────────────────────────────────── */
function buildListScreen(payload) {
    if (payload) { S.series = payload.seriesId; S.game = payload.gameId; S.tab = payload.tab || 'personas'; }
    const series = SERIES.find(s => s.id === S.series);
    const game   = series?.games.find(g => g.id === S.game);
    if (!series || !game) return;

    const color = series.color;
    const tabLabels = { personas:'Personas', enemies:'Enemies', classroom:'Classroom' };

    // Top bar title (just game name, tab shown in section tabs)
    document.getElementById('listScreenTitle').textContent = game.title;

    // Section tabs
    const sectionTabs = document.getElementById('sectionTabs');
    sectionTabs.innerHTML = ['personas','enemies','classroom'].map(t => `
        <button class="section-tab ${S.tab===t?'active':''}"
                style="${S.tab===t?`color:${color};border-bottom-color:${color}`:''}"
                onclick="switchTab('${t}')">
            ${tabLabels[t]}
        </button>`).join('');

    // Sort bar (personas only)
    const sortBar = document.getElementById('sortBar');
    if (S.tab === 'personas') {
        sortBar.style.display = 'flex';
        sortBar.innerHTML = ['arcana','level','name'].map(opt => `
            <button class="sort-chip ${S.sort===opt?'active':''}"
                    style="${S.sort===opt?`color:${color};background:${color}22`:''}"
                    onclick="setSort('${opt}','${color}')">
                ${opt.charAt(0).toUpperCase()+opt.slice(1)}
            </button>`).join('');
    } else {
        sortBar.style.display = 'none';
    }

    // Tab bar (enemies only)
    const tabBar = document.getElementById('tabBar');
    if (S.tab === 'enemies') {
        tabBar.style.display = 'flex';
        // counts filled after data loads
        tabBar.innerHTML = ['enemies','mini_bosses','main_bosses'].map(t => `
            <button class="tab-item ${S.enemyTab===t?'active':''}" id="etab-${t}"
                    onclick="setEnemyTab('${t}')">
                ${t.replace('_',' ')}
            </button>`).join('');
    } else {
        tabBar.style.display = 'none';
    }

    // Search
    document.getElementById('searchInput').value = S.query;
    document.getElementById('searchClear').style.display = S.query ? 'block' : 'none';

    loadAndRender(color);
}

async function loadAndRender(color) {
    const key = `${S.tab}_${S.game}`;
    if (!S.rawData[key]) {
        showLoading();
        try {
            const path = DATA_PATHS[S.tab]?.[S.game];
            if (!path) { showEmpty('No data available'); return; }
            const r = await fetch(path);
            if (!r.ok) throw new Error(r.statusText);
            S.rawData[key] = await r.json();
        } catch(e) {
            showEmpty('Failed to load: ' + e.message);
            return;
        }
    }
    renderList(S.rawData[key], color);
}

function renderList(data, color) {
    const q = S.query.toLowerCase();
    const el = document.getElementById('listContent');

    if (S.tab === 'personas') renderPersonas(data, q, color, el);
    else if (S.tab === 'enemies') renderEnemies(data, q, color, el);
    else if (S.tab === 'classroom') renderClassroom(data, q, el);
}

/* ── Personas ──────────────────────────────────────────────────────────────── */
function renderPersonas(data, q, color, el) {
    let items = Object.entries(data).filter(([name, p]) =>
        !q || name.toLowerCase().includes(q) || (p.arcana||'').toLowerCase().includes(q)
    );

    if (items.length === 0) { showEmpty('No personas found'); return; }

    let html = '';
    if (S.sort === 'arcana') {
        const grouped = {};
        items.forEach(([name, p]) => {
            const a = p.arcana || 'Unknown';
            if (!grouped[a]) grouped[a] = [];
            grouped[a].push([name, p]);
        });
        Object.keys(grouped).sort().forEach(arcana => {
            html += `<div class="arcana-header">
                <div class="arcana-bar" style="background:${color}"></div>
                <div class="arcana-label" style="color:${color}">${arcana}</div>
            </div>`;
            grouped[arcana].forEach(([name, p]) => { html += personaRow(name, p, color); });
        });
    } else {
        if (S.sort === 'level') items.sort((a,b) => (a[1].level||0)-(b[1].level||0));
        if (S.sort === 'name')  items.sort((a,b) => a[0].localeCompare(b[0]));
        items.forEach(([name, p]) => { html += personaRow(name, p, color); });
    }

    el.innerHTML = html;
}

function personaRow(name, p, color) {
    const skills = p.skills ? Object.keys(p.skills).length : 0;
    return `<div class="row-card" onclick="openPersona('${esc(name)}')">
        <div class="level-badge" style="background:${color}22;color:${color}">${p.level||'?'}</div>
        <div class="row-main">
            <div class="row-name">${name}</div>
            <div class="row-sub">${p.arcana||'Unknown'}</div>
        </div>
        ${skills ? `<div class="row-hint">${skills} skills</div>` : ''}
    </div>`;
}

/* ── Enemies ───────────────────────────────────────────────────────────────── */
function renderEnemies(data, q, color, el) {
    const all = Object.entries(data);
    const enemies    = all.filter(([,e]) => !e.isMiniBoss && !e.isBoss);
    const miniBosses = all.filter(([,e]) => e.isMiniBoss);
    const mainBosses = all.filter(([,e]) => e.isBoss);

    // Update tab counts
    ['enemies','mini_bosses','main_bosses'].forEach((t, i) => {
        const btn = document.getElementById('etab-'+t);
        if (btn) {
            const counts = [enemies.length, miniBosses.length, mainBosses.length];
            btn.textContent = `${t.replace('_',' ')} (${counts[i]})`;
        }
    });

    let pool = S.enemyTab === 'enemies' ? enemies : S.enemyTab === 'mini_bosses' ? miniBosses : mainBosses;
    if (q) pool = pool.filter(([name,e]) =>
        name.toLowerCase().includes(q) || (e.arcana||'').toLowerCase().includes(q) || (e.area||'').toLowerCase().includes(q)
    );

    if (pool.length === 0) { showEmpty('No enemies found'); return; }

    el.innerHTML = pool.map(([name, e]) => `
        <div class="row-card" onclick="openEnemy('${esc(name)}')">
            <div class="row-main">
                <div class="row-name">${name}</div>
                <div class="row-sub">${e.arcana||'Shadow'} · Lv. ${e.level||'?'}</div>
                ${e.area && e.area!=='Unknown' ? `<div class="row-hint" style="font-size:0.75rem;color:var(--text3);margin-top:2px">${e.area}</div>` : ''}
            </div>
            <div class="row-right">
                <div class="row-hp">${e.hp||''} HP</div>
                <div class="row-exp">${e.exp||''} EXP</div>
            </div>
        </div>`).join('');
}

/* ── Classroom ─────────────────────────────────────────────────────────────── */
function renderClassroom(data, q, el) {
    let items = Object.values(data);
    if (q) items = items.filter(qa =>
        (qa.Question||'').toLowerCase().includes(q) || (qa.Answer||'').toLowerCase().includes(q)
    );
    if (items.length === 0) { showEmpty('No answers found'); return; }
    el.innerHTML = items.map(qa => `
        <div class="qa-card">
            ${qa.Date ? `<div class="qa-date">${qa.Date}</div>` : ''}
            <div class="qa-question">${qa.Question || 'Question not available'}</div>
            <div class="qa-answer">${qa.Answer || '—'}</div>
        </div>`).join('');
}

/* ── Detail Screen ─────────────────────────────────────────────────────────── */
function openPersona(name) {
    const key = `personas_${S.game}`;
    const data = S.rawData[key];
    if (!data || !data[name]) return;
    S.detail = { type: 'persona', name, data: data[name] };
    navigate('detail');
}

function openEnemy(name) {
    const key = `enemies_${S.game}`;
    const data = S.rawData[key];
    if (!data || !data[name]) return;
    S.detail = { type: 'enemy', name, data: data[name] };
    navigate('detail');
}

function buildDetailScreen() {
    if (!S.detail) return;
    const series = SERIES.find(s => s.id === S.series);
    const color  = series?.color || '#2196F3';
    document.getElementById('detailTitle').textContent = S.detail.name;

    // Favorite button
    const favId = `${S.game}_${S.detail.name}`;
    const isFav = S.favorites.has(favId);
    document.getElementById('favBtn').textContent = isFav ? '♥' : '♡';
    document.getElementById('favBtn').style.color  = isFav ? color : '';

    if (S.detail.type === 'persona') renderPersonaDetail(S.detail.name, S.detail.data, color);
    else renderEnemyDetail(S.detail.name, S.detail.data, color);
}

function renderPersonaDetail(name, p, color) {
    const el = document.getElementById('detailContent');
    const stats = p.stats || [];
    const maxStat = stats.length ? Math.max(...stats, 1) : 1;
    const statLabels = ['STR','MAG','END','AGI','LUK'];

    let html = `
    <div class="detail-hero">
        <div class="detail-level-box" style="background:${color}22">
            <div class="detail-level-label" style="color:${color}">Lv.</div>
            <div class="detail-level-num" style="color:${color}">${p.level||'?'}</div>
        </div>
        <div class="detail-hero-info">
            <div class="detail-hero-name">${name}</div>
            <div class="detail-hero-arcana">${p.arcana||'Unknown'} Arcana</div>
            ${p.trait ? `<div class="detail-hero-trait" style="color:${color}">Trait: ${p.trait}</div>` : ''}
        </div>
    </div>`;

    if (p.description) html += `<div class="desc-box">${p.description}</div>`;

    if (p.unlock) html += `
    <div class="unlock-box">
        <div class="unlock-icon">🔒</div>
        <div>
            <div class="unlock-label">Unlock Requirement</div>
            <div class="unlock-text">${p.unlock}</div>
        </div>
    </div>`;

    if (stats.length >= 5) {
        html += `<div class="section-card"><div class="section-title">Base Stats</div>`;
        statLabels.forEach((lbl, i) => {
            const pct = Math.round((stats[i] / maxStat) * 100);
            html += `<div class="stat-row">
                <div class="stat-label">${lbl}</div>
                <div class="stat-bar-wrap"><div class="stat-bar-fill" style="width:${pct}%;background:${color}"></div></div>
                <div class="stat-val">${stats[i]}</div>
            </div>`;
        });
        html += `</div>`;
    }

    if (p.skills && Object.keys(p.skills).length) {
        html += `<div class="section-card"><div class="section-title">Skills</div>`;
        Object.entries(p.skills).forEach(([skill, lvl]) => {
            const label = lvl < 1 ? 'Innate' : lvl >= 100 ? 'Special' : `Lv. ${Math.floor(lvl)}`;
            const lcolor = lvl < 1 ? color : lvl >= 100 ? '#FFD700' : 'var(--text2)';
            html += `<div class="skill-row">
                <div class="skill-name">${skill}</div>
                <div class="skill-level" style="color:${lcolor}">${label}</div>
            </div>`;
        });
        html += `</div>`;
    }

    // Affinities
    const affinities = [
        { label:'Weak',    list: p.weaknesses,  color:'#E57373' },
        { label:'Resists', list: p.resistances, color:'#81C784' },
        { label:'Null',    list: p.nullifies,   color:'#B0BEC5' },
        { label:'Repel',   list: p.repels,      color:'#64B5F6' },
        { label:'Absorb',  list: p.absorbs,     color:'#FFD54F' },
    ].filter(a => a.list && a.list.length);

    if (affinities.length) {
        html += `<div class="section-card"><div class="section-title">Affinities</div>`;
        affinities.forEach(a => {
            html += `<div class="affinity-group">
                <div class="affinity-label">${a.label}</div>
                <div class="chips">${a.list.map(e => `<span class="chip" style="background:${a.color}22;color:${a.color}">${e}</span>`).join('')}</div>
            </div>`;
        });
        html += `</div>`;
    }

    el.innerHTML = html;
    el.scrollTop = 0;
}

function renderEnemyDetail(name, e, color) {
    const el = document.getElementById('detailContent');
    const seriesKey = S.series;
    const elems = ELEMENTS[seriesKey] || ELEMENTS.p5;

    let html = `
    <div class="section-card">
        <div style="display:flex;justify-content:space-between;align-items:flex-start">
            <div>
                <div style="font-size:1rem;color:var(--text2)">${e.arcana||'Shadow'}</div>
                <div style="font-size:1.125rem;color:var(--text);margin-top:2px">Level ${e.level||'?'}</div>
            </div>
            <div style="text-align:right">
                <div style="font-size:1.125rem;color:var(--text)">${e.hp||0} HP</div>
                <div style="font-size:0.875rem;color:var(--text2)">${e.sp||0} SP</div>
            </div>
        </div>
    </div>`;

    if (e.version) html += `<div class="desc-box">${e.version}</div>`;

    if (e.stats) {
        html += `<div class="section-card"><div class="section-title">Stats</div>`;
        [['Strength',e.stats.strength],['Magic',e.stats.magic],['Endurance',e.stats.endurance],
         ['Agility',e.stats.agility],['Luck',e.stats.luck]].forEach(([l,v]) => {
            html += `<div class="info-row"><div class="info-label">${l}</div><div class="info-val">${v}</div></div>`;
        });
        html += `</div>`;
    }

    if (e.resists) {
        html += `<div class="section-card"><div class="section-title">Resistances</div>
            <div class="resist-text">${parseResists(e.resists, elems)}</div></div>`;
    }

    if (e.skills && e.skills.length) {
        html += `<div class="section-card"><div class="section-title">Skills</div>`;
        e.skills.forEach(s => { html += `<div class="skill-row"><div class="skill-name">${s}</div></div>`; });
        html += `</div>`;
    }

    if (e.phases && e.phases.length) {
        e.phases.forEach(ph => {
            html += `<div class="section-card"><div class="section-title">Phase: ${ph.name}</div>
                <div class="info-row"><div class="info-label">HP</div><div class="info-val">${ph.hp}</div></div>
                <div class="info-row"><div class="info-label">SP</div><div class="info-val">${ph.sp}</div></div>`;
            if (ph.resists) html += `<div style="margin-top:10px"><div class="section-title" style="font-size:0.875rem">Resistances</div>
                <div class="resist-text">${parseResists(ph.resists, elems)}</div></div>`;
            if (ph.skills?.length) {
                html += `<div style="margin-top:10px"><div class="section-title" style="font-size:0.875rem">Skills</div>`;
                ph.skills.forEach(s => { html += `<div class="skill-row"><div class="skill-name">${s}</div></div>`; });
                html += `</div>`;
            }
            html += `</div>`;
        });
    }

    if (e.drops) {
        html += `<div class="section-card"><div class="section-title">Location & Drops</div>`;
        if (e.area && e.area !== 'Unknown') html += `<div class="info-row"><div class="info-label">Area</div><div class="info-val">${e.area}</div></div>`;
        if (e.exp > 0) html += `<div class="info-row"><div class="info-label">EXP</div><div class="info-val">${e.exp}</div></div>`;
        if (e.drops.gem !== '-') html += `<div class="info-row"><div class="info-label">Gem</div><div class="info-val">${e.drops.gem}</div></div>`;
        if (e.drops.item !== '-') html += `<div class="info-row"><div class="info-label">Item</div><div class="info-val">${e.drops.item}</div></div>`;
        html += `</div>`;
    }

    el.innerHTML = html;
    el.scrollTop = 0;
}

/* ── Helpers ───────────────────────────────────────────────────────────────── */
function parseResists(str, elems) {
    const map = { '-':'Normal', w:'Weak', s:'Strong', r:'Resist', n:'Null', d:'Drain' };
    return str.split('').map((c, i) => {
        const r = map[c] || 'Normal';
        if (r === 'Normal' || i >= elems.length) return null;
        const colors = { Weak:'#E57373', Null:'#B0BEC5', Drain:'#FFD54F', Resist:'#81C784', Strong:'#81C784' };
        return `<span class="chip" style="background:${colors[r]||'#555'}22;color:${colors[r]||'#aaa'}">${elems[i]}: ${r}</span>`;
    }).filter(Boolean).join(' ') || 'No special resistances';
}

function switchTab(tab) {
    S.tab = tab;
    S.query = '';
    S.sort = 'arcana';
    S.enemyTab = 'enemies';
    buildListScreen();
}

function setSort(opt, color) {
    S.sort = opt;
    buildListScreen();
}

function setEnemyTab(t) {
    S.enemyTab = t;
    buildListScreen();
}

function onSearch(val) {
    S.query = val;
    document.getElementById('searchClear').style.display = val ? 'block' : 'none';
    const series = SERIES.find(s => s.id === S.series);
    renderList(S.rawData[`${S.tab}_${S.game}`] || {}, series?.color || '#2196F3');
}

function clearSearch() {
    document.getElementById('searchInput').value = '';
    onSearch('');
}

function toggleFavorite() {
    if (!S.detail) return;
    const id = `${S.game}_${S.detail.name}`;
    if (S.favorites.has(id)) { S.favorites.delete(id); document.getElementById('favBtn').textContent = '♡'; document.getElementById('favBtn').style.color = ''; }
    else { S.favorites.add(id); const c = SERIES.find(s=>s.id===S.series)?.color||'#f00'; document.getElementById('favBtn').textContent = '♥'; document.getElementById('favBtn').style.color = c; }
    localStorage.setItem('favs', JSON.stringify([...S.favorites]));
}

function shareDetail() {
    if (!S.detail) return;
    const text = `${S.detail.name}\n${window.location.href}`;
    if (navigator.share) navigator.share({ title: S.detail.name, text });
    else navigator.clipboard?.writeText(text).then(() => alert('Copied to clipboard'));
}

function toggleFilter() { /* placeholder */ }

function showLoading() {
    document.getElementById('listContent').innerHTML = `<div class="loading-wrap"><div class="spinner"></div><div>Loading…</div></div>`;
}

function showEmpty(msg) {
    document.getElementById('listContent').innerHTML = `<div class="empty-state">${msg}</div>`;
}

function esc(s) { return s.replace(/'/g, "\\'"); }
