/* ── Series / Game Data ────────────────────────────────────────────────────── */
const SERIES = [
    { id:'p3', title:'Persona 3', color:'#1A6FCC', games:[
        { id:'p3fes', title:'Persona 3 FES' },
        { id:'p3p',   title:'Persona 3 Portable' },
        { id:'p3r',   title:'Persona 3 Reload' },
    ]},
    { id:'p4', title:'Persona 4', color:'#F5A800', games:[
        { id:'p4',  title:'Persona 4' },
        { id:'p4g', title:'Persona 4 Golden' },
    ]},
    { id:'p5', title:'Persona 5', color:'#CC1A1A', games:[
        { id:'p5',  title:'Persona 5' },
        { id:'p5r', title:'Persona 5 Royal' },
    ]},
];

const PERSONA_PATHS = {
    p3fes:'./data/persona3/personas.json', p3p:'./data/persona3/portable_personas.json',
    p3r:'./data/persona3/reload_personas.json', p4:'./data/persona4/personas.json',
    p4g:'./data/persona4/golden_personas.json', p5:'./data/persona5/personas.json',
    p5r:'./data/persona5/royal_personas.json'
};
const ENEMY_PATHS = {
    p3fes:'./data/enemies/p3fes_enemies.json', p3p:'./data/enemies/p3p_enemies.json',
    p3r:'./data/enemies/p3r_enemies.json', p4:'./data/enemies/p4_enemies.json',
    p4g:'./data/enemies/p4g_enemies.json', p5:'./data/enemies/p5_enemies.json',
    p5r:'./data/enemies/p5r_enemies.json'
};
const CLASSROOM_PATHS = {
    p3fes:'./data/classroom/p3_classroom_answers.json', p3p:'./data/classroom/p3_classroom_answers.json',
    p3r:'./data/classroom/p3_classroom_answers.json', p4:'./data/classroom/p4_classroom_answers.json',
    p4g:'./data/classroom/p4_classroom_answers.json', p5:'./data/classroom/p5_classroom_answers.json',
    p5r:'./data/classroom/p5_classroom_answers.json'
};
const SL_PATHS = {
    p3fes:'./data/social-links/p3fes_social_links.json',
    p3p:'./data/social-links/p3p_male_social_links.json',
    p3r:'./data/social-links/p3r_social_links.json',
    p4:'./data/social-links/p4+p4g_social_links.json',
    p4g:'./data/social-links/p4+p4g_social_links.json',
    p5:'./data/social-links/p5+p5r_social_links.json',
    p5r:'./data/social-links/p5+p5r_social_links.json'
};
const ITEM_PATHS = {
    p3fes:'./data/items/p3fes_items.json', p3p:'./data/items/p3p_items.json',
    p3r:'./data/items/p3r_items.json', p4:'./data/items/p4_items.json',
    p4g:'./data/items/p4g_items.json', p5:'./data/items/p5_items.json',
    p5r:'./data/items/p5r_items.json'
};
const SKILL_PATHS = {
    p3fes:'./data/skills/p3fes_skills.json', p3p:'./data/skills/p3p_skills.json',
    p3r:'./data/skills/p3r_skills.json', p4:'./data/skills/p4_skills.json',
    p4g:'./data/skills/p4g_skills.json', p5:'./data/skills/p5_skills.json',
    p5r:'./data/skills/p5r_skills.json'
};
const REQUEST_PATHS = {
    p3fes:'./data/requests/p3fes_requests.json', p3p:'./data/requests/p3p_requests.json',
    p3r:'./data/requests/p3r_requests.json', p4:'./data/requests/p4_requests.json',
    p4g:'./data/requests/p4g_requests.json', p5:'./data/requests/p5r_requests.json',
    p5r:'./data/requests/p5r_requests.json'
};
const ELEMENTS = {
    p3:['Slash','Strike','Pierce','Fire','Ice','Elec','Wind','Light','Dark','Almighty'],
    p4:['Phys','Fire','Ice','Elec','Wind','Light','Dark','Almighty'],
    p5:['Phys','Gun','Fire','Ice','Elec','Wind','Psy','Nuke','Bless','Curse']
};

const CAT_ICONS = {
    'Personas':         `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 14H8v-2h8v2zm0-4H8v-2h8v2zm0-4H8V6h8v2z"/></svg>`,
    'Fusion Calculator':`<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l7.59-7.59L21 8l-9 9z"/></svg>`,
    'Enemies':          `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/></svg>`,
    'Social Links':     `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>`,
    'Confidants':       `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>`,
    'Classroom Answers':`<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3L1 9l11 6 9-4.91V17h2V9L12 3z"/></svg>`,
    'Items':            `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M20 7h-9l-3 3h-5V5h2V3c0-1.1.9-2 2-2h6c1.1 0 2 .9 2 2v2h2v2zm-12-2h6V3H8v2zM3 10h18v10c0 1.1-.9 2-2 2H5c-1.1 0-2-.9-2-2V10zm2 2v6h14v-6H5z"/></svg>`,
    'Skills':           `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>`,
    'Requests & Quests': `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5H8V7h5v2z"/></svg>`
};

function normalizeListData(raw, type) {
    if (!raw) return [];
    if (Array.isArray(raw)) return raw;
    const ELEM_MAP = {
        'fir':'Fire', 'ice':'Ice', 'win':'Wind', 'ele':'Elec', 'ble':'Bless', 'cur':'Curse', 'alm':'Almighty', 'nuk':'Nuke', 'psy':'Psy', 'ail':'Ailment',
        'lig':'Light', 'dar':'Dark', 'phy':'Phys', 'gun':'Gun', 'rec':'Recovery', 'sup':'Support', 'pas':'Passive', 'sla':'Slash', 'pie':'Pierce', 'str':'Strike', 'spe':'Special'
    };
    if (type === 'skills' && typeof raw === 'object' && !raw.skills) {
        return Object.keys(raw).map(id => {
            const item = raw[id];
            const parts = item.a || [];
            const costs = item.b || [];
            const effects = item.c || [];
            return {
                name: parts[0] || 'Unknown',
                element: ELEM_MAP[parts[1]] || parts[1] || 'Other',
                target: parts[2] || '-',
                cost: costs[7] ? (costs[1] >= 1000 ? `${costs[7]} SP` : `${costs[7]}% HP`) : (costs[2] ? `${costs[2]} HP` : ''),
                effect: effects[0] || effects[1] || '',
                note: effects[2] || ''
            };
        });
    }
    if (raw.skills)   return Array.isArray(raw.skills) ? raw.skills : normalizeListData(raw.skills, 'skills');
    if (raw.items)    return raw.items;
    if (raw.requests) return raw.requests;
    if (raw.quests)   return raw.quests;
    return [];
}

const CHEVRON_SVG = `<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>`;

/* ── State ─────────────────────────────────────────────────────────────────── */
const S = {
    screen:'home', series:null, game:null,
    listMode:null, // 'personas'|'enemies'|'classroom'|'items'|'skills'|'requests'
    sort:'arcana', enemyTab:'enemies', query:'',
    enemySort:'level', enemySortDir:1, favOnly:false, hideCompletedReq:false,
    itemQuery:'', skillQuery:'', requestQuery:'',
    detail:null, favorites:new Set(), completedRequests:new Set(),
    rawData:{},
    slData:null, slQuery:'', slDetail:null,
    fusion:{
        personas:null, query:'', selected:null, recipes:null,
        mode:'reverse', forwardSubTab:'chamber', forwardSlots:[null, null, null],
        forwardSource:null, forwardQuery:'', activePickerSlot:null,
        skillRouteTarget:null, skillRouteSkill:null, skillRouteSkills:[], skillsList:null,
        personaMap:{}, chart:null, byArcana:{}, specialData:{}, fissionTable:{}
    },
    settings:{ showDlc:true, showEpisodeAigis:true, p3pProtagonist:'MALE' }
};

/* ── Boot ──────────────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    S.favorites = new Set(JSON.parse(localStorage.getItem('favs')||'[]'));
    S.completedRequests = new Set(JSON.parse(localStorage.getItem('completed_requests')||'[]'));
    const saved = localStorage.getItem('settings');
    if (saved) S.settings = {...S.settings, ...JSON.parse(saved)};
    // Restore location: URL hash wins, then last visited, else home
    const h = location.hash && location.hash !== '#' ? location.hash : (localStorage.getItem('last_loc') || '');
    if (h && h.replace('#','')) applyHash(h); else buildHome();
    window.addEventListener('hashchange', () => {
        // Ignore echoes of our own hash writes — only react when the hash differs from current state
        if (location.hash.replace(/^#/,'') !== currentHash()) applyHash(location.hash);
    });
    initKeyboardShortcuts();
    initBackToTop();
});

/* ── Deep links (#series/game/section) ─────────────────────────────────────── */
function currentHash() {
    if (!S.series) return '';
    if (S.screen === 'game' || !S.game) return S.series;
    let part = '';
    if (S.screen === 'list' || S.screen === 'detail') part = S.listMode;
    else if (S.screen === 'sldetail') part = 'sociallinks';
    else if (['items','skills','requests','fusion','sociallinks'].includes(S.screen)) part = S.screen;
    return S.series + '/' + S.game + (part ? '/' + part : '');
}

function applyHash(h) {
    window._applyingHash = true;
    try {
        const parts = (h||'').replace(/^#/,'').split('/').filter(Boolean);
        if (!parts.length) { navigate('home'); return; }
        const series = SERIES.find(s => s.id === parts[0]);
        if (!series) { navigate('home'); return; }
        S.series = series.id;
        const game = series.games.find(g => g.id === parts[1]);
        if (!game) { navigate('game', S.series); return; }
        S.game = game.id;
        const sec = parts[2];
        if (!sec) { navigate('category'); return; }
        if (['personas','enemies','classroom'].includes(sec)) { S.listMode = sec; navigate('list'); }
        else if (sec === 'items')    { S.listMode = 'items';    navigate('items'); }
        else if (sec === 'skills')   { S.listMode = 'skills';   navigate('skills'); }
        else if (sec === 'requests') { S.listMode = 'requests'; navigate('requests'); }
        else if (sec === 'fusion')   { openFusion(); }
        else if (sec === 'sociallinks') { openSocialLinks(); }
        else navigate('category');
    } finally { window._applyingHash = false; }
}

function syncHash() {
    const h = currentHash();
    localStorage.setItem('last_loc', h);
    if (window._applyingHash) return;
    if (location.hash.replace(/^#/,'') !== h) {
        location.hash = h;  // the hashchange listener ignores writes matching currentHash()
    }
}

/* ── Keyboard shortcuts ────────────────────────────────────────────────────── */
function initKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        const ae = document.activeElement;
        const inInput = ae && (ae.tagName === 'INPUT' || ae.tagName === 'TEXTAREA');
        if (e.key === '/' && !inInput) {
            const input = document.querySelector('.screen.active .search-input');
            if (input) { e.preventDefault(); input.focus(); input.select(); }
        } else if (e.key === 'Escape' && inInput && ae.classList.contains('search-input')) {
            ae.value = '';
            ae.dispatchEvent(new Event('input'));
            ae.blur();
        }
    });
}

/* ── Debounced search ──────────────────────────────────────────────────────── */
let _searchTimer = null;
function debounceSearch(fn) { clearTimeout(_searchTimer); _searchTimer = setTimeout(fn, 150); }

/* ── Back to top ───────────────────────────────────────────────────────────── */
function initBackToTop() {
    const btn = document.getElementById('backToTop');
    if (!btn) return;
    document.addEventListener('scroll', (e) => {
        const el = e.target;
        if (!el || !el.classList || !(el.classList.contains('list-content') || el.classList.contains('detail-content'))) return;
        window._scrollEl = el;
        btn.style.display = el.scrollTop > 400 ? 'flex' : 'none';
    }, true);
    btn.addEventListener('click', () => {
        if (window._scrollEl) window._scrollEl.scrollTo({ top: 0, behavior: 'smooth' });
        btn.style.display = 'none';
    });
}

/* ── Navigation ────────────────────────────────────────────────────────────── */
function isTablet() { return window.innerWidth >= 840; }

function navigate(to, payload) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById('screen-'+to).classList.add('active');
    S.screen = to;
    if (to==='home')        buildHome();
    if (to==='game')        buildGameScreen(payload);
    if (to==='category')    buildCategoryScreen();
    if (to==='list')        buildListScreen(payload);
    if (to==='detail')      buildDetailScreen();
    if (to==='sociallinks') buildSocialLinksScreen();
    if (to==='sldetail')    buildSlDetailScreen();
    if (to==='fusion')      buildFusionScreen();
    if (to==='items')       buildItemsScreen();
    if (to==='skills')      buildSkillsScreen();
    if (to==='requests')    buildRequestsScreen();
    if (to==='settings')    buildSettingsScreen();
    updateRailState(to);
    syncHash();
}

/* ── Rail Navigation ───────────────────────────────────────────────────────── */
function railNav(section) {
    if (!S.game) return;
    if (section === 'personas')       { S.listMode='personas';  navigate('list'); }
    else if (section === 'fusion')    { openFusion(); }
    else if (section === 'enemies')   { S.listMode='enemies';   navigate('list'); }
    else if (section === 'sl')        { openSocialLinks(); }
    else if (section === 'classroom') { S.listMode='classroom'; navigate('list'); }
    else if (section === 'items')     { S.listMode='items';     navigate('items'); }
    else if (section === 'skills')    { S.listMode='skills';    navigate('skills'); }
    else if (section === 'requests')  { S.listMode='requests';  navigate('requests'); }
}

function updateRailState(screenName) {
    // Show/hide game-specific rail items
    const gameItems = ['rail-personas','rail-fusion','rail-enemies','rail-sl','rail-class','rail-items','rail-skills','rail-requests'];
    gameItems.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = S.game ? 'flex' : 'none';
    });
    // Map screen → rail item id
    const screenToRail = {
        home: 'rail-home', game: 'rail-home',
        list: S.listMode==='personas'?'rail-personas':S.listMode==='enemies'?'rail-enemies':S.listMode==='classroom'?'rail-class':null,
        items: 'rail-items', skills: 'rail-skills', requests: 'rail-requests',
        detail: S.listMode==='personas'?'rail-personas':S.listMode==='enemies'?'rail-enemies':S.listMode==='items'?'rail-items':S.listMode==='skills'?'rail-skills':null,
        fusion: 'rail-fusion', sociallinks: 'rail-sl', sldetail: 'rail-sl',
    };
    document.querySelectorAll('.rail-item').forEach(el => el.classList.remove('active'));
    const activeId = screenToRail[screenName];
    if (activeId) document.getElementById(activeId)?.classList.add('active');
}

/* ── Home ──────────────────────────────────────────────────────────────────── */
const GAME_SHORT = { p3fes:'FES', p3p:'Portable', p3r:'Reload', p4:'Original', p4g:'Golden', p5:'Original', p5r:'Royal' };
const SECTION_LABELS = { personas:'Personas', enemies:'Enemies', classroom:'Classroom', items:'Items', skills:'Skills', requests:'Requests', fusion:'Fusion', sociallinks:'Social Links' };

function buildHome() {
    // Continue card — jump straight back to the last visited game/section
    let continueHtml = '';
    const last = (localStorage.getItem('last_loc') || '').split('/').filter(Boolean);
    if (last.length >= 2) {
        const cs = SERIES.find(s => s.id === last[0]);
        const cg = cs && cs.games.find(g => g.id === last[1]);
        if (cs && cg) {
            const secLabel = SECTION_LABELS[last[2]] || 'Overview';
            continueHtml = `
            <div class="continue-card" onclick="applyHash('${last.join('/')}')">
                <div class="continue-kicker">Continue</div>
                <div class="continue-main">${cg.title} · ${secLabel}</div>
                <div class="series-card-arrow">›</div>
            </div>`;
        }
    }
    document.getElementById('continueSlot').innerHTML = continueHtml;

    const SERIES_LOGOS = { p3:'assets/p3r_logo.png', p4:'assets/p4g_logo.png', p5:'assets/p5r_logo.png' };
    document.getElementById('seriesList').innerHTML = SERIES.map(s => `
        <div class="series-card series-card--${s.id}" style="background:linear-gradient(135deg,${s.color}dd,${s.color}88)"
             onclick="navigate('game','${s.id}')">
            ${s.id==='p5' ? '<div class="series-card-star">★</div>' : ''}
            <div class="series-card-bg-num">${s.id.replace('p','')}</div>
            <div class="series-card-text">
                <img class="series-card-logo" src="${SERIES_LOGOS[s.id]}" alt="${s.title}"
                     onerror="this.outerHTML='<div class=&quot;series-card-title&quot;>${s.title}</div>'">
                <div class="series-card-sub">${s.games.length} game${s.games.length>1?'s':''}</div>
                <div class="series-card-games">
                    ${s.games.map(g => `<button class="series-game-chip" onclick="event.stopPropagation(); selectGame('${s.id}','${g.id}')">${GAME_SHORT[g.id]||g.title}</button>`).join('')}
                </div>
            </div>
            <div class="series-card-arrow">›</div>
        </div>`).join('');
}

/* ── Game Selection ────────────────────────────────────────────────────────── */
function buildGameScreen(seriesId) {
    if (seriesId) S.series = seriesId;
    const series = SERIES.find(s=>s.id===S.series);
    if (!series) return;
    document.getElementById('gameScreenTitle').textContent = series.title;
    document.getElementById('gameList').innerHTML = series.games.map(g => `
        <button class="game-btn" onclick="selectGame('${S.series}','${g.id}')">
            <span class="game-btn-dot" style="background:${series.color}"></span>
            ${g.title}
            <span class="game-btn-arrow">›</span>
        </button>`).join('');
}

function selectGame(seriesId, gameId) {
    S.series = seriesId; S.game = gameId;
    S.query=''; S.sort='arcana'; S.enemyTab='enemies';
    S.enemySort='level'; S.enemySortDir=1; S.favOnly=false; S.hideCompletedReq=false;
    S.fusion = { personas:null, query:'', selected:null, recipes:null, mode:'reverse', forwardSubTab:'chamber', forwardSlots:[null,null,null], forwardSource:null, forwardQuery:'', activePickerSlot:null, skillRouteTarget:null, skillRouteSkill:null, skillRouteSkills:[], skillsList:null }; // reset on game change
    navigate('category');
}



/* ── Category Screen (mirrors Android CategoryScreen) ──────────────────────── */
function buildCategoryScreen() {
    const series = SERIES.find(s=>s.id===S.series);
    const game   = series?.games.find(g=>g.id===S.game);
    if (!series||!game) return;
    document.getElementById('categoryTitle').textContent = game.title;
    const color = series.color;
    const isP5 = S.series==='p5';
    const slLabel = isP5 ? 'Confidants' : 'Social Links';


    const categories = [
        { label:'Personas',         available:true, action:()=>{ S.listMode='personas'; navigate('list'); } },
        { label:'Fusion Calculator',available:true, action:()=>openFusion() },
        { label:'Enemies',          available:true, action:()=>{ S.listMode='enemies';  navigate('list'); } },
        { label:slLabel,            available:true, action:()=>openSocialLinks() },
        { label:'Classroom Answers',available:true, action:()=>{ S.listMode='classroom'; navigate('list'); } },
        { label:'Items',            available:true, action:()=>{ S.listMode='items';     navigate('items'); } },
        { label:'Skills',           available:true, action:()=>{ S.listMode='skills';    navigate('skills'); } },
        { label:'Requests & Quests',available:true, action:()=>{ S.listMode='requests';  navigate('requests'); } },
    ];

    document.getElementById('categoryList').innerHTML = categories.map(c => `
        <div class="category-row ${c.available?'':'category-row--locked'}"
             onclick="${c.available&&c.action ? 'categoryAction(\''+c.label+'\')' : ''}">
            <span class="category-icon">${CAT_ICONS[c.label]||''}</span>
            <span class="category-label" style="${c.available?'color:var(--text)':'color:var(--text3)'}">${c.label}</span>
            ${c.available
                ? `<span class="category-chevron" style="color:${color}">${CHEVRON_SVG}</span>`
                : `<span class="category-soon">Soon</span>`}
        </div>`).join('');

    // Store actions by label for onclick
    window._catActions = {};
    categories.forEach(c => { if (c.action) window._catActions[c.label] = c.action; });
}

function categoryAction(label) {
    if (window._catActions && window._catActions[label]) window._catActions[label]();
}

function openSocialLinks() {
    // P3P: respect protagonist setting
    if (S.game==='p3p') {
        SL_PATHS['p3p'] = S.settings.p3pProtagonist==='FEMC'
            ? './data/social-links/p3p_femc_social_links.json'
            : './data/social-links/p3p_male_social_links.json';
    }
    // P5/P5R shared file, filter by game inside
    S.slData = null; S.slQuery = '';
    navigate('sociallinks');
}

function openFusion() {
    navigate('fusion');
}

/* ── List Screen ───────────────────────────────────────────────────────────── */
function buildListScreen(mode) {
    if (mode) S.listMode = mode;
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    const titles = { personas:'Personas', enemies:'Enemies', classroom:'Classroom Answers' };
    document.getElementById('listScreenTitle').textContent = titles[S.listMode]||'';

    // Sort bar (personas and enemies)
    const sortBar = document.getElementById('sortBar');
    const favChip = `<button class="sort-chip fav-chip ${S.favOnly?'active':''}"
            style="flex:0 0 auto;${S.favOnly?`color:${color};background:${color}22`:''}"
            onclick="toggleFavOnly()" title="Favorites only">&#x2665;</button>`;
    if (S.listMode==='personas') {
        sortBar.style.display='flex';
        sortBar.innerHTML = ['arcana','level','name'].map(opt=>`
            <button class="sort-chip ${S.sort===opt?'active':''}"
                    style="${S.sort===opt?`color:${color};background:${color}22`:''}"
                    onclick="setSort('${opt}','${color}')">
                ${opt[0].toUpperCase()+opt.slice(1)}
            </button>`).join('') + favChip;
    } else if (S.listMode==='enemies') {
        sortBar.style.display='flex';
        sortBar.innerHTML = [['level','Level'],['name','Name'],['hp','HP']].map(([opt,label])=>`
            <button class="sort-chip ${S.enemySort===opt?'active':''}"
                    style="${S.enemySort===opt?`color:${color};background:${color}22`:''}"
                    onclick="setEnemySort('${opt}')">
                ${label} ${S.enemySort===opt?(S.enemySortDir===1?'&#x25B2;':'&#x25BC;'):''}
            </button>`).join('') + favChip;
    } else { sortBar.style.display='none'; }

    // Tab bar (enemies only)
    const tabBar = document.getElementById('tabBar');
    if (S.listMode==='enemies') {
        tabBar.style.display='flex';
        tabBar.innerHTML = ['enemies','mini_bosses','main_bosses'].map(t=>`
            <button class="tab-item ${S.enemyTab===t?'active':''}" id="etab-${t}"
                    onclick="setEnemyTab('${t}')">${t.replace('_',' ')}</button>`).join('');
    } else { tabBar.style.display='none'; }

    document.getElementById('searchInput').value = S.query;
    document.getElementById('searchClear').style.display = S.query?'block':'none';
    loadAndRender(color);
}

async function loadAndRender(color) {
    const key = `${S.listMode}_${S.game}`;
    if (!S.rawData[key]) {
        showLoading();
        try {
            const paths = { 
                personas: PERSONA_PATHS, 
                enemies: ENEMY_PATHS, 
                classroom: CLASSROOM_PATHS,
                items: ITEM_PATHS,
                skills: SKILL_PATHS,
                requests: REQUEST_PATHS
            };
            const path = paths[S.listMode]?.[S.game];
            if (!path) { showEmpty('No data available'); return; }
            const r = await fetch(path);
            if (!r.ok) throw new Error(r.statusText);
            S.rawData[key] = await r.json();
        } catch(e) { showEmpty('Failed to load: '+e.message); return; }
    }
    renderList(S.rawData[key], color);
}

function renderList(data, color) {
    const q = S.query.toLowerCase();
    const el = document.getElementById('listContent');
    if (S.listMode==='personas')  renderPersonas(data, q, color, el);
    else if (S.listMode==='enemies')   renderEnemies(data, q, color, el);
    else if (S.listMode==='classroom') renderClassroom(data, q, el);
}

/* ── Personas ──────────────────────────────────────────────────────────────── */
function renderPersonas(data, q, color, el) {
    const DLC_NAMES = {
        p3r: new Set(['Arsene','Captain Kidd','Zorro','Carmen','Goemon','Johanna','Milady',
                      'Robin Hood','Cendrillon','Satanael','Seiten Taisei A','Mercurius',
                      'Hecate','Kamu Susano-o','Anat','Astarte','Loki A','Vanadis',
                      'Izanagi','Magatsu-Izanagi','Kaguya']),
        p5:  new Set(['Izanagi','Izanagi Picaro','Orpheus','Orpheus Picaro','Ariadne',
                      'Ariadne Picaro','Asterius','Asterius Picaro','Thanatos','Thanatos Picaro',
                      'Magatsu-Izanagi','Magatsu-Izanagi Picaro','Kaguya','Kaguya Picaro',
                      'Tsukiyomi','Tsukiyomi Picaro','Messiah','Messiah Picaro']),
        p5r: new Set(['Orpheus F','Orpheus F Picaro','Izanagi','Izanagi Picaro','Orpheus',
                      'Orpheus Picaro','Raoul','Athena','Athena Picaro','Ariadne','Ariadne Picaro',
                      'Asterius','Asterius Picaro','Thanatos','Thanatos Picaro',
                      'Magatsu-Izanagi','Magatsu-Izanagi Picaro','Kaguya','Kaguya Picaro',
                      'Tsukiyomi','Tsukiyomi Picaro','Messiah','Messiah Picaro',
                      'Izanagi-no-Okami','Izanagi-no-Okami Picaro'])
    };
    const gameDlc = DLC_NAMES[S.game] || new Set();
    const allEntries = Object.entries(data).filter(([name, p]) => {
        const isDlc = p.isDlc || gameDlc.has(name);
        if (!S.settings.showDlc && isDlc) return false;
        if (!S.settings.showEpisodeAigis && p.episodeAigis) return false;
        return true;
    });
    const total = allEntries.length;
    let items = allEntries.filter(([name, p]) =>
        !q || name.toLowerCase().includes(q) || (p.arcana||p.race||'').toLowerCase().includes(q));
    if (S.favOnly) items = items.filter(([name])=>S.favorites.has(`${S.game}_${name}`));
    if (!items.length) { showEmpty(S.favOnly ? 'No favorites yet — open a persona and tap the heart' : 'No personas found'); return; }

    let html = (q || S.favOnly) ? `<div class="result-count">${items.length} of ${total} shown</div>` : '';
    if (S.sort==='arcana') {
        const grouped = {};
        items.forEach(([name,p]) => {
            const a = p.arcana||p.race||'Unknown';
            (grouped[a]||(grouped[a]=[])).push([name,p]);
        });
        Object.keys(grouped).sort().forEach(arcana => {
            html += `<div class="arcana-header">
                <div class="arcana-bar" style="background:${color}"></div>
                <div class="arcana-label" style="color:${color}">${arcana}</div>
            </div>`;
            grouped[arcana].forEach(([name,p]) => { html += personaRow(name,p,color); });
        });
    } else {
        if (S.sort==='level') items.sort((a,b)=>((a[1].level??a[1].lvl??0)-(b[1].level??b[1].lvl??0)));
        if (S.sort==='name')  items.sort((a,b)=>a[0].localeCompare(b[0]));
        items.forEach(([name,p]) => { html += personaRow(name,p,color); });
    }
    el.innerHTML = html;
}

function personaRow(name, p, color) {
    const level = p.level??p.lvl??'?';
    const arcana = p.arcana||p.race||'Unknown';
    const skills = p.skills ? Object.keys(p.skills).length : 0;
    const weakRow = renderWeaknessRow(p, S.game);
    const isFav = S.favorites.has(`${S.game}_${name}`);
    return `<div class="row-card" onclick="openPersona('${esc(name)}')">
        <div class="level-badge" style="background:${color}22;color:${color}">${level}</div>
        <div class="row-main">
            <div class="row-name">${name}${isFav?` <span class="fav-mark" style="color:${color}">&#x2665;</span>`:''}</div>
            <div class="row-sub">${arcana}</div>
            ${weakRow}
        </div>
        ${skills?`<div class="row-hint">${skills} skills</div>`:''}
    </div>`;
}

function renderWeaknessRow(p, gameId) {
    const aff = parsePersonaAffinities(p, gameId);
    const weak = aff.weak || [];
    const res = aff.resist || [];
    if (!weak.length && !res.length) return '';
    let html = '<div class="weakness-row">';
    weak.slice(0, 4).forEach(e => html += `<span class="weak-label">${e.slice(0,2)}</span>`);
    res.slice(0, 2).forEach(e => html += `<span class="resist-label">${e.slice(0,2)}</span>`);
    html += '</div>';
    return html;
}

/* ── Enemies ───────────────────────────────────────────────────────────────── */
function renderEnemies(data, q, color, el) {
    const all = Array.isArray(data) ? data.map(e=>[e.name,e]) : Object.entries(data);
    const enemies    = all.filter(([,e])=>!e.isMiniBoss&&!e.isBoss);
    const miniBosses = all.filter(([,e])=>e.isMiniBoss);
    const mainBosses = all.filter(([,e])=>e.isBoss);
    ['enemies','mini_bosses','main_bosses'].forEach((t,i)=>{
        const btn = document.getElementById('etab-'+t);
        if (btn) btn.textContent = `${t.replace('_',' ')} (${[enemies,miniBosses,mainBosses][i].length})`;
    });
    let pool = S.enemyTab==='enemies'?enemies:S.enemyTab==='mini_bosses'?miniBosses:mainBosses;
    const total = pool.length;
    if (q) pool = pool.filter(([name,e])=>name.toLowerCase().includes(q)||(e.arcana||'').toLowerCase().includes(q)||(e.area||'').toLowerCase().includes(q));
    if (S.favOnly) pool = pool.filter(([name])=>S.favorites.has(`${S.game}_${name}`));
    if (!pool.length) { showEmpty(S.favOnly ? 'No favorites yet — open an enemy and tap the heart' : 'No enemies found'); return; }
    const dir = S.enemySortDir;
    pool = pool.slice().sort((a,b)=>{
        if (S.enemySort==='name') return a[0].localeCompare(b[0])*dir;
        if (S.enemySort==='hp')   return ((a[1].hp||0)-(b[1].hp||0))*dir;
        return ((a[1].level||0)-(b[1].level||0))*dir;
    });
    const countLine = (q || S.favOnly) ? `<div class="result-count">${pool.length} of ${total} shown</div>` : '';
    el.innerHTML = countLine + pool.map(([name,e])=>{
        const elems = ELEMENTS[S.series]||ELEMENTS.p5;
        const resists = e.resists ? parseResistSummary(e.resists, elems) : '';
        const isFav = S.favorites.has(`${S.game}_${name}`);
        return `
        <div class="row-card" onclick="openEnemy('${esc(name)}')">
            <div class="row-main">
                <div class="row-name">${name}${isFav?` <span class="fav-mark" style="color:${color}">&#x2665;</span>`:''}</div>
                <div class="row-sub">${e.arcana||'Shadow'} · Lv. ${e.level||'?'}</div>
                ${resists}
                ${e.area&&e.area!=='Unknown'?`<div class="row-hint" style="font-size:.75rem;color:var(--text3);margin-top:2px">${e.area}</div>`:''}
            </div>
            <div class="row-right">
                <div class="row-hp">${e.hp||''} HP</div>
                <div class="row-exp">${e.exp||''} EXP</div>
            </div>
        </div>`}).join('');
}

function parseResistSummary(str, elems) {
    const weak = [], res = [], nul = [], rep = [], abs = [];
    str.split('').forEach((c,i)=>{
        if(i>=elems.length) return;
        if(c==='R'){ rep.push(elems[i]); return; }  // 'R' = Repel, 'r' = Resist
        const lc=c.toLowerCase();
        if(lc==='w') weak.push(elems[i]);
        else if(lc==='s'||lc==='r') res.push(elems[i]);
        else if(lc==='n'||c==='_') nul.push(elems[i]);
        else if(lc==='p') rep.push(elems[i]);
        else if(lc==='d'||lc==='a') abs.push(elems[i]);
    });
    if(!weak.length && !res.length && !nul.length && !rep.length && !abs.length) return '';
    let html = '<div class="weakness-row">';
    weak.slice(0,4).forEach(e => html += `<span class="weak-label">${e.slice(0,2)}</span>`);
    res.slice(0,2).forEach(e => html += `<span class="resist-label">${e.slice(0,2)}</span>`);
    nul.slice(0,2).forEach(e => html += `<span class="null-label">${e.slice(0,2)}</span>`);
    rep.slice(0,2).forEach(e => html += `<span class="repel-label">${e.slice(0,2)}</span>`);
    abs.slice(0,2).forEach(e => html += `<span class="drain-label">${e.slice(0,2)}</span>`);
    html += '</div>';
    return html;
}

/* ── Classroom ─────────────────────────────────────────────────────────────── */
function flattenClassroom(data) {
    if (Array.isArray(data)) return data;
    let items = [];
    
    // Extract everything first
    Object.entries(data).forEach(([month, monthData]) => {
        if (typeof monthData !== 'object') return;
        
        const types = monthData.Classroom || monthData.Exam ? ['Classroom', 'Exam'] : ['_root_'];
        
        types.forEach(type => {
            const section = type === '_root_' ? monthData : monthData[type];
            if (!section) return;
            
            Object.entries(section).forEach(([date, qas]) => {
                if (Array.isArray(qas)) {
                    qas.forEach(qa => items.push({ Date: date, Kind: type === '_root_' ? '' : type, ...qa }));
                }
            });
        });
    });

    // Sort chronologically: April-Dec is Year 1, Jan-Mar is Year 2
    items.sort((a, b) => {
        if (!a.Date || !b.Date) return 0;
        const [m1, d1] = a.Date.split('/').map(Number);
        const [m2, d2] = b.Date.split('/').map(Number);
        
        const mon1 = (m1 < 4) ? m1 + 12 : m1;
        const mon2 = (m2 < 4) ? m2 + 12 : m2;
        
        if (mon1 !== mon2) return mon1 - mon2;
        return d1 - d2;
    });

    return items;
}

function renderClassroom(data, q, el) {
    const MONTH_NAMES = ['','January','February','March','April','May','June','July','August','September','October','November','December'];
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    const all = flattenClassroom(data);
    let items = all;
    if (q) items = items.filter(qa=>(qa.Question||'').toLowerCase().includes(q)||(qa.Answer||'').toLowerCase().includes(q));
    if (!items.length) { showEmpty('No answers found'); return; }
    let html = q ? `<div class="result-count">${items.length} of ${all.length} shown</div>` : '';
    let lastMonth = null;
    items.forEach(qa=>{
        const month = qa.Date ? parseInt(qa.Date.split('/')[0], 10) : null;
        if (month && month !== lastMonth) {
            lastMonth = month;
            html += `<div class="arcana-header">
                <div class="arcana-bar" style="background:${color}"></div>
                <div class="arcana-label" style="color:${color}">${MONTH_NAMES[month]||('Month '+month)}</div>
            </div>`;
        }
        const isExam = (qa.Kind||'').toLowerCase() === 'exam';
        html += `
        <div class="qa-card ${isExam?'qa-card--exam':''}">
            <div style="display:flex;align-items:center;gap:8px">
                ${qa.Date?`<div class="qa-date">${qa.Date}</div>`:''}
                ${isExam?`<span class="exam-badge">EXAM</span>`:''}
            </div>
            <div class="qa-question">${qa.Question||'Question not available'}</div>
            <div class="qa-answer">${qa.Answer||'—'}</div>
        </div>`;
    });
    el.innerHTML = html;
}

/* ── Social Links ──────────────────────────────────────────────────────────── */
async function buildSocialLinksScreen() {
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    const isP5   = S.series==='p5';
    document.getElementById('slTitle').textContent = isP5 ? 'Confidants' : 'Social Links';

    if (!S.slData) {
        document.getElementById('slContent').innerHTML = `<div class="loading-wrap"><div class="spinner"></div><div>Loading…</div></div>`;
        try {
            const path = SL_PATHS[S.game];
            if (!path) { document.getElementById('slContent').innerHTML=`<div class="empty-state">Not available</div>`; return; }
            const r = await fetch(path);
            if (!r.ok) throw new Error(r.statusText);
            const raw = await r.json();
            // Filter P5/P5R and P4/P4G exclusives
            S.slData = Object.entries(raw).filter(([arcana, data]) => {
                if (data['P5R Exclusive'] && S.game==='p5') return false;
                if (data['P4G Exclusive'] && S.game==='p4') return false;
                return true;
            });
        } catch(e) {
            document.getElementById('slContent').innerHTML=`<div class="empty-state">Failed to load: ${e.message}</div>`;
            return;
        }
    }
    renderSlList(color);
}

function renderSlList(color) {
    const q = S.slQuery.toLowerCase();
    let items = S.slData||[];
    if (q) items = items.filter(([arcana])=>arcana.toLowerCase().includes(q));
    if (!items.length) { document.getElementById('slContent').innerHTML=`<div class="empty-state">No results</div>`; return; }
    document.getElementById('slContent').innerHTML = items.map(([arcana, data]) => {
        const rankCount = countRanks(data);
        return `<div class="row-card" onclick="openSlDetail('${esc(arcana)}')">
            <div class="row-main">
                <div class="row-name">${arcana}</div>
                <div class="row-sub">${rankCount} ranks</div>
            </div>
            <div class="level-badge" style="background:${color}22;color:${color}">Rank ${rankCount}</div>
        </div>`;
    }).join('');
}

/* Count total ranks across both auto and progression formats */
function countRanks(data) {
    let count = 0;
    Object.keys(data).forEach(key => {
        if (['P4G Exclusive','P5R Exclusive','P5R Reworked','Details'].includes(key)) return;
        if (key === 'Rank Up Progression') {
            count += Object.keys(data[key]).length;
        } else if (typeof data[key] === 'object') {
            count++;
        }
    });
    return count;
}

function openSlDetail(arcana) {
    S.slDetail = arcana;
    if (isTablet()) {
        showSlDetailPane();
    } else {
        navigate('sldetail');
    }
}

function showSlDetailPane() {
    if (!S.slDetail||!S.slData) return;
    document.getElementById('slDetailPlaceholder').style.display = 'none';
    const paneContent = document.getElementById('slDetailPaneContent');
    paneContent.style.display = 'flex';
    document.getElementById('slDetailTitle').textContent = S.slDetail;
    buildSlDetailScreen(); // writes to #slDetailContent
}

function renderSlDetailInPane() {} // no-op, kept for compat

function buildSlDetailScreen() {
    if (!S.slDetail||!S.slData) return;
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    const entry  = S.slData.find(([a])=>a===S.slDetail);
    if (!entry) return;
    const [arcana, data] = entry;

    const phoneEl = document.getElementById('slDetailContentPhone');
    const paneEl  = document.getElementById('slDetailContent');
    if (phoneEl) document.getElementById('slDetailTitlePhone').textContent = arcana;

    let html = '';

    // ── Details block (Schedule / Location / Trigger) ──────────────────────
    if (data.Details && typeof data.Details === 'object') {
        html += `<div class="section-card">`;
        Object.entries(data.Details).forEach(([k, v]) => {
            html += `<div class="info-row"><div class="info-label">${k}</div><div class="info-val" style="text-align:right;max-width:60%">${v}</div></div>`;
        });
        html += `</div>`;
    }

    // ── Auto ranks at top level ─────────────────────────────────────────────
    Object.entries(data).forEach(([rankKey, rankVal]) => {
        if (['P4G Exclusive','P5R Exclusive','P5R Reworked','Details','Rank Up Progression'].includes(rankKey)) return;
        if (typeof rankVal !== 'object') return;
        const isAuto = rankKey.toLowerCase().includes('auto');
        html += `<div class="section-card">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
                <div class="section-title" style="color:${color};margin-bottom:0">${rankKey}</div>
                ${isAuto ? `<span style="font-size:.7rem;font-weight:700;color:#4CAF50;background:rgba(76,175,80,.15);padding:3px 8px;border-radius:6px">AUTO</span>` : ''}
            </div>`;
        if (rankVal.Requirements) {
            html += `<div class="info-row"><div class="info-label">Requirements</div><div class="info-val" style="text-align:right;max-width:65%">${rankVal.Requirements}</div></div>`;
        }
        html += `</div>`;
    });

    // ── Rank Up Progression ─────────────────────────────────────────────────
    if (data['Rank Up Progression'] && typeof data['Rank Up Progression'] === 'object') {
        Object.entries(data['Rank Up Progression']).forEach(([rankKey, rankVal]) => {
            if (typeof rankVal !== 'object') return;
            html += `<div class="section-card">
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
                    <div class="section-title" style="color:${color};margin-bottom:0">${rankKey}</div>
                    ${rankVal['Next Pts'] > 0 ? `<span style="font-size:.75rem;color:var(--text2)">${rankVal['Next Pts']} pts needed</span>` : ''}
                </div>`;

            if (rankVal.Requirements) {
                html += `<div class="info-row" style="margin-bottom:8px"><div class="info-label">Requirements</div><div class="info-val" style="text-align:right;max-width:65%">${rankVal.Requirements}</div></div>`;
            }

            // Dialogues
            if (Array.isArray(rankVal.Dialogues)) {
                rankVal.Dialogues.forEach(dialogue => {
                    const isPhone = (dialogue.Question||'').toLowerCase().includes('phone');
                    if (dialogue.Question && dialogue.Question !== 'Dialogue 1' && dialogue.Question !== 'Dialogue 2' && dialogue.Question !== 'Dialogue 3') {
                        html += `<div style="font-size:.8rem;color:var(--text2);margin:8px 0 4px;font-style:italic">${dialogue.Question}</div>`;
                    }
                    if (Array.isArray(dialogue.Choices)) {
                        dialogue.Choices.forEach(choice => {
                            const pts = choice.Points || 0;
                            const ptColor = pts >= 10 ? '#4CAF50' : pts > 0 ? color : 'var(--text3)';
                            const ptLabel = pts > 0 ? `+${pts}` : pts === 0 && choice.Answer === 'Any' ? '—' : `${pts}`;
                            html += `<div class="skill-row" style="margin-bottom:6px">
                                <div class="skill-name" style="font-size:.875rem;font-weight:400">${isPhone ? '📱 ' : ''}${choice.Answer}</div>
                                <div class="skill-level" style="color:${ptColor};font-weight:700;font-size:.8rem;flex-shrink:0;margin-left:8px">${ptLabel}</div>
                            </div>`;
                        });
                    }
                });
            }
            html += `</div>`;
        });
    }

    if (data.UltimatePersona) {
        html += `<div class="awakening-card" style="border-color:${color}">
            <div class="awakening-title">Ultimate Persona</div>
            <div class="awakening-name">${data.UltimatePersona}</div>
            <div class="awakening-req">Unlocked at Rank 10</div>
        </div>`;
    }
    if (data.ThirdAwakening) {
        html += `<div class="awakening-card" style="border-color:#FFD700">
            <div class="awakening-title">Third Awakening</div>
            <div class="awakening-name">${data.ThirdAwakening.Persona || data.ThirdAwakening}</div>
            <div class="awakening-req">${data.ThirdAwakening.Requirement || 'Third Semester Event'}</div>
        </div>`;
    }

    const content = html || `<div class="empty-state">No data available</div>`;
    if (paneEl)  { paneEl.innerHTML  = content; paneEl.scrollTop  = 0; }
    if (phoneEl) { phoneEl.innerHTML = content; phoneEl.scrollTop = 0; }
}

function onSlSearch(val) {
    S.slQuery = val;
    document.getElementById('slSearchClear').style.display = val?'block':'none';
    const series = SERIES.find(s=>s.id===S.series);
    debounceSearch(()=>renderSlList(series?.color||'#2196F3'));
}
function clearSlSearch() { document.getElementById('slSearch').value=''; onSlSearch(''); }

/* ── Detail Screen ─────────────────────────────────────────────────────────── */
function openPersona(name) {
    const key = `personas_${S.game}`;
    const data = S.rawData[key];
    if (!data||!data[name]) return;
    S.detail = {type:'persona', name, data:data[name]};
    if (isTablet()) {
        showListDetailPane();
    } else {
        navigate('detail');
    }
}
function openEnemy(name) {
    const key = `enemies_${S.game}`;
    const data = S.rawData[key];
    if (!data) return;
    const enemy = Array.isArray(data)?data.find(e=>e.name===name):data[name];
    if (!enemy) return;
    S.detail = {type:'enemy', name, data:enemy};
    if (isTablet()) {
        showListDetailPane();
    } else {
        navigate('detail');
    }
}

function showListDetailPane() {
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    document.getElementById('listDetailPlaceholder').style.display = 'none';
    const paneContent = document.getElementById('listDetailContent');
    paneContent.style.display = 'flex';
    document.getElementById('listDetailTitle').textContent = S.detail.name;
    const favId = `${S.game}_${S.detail.name}`;
    const isFav = S.favorites.has(favId);
    const heartFilled = `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>`;
    const heartEmpty = `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>`;
    document.getElementById('favBtn').innerHTML = isFav ? heartFilled : heartEmpty;
    document.getElementById('favBtn').style.color = isFav ? color : '';
    if (S.detail.type==='persona') renderPersonaDetail(S.detail.name, S.detail.data, color);
    else renderEnemyDetail(S.detail.name, S.detail.data, color);
}

function renderDetailInPane() {} // no-op, kept for compat

function buildDetailScreen() {
    if (!S.detail) return;
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    document.getElementById('detailTitle').textContent = S.detail.name;
    const favId = `${S.game}_${S.detail.name}`;
    const isFav = S.favorites.has(favId);
    const heartFilled = `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>`;
    const heartEmpty = `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>`;
    document.getElementById('favBtnPhone').innerHTML = isFav ? heartFilled : heartEmpty;
    document.getElementById('favBtnPhone').style.color = isFav ? color : '';
    if (S.detail.type==='persona') renderPersonaDetail(S.detail.name, S.detail.data, color, 'detailContentPhone');
    else renderEnemyDetail(S.detail.name, S.detail.data, color, 'detailContentPhone');
}

function renderPersonaDetail(name, p, color, containerId) {
    const el = document.getElementById(containerId||'detailContent');
    const stats = p.stats||[];
    const maxStat = stats.length?Math.max(...stats,1):1;
    const statLabels = ['STR','MAG','END','AGI','LUK'];
    const level = p.level??p.lvl??'?';
    const arcana = p.arcana||p.race||'Unknown';

    let html = `<div class="detail-hero">
        <div class="detail-level-box" style="background:${color}22">
            <div class="detail-level-label" style="color:${color}">Lv.</div>
            <div class="detail-level-num" style="color:${color}">${level}</div>
        </div>
        <div class="detail-hero-info">
            <div class="detail-hero-name">${name}</div>
            <div class="detail-hero-arcana">${arcana} Arcana</div>
            ${p.trait?`<div class="detail-hero-trait" style="color:${color}">Trait: ${p.trait}</div>`:''}
        </div>
        ${p.image ? `<div class="detail-hero-image-wrap"><img src="${p.image}" class="detail-hero-image" alt="${name}" onerror="this.parentElement.style.display='none'"></div>` : ''}
    </div>`;

    if (p.description) html += `<div class="desc-box">${p.description}</div>`;
    if (p.unlock) html += `<div class="unlock-box"><div class="unlock-icon"><svg viewBox="0 0 24 24" width="20" height="20" fill="#FFD700"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/></svg></div><div><div class="unlock-label">Unlock</div><div class="unlock-text">${p.unlock}</div></div></div>`;

    if (stats.length>=5) {
        html += `<div class="section-card"><div class="section-title">Base Stats</div>`;
        statLabels.forEach((lbl,i)=>{
            const pct = Math.round((stats[i]/maxStat)*100);
            html += `<div class="stat-row">
                <div class="stat-label">${lbl}</div>
                <div class="stat-bar-wrap"><div class="stat-bar-fill" style="width:${pct}%;background:${color}"></div></div>
                <div class="stat-val">${stats[i]}</div>
            </div>`;
        });
        html += `</div>`;
    }

    if (p.skills&&Object.keys(p.skills).length) {
        html += `<div class="section-card"><div class="section-title">Skills</div>`;
        Object.entries(p.skills).forEach(([skill,lvl])=>{
            const label = lvl<1?'Innate':lvl>=100?'Special':`Lv. ${Math.floor(lvl)}`;
            const lcolor = lvl<1?color:lvl>=100?'#FFD700':'var(--text2)';
            html += `<div class="skill-row"><div class="skill-name">${skill}</div><div class="skill-level" style="color:${lcolor}">${label}</div></div>`;
        });
        html += `<div style="margin-top:10px">
            <button class="slot-action-btn" style="width:100%;padding:10px 14px;font-size:.85rem;font-weight:700;color:${color}" onclick="setSkillRoutePreload('${esc(name)}', null)">
                Inherit a Desired Skill (Find Fusion Route) ›
            </button>
        </div>`;
        html += `</div>`;
    }

    const aff = parsePersonaAffinities(p, S.game);
    const affinities = [
        {label:'Weak',    list:aff.weak,   color:'#E57373'},
        {label:'Resists', list:aff.resist, color:'#81C784'},
        {label:'Null',    list:aff.null_,  color:'#B0BEC5'},
        {label:'Repel',   list:aff.repel,  color:'#64B5F6'},
        {label:'Absorb',  list:aff.absorb, color:'#FFD54F'},
    ].filter(a=>a.list&&a.list.length);

    if (affinities.length) {
        html += `<div class="section-card"><div class="section-title">Affinities</div>`;
        affinities.forEach(a=>{
            html += `<div class="affinity-group"><div class="affinity-label">${a.label}</div>
                <div class="chips">${a.list.map(e=>`<span class="chip" style="background:${a.color}22;color:${a.color}">${e}</span>`).join('')}</div>
            </div>`;
        });
        html += `</div>`;
    }
    el.innerHTML = html;
    el.scrollTop = 0;
}

function renderEnemyDetail(name, e, color, containerId) {
    console.log('Rendering enemy:', name, containerId);
    const el = document.getElementById(containerId||'detailContent');
    if (!el) return;
    const isBoss = e.isBoss || e.isMiniBoss;
    const elems = ELEMENTS[S.series]||ELEMENTS.p5;

    let html = '';
    // Hero Section
    if (isBoss) {
        html += `<div class="boss-hero" style="background:${color}11">
            ${e.image ? `<img src="${e.image}" class="boss-image" alt="${name}" onerror="this.style.display='none'">` : ''}
            <div class="boss-name" style="color:${color}">${name}</div>
            <div style="font-size:1rem;color:var(--text2);margin-top:4px">${e.arcana||'Shadow'} · Level ${e.level||'?'}</div>
            <div class="boss-stats-grid">
                <div class="boss-stat-box"><div class="boss-stat-val">${e.hp||'???'}</div><div class="boss-stat-label">HP</div></div>
                <div class="boss-stat-box"><div class="boss-stat-val">${e.sp||'???'}</div><div class="boss-stat-label">SP</div></div>
            </div>
        </div>`;
    } else {
        html += `<div class="section-card">
            <div style="display:flex;justify-content:space-between;align-items:flex-start">
                <div>
                    <div style="font-size:1rem;color:var(--text2)">${e.arcana||'Shadow'}</div>
                    <div style="font-size:1.125rem;color:var(--text);margin-top:2px">Level ${e.level||'?'}</div>
                </div>
                <div style="text-align:right">
                    <div style="font-size:1.125rem;color:var(--text)">${e.hp||0} HP</div>
                    <div style="font-size:.875rem;color:var(--text2)">${e.sp||0} SP</div>
                </div>
            </div>
            ${e.image ? `<div style="margin-top:16px;text-align:center"><img src="${e.image}" style="max-width:100%;max-height:180px;border-radius:8px" onerror="this.parentElement.style.display='none'"></div>` : ''}
        </div>`;
    }

    if (e.version) html += `<div class="desc-box">${e.version}</div>`;
    
    // Stats (Safely handle missing stats object)
    if (e.stats && typeof e.stats === 'object') {
        html += `<div class="section-card"><div class="section-title">Stats</div>`;
        const statLabels = {strength:'Strength', magic:'Magic', endurance:'Endurance', agility:'Agility', luck:'Luck'};
        Object.entries(statLabels).forEach(([key, label]) => {
            if (e.stats[key] != null) {
                html += `<div class="info-row"><div class="info-label">${label}</div><div class="info-val">${e.stats[key]}</div></div>`;
            }
        });
        html += `</div>`;
    }

    // Resistances
    if (e.resists) {
        html += `<div class="section-card"><div class="section-title">Resistances</div><div class="resist-text">${parseResists(e.resists, elems)}</div></div>`;
    }

    // Skills
    if (e.skills && e.skills.length) {
        html += `<div class="section-card"><div class="section-title">Skills</div>`;
        e.skills.forEach(s => html += `<div class="skill-row"><div class="skill-name">${s}</div></div>`);
        html += `</div>`;
    }

    // Phases / Parts (Handle both names used in different games)
    const phases = e.phases || e.parts || [];
    if (phases.length) {
        phases.forEach(ph => {
            html += `<div class="section-card">
                <div class="section-title" style="color:${color}">Phase: ${ph.name}</div>
                ${ph.hp ? `<div class="info-row"><div class="info-label">HP</div><div class="info-val">${ph.hp}</div></div>` : ''}
                ${ph.sp ? `<div class="info-row"><div class="info-label">SP</div><div class="info-val">${ph.sp}</div></div>` : ''}
                ${ph.resists ? `<div style="margin-top:10px"><div class="section-title" style="font-size:.875rem;opacity:.8">Resistances</div><div class="resist-text">${parseResists(ph.resists, elems)}</div></div>` : ''}
                ${ph.skills && ph.skills.length ? `<div style="margin-top:10px"><div class="section-title" style="font-size:.875rem;opacity:.8">Skills</div>${ph.skills.map(s=>`<div class="skill-row" style="font-size:.85rem"><div class="skill-name">${s}</div></div>`).join('')}</div>` : ''}
            </div>`;
        });
    }

    // Drops
    if (e.drops) {
        html += `<div class="section-card"><div class="section-title">Location & Drops</div>`;
        if (e.area && e.area !== 'Unknown') html += `<div class="info-row"><div class="info-label">Area</div><div class="info-val">${e.area}</div></div>`;
        if (e.exp > 0) html += `<div class="info-row"><div class="info-label">EXP</div><div class="info-val">${e.exp}</div></div>`;
        if (e.drops.gem && e.drops.gem !== '-') html += `<div class="info-row"><div class="info-label">Gem</div><div class="info-val">${e.drops.gem}</div></div>`;
        if (e.drops.item && e.drops.item !== '-') html += `<div class="info-row"><div class="info-label">Item</div><div class="info-val">${e.drops.item}</div></div>`;
        if (e.drops.rare && e.drops.rare !== '-') html += `<div class="info-row" style="color:#FFD700"><div class="info-label" style="color:#FF9800">Rare Drop</div><div class="info-val">${e.drops.rare}</div></div>`;
        html += `</div>`;
    }

    el.innerHTML = html || '<div class="empty-state">No details available</div>';
    el.scrollTop = 0;
}

/* ── Affinity Parsers ──────────────────────────────────────────────────────── */
function parsePersonaAffinities(p, gameId) {
    if (p.weak||p.reflects||p.absorbs||p.nullifies) {
        return { weak:p.weak||[], resist:Array.isArray(p.resists)?p.resists:[], null_:p.nullifies||[], repel:p.reflects||[], absorb:p.absorbs||[] };
    }
    const resistStr = typeof p.resists==='string'?p.resists:'';
    if (!resistStr) return {weak:[],resist:[],null_:[],repel:[],absorb:[]};
    const isP3 = p.heart!=null||p.cardlvl!=null||(gameId||'').startsWith('p3');
    const isP5 = p.trait!=null||p.item!=null||(gameId||'').startsWith('p5');
    let elems;
    if (isP3||(resistStr.length===10&&!isP5)) elems=['Slash','Strike','Pierce','Fire','Ice','Elec','Wind','Light','Dark','Almighty'];
    else if (resistStr.length===7) elems=['Phys','Fire','Ice','Elec','Wind','Light','Dark'];
    else if (resistStr.length===8) elems=['Phys','Fire','Ice','Elec','Wind','Light','Dark','Almighty'];
    else elems=['Phys','Gun','Fire','Ice','Elec','Wind','Psy','Nuke','Bless','Curse'];
    const result={weak:[],resist:[],null_:[],repel:[],absorb:[]};
    // 'R' means Repel while lowercase 'r' means Resist; other uppercase codes are case typos
    const map={w:'weak',s:'resist',r:'resist',n:'null_','_':'null_',p:'repel',d:'absorb',a:'absorb'};
    resistStr.split('').forEach((c,i)=>{
        if(i>=elems.length) return;
        const key = c==='R' ? 'repel' : map[c.toLowerCase()];
        if(key) result[key].push(elems[i]);
    });
    return result;
}
function parseResists(str, elems) {
    // 'R' means Repel while lowercase 'r' means Resist; other uppercase codes are case typos
    const map={w:'Weak',s:'Strong',r:'Resist',n:'Null','_':'Null',p:'Repel',d:'Drain',a:'Drain'};
    return str.split('').map((c,i)=>{
        const r=(c==='R'?'Repel':map[c.toLowerCase()])||'Normal';
        if(r==='Normal'||i>=elems.length) return null;
        const colors={Weak:'#E57373',Null:'#B0BEC5',Drain:'#FFD54F',Repel:'#64B5F6',Resist:'#81C784',Strong:'#81C784'};
        return `<span class="chip" style="background:${colors[r]||'#555'}22;color:${colors[r]||'#aaa'}">${elems[i]}: ${r}</span>`;
    }).filter(Boolean).join(' ')||'No special resistances';
}

/* ── Settings ──────────────────────────────────────────────────────────────── */
function buildSettingsScreen() {
    document.getElementById('settingsContent').innerHTML = `
    <div class="section-card">
        <div class="section-title">Persona 3 Portable</div>
        <div class="setting-row" onclick="setP3PProtagonist('MALE')">
            <div class="setting-info"><div class="setting-label">Male MC</div></div>
            <div class="toggle ${S.settings.p3pProtagonist==='MALE'?'on':''}" id="tog-male"></div>
        </div>
        <div class="setting-row" onclick="setP3PProtagonist('FEMC')">
            <div class="setting-info"><div class="setting-label">FeMC</div></div>
            <div class="toggle ${S.settings.p3pProtagonist==='FEMC'?'on':''}" id="tog-femc"></div>
        </div>
    </div>
    <div class="section-card" style="margin-top:12px">
        <div class="section-title">Content Filters</div>
        <div class="setting-row" onclick="toggleSetting('showDlc')">
            <div class="setting-info">
                <div class="setting-label">Show DLC Personas</div>
                <div class="setting-desc">Include DLC personas in lists and fusion</div>
            </div>
            <div class="toggle ${S.settings.showDlc?'on':''}" id="toggle-showDlc"></div>
        </div>
        <div class="setting-row" onclick="toggleSetting('showEpisodeAigis')">
            <div class="setting-info">
                <div class="setting-label">Show Episode Aigis Personas</div>
                <div class="setting-desc">Include Episode Aigis personas (P3R)</div>
            </div>
            <div class="toggle ${S.settings.showEpisodeAigis?'on':''}" id="toggle-showEpisodeAigis"></div>
        </div>
    </div>`;
}

function toggleSetting(key) {
    S.settings[key] = !S.settings[key];
    localStorage.setItem('settings', JSON.stringify(S.settings));
    const el = document.getElementById('toggle-'+key);
    if (el) el.classList.toggle('on', S.settings[key]);
    Object.keys(S.rawData).forEach(k=>{ if(k.startsWith('personas_')) delete S.rawData[k]; });
}

function setP3PProtagonist(val) {
    S.settings.p3pProtagonist = val;
    localStorage.setItem('settings', JSON.stringify(S.settings));
    document.getElementById('tog-male')?.classList.toggle('on', val==='MALE');
    document.getElementById('tog-femc')?.classList.toggle('on', val==='FEMC');
    S.slData = null; // force reload
}

/* ── Misc ──────────────────────────────────────────────────────────────────── */
function setSort(opt, color) { S.sort=opt; buildListScreen(); }
function setEnemyTab(t) { S.enemyTab=t; buildListScreen(); }
function setEnemySort(opt) {
    if (S.enemySort === opt) S.enemySortDir = -S.enemySortDir;
    else { S.enemySort = opt; S.enemySortDir = 1; }
    buildListScreen();
}
function toggleFavOnly() { S.favOnly = !S.favOnly; buildListScreen(); }

function onSearch(val) {
    S.query=val;
    document.getElementById('searchClear').style.display=val?'block':'none';
    const series=SERIES.find(s=>s.id===S.series);
    debounceSearch(()=>renderList(S.rawData[`${S.listMode}_${S.game}`]||{}, series?.color||'#2196F3'));
}
function clearSearch() { document.getElementById('searchInput').value=''; onSearch(''); }

function toggleFavorite() {
    if (!S.detail) return;
    const id=`${S.game}_${S.detail.name}`;
    const c=SERIES.find(s=>s.id===S.series)?.color||'#f00';
    const wasFav = S.favorites.has(id);
    if (wasFav) { S.favorites.delete(id); } else { S.favorites.add(id); }
    const nowFav = !wasFav;
    const heartFilled = `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>`;
    const heartEmpty = `<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>`;
    ['favBtn','favBtnPhone'].forEach(btnId => {
        const btn = document.getElementById(btnId);
        if (btn) { btn.innerHTML = nowFav ? heartFilled : heartEmpty; btn.style.color = nowFav ? c : ''; }
    });
    localStorage.setItem('favs', JSON.stringify([...S.favorites]));
    // Refresh row hearts if the list is visible (tablet two-pane)
    if (document.getElementById('screen-list')?.classList.contains('active')) {
        renderList(S.rawData[`${S.listMode}_${S.game}`]||{}, c);
    }
}
function shareDetail() {
    if (!S.detail) return;
    if (navigator.share) navigator.share({title:S.detail.name,text:S.detail.name});
    else navigator.clipboard?.writeText(S.detail.name+'\n'+window.location.href).then(()=>alert('Copied!'));
}
function showLoading() { document.getElementById('listContent').innerHTML=`<div class="loading-wrap"><div class="spinner"></div><div>Loading…</div></div>`; }
function showEmpty(msg) { document.getElementById('listContent').innerHTML=`<div class="empty-state">${msg}</div>`; }
function esc(s) { return s.replace(/'/g,"\\'"); }

/* ── Fusion Calculator ─────────────────────────────────────────────────────── */
const FUSION_CHART_PATHS = {
    p3fes:'./data/fusion-charts/p3-fusion-chart.json',      p3p:'./data/fusion-charts/p3p-fusion-chart.json',
    p3r:'./data/fusion-charts/p3r-fusion-chart.json',        p4:'./data/fusion-charts/p4-base-fusion-chart.json',
    p4g:'./data/fusion-charts/p4-fusion-chart.json',         p5:'./data/fusion-charts/p5-base-fusion-chart.json',
    p5r:'./data/fusion-charts/p5-fusion-chart.json'
};
const SPECIAL_PATHS = {
    p3fes:'./data/special-fusions/p3-special.json', p3p:'./data/special-fusions/p3-special.json',
    p3r:'./data/special-fusions/p3r-special.json',  p4:'./data/special-fusions/p4-special.json',
    p4g:'./data/special-fusions/p4-special.json',   p5:'./data/special-fusions/p5-special.json',
    p5r:'./data/special-fusions/p5r-special.json'
};
// P3R/P5/P5R use a triangular matrix; all others are square
const IS_TRIANGULAR = { p3r:true, p5:true, p5r:true };

async function buildFusionScreen() {
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    const el     = document.getElementById('fusionContent');

    if (!S.fusion.personas || !S.fusion.fissionTable) {
        el.innerHTML = `<div class="loading-wrap"><div class="spinner"></div><div>Loading fusion data…</div></div>`;
        try {
            // Load personas
            const pRes = await fetch(PERSONA_PATHS[S.game]);
            if (!pRes.ok) throw new Error('Failed to load personas');
            const allPersonas = await pRes.json();

            // DLC persona names per game (fallback for JSONs without isDlc field)
            const DLC_NAMES = {
                p3fes: new Set(),
                p3p:   new Set(),
                p3r:   new Set(['Arsene','Captain Kidd','Zorro','Carmen','Goemon','Johanna','Milady',
                                'Robin Hood','Cendrillon','Satanael','Seiten Taisei A','Mercurius',
                                'Hecate','Kamu Susano-o','Anat','Astarte','Loki A','Vanadis',
                                'Izanagi','Magatsu-Izanagi','Kaguya']),
                p4:    new Set(),
                p4g:   new Set(),
                p5:    new Set(['Izanagi','Izanagi Picaro','Orpheus','Orpheus Picaro','Ariadne',
                                'Ariadne Picaro','Asterius','Asterius Picaro','Thanatos','Thanatos Picaro',
                                'Magatsu-Izanagi','Magatsu-Izanagi Picaro','Kaguya','Kaguya Picaro',
                                'Tsukiyomi','Tsukiyomi Picaro','Messiah','Messiah Picaro']),
                p5r:   new Set(['Orpheus F','Orpheus F Picaro','Izanagi','Izanagi Picaro','Orpheus',
                                'Orpheus Picaro','Raoul','Athena','Athena Picaro','Ariadne','Ariadne Picaro',
                                'Asterius','Asterius Picaro','Thanatos','Thanatos Picaro',
                                'Magatsu-Izanagi','Magatsu-Izanagi Picaro','Kaguya','Kaguya Picaro',
                                'Tsukiyomi','Tsukiyomi Picaro','Messiah','Messiah Picaro',
                                'Izanagi-no-Okami','Izanagi-no-Okami Picaro'])
            };
            const gameDlc = DLC_NAMES[S.game] || new Set();

            // Filter DLC / EpisodeAigis
            const personaMap = {};
            Object.entries(allPersonas).forEach(([name, p]) => {
                const isDlc = p.isDlc || gameDlc.has(name);
                if (!S.settings.showDlc && isDlc) return;
                if (!S.settings.showEpisodeAigis && p.episodeAigis) return;
                personaMap[name] = { ...p, name };
            });

            // Load fusion chart
            const cRes = await fetch(FUSION_CHART_PATHS[S.game]);
            if (!cRes.ok) throw new Error('Failed to load fusion chart');
            const chart = await cRes.json();

            // Load special fusions
            let specialData = {};
            try {
                const sRes = await fetch(SPECIAL_PATHS[S.game] + '?v=' + Date.now());
                if (sRes.ok) specialData = await sRes.json();
            } catch(e) {}
const NON_FUSABLE = new Set(['party','accident','special']);
            const elemDemonNames = new Set(
                Object.entries(specialData)
                    .filter(([, v]) => Array.isArray(v) && v.length === 0)
                    .map(([k]) => k)
            );
            const byArcana = {};
            Object.entries(personaMap).forEach(([name, p]) => {
                if (NON_FUSABLE.has(p.fusion)) return;
                if (elemDemonNames.has(name)) return;
                const a = p.arcana || p.race || '';
                if (!byArcana[a]) byArcana[a] = [];
                byArcana[a].push({ name, data: p });
            });
            Object.values(byArcana).forEach(arr => arr.sort((a,b) => (a.data.level??a.data.lvl??0) - (b.data.level??b.data.lvl??0)));

            const fissionTable = {};
            const _races = chart.races, _table = chart.table;
            const _triangular = IS_TRIANGULAR[S.game] || false;
            if (_triangular) {
                for (let idxA = 0; idxA < _races.length; idxA++) {
                    const raceA = _races[idxA];
                    const row = _table[idxA];
                    if (!row) continue;
                    for (let c = 0; c < row.length; c++) {
                        if (c === idxA) continue;
                        const raceB = _races[c];
                        const raceR = row[c];
                        if (!raceR || raceR === '-') continue;
                        if (!fissionTable[raceR]) fissionTable[raceR] = {};
                        if (!fissionTable[raceR][raceA]) fissionTable[raceR][raceA] = [];
                        if (!fissionTable[raceR][raceA].includes(raceB)) fissionTable[raceR][raceA].push(raceB);
                    }
                }
            } else {
                for (let idxA = 0; idxA < _races.length; idxA++) {
                    const raceA = _races[idxA];
                    const row = _table[idxA];
                    if (!row) continue;
                    for (let idxB = idxA; idxB < _races.length; idxB++) {
                        if (idxB === idxA) continue;
                        const raceB = _races[idxB];
                        const raceR = row[idxB];
                        if (!raceR || raceR === '-') continue;
                        if (!fissionTable[raceR]) fissionTable[raceR] = {};
                        if (!fissionTable[raceR][raceA]) fissionTable[raceR][raceA] = [];
                        if (!fissionTable[raceR][raceA].includes(raceB)) fissionTable[raceR][raceA].push(raceB);
                    }
                }
            }
            S.fusion.fissionTable = fissionTable;
            S.fusion.personaMap = personaMap;
            S.fusion.chart = chart;
            S.fusion.byArcana = byArcana;
            S.fusion.specialData = specialData;
            S.fusion.personas = Object.keys(personaMap).sort((a,b)=>a.localeCompare(b));
            S.fusion.mode = 'reverse';
            S.fusion.selected = null;
            S.fusion.forwardSlots = [null, null, null];
            S.fusion.forwardSource = null;
            S.fusion.forwardSubTab = 'chamber';
        } catch(e) {
            el.innerHTML = `<div class="empty-state">Failed to load: ${e.message}</div>`;
            return;
        }
    }

    updateFusionTabUI();
    const searchWrap = document.querySelector('#screen-fusion .pane-list > .search-wrap');
    if (searchWrap) {
        searchWrap.style.display = (S.fusion.mode === 'reverse') ? 'flex' : 'none';
    }
    const placeholder = document.getElementById('fusionDetailPlaceholder');
    const detailContent = document.getElementById('fusionDetailContent');

    if (S.fusion.mode === 'forward') {
        if (placeholder) placeholder.style.display = 'none';
        if (detailContent) detailContent.style.display = 'none';
        renderForwardFusionScreen(color);
    } else if (S.fusion.mode === 'skillRoute') {
        if (placeholder) placeholder.style.display = 'none';
        if (detailContent) detailContent.style.display = 'none';
        renderSkillRouteScreen(color);
    } else {
        if (S.fusion.selected) {
            if (isTablet()) {
                showFusionDetailPane(color);
            } else {
                renderFusionResults(color);
            }
        } else {
            if (placeholder) placeholder.style.display = '';
            if (detailContent) detailContent.style.display = 'none';
            renderFusionPersonaList(color);
        }
    }
}

function updateFusionTabUI() {
    const revBtn = document.getElementById('fusionTabReverse');
    const fwdBtn = document.getElementById('fusionTabForward');
    const routeBtn = document.getElementById('fusionTabSkillRoute');
    if (revBtn) revBtn.classList.toggle('active', S.fusion.mode === 'reverse');
    if (fwdBtn) fwdBtn.classList.toggle('active', S.fusion.mode === 'forward');
    if (routeBtn) routeBtn.classList.toggle('active', S.fusion.mode === 'skillRoute');
}

function setFusionMode(mode) {
    S.fusion.mode = mode;
    updateFusionTabUI();
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    const searchWrap = document.querySelector('#screen-fusion .pane-list > .search-wrap');
    if (searchWrap) {
        searchWrap.style.display = (mode === 'reverse') ? 'flex' : 'none';
    }
    const placeholder = document.getElementById('fusionDetailPlaceholder');
    const detailContent = document.getElementById('fusionDetailContent');
    if (mode === 'forward') {
        if (placeholder) placeholder.style.display = 'none';
        if (detailContent) detailContent.style.display = 'none';
        renderForwardFusionScreen(color);
    } else if (mode === 'skillRoute') {
        if (placeholder) placeholder.style.display = 'none';
        if (detailContent) detailContent.style.display = 'none';
        renderSkillRouteScreen(color);
    } else {
        if (S.fusion.selected) {
            if (isTablet()) {
                showFusionDetailPane(color);
            } else {
                renderFusionResults(color);
            }
        } else {
            if (placeholder) placeholder.style.display = '';
            if (detailContent) detailContent.style.display = 'none';
            renderFusionPersonaList(color);
        }
    }
}

/* ── Reverse Fusion (How to craft target Persona) ────────────────────────── */
function renderFusionPersonaList(color) {
    const el = document.getElementById('fusionContent');
    const q  = (S.fusion.query || '').toLowerCase();
    document.getElementById('fusionSearch').value = S.fusion.query || '';
    document.getElementById('fusionSearchClear').style.display = q ? 'block' : 'none';

    let items = S.fusion.personas || [];
    if (q) items = items.filter(name => {
        const p = S.fusion.personaMap[name];
        return name.toLowerCase().includes(q) || (p.arcana||p.race||'').toLowerCase().includes(q);
    });

    if (!items.length) { el.innerHTML = `<div class="empty-state">No personas found</div>`; return; }

    el.innerHTML = `<div class="fusion-hint">Select a persona to see fusion recipes</div>` +
        items.map(name => {
            const p = S.fusion.personaMap[name];
            const level = p.level??p.lvl??'?';
            const arcana = p.arcana||p.race||'Unknown';
            return `<div class="row-card" onclick="selectFusionPersona('${esc(name)}')">
                <div class="level-badge" style="background:${color}22;color:${color}">${level}</div>
                <div class="row-main">
                    <div class="row-name">${name}</div>
                    <div class="row-sub">${arcana}</div>
                </div>
                <div class="row-hint" style="color:${color}">›</div>
            </div>`;
        }).join('');
}

// Games with triangle (3-persona) fusion — P3/P4 games
const TRIANGLE_GAMES = new Set(['p3fes','p3p','p3r','p4','p4g']);

function selectFusionPersona(name) {
    const p = S.fusion.personaMap[name];
    if (!p) return;
    S.fusion.selected = { name, data: p };
    S.fusion.recipes  = calcFusionRecipes(name);
    S.fusion.isTriangle = false;
    if (!S.fusion.recipes.length && TRIANGLE_GAMES.has(S.game)) {
        S.fusion.recipes = calcTriangleRecipes(name);
        S.fusion.isTriangle = S.fusion.recipes.length > 0;
    }
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    if (isTablet()) {
        showFusionDetailPane(color);
    } else {
        renderFusionResults(color);
    }
}

function showFusionDetailPane(color) {
    const { selected, recipes } = S.fusion;
    if (!selected) return;
    document.getElementById('fusionDetailPlaceholder').style.display = 'none';
    const paneContent = document.getElementById('fusionDetailContent');
    paneContent.style.display = 'flex';
    document.getElementById('fusionDetailTitle').textContent = selected.name;

    const p = selected.data;
    const level = p.level??p.lvl??'?';
    const arcana = p.arcana||p.race||'Unknown';
    const el = document.getElementById('fusionRecipeContent');

    let html = `<div class="fusion-selected-card" style="border-left:4px solid ${color};margin:0">
        <div class="fusion-selected-info">
            <div class="fusion-selected-name">${selected.name}</div>
            <div class="fusion-selected-sub" style="color:${color}">${arcana} · Lv. ${level}</div>
        </div>
    </div>`;

    if (p.unlock) {
        html += `<div class="unlock-box" style="margin:8px 0">
            <div class="unlock-icon"><svg viewBox="0 0 24 24" width="18" height="18" fill="#FFD700"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/></svg></div>
            <div><div class="unlock-label">How to Unlock</div><div class="unlock-text">${p.unlock}</div></div>
        </div>`;
    }

    if (!recipes || !recipes.length) {
        html += `<div class="empty-state">No two-persona recipes exist — in-game this persona comes from triangle or conditional fusion${!S.settings.showDlc?' (DLC personas are also hidden)':''}</div>`;
    } else {
        html += `<div class="fusion-count" style="padding:8px 0 4px">${recipes.length} recipe${recipes.length!==1?'s':''} found${S.fusion.isTriangle?' · triangle fusion (3 personas)':''}${recipes.length===60&&S.fusion.isTriangle?' · first 60 shown':''}</div>`;
        html += recipes.map(combo => {
            if (combo.length === 2) {
                return `<div class="fusion-recipe-card" style="margin:0 0 8px">
                    ${combo.map((ing, i) => `
                        ${i>0?`<div class="fusion-plus" style="color:${color}">+</div>`:''}
                        <div class="fusion-ingredient" onclick="selectFusionPersona('${esc(ing.name)}')">
                            <div class="fusion-ing-name" style="color:${color}">${ing.name}</div>
                            <div class="fusion-ing-sub">${ing.data.arcana||ing.data.race||'Unknown'} · Lv. ${ing.data.level??ing.data.lvl??'?'}</div>
                        </div>`).join('')}
                </div>`;
            } else {
                return `<div class="fusion-recipe-card fusion-recipe-card--vertical" style="margin:0 0 8px">
                    ${combo.map((ing, i) => `
                        ${i>0?`<div class="fusion-plus-v" style="color:${color}">+</div>`:''}
                        <div class="fusion-ingredient-v" onclick="selectFusionPersona('${esc(ing.name)}')">
                            <div class="fusion-ing-name" style="color:${color}">${ing.name}</div>
                            <div class="fusion-ing-sub">${ing.data.arcana||ing.data.race||'Unknown'} · Lv. ${ing.data.level??ing.data.lvl??'?'}</div>
                        </div>`).join('')}
                </div>`;
            }
        }).join('');
    }
    el.innerHTML = html;
    el.scrollTop = 0;
}

function renderFusionResults(color) {
    const el = document.getElementById('fusionContent');
    const { selected, recipes } = S.fusion;
    if (!selected) return;

    const p = selected.data;
    const level = p.level??p.lvl??'?';
    const arcana = p.arcana||p.race||'Unknown';

    let html = `<div class="fusion-selected-card" style="border-left:4px solid ${color}">
        <div class="fusion-selected-info">
            <div class="fusion-selected-name">${selected.name}</div>
            <div class="fusion-selected-sub" style="color:${color}">${arcana} · Lv. ${level}</div>
        </div>
        <button class="icon-btn" onclick="clearFusionSelection()" title="Clear">✕</button>
    </div>`;

    if (p.unlock) {
        html += `<div class="unlock-box">
            <div class="unlock-icon"><svg viewBox="0 0 24 24" width="20" height="20" fill="#FFD700"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/></svg></div>
            <div><div class="unlock-label">How to Unlock</div><div class="unlock-text">${p.unlock}</div></div>
        </div>`;
    }

    if (!recipes || !recipes.length) {
        html += `<div class="empty-state" style="margin-top:24px">No two-persona recipes exist — in-game this persona comes from triangle or conditional fusion${!S.settings.showDlc?' (DLC personas are also hidden)':''}</div>`;
    } else {
        html += `<div class="fusion-count">${recipes.length} recipe${recipes.length!==1?'s':''} found${S.fusion.isTriangle?' · triangle fusion (3 personas)':''}${recipes.length===60&&S.fusion.isTriangle?' · first 60 shown':''}</div>`;
        html += recipes.map(combo => {
            if (combo.length === 2) {
                return `<div class="fusion-recipe-card">
                    ${combo.map((ing, i) => `
                        ${i>0?`<div class="fusion-plus" style="color:${color}">+</div>`:''}
                        <div class="fusion-ingredient" onclick="selectFusionPersona('${esc(ing.name)}')">
                            <div class="fusion-ing-name" style="color:${color}">${ing.name}</div>
                            <div class="fusion-ing-sub">${ing.data.arcana||ing.data.race||'Unknown'} · Lv. ${ing.data.level??ing.data.lvl??'?'}</div>
                        </div>`).join('')}
                </div>`;
            } else {
                return `<div class="fusion-recipe-card fusion-recipe-card--vertical">
                    ${combo.map((ing, i) => `
                        ${i>0?`<div class="fusion-plus-v" style="color:${color}">+</div>`:''}
                        <div class="fusion-ingredient-v" onclick="selectFusionPersona('${esc(ing.name)}')">
                            <div class="fusion-ing-name" style="color:${color}">${ing.name}</div>
                            <div class="fusion-ing-sub">${ing.data.arcana||ing.data.race||'Unknown'} · Lv. ${ing.data.level??ing.data.lvl??'?'}</div>
                        </div>`).join('')}
                </div>`;
            }
        }).join('');
    }
    el.innerHTML = html;
    el.scrollTop = 0;
}

function clearFusionSelection() {
    S.fusion.selected = null;
    S.fusion.recipes  = null;
    if (isTablet()) {
        clearFusionSelectionPane();
    } else {
        const series = SERIES.find(s=>s.id===S.series);
        if (S.fusion.mode === 'forward') {
            renderForwardFusionScreen(series?.color||'#2196F3');
        } else {
            renderFusionPersonaList(series?.color||'#2196F3');
        }
    }
}

function clearFusionSelectionPane() {
    S.fusion.selected = null;
    S.fusion.recipes  = null;
    document.getElementById('fusionDetailPlaceholder').style.display = '';
    document.getElementById('fusionDetailContent').style.display = 'none';
    const series = SERIES.find(s=>s.id===S.series);
    renderFusionPersonaList(series?.color||'#2196F3');
}

function onFusionSearch(val) {
    S.fusion.query = val;
    document.getElementById('fusionSearchClear').style.display = val?'block':'none';
    if (S.fusion.selected) return;
    const series = SERIES.find(s=>s.id===S.series);
    debounceSearch(()=>renderFusionPersonaList(series?.color||'#2196F3'));
}
function clearFusionSearch() { document.getElementById('fusionSearch').value=''; onFusionSearch(''); }

function calcFusionRecipes(targetName) {
    const recipes = [];
    const { personaMap, chart, byArcana, specialData, fissionTable } = S.fusion;
    const target = personaMap[targetName];
    if (!target) return recipes;
    const special = specialData[targetName];
    if (special && special.length) {
        for (const combo of special) {
            const personas = combo.map(n => personaMap[n] ? {name:n, data:personaMap[n]} : null).filter(Boolean);
            if (personas.length === combo.length) recipes.push(personas);
        }
        return recipes;
    }
    if (special && special.length === 0) return recipes;
    const targetArcana = target.arcana || target.race;
    const targetLevel  = target.level  ?? target.lvl ?? 0;
    if (!targetArcana || !chart) return recipes;
    const resultLvls = (byArcana[targetArcana] || [])
        .filter(p => !specialData[p.name])
        .map(p => p.data.level ?? p.data.lvl ?? 0)
        .sort((a,b) => a-b);
    const targetLvlIndex = resultLvls.indexOf(targetLevel);
    if (targetLvlIndex < 0) return recipes;
    const sameArcanaList = (byArcana[targetArcana] || []).filter(p => !specialData[p.name]);
    for (let i = 0; i < sameArcanaList.length; i++) {
        for (let j = i + 1; j < sameArcanaList.length; j++) {
            const p1 = sameArcanaList[i], p2 = sameArcanaList[j];
            const avgLvl = ((p1.data.level ?? p1.data.lvl ?? 0) + (p2.data.level ?? p2.data.lvl ?? 0)) / 2;
            const lowerRank = sameArcanaList
                .filter(p => (p.data.level ?? p.data.lvl ?? 0) < avgLvl && p.name !== p1.name && p.name !== p2.name)
                .sort((a,b) => (b.data.level ?? b.data.lvl ?? 0) - (a.data.level ?? a.data.lvl ?? 0))[0];
            if (lowerRank && lowerRank.name === targetName) {
                recipes.push([p1, p2]);
            }
        }
    }
    const arcanaPairs = fissionTable[targetArcana] || {};
    const minLvl = targetLvlIndex === 0 ? 0 : (resultLvls[targetLvlIndex - 1] - 1) * 2;
    const maxLvl = (targetLevel - 1) * 2;
    for (const [arcA, listB] of Object.entries(arcanaPairs)) {
        const listA = (byArcana[arcA] || []).filter(p => !specialData[p.name]);
        for (const arcB of listB) {
            const bList = (byArcana[arcB] || []).filter(p => !specialData[p.name]);
            for (const pA of listA) {
                const lvlA = pA.data.level ?? pA.data.lvl ?? 0;
                for (const pB of bList) {
                    const lvlB = pB.data.level ?? pB.data.lvl ?? 0;
                    const sum = lvlA + lvlB;
                    if (sum > minLvl && sum <= maxLvl) {
                        recipes.push([pA, pB]);
                    }
                }
            }
        }
    }
    return recipes.sort((a, b) => {
        const costA = (a[0].data.level ?? a[0].data.lvl ?? 0) + (a[1].data.level ?? a[1].data.lvl ?? 0);
        const costB = (b[0].data.level ?? b[0].data.lvl ?? 0) + (b[1].data.level ?? b[1].data.lvl ?? 0);
        return costA - costB;
    });
}

function calcTriangleRecipes(targetName) {
    const recipes = [];
    const { personaMap, byArcana, specialData } = S.fusion;
    const target = personaMap[targetName];
    if (!target) return recipes;
    const allFusable = Object.values(personaMap).filter(p => !['party','accident','special'].includes(p.fusion) && !specialData[p.name]);
    for (let i = 0; i < allFusable.length; i++) {
        for (let j = i + 1; j < allFusable.length; j++) {
            for (let k = j + 1; k < allFusable.length; k++) {
                const names = [allFusable[i].name, allFusable[j].name, allFusable[k].name];
                const res = calcForwardFusionWeb(names);
                if (res && res.name === targetName) {
                    recipes.push(names.map(n => ({ name: n, data: personaMap[n] })));
                    if (recipes.length >= 60) return recipes;
                }
            }
        }
    }
    return recipes;
}

/* ── Forward Fusion Functions (Combine Ingredients & Explore Outputs) ────── */
function getResultArcanaWeb(arc1, arc2) {
    const chart = S.fusion.chart;
    if (!chart || !chart.races || !chart.table) return null;
    const { races, table } = chart;
    const idx1 = races.indexOf(arc1);
    const idx2 = races.indexOf(arc2);
    if (idx1 < 0 || idx2 < 0) return null;

    if (IS_TRIANGULAR[S.game]) {
        const r = Math.max(idx1, idx2);
        const c = Math.min(idx1, idx2);
        return (table[r] && table[r][c]) ? table[r][c] : null;
    } else {
        const r = Math.min(idx1, idx2);
        const c = Math.max(idx1, idx2);
        return (table[r] && table[r][c]) ? table[r][c] : null;
    }
}

function calcForwardFusionWeb(names) {
    const validNames = (names || []).filter(Boolean);
    if (validNames.length < 2) return null;
    const { personaMap, specialData } = S.fusion;
    if (!personaMap) return null;

    // 1. Check special group/special fusions first
    const sortedInput = [...validNames].sort();
    for (const [specialResult, recipes] of Object.entries(specialData || {})) {
        for (const recipe of recipes) {
            if (recipe.length === validNames.length) {
                const sortedRecipe = [...recipe].sort();
                if (sortedRecipe.every((n, i) => n === sortedInput[i])) {
                    return { name: specialResult, data: personaMap[specialResult], isSpecial: true };
                }
            }
        }
    }

    // 2. Triangle fusion (3 personas)
    if (validNames.length === 3) {
        const [p1, p2, p3] = validNames.map(n => ({ name: n, data: personaMap[n] }));
        if (!p1.data || !p2.data || !p3.data) return null;

        const sorted = [p1, p2, p3].sort((a, b) => {
            const lvlA = a.data.level ?? a.data.lvl ?? 0;
            const lvlB = b.data.level ?? b.data.lvl ?? 0;
            return lvlA - lvlB;
        });
        const [lowest, middle, highest] = sorted;

        const arcL = lowest.data.arcana || lowest.data.race;
        const arcM = middle.data.arcana || middle.data.race;
        const arcH = highest.data.arcana || highest.data.race;

        let intermediateArc = null;
        if (arcL === arcM) {
            intermediateArc = arcL;
        } else {
            intermediateArc = getResultArcanaWeb(arcL, arcM);
        }
        if (!intermediateArc || intermediateArc === '-') return null;

        const finalArc = getResultArcanaWeb(intermediateArc, arcH);
        if (!finalArc || finalArc === '-') return null;

        const lvlL = lowest.data.level ?? lowest.data.lvl ?? 0;
        const lvlM = middle.data.level ?? middle.data.lvl ?? 0;
        const lvlH = highest.data.level ?? highest.data.lvl ?? 0;
        const targetLvl = Math.floor((lvlL + lvlM + lvlH) / 3) + 5;

        const candidates = (S.fusion.byArcana[finalArc] || [])
            .filter(p => !validNames.includes(p.name) && !specialData[p.name] && !['party','accident','special'].includes(p.data.fusion))
            .map(p => ({ name: p.name, data: p.data, lvl: p.data.level ?? p.data.lvl ?? 0 }))
            .sort((a, b) => a.lvl - b.lvl);

        const match = candidates.find(p => p.lvl >= targetLvl);
        if (match) return { name: match.name, data: match.data, isSpecial: false };
        if (candidates.length) return { name: candidates[candidates.length - 1].name, data: candidates[candidates.length - 1].data, isSpecial: false };
        return null;
    }

    // 3. Normal 2-Persona fusion
    if (validNames.length === 2) {
        const p1 = personaMap[validNames[0]];
        const p2 = personaMap[validNames[1]];
        if (!p1 || !p2) return null;

        const arc1 = p1.arcana || p1.race;
        const arc2 = p2.arcana || p2.race;
        const lvl1 = p1.level ?? p1.lvl ?? 0;
        const lvl2 = p2.level ?? p2.lvl ?? 0;

        if (arc1 === arc2) {
            // Same arcana -> downgrade below average
            const avgLvl = (lvl1 + lvl2) / 2.0;
            const candidates = (S.fusion.byArcana[arc1] || [])
                .filter(p => !validNames.includes(p.name) && !specialData[p.name] && !['party','accident','special'].includes(p.data.fusion))
                .map(p => ({ name: p.name, data: p.data, lvl: p.data.level ?? p.data.lvl ?? 0 }))
                .filter(p => p.lvl < avgLvl)
                .sort((a, b) => b.lvl - a.lvl);
            if (candidates.length) return { name: candidates[0].name, data: candidates[0].data, isSpecial: false };
            return null;
        } else {
            // Cross arcana
            const resArc = getResultArcanaWeb(arc1, arc2);
            if (!resArc || resArc === '-') return null;
            const targetLvl = Math.floor((lvl1 + lvl2) / 2) + 1;
            const candidates = (S.fusion.byArcana[resArc] || [])
                .filter(p => !validNames.includes(p.name) && !specialData[p.name] && !['party','accident','special'].includes(p.data.fusion))
                .map(p => ({ name: p.name, data: p.data, lvl: p.data.level ?? p.data.lvl ?? 0 }))
                .sort((a, b) => a.lvl - b.lvl);
            const match = candidates.find(p => p.lvl >= targetLvl);
            if (match) return { name: match.name, data: match.data, isSpecial: false };
            return null;
        }
    }
    return null;
}

function calcForwardFusionsFromWeb(sourceName) {
    const { personas, personaMap } = S.fusion;
    if (!sourceName || !personaMap[sourceName]) return [];
    const results = [];
    const seen = new Set();
    for (const otherName of personas) {
        if (otherName === sourceName) continue;
        const outcome = calcForwardFusionWeb([sourceName, otherName]);
        if (outcome && outcome.data) {
            const key = `${otherName}=>${outcome.name}`;
            if (!seen.has(key)) {
                seen.add(key);
                results.push({ other: { name: otherName, data: personaMap[otherName] }, result: outcome, isSpecial: outcome.isSpecial });
            }
        }
    }
    return results.sort((a, b) => (a.result.data.level ?? 0) - (b.result.data.level ?? 0));
}

function setForwardSubTab(subTab) {
    S.fusion.forwardSubTab = subTab;
    const series = SERIES.find(s=>s.id===S.series);
    renderForwardFusionScreen(series?.color||'#2196F3');
}

function renderForwardFusionScreen(color) {
    const el = document.getElementById('fusionContent');
    const { forwardSubTab, forwardSlots, forwardSource, forwardQuery } = S.fusion;

    let html = `
    <div class="fusion-subtab-bar">
        <button class="fusion-subtab-btn ${forwardSubTab==='chamber'?'active':''}" onclick="setForwardSubTab('chamber')">
            <span>Combine Ingredients (2-3)</span>
        </button>
        <button class="fusion-subtab-btn ${forwardSubTab==='fromPersona'?'active':''}" onclick="setForwardSubTab('fromPersona')">
            <span>Browse by Base Persona</span>
        </button>
    </div>`;

    if (forwardSubTab === 'chamber') {
        const slot0 = forwardSlots[0], slot1 = forwardSlots[1], slot2 = forwardSlots[2];
        const result = calcForwardFusionWeb(forwardSlots);

        html += `
        <div class="forward-chamber-wrap">
            <div style="display:flex;justify-content:space-between;align-items:center">
                <div style="font-weight:700;font-size:1rem;color:var(--text)">Ingredient Slots:</div>
                <div style="display:flex;gap:6px">
                    <button class="slot-action-btn" onclick="swapForwardIngredients()" ${(!slot0&&!slot1)?'disabled style=\"opacity:.4;cursor:not-allowed\"':''}>Swap</button>
                    <button class="slot-action-btn" onclick="clearForwardSlots()" ${(!slot0&&!slot1&&!slot2)?'disabled style=\"opacity:.4;cursor:not-allowed\"':''}>Clear All</button>
                </div>
            </div>

            <!-- Slot 1 -->
            ${renderForwardSlotCard(0, slot0, '1st Ingredient (Required)', color)}

            <!-- Slot 2 -->
            ${renderForwardSlotCard(1, slot1, '2nd Ingredient (Required)', color)}

            <!-- Slot 3 -->
            ${renderForwardSlotCard(2, slot2, '3rd Ingredient (Optional / Triangle)', color)}

            <div style="font-weight:700;font-size:1rem;color:var(--text);margin-top:12px">Fused Persona Result:</div>
            ${renderForwardResultBox(result, color)}
        </div>`;
    } else {
        // From Persona sub-tab
        const sourceData = forwardSource ? S.fusion.personaMap[forwardSource] : null;
        html += `
        <div class="forward-chamber-wrap">
            <div style="font-weight:700;font-size:1rem;color:var(--text)">Base Persona to Fuse:</div>
            ${sourceData ? `
                <div class="forward-slot-card" style="border-left:4px solid ${color}">
                    <div onclick="openForwardPicker('source')" style="flex:1;cursor:pointer">
                        <div style="font-weight:700;font-size:1.05rem;color:var(--text)">${sourceData.name || forwardSource}</div>
                        <div style="font-size:.85rem;color:${color}">${sourceData.arcana||sourceData.race||''} · Lv. ${sourceData.level??sourceData.lvl??'?'}</div>
                    </div>
                    <div style="display:flex;align-items:center;gap:6px">
                        <button class="slot-action-btn" onclick="openForwardPicker('source')">Change</button>
                        <button class="slot-clear-btn" onclick="clearForwardSource()" title="Remove">✕</button>
                    </div>
                </div>
            ` : `
                <div class="forward-slot-card forward-slot-empty" onclick="openForwardPicker('source')">
                    <div style="display:flex;align-items:center;gap:10px">
                        <div class="slot-num-badge">+</div>
                        <div>
                            <div style="font-weight:700;color:${color}">+ Select Base Persona</div>
                            <div style="font-size:.8rem;color:var(--text2)">Explore all Personas you can fuse with this base</div>
                        </div>
                    </div>
                    <div class="slot-add-chip" style="background:${color}22;color:${color}">Select +</div>
                </div>
            `}
        `;

        if (sourceData) {
            html += `
            <div class="search-wrap" style="margin-top:8px">
                <input id="forwardSearchInput" class="search-input" type="text" placeholder="Filter outputs or partner persona..." value="${esc(forwardQuery||'')}" oninput="onForwardQuery(this.value)">
            </div>
            <div id="forwardOutputCount" class="fusion-count"></div>
            <div id="forwardOutputList" style="display:flex;flex-direction:column;gap:8px"></div>`;
        }
        html += `</div>`;
    }

    el.innerHTML = html;
    el.scrollTop = 0;

    if (forwardSubTab === 'fromPersona' && forwardSource && S.fusion.personaMap[forwardSource]) {
        renderForwardOutputList(color);
    }
}

function renderForwardOutputList(color) {
    const listEl = document.getElementById('forwardOutputList');
    const countEl = document.getElementById('forwardOutputCount');
    if (!listEl) return;
    const allRecipes = calcForwardFusionsFromWeb(S.fusion.forwardSource);
    const q = (S.fusion.forwardQuery || '').toLowerCase();
    const filtered = q ? allRecipes.filter(r =>
        r.result.name.toLowerCase().includes(q) ||
        (r.result.data.arcana||'').toLowerCase().includes(q) ||
        r.other.name.toLowerCase().includes(q)
    ) : allRecipes;

    if (countEl) {
        countEl.textContent = `${filtered.length} possible fusion${filtered.length!==1?'s':''} found`;
    }

    if (!filtered.length) {
        listEl.innerHTML = `<div class="empty-state">No fusions match filter</div>`;
        return;
    }

    listEl.innerHTML = filtered.map(r => `
        <div class="fusion-recipe-card" style="margin:0;cursor:pointer" onclick="selectPersona('${esc(r.result.name)}')">
            <div style="flex:1">
                <div style="font-size:.82rem;color:var(--text2)">+ ${r.other.name} (${r.other.data.arcana||''} Lv.${r.other.data.level??'?'})</div>
                <div style="font-weight:700;font-size:1rem;color:var(--text);margin-top:2px">
                    = ${r.result.name}
                    ${r.isSpecial ? `<span class="badge" style="background:#FFD70022;color:#FFD700;font-size:.7rem;margin-left:4px">Special</span>` : ''}
                </div>
                <div style="font-size:.82rem;color:${color}">${r.result.data.arcana||''} · Lv. ${r.result.data.level??'?'}</div>
            </div>
            <div style="color:var(--text2);font-size:1.25rem">›</div>
        </div>
    `).join('');
}

function renderForwardSlotCard(slotIndex, personaName, label, color) {
    const p = personaName ? S.fusion.personaMap[personaName] : null;
    if (!p) {
        return `
        <div class="forward-slot-card forward-slot-empty" onclick="openForwardPicker(${slotIndex})">
            <div style="display:flex;align-items:center;gap:10px">
                <div class="slot-num-badge">${slotIndex + 1}</div>
                <div>
                    <div style="font-weight:700;font-size:.95rem;color:${color}">+ ${label}</div>
                    <div style="font-size:.78rem;color:var(--text3)">Click here to choose a Persona</div>
                </div>
            </div>
            <div class="slot-add-chip" style="background:${color}22;color:${color}">Select +</div>
        </div>`;
    }
    return `
    <div class="forward-slot-card" style="border-left:4px solid ${color}">
        <div onclick="openForwardPicker(${slotIndex})" style="flex:1;cursor:pointer">
            <div style="font-size:.72rem;color:var(--text3);text-transform:uppercase;font-weight:700">Ingredient ${slotIndex + 1}</div>
            <div style="font-weight:700;font-size:1.05rem;color:var(--text);margin-top:1px">${personaName}</div>
            <div style="font-size:.82rem;color:${color}">${p.arcana||p.race||''} · Lv. ${p.level??p.lvl??'?'}</div>
        </div>
        <div style="display:flex;align-items:center;gap:6px">
            <button class="slot-action-btn" onclick="openForwardPicker(${slotIndex})">Change</button>
            <button class="slot-clear-btn" onclick="clearForwardSlot(${slotIndex})" title="Remove">✕</button>
        </div>
    </div>`;
}

function renderForwardResultBox(result, color) {
    const activeCount = S.fusion.forwardSlots.filter(Boolean).length;
    if (activeCount === 0) {
        return `<div class="empty-state" style="padding:24px;border:1px dashed var(--hairline-soft);border-radius:var(--r-lg)">Select at least 2 ingredients above to fuse</div>`;
    }
    if (activeCount === 1) {
        return `<div class="empty-state" style="padding:24px;border:1px dashed var(--brass);color:var(--brass);border-radius:var(--r-lg);font-weight:600">1 ingredient selected. Click Slot 2 above to complete the fusion!</div>`;
    }
    if (!result || !result.data) {
        return `<div class="empty-state" style="padding:24px;border:1px solid #ff525244;color:#ff5252;border-radius:var(--r-lg);font-weight:700">No Valid Fusion Combination for these ingredients</div>`;
    }

    const p = result.data;
    const level = p.level ?? p.lvl ?? '?';
    const arcana = p.arcana || p.race || 'Unknown';
    const elems = ELEMENTS[S.series] || ELEMENTS.p5;
    const estCost = Math.round((Number(level || 1) * Number(level || 1) * 27) + 2000);

    return `
    <div class="forward-result-container" style="border-color:${color}">
        <div class="forward-result-header">
            <div>
                ${result.isSpecial ? `<div style="font-size:.7rem;font-weight:800;color:#FFD700;letter-spacing:.08em;margin-bottom:2px">SPECIAL FUSION</div>` : ''}
                <div class="forward-result-name">${result.name}</div>
                <div class="forward-result-sub" style="color:${color}">${arcana} · Lv. ${level}</div>
            </div>
            <div class="level-badge" style="background:${color}22;color:${color};font-size:1rem;padding:6px 12px;border-radius:8px">Lv. ${level}</div>
        </div>

        ${p.stats && p.stats.length >= 5 ? `
            <div class="forward-stats-grid">
                <div><div class="forward-stat-label">St</div><div class="forward-stat-val">${p.stats[0]}</div></div>
                <div><div class="forward-stat-label">Ma</div><div class="forward-stat-val">${p.stats[1]}</div></div>
                <div><div class="forward-stat-label">En</div><div class="forward-stat-val">${p.stats[2]}</div></div>
                <div><div class="forward-stat-label">Ag</div><div class="forward-stat-val">${p.stats[3]}</div></div>
                <div><div class="forward-stat-label">Lu</div><div class="forward-stat-val">${p.stats[4]}</div></div>
            </div>
        ` : ''}

        ${p.resists ? `
            <div style="margin:10px 0">
                <div style="font-size:.8rem;font-weight:700;color:var(--text2);margin-bottom:4px">Resistances</div>
                <div class="resist-text">${parseResists(p.resists, elems)}</div>
            </div>
        ` : ''}

        <div style="display:flex;justify-content:space-between;align-items:center;margin:12px 0 16px">
            <span style="font-size:.85rem;color:var(--text2)">Estimated Summon Cost:</span>
            <span class="forward-cost-tag">¥ ${estCost.toLocaleString()}</span>
        </div>

        <button class="nav-card" style="width:100%;justify-content:center;font-weight:700;background:var(--raised);padding:12px;border-radius:var(--r-md);color:${color};border:1px solid ${color}44;cursor:pointer" onclick="selectPersona('${esc(result.name)}')">
            View Persona Compendium Entry ›
        </button>
    </div>`;
}

function openForwardPicker(slot) {
    S.fusion.activePickerSlot = slot;
    renderPersonaPickerModal();
}

function closePersonaPicker() {
    const modal = document.getElementById('personaPickerModal');
    if (modal) modal.remove();
}

function selectPersonaFromPicker(name) {
    if (S.fusion.activePickerSlot === 'source') {
        S.fusion.forwardSource = name;
        S.fusion.forwardSubTab = 'fromPersona';
    } else if (S.fusion.activePickerSlot === 'skillRouteTarget') {
        S.fusion.skillRouteTarget = name;
    } else {
        const slotIdx = parseInt(S.fusion.activePickerSlot, 10);
        if (!isNaN(slotIdx) && slotIdx >= 0 && slotIdx <= 2) {
            S.fusion.forwardSlots[slotIdx] = name;
            S.fusion.forwardSubTab = 'chamber';
        }
    }
    closePersonaPicker();
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    if (S.fusion.mode === 'skillRoute') {
        renderSkillRouteScreen(color);
    } else {
        renderForwardFusionScreen(color);
    }
}

function clearForwardSlot(slotIdx) {
    S.fusion.forwardSlots[slotIdx] = null;
    const series = SERIES.find(s=>s.id===S.series);
    renderForwardFusionScreen(series?.color||'#2196F3');
}

function clearForwardSlots() {
    S.fusion.forwardSlots = [null, null, null];
    const series = SERIES.find(s=>s.id===S.series);
    renderForwardFusionScreen(series?.color||'#2196F3');
}

function clearForwardSource() {
    S.fusion.forwardSource = null;
    const series = SERIES.find(s=>s.id===S.series);
    renderForwardFusionScreen(series?.color||'#2196F3');
}

function swapForwardIngredients() {
    const temp = S.fusion.forwardSlots[0];
    S.fusion.forwardSlots[0] = S.fusion.forwardSlots[1];
    S.fusion.forwardSlots[1] = temp;
    const series = SERIES.find(s=>s.id===S.series);
    renderForwardFusionScreen(series?.color||'#2196F3');
}

function onForwardQuery(val) {
    S.fusion.forwardQuery = val;
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    debounceSearch(() => renderForwardOutputList(color));
}

function openSkillRouteTargetPicker() {
    S.fusion.activePickerSlot = 'skillRouteTarget';
    renderPersonaPickerModal();
}

function clearSkillRouteTarget() {
    S.fusion.skillRouteTarget = null;
    const series = SERIES.find(s=>s.id===S.series);
    renderSkillRouteScreen(series?.color||'#2196F3');
}

function openSkillRouteSkillPicker() {
    renderSkillPickerModal();
}

function clearSkillRouteSkills() {
    S.fusion.skillRouteSkills = [];
    S.fusion.skillRouteSkill = null;
    const series = SERIES.find(s=>s.id===S.series);
    renderSkillRouteScreen(series?.color||'#2196F3');
}

function removeSkillRouteSkill(skillName) {
    S.fusion.skillRouteSkills = (S.fusion.skillRouteSkills || []).filter(s => s !== skillName);
    if (S.fusion.skillRouteSkill === skillName) {
        S.fusion.skillRouteSkill = S.fusion.skillRouteSkills[0] || null;
    }
    const series = SERIES.find(s=>s.id===S.series);
    renderSkillRouteScreen(series?.color||'#2196F3');
}

function selectSkillFromPicker(skillName) {
    if (!S.fusion.skillRouteSkills) S.fusion.skillRouteSkills = [];
    if (!S.fusion.skillRouteSkills.includes(skillName)) {
        S.fusion.skillRouteSkills.push(skillName);
    }
    S.fusion.skillRouteSkill = skillName;
    closeSkillPicker();
    const series = SERIES.find(s=>s.id===S.series);
    renderSkillRouteScreen(series?.color||'#2196F3');
}

function setSkillRoutePreload(targetName, skillName) {
    if (targetName) S.fusion.skillRouteTarget = targetName;
    if (skillName) {
        S.fusion.skillRouteSkills = [skillName];
        S.fusion.skillRouteSkill = skillName;
    }
    S.fusion.mode = 'skillRoute';
    navigate('fusion');
}

async function ensureSkillsLoaded() {
    const key = `skills_${S.game}`;
    if (!S.rawData[key]) {
        try {
            const r = await fetch(SKILL_PATHS[S.game]);
            if (r.ok) {
                const raw = await r.json();
                S.rawData[key] = normalizeListData(raw, 'skills');
            }
        } catch(e) {}
    }
    return S.rawData[key] || [];
}

function renderPersonaPickerModal() {
    closePersonaPicker();
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';

    const modal = document.createElement('div');
    modal.id = 'personaPickerModal';
    modal.className = 'picker-modal-backdrop';
    modal.innerHTML = `
        <div class="picker-modal-sheet">
            <div class="picker-modal-header">
                <div style="font-weight:700;font-size:1.1rem;color:var(--text)">Select Persona</div>
                <button class="icon-btn" onclick="closePersonaPicker()">✕</button>
            </div>
            <div style="padding:10px 16px 0">
                <div class="search-wrap">
                    <svg class="search-icon-svg" viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 14z"/></svg>
                    <input id="pickerModalSearch" class="search-input" type="text" placeholder="Search by name or arcana..." oninput="onPickerModalFilter(this.value)">
                </div>
            </div>
            <div id="pickerModalList" class="list-content" style="flex:1;overflow-y:auto;padding:8px 16px">
                ${renderPickerModalItems('', color)}
            </div>
        </div>
    `;
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closePersonaPicker();
    });
    document.body.appendChild(modal);
    setTimeout(() => document.getElementById('pickerModalSearch')?.focus(), 50);
}

function renderPickerModalItems(query, color) {
    const q = (query || '').toLowerCase();
    const personas = S.fusion.personas || [];
    const filtered = q ? personas.filter(name => {
        const p = S.fusion.personaMap[name];
        return name.toLowerCase().includes(q) || (p.arcana||p.race||'').toLowerCase().includes(q);
    }) : personas;

    if (!filtered.length) return `<div class="empty-state">No personas match search</div>`;

    return filtered.map(name => {
        const p = S.fusion.personaMap[name];
        const level = p.level ?? p.lvl ?? '?';
        const arcana = p.arcana || p.race || 'Unknown';
        return `
        <div class="row-card" onclick="selectPersonaFromPicker('${esc(name)}')">
            <div class="level-badge" style="background:${color}22;color:${color}">${level}</div>
            <div class="row-main">
                <div class="row-name">${name}</div>
                <div class="row-sub">${arcana}</div>
            </div>
            <div class="row-hint" style="color:${color}">+</div>
        </div>`;
    }).join('');
}

function onPickerModalFilter(val) {
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    const listEl = document.getElementById('pickerModalList');
    if (listEl) listEl.innerHTML = renderPickerModalItems(val, color);
}

/* ── Skill Modal Picker ────────────────────────────────────────────────────── */
async function renderSkillPickerModal() {
    closeSkillPicker();
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    await ensureSkillsLoaded();

    const modal = document.createElement('div');
    modal.id = 'skillPickerModal';
    modal.className = 'picker-modal-backdrop';
    modal.innerHTML = `
        <div class="picker-modal-sheet">
            <div class="picker-modal-header">
                <div style="font-weight:700;font-size:1.1rem;color:var(--text)">Select Desired Skill</div>
                <button class="icon-btn" onclick="closeSkillPicker()">✕</button>
            </div>
            <div style="padding:10px 16px 0">
                <div class="search-wrap">
                    <svg class="search-icon-svg" viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 14z"/></svg>
                    <input id="skillPickerModalSearch" class="search-input" type="text" placeholder="Search skills by name, element, effect..." oninput="onSkillPickerModalFilter(this.value)">
                </div>
            </div>
            <div id="skillPickerModalList" class="list-content" style="flex:1;overflow-y:auto;padding:8px 16px">
                ${renderSkillPickerModalItems('', color)}
            </div>
        </div>
    `;
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeSkillPicker();
    });
    document.body.appendChild(modal);
    setTimeout(() => document.getElementById('skillPickerModalSearch')?.focus(), 50);
}

function closeSkillPicker() {
    const modal = document.getElementById('skillPickerModal');
    if (modal) modal.remove();
}

function renderSkillPickerModalItems(query, color) {
    const q = (query || '').toLowerCase();
    const skills = S.rawData[`skills_${S.game}`] || [];
    const filtered = q ? skills.filter(sk =>
        sk.name.toLowerCase().includes(q) ||
        (sk.element||sk.type||'').toLowerCase().includes(q) ||
        (sk.effect||'').toLowerCase().includes(q)
    ) : skills;

    if (!filtered.length) return `<div class="empty-state">No skills match search</div>`;

    return filtered.map(sk => `
        <div class="row-card" onclick="selectSkillFromPicker('${esc(sk.name)}')">
            <div class="row-main">
                <div class="row-name" style="font-weight:700">${sk.name}</div>
                <div class="row-sub">${sk.effect || sk.element || ''}</div>
            </div>
            <div class="row-hint" style="color:#FFD700;font-size:.8rem">${sk.cost || sk.element || '+'}</div>
        </div>
    `).join('');
}

function onSkillPickerModalFilter(val) {
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    const listEl = document.getElementById('skillPickerModalList');
    if (listEl) listEl.innerHTML = renderSkillPickerModalItems(val, color);
}

/* ── Multi-Skill & Multi-Part Fusion Tree Engine ─────────────────────────── */
function findLearners(skillName) {
    const list = [];
    const { personaMap } = S.fusion;
    for (const [pname, p] of Object.entries(personaMap)) {
        if (p.skills && p.skills[skillName] !== undefined) {
            const at = p.skills[skillName];
            list.push({
                name: pname,
                data: p,
                atLevel: at < 1 ? 'Innate' : at >= 100 ? 'Special' : `Lv. ${Math.floor(at)}`,
                level: p.level ?? p.lvl ?? 0
            });
        }
    }
    return list.sort((a, b) => a.level - b.level);
}

function findRouteForPersona(destinationPersona, skillName) {
    const { personaMap } = S.fusion;
    const dest = personaMap[destinationPersona];
    if (!dest) return null;
    if (dest.skills && dest.skills[skillName] !== undefined) {
        const at = dest.skills[skillName];
        return {
            type: 'direct_learner',
            persona: destinationPersona,
            personaData: dest,
            atLevel: at < 1 ? 'Innate' : at >= 100 ? 'Special' : `Lv. ${Math.floor(at)}`
        };
    }
    const sources = findLearners(skillName).slice(0, 8);
    for (const src of sources) {
        if (src.name === destinationPersona) continue;
        for (const partner of (S.fusion.personas || [])) {
            if (partner === src.name) continue;
            const res = calcForwardFusionWeb([src.name, partner]);
            if (res && res.name === destinationPersona) {
                return {
                    type: 'fuse_step',
                    source: src,
                    partner: { name: partner, data: personaMap[partner] },
                    result: destinationPersona,
                    resultData: dest,
                    atLevel: src.atLevel
                };
            }
        }
    }
    return null;
}

function calcSkillInheritanceRoutes(targetName, skillNamesInput) {
    const { personaMap, specialData } = S.fusion;
    if (!targetName || !personaMap[targetName]) return null;

    const skillNames = Array.isArray(skillNamesInput)
        ? skillNamesInput.filter(Boolean)
        : (skillNamesInput ? [skillNamesInput] : []);

    if (!skillNames.length) return null;

    const target = personaMap[targetName];

    // 1. Natural check & itemization
    const naturalSkills = [];
    const neededSkills = [];
    const itemizers = {};

    for (const sk of skillNames) {
        if (target.skills && target.skills[sk] !== undefined) {
            const at = target.skills[sk];
            naturalSkills.push({
                skill: sk,
                atLevel: at < 1 ? 'Innate' : at >= 100 ? 'Special' : `Lv. ${Math.floor(at)}`
            });
        } else {
            neededSkills.push(sk);
        }

        for (const [pname, p] of Object.entries(personaMap)) {
            if (p.item === sk) {
                if (!itemizers[sk]) itemizers[sk] = [];
                itemizers[sk].push({ name: pname, data: p, isAlarm: false });
            } else if (p.itemr === sk) {
                if (!itemizers[sk]) itemizers[sk] = [];
                itemizers[sk].push({ name: pname, data: p, isAlarm: true });
            }
        }
    }

    const targetRecipes = calcFusionRecipes(targetName) || [];
    const multiTrees = [];
    const singleDirectRoutes = [];
    const singleTwoStepRoutes = [];

    // Single Skill Mode Routes
    if (neededSkills.length === 1) {
        const sk = neededSkills[0];
        const sources = findLearners(sk);

        for (const recipe of targetRecipes) {
            if (specialData[targetName]) {
                for (const ing of recipe) {
                    const src = sources.find(s => s.name === ing.name);
                    if (src) {
                        singleDirectRoutes.push({
                            type: 'special_direct',
                            source: src,
                            allIngredients: recipe.map(r => r.name),
                            skill: sk,
                            targetName
                        });
                    }
                }
            } else if (recipe.length === 2) {
                const [pA, pB] = recipe;
                const srcA = sources.find(s => s.name === pA.name);
                const srcB = sources.find(s => s.name === pB.name);
                if (srcA) {
                    singleDirectRoutes.push({ type: 'direct_2p', source: srcA, partner: pB, skill: sk, targetName });
                } else if (srcB) {
                    singleDirectRoutes.push({ type: 'direct_2p', source: srcB, partner: pA, skill: sk, targetName });
                }
            }
            if (singleDirectRoutes.length >= 10) break;
        }

        const topSources = sources.slice(0, 10);
        const seenChains = new Set();

        for (const recipe of targetRecipes) {
            if (specialData[targetName]) {
                for (const ing of recipe) {
                    for (const src of topSources) {
                        if (src.name === ing.name) continue;
                        for (const other of (S.fusion.personas || [])) {
                            if (other === src.name) continue;
                            const bridge = calcForwardFusionWeb([src.name, other]);
                            if (bridge && bridge.name === ing.name) {
                                const key = `${src.name}+${other}=>${ing.name}`;
                                if (!seenChains.has(key)) {
                                    seenChains.add(key);
                                    singleTwoStepRoutes.push({
                                        type: '2step_special',
                                        source: src,
                                        skill: sk,
                                        step1: { p1: src.name, p2: other, result: ing.name, resultData: bridge.data },
                                        step2: { specialRecipe: recipe.map(r => r.name), result: targetName }
                                    });
                                }
                            }
                            if (singleTwoStepRoutes.length >= 8) break;
                        }
                        if (singleTwoStepRoutes.length >= 8) break;
                    }
                    if (singleTwoStepRoutes.length >= 8) break;
                }
            } else if (recipe.length === 2) {
                const [pA, pB] = recipe;
                for (const src of topSources) {
                    if (src.name === pA.name || src.name === pB.name) continue;
                    for (const other of (S.fusion.personas || [])) {
                        if (other === src.name) continue;
                        const bridge = calcForwardFusionWeb([src.name, other]);
                        if (bridge && bridge.name === pA.name) {
                            const key = `${src.name}+${other}=>${pA.name}+${pB.name}`;
                            if (!seenChains.has(key)) {
                                seenChains.add(key);
                                singleTwoStepRoutes.push({
                                    type: '2step_2p',
                                    source: src,
                                    skill: sk,
                                    step1: { p1: src.name, p2: other, result: pA.name, resultData: bridge.data },
                                    step2: { p1: pA.name, p2: pB.name, result: targetName }
                                });
                            }
                        }
                        if (singleTwoStepRoutes.length >= 8) break;
                    }
                    if (singleTwoStepRoutes.length >= 8) break;
                }
            }
            if (singleTwoStepRoutes.length >= 8) break;
        }
    }

    // Multi-Skill Tree Planner (for 2 or more skills, or full multi-part blueprints!)
    if (neededSkills.length >= 2) {
        if (specialData[targetName]) {
            for (const recipe of targetRecipes) {
                const ingNames = recipe.map(r => r.name);
                const branches = [];
                let allFound = true;

                for (const sk of neededSkills) {
                    let branchFound = null;
                    for (const ing of ingNames) {
                        const route = findRouteForPersona(ing, sk);
                        if (route) {
                            branchFound = { skill: sk, targetIngredient: ing, route };
                            break;
                        }
                    }
                    if (branchFound) {
                        branches.push(branchFound);
                    } else {
                        allFound = false;
                        break;
                    }
                }

                if (allFound && branches.length === neededSkills.length) {
                    multiTrees.push({
                        type: 'special_multi',
                        ingredients: ingNames,
                        branches,
                        targetName
                    });
                    if (multiTrees.length >= 6) break;
                }
            }
        } else {
            for (const recipe of targetRecipes) {
                const [pA, pB] = recipe;
                const sA = neededSkills[0];
                const sB = neededSkills[1];

                const rA = findRouteForPersona(pA.name, sA);
                const rB = findRouteForPersona(pB.name, sB);

                if (rA && rB) {
                    multiTrees.push({
                        type: '2p_tree',
                        parentA: { name: pA.name, data: pA.data, skill: sA, route: rA },
                        parentB: { name: pB.name, data: pB.data, skill: sB, route: rB },
                        extraSkills: neededSkills.slice(2),
                        targetName
                    });
                } else {
                    const rA_alt = findRouteForPersona(pA.name, sB);
                    const rB_alt = findRouteForPersona(pB.name, sA);
                    if (rA_alt && rB_alt) {
                        multiTrees.push({
                            type: '2p_tree',
                            parentA: { name: pA.name, data: pA.data, skill: sB, route: rA_alt },
                            parentB: { name: pB.name, data: pB.data, skill: sA, route: rB_alt },
                            extraSkills: neededSkills.slice(2),
                            targetName
                        });
                    }
                }
                if (multiTrees.length >= 6) break;
            }
        }
    }

    return {
        targetName,
        skillNames,
        naturalSkills,
        neededSkills,
        itemizers,
        singleDirectRoutes,
        singleTwoStepRoutes,
        multiTrees
    };
}

async function renderSkillRouteScreen(color) {
    const el = document.getElementById('fusionContent');
    const { skillRouteTarget, skillRouteSkills, personaMap } = S.fusion;
    const skills = skillRouteSkills || [];

    const targetData = skillRouteTarget ? personaMap[skillRouteTarget] : null;
    await ensureSkillsLoaded();

    let html = `
    <div class="skill-route-container">
        <div class="route-selector-grid">
            <!-- Target Persona Card -->
            <div>
                <div style="font-size:.75rem;font-weight:700;color:var(--text3);text-transform:uppercase;margin-bottom:4px">Target Persona</div>
                ${targetData ? `
                    <div class="forward-slot-card" style="border-left:4px solid ${color};margin:0">
                        <div onclick="openSkillRouteTargetPicker()" style="flex:1;cursor:pointer">
                            <div style="font-weight:700;font-size:1.05rem;color:var(--text)">${skillRouteTarget}</div>
                            <div style="font-size:.82rem;color:${color}">${targetData.arcana||targetData.race||''} · Lv. ${targetData.level??targetData.lvl??'?'}</div>
                        </div>
                        <div style="display:flex;align-items:center;gap:6px">
                            <button class="slot-action-btn" onclick="openSkillRouteTargetPicker()">Change</button>
                            <button class="slot-clear-btn" onclick="clearSkillRouteTarget()" title="Remove">✕</button>
                        </div>
                    </div>
                ` : `
                    <div class="forward-slot-card forward-slot-empty" onclick="openSkillRouteTargetPicker()" style="margin:0">
                        <div style="display:flex;align-items:center;gap:10px">
                            <div class="slot-num-badge">+</div>
                            <div>
                                <div style="font-weight:700;font-size:.92rem;color:${color}">+ Select Target Persona</div>
                                <div style="font-size:.78rem;color:var(--text3)">Persona to craft with desired skills</div>
                            </div>
                        </div>
                        <div class="slot-add-chip" style="background:${color}22;color:${color}">Select +</div>
                    </div>
                `}
            </div>

            <!-- Desired Skills Card (Supports Multiple Skills!) -->
            <div>
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4px">
                    <div style="font-size:.75rem;font-weight:700;color:var(--text3);text-transform:uppercase">
                        Desired Skills ${skills.length ? `(${skills.length})` : ''}
                    </div>
                    ${skills.length ? `<button class="inline-btn" style="font-size:.75rem;color:#EF5350" onclick="clearSkillRouteSkills()">Clear All</button>` : ''}
                </div>
                ${skills.length ? `
                    <div class="forward-slot-card" style="border-left:4px solid #FFD700;margin:0;flex-direction:column;align-items:stretch;gap:8px">
                        <div class="selected-skills-wrap">
                            ${skills.map(sk => `
                                <div class="skill-pill-chip">
                                    <span>${sk}</span>
                                    <button class="skill-pill-chip-del" onclick="removeSkillRouteSkill('${esc(sk)}')" title="Remove skill">✕</button>
                                </div>
                            `).join('')}
                        </div>
                        <div style="display:flex;justify-content:flex-end">
                            <button class="slot-action-btn" style="padding:4px 10px;font-size:.8rem;color:#FFD700" onclick="openSkillRouteSkillPicker()">+ Add Another Skill</button>
                        </div>
                    </div>
                ` : `
                    <div class="forward-slot-card forward-slot-empty" onclick="openSkillRouteSkillPicker()" style="margin:0">
                        <div style="display:flex;align-items:center;gap:10px">
                            <div class="slot-num-badge" style="color:#FFD700;border-color:#FFD700">+</div>
                            <div>
                                <div style="font-weight:700;font-size:.92rem;color:#FFD700">+ Select Desired Skill(s)</div>
                                <div style="font-size:.78rem;color:var(--text3)">Add one or more skills to inherit</div>
                            </div>
                        </div>
                        <div class="slot-add-chip" style="background:#FFD70022;color:#FFD700">Add +</div>
                    </div>
                `}
            </div>
        </div>
    `;

    // Quick Popular Skills Pill Row
    const popularSkills = ['Arms Master', 'Spell Master', 'Victory Cry', 'Debilitate', 'Megidolaon', 'Ali Dance', 'Drain Phys', 'Charge', 'Concentrate', 'Heat Riser', 'Enduring Soul', 'Insta-Heal'];
    html += `
    <div style="margin-top:14px">
        <div style="font-size:.82rem;font-weight:700;color:var(--text2);margin-bottom:6px">Quick-Add Skills:</div>
        <div style="display:flex;flex-wrap:wrap;gap:6px">
            ${popularSkills.map(skName => `
                <button class="slot-action-btn" style="padding:5px 10px;font-size:.8rem;${skills.includes(skName)?'border-color:#FFD700;color:#FFD700':''}" onclick="selectSkillFromPicker('${esc(skName)}')">
                    ${skills.includes(skName) ? '✓ ' : '+ '}${skName}
                </button>
            `).join('')}
        </div>
    </div>`;

    if (!skillRouteTarget || !skills.length) {
        html += `
        <div class="empty-state" style="margin-top:20px;padding:24px;border:1px dashed var(--hairline-soft);border-radius:var(--r-lg)">
            Select a Target Persona and at least one Desired Skill above to generate complete multi-branch fusion blueprints.
        </div>`;
    } else {
        const routes = calcSkillInheritanceRoutes(skillRouteTarget, skills);
        html += `<div style="margin-top:16px">`;

        // 1. Natural Skills Banner
        if (routes.naturalSkills && routes.naturalSkills.length > 0) {
            html += `
            <div class="route-banner" style="border-color:#81C784;margin-bottom:12px">
                <div class="route-banner-icon" style="color:#81C784">✓</div>
                <div>
                    <div class="route-banner-title" style="color:#81C784">Learned Naturally</div>
                    <div class="route-banner-desc">
                        <strong>${skillRouteTarget}</strong> already learns naturally:
                        ${routes.naturalSkills.map(n => `<strong>${n.skill}</strong> (${n.atLevel})`).join(', ')}.
                    </div>
                </div>
            </div>`;
        }

        // 2. Velvet Room Transmute / Itemization Banner
        const itemizerEntries = Object.entries(routes.itemizers);
        if (itemizerEntries.length > 0) {
            html += `
            <div class="route-banner" style="border-color:#FFD700;margin-bottom:12px">
                <div class="route-banner-icon" style="color:#FFD700">★</div>
                <div>
                    <div class="route-banner-title" style="color:#FFD700">Velvet Room Itemization Cards Available</div>
                    <div class="route-banner-desc">
                        ${itemizerEntries.map(([skName, list]) => `
                            <div><strong>${skName}</strong> card via: ${list.map(it => `<strong>${it.name}</strong>${it.isAlarm ? ' (Fusion Alarm)' : ''}`).join(', ')}</div>
                        `).join('')}
                    </div>
                </div>
            </div>`;
        }

        // 3. Multi-Branch Fusion Trees (for 2 or more skills)
        if (routes.multiTrees && routes.multiTrees.length > 0) {
            html += `
            <div class="fusion-count" style="font-size:.95rem;font-weight:700;margin:12px 0 8px;color:var(--text)">
                Multi-Part Fusion Blueprints (${routes.multiTrees.length} Plans Found)
            </div>
            <div style="display:flex;flex-direction:column;gap:14px;margin-bottom:16px">
                ${routes.multiTrees.map((tree, idx) => {
                    if (tree.type === 'special_multi') {
                        return `
                        <div class="route-card-wrap">
                            <div class="route-header">
                                <span class="route-step-badge" style="background:#FFD70022;color:#FFD700">Special Multi-Tree Plan #${idx+1}</span>
                                <span style="font-size:.8rem;color:var(--text3)">${tree.branches.length} Sub-Branches</span>
                            </div>
                            <!-- Sub Branches for each ingredient -->
                            <div style="display:flex;flex-direction:column;gap:8px;margin-bottom:10px">
                                ${tree.branches.map((b, bIdx) => `
                                    <div class="route-branch-box">
                                        <div class="route-branch-label" style="color:${color}">
                                            Part ${bIdx+1}: Craft ${b.targetIngredient} with ${b.skill}
                                        </div>
                                        ${b.route.type === 'direct_learner' ? `
                                            <div class="route-step-action">
                                                Train ingredient <strong style="color:${color}">${b.targetIngredient}</strong> to <strong>${b.route.atLevel}</strong> (learns <strong>${b.skill}</strong> naturally).
                                            </div>
                                        ` : `
                                            <div class="route-step-action">
                                                1. Train <strong style="color:${color}">${b.route.source.name}</strong> to <strong>${b.route.source.atLevel}</strong> to learn <strong>${b.skill}</strong>.
                                            </div>
                                            <div class="route-step-action" style="margin-top:4px">
                                                2. Fuse <strong>${b.route.source.name}</strong> + <strong>${b.route.partner.name}</strong> = <strong style="color:${color}">${b.targetIngredient}</strong> (inherits <strong>${b.skill}</strong>).
                                            </div>
                                        `}
                                    </div>
                                `).join('')}
                            </div>
                            <!-- Final Merge -->
                            <div class="route-merge-box">
                                <div class="route-branch-label" style="color:#FFD700">Final Step: Complete Special Fusion</div>
                                <div class="route-step-action">
                                    Combine all prepared ingredients (<strong>${tree.ingredients.join(' + ')}</strong>) in the Velvet Room = <strong style="color:${color}">${tree.targetName}</strong> inheriting ALL desired skills!
                                </div>
                            </div>
                        </div>`;
                    } else {
                        return `
                        <div class="route-card-wrap">
                            <div class="route-header">
                                <span class="route-step-badge" style="background:${color}22;color:${color}">Multi-Part Plan #${idx+1}</span>
                                <span style="font-size:.8rem;color:var(--text3)">2 Branches + Merge</span>
                            </div>
                            <!-- Branch A -->
                            <div class="route-branch-box">
                                <div class="route-branch-label" style="color:${color}">
                                    Part 1 (Branch A): Craft ${tree.parentA.name} with ${tree.parentA.skill}
                                </div>
                                ${tree.parentA.route.type === 'direct_learner' ? `
                                    <div class="route-step-action">
                                        Train <strong style="color:${color}">${tree.parentA.name}</strong> to <strong>${tree.parentA.route.atLevel}</strong> (learns <strong>${tree.parentA.skill}</strong> naturally).
                                    </div>
                                ` : `
                                    <div class="route-step-action">
                                        1. Train <strong style="color:${color}">${tree.parentA.route.source.name}</strong> to <strong>${tree.parentA.route.source.atLevel}</strong> to learn <strong>${tree.parentA.skill}</strong>.
                                    </div>
                                    <div class="route-step-action" style="margin-top:4px">
                                        2. Fuse <strong>${tree.parentA.route.source.name}</strong> + <strong>${tree.parentA.route.partner.name}</strong> = <strong style="color:${color}">${tree.parentA.name}</strong> (inherits <strong>${tree.parentA.skill}</strong>).
                                    </div>
                                `}
                            </div>
                            <!-- Branch B -->
                            <div class="route-branch-box">
                                <div class="route-branch-label" style="color:#FFD700">
                                    Part 2 (Branch B): Craft ${tree.parentB.name} with ${tree.parentB.skill}
                                </div>
                                ${tree.parentB.route.type === 'direct_learner' ? `
                                    <div class="route-step-action">
                                        Train <strong style="color:#FFD700">${tree.parentB.name}</strong> to <strong>${tree.parentB.route.atLevel}</strong> (learns <strong>${tree.parentB.skill}</strong> naturally).
                                    </div>
                                ` : `
                                    <div class="route-step-action">
                                        1. Train <strong style="color:#FFD700">${tree.parentB.route.source.name}</strong> to <strong>${tree.parentB.route.source.atLevel}</strong> to learn <strong>${tree.parentB.skill}</strong>.
                                    </div>
                                    <div class="route-step-action" style="margin-top:4px">
                                        2. Fuse <strong>${tree.parentB.route.source.name}</strong> + <strong>${tree.parentB.route.partner.name}</strong> = <strong style="color:#FFD700">${tree.parentB.name}</strong> (inherits <strong>${tree.parentB.skill}</strong>).
                                    </div>
                                `}
                            </div>
                            <!-- Final Merge -->
                            <div class="route-merge-box">
                                <div class="route-branch-label" style="color:#81C784">Final Step: Merge Branches</div>
                                <div class="route-step-action">
                                    Fuse <strong>${tree.parentA.name}</strong> (carries <strong>${tree.parentA.skill}</strong>) + <strong>${tree.parentB.name}</strong> (carries <strong>${tree.parentB.skill}</strong>) = <strong style="color:${color}">${tree.targetName}</strong> with BOTH skills!
                                </div>
                            </div>
                        </div>`;
                    }
                }).join('')}
            </div>`;
        }

        // 4. Single Skill Mode: Direct & 2-Step Pathways
        if (routes.neededSkills.length === 1) {
            const neededSk = routes.neededSkills[0];

            if (routes.singleDirectRoutes && routes.singleDirectRoutes.length > 0) {
                html += `
                <div class="fusion-count" style="font-size:.95rem;font-weight:700;margin:12px 0 8px;color:var(--text)">
                    Direct Recipes for ${neededSk} (${routes.singleDirectRoutes.length} Found)
                </div>
                <div style="display:flex;flex-direction:column;gap:10px;margin-bottom:16px">
                    ${routes.singleDirectRoutes.map((r, idx) => `
                        <div class="route-card-wrap">
                            <div class="route-header">
                                <span class="route-step-badge" style="background:${color}22;color:${color}">Direct Recipe #${idx+1}</span>
                            </div>
                            <div class="route-step-card" style="border-left:3px solid ${color}">
                                <div class="route-step-action">
                                    1. Train <strong style="color:${color}">${r.source.name}</strong> to <strong>${r.source.atLevel}</strong> (learns <strong>${neededSk}</strong>).
                                </div>
                                <div class="route-step-action" style="margin-top:4px">
                                    ${r.type === 'special_direct' ? `
                                        2. Combine all ingredients (<strong>${r.allIngredients.join(' + ')}</strong>) = <strong style="color:${color}">${r.targetName}</strong> (inherits <strong>${neededSk}</strong>).
                                    ` : `
                                        2. Fuse <strong>${r.source.name}</strong> + <strong>${r.partner.name}</strong> = <strong style="color:${color}">${r.targetName}</strong> (inherits <strong>${neededSk}</strong>).
                                    `}
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>`;
            }

            if (routes.singleTwoStepRoutes && routes.singleTwoStepRoutes.length > 0) {
                html += `
                <div class="fusion-count" style="font-size:.95rem;font-weight:700;margin:12px 0 8px;color:var(--text)">
                    Multi-Step Fusion Pathways for ${neededSk} (${routes.singleTwoStepRoutes.length} Found)
                </div>
                <div style="display:flex;flex-direction:column;gap:12px;margin-bottom:16px">
                    ${routes.singleTwoStepRoutes.map((chain, idx) => `
                        <div class="route-card-wrap">
                            <div class="route-header">
                                <span class="route-step-badge" style="background:${color}22;color:${color}">Pathway #${idx+1}</span>
                                <span style="font-size:.8rem;color:var(--text3)">2 Steps</span>
                            </div>
                            <div class="route-steps-flow">
                                <div class="route-step-card">
                                    <div class="route-step-header">
                                        <span class="route-step-badge" style="background:var(--card);color:${color}">Step 1</span>
                                        <span class="route-step-title">Learn ${neededSk}</span>
                                    </div>
                                    <div class="route-step-action">
                                        Train <strong style="color:${color}">${chain.source.name}</strong> (Lv. ${chain.source.level}) to <strong>${chain.source.atLevel}</strong> to learn <strong>${neededSk}</strong>.
                                    </div>
                                </div>
                                <div class="route-step-arrow">↓</div>
                                <div class="route-step-card">
                                    <div class="route-step-header">
                                        <span class="route-step-badge" style="background:var(--card);color:${color}">Step 2</span>
                                        <span class="route-step-title">Bridge Fusion</span>
                                    </div>
                                    <div class="route-step-action">
                                        Fuse <strong>${chain.step1.p1}</strong> + <strong>${chain.step1.p2}</strong> = <strong style="color:${color}">${chain.step1.result}</strong> (inherits <strong>${neededSk}</strong>).
                                    </div>
                                </div>
                                <div class="route-step-arrow">↓</div>
                                <div class="route-step-card" style="border-color:${color}66">
                                    <div class="route-step-header">
                                        <span class="route-step-badge" style="background:${color}22;color:${color}">Final Step</span>
                                        <span class="route-step-title">Craft ${skillRouteTarget}</span>
                                    </div>
                                    <div class="route-step-action">
                                        ${chain.type === '2step_special' ? `
                                            Combine <strong>${chain.step1.result}</strong> with remaining ingredients (${chain.step2.specialRecipe.filter(n=>n!==chain.step1.result).join(', ')}) = <strong style="color:${color}">${skillRouteTarget}</strong> with <strong>${neededSk}</strong>!
                                        ` : `
                                            Fuse <strong>${chain.step2.p1}</strong> + <strong>${chain.step2.p2}</strong> = <strong style="color:${color}">${skillRouteTarget}</strong> with <strong>${neededSk}</strong>!
                                        `}
                                    </div>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>`;
            }
        }

        if (routes.neededSkills.length > 0 && (!routes.multiTrees || routes.multiTrees.length === 0) && (!routes.singleDirectRoutes || routes.singleDirectRoutes.length === 0) && (!routes.singleTwoStepRoutes || routes.singleTwoStepRoutes.length === 0)) {
            html += `
            <div class="empty-state" style="padding:24px;border:1px solid #EF535044;color:#EF5350;border-radius:var(--r-lg);font-weight:700">
                No complete fusion pathway found to transfer all requested skills onto ${skillRouteTarget}. Some skills may be exclusive or incompatible.
            </div>`;
        }

        html += `</div>`;
    }

    html += `</div>`;
    el.innerHTML = html;
    el.scrollTop = 0;
}

/* ── Items ─────────────────────────────────────────────────────────────────── */
async function buildItemsScreen() {
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    document.getElementById('itemSearch').value = S.itemQuery;
    document.getElementById('itemSearchClear').style.display = S.itemQuery ? 'block' : 'none';
    
    // Check if we need to load data
    const key = `items_${S.game}`;
    if (!S.rawData[key]) {
        showLoadingItem();
        try {
            const r = await fetch(ITEM_PATHS[S.game]);
            if (!r.ok) throw new Error(r.statusText);
            const raw = await r.json();
            S.rawData[key] = normalizeListData(raw, 'items');
        } catch(e) { showEmptyItem('Failed to load: '+e.message); return; }
    }
    renderItems(S.rawData[key], color);
}

function renderItems(data, color) {
    const q = S.itemQuery.toLowerCase();
    const el = document.getElementById('itemContent');
    let items = data.filter(it => it.name.toLowerCase().includes(q)
        || (it.category||'').toLowerCase().includes(q)
        || (it.effect||'').toLowerCase().includes(q)
        || (it.description||'').toLowerCase().includes(q));

    if (!items.length) { el.innerHTML = `<div class="empty-state">No items found</div>`; return; }

    // Group by category
    const grouped = {};
    items.forEach(it => {
        const cat = it.category || 'Other';
        if (!grouped[cat]) grouped[cat] = [];
        grouped[cat].push(it);
    });

    const countLine = q ? `<div class="result-count">${items.length} of ${data.length} shown</div>` : '';
    el.innerHTML = countLine + Object.keys(grouped).sort().map(cat => `
        <div class="arcana-header">
            <div class="arcana-bar" style="background:${color}"></div>
            <div class="arcana-label" style="color:${color}">${cat} <span class="header-count">(${grouped[cat].length})</span></div>
        </div>
        ${grouped[cat].map(it => `
            <div class="row-card" onclick="openItem('${esc(it.name)}')">
                <div class="row-main">
                    <div class="row-name">${it.name}</div>
                    <div class="row-sub">${it.effect || it.description || ''}</div>
                </div>
                ${priceHint(it) ? `<div class="row-right"><div class="row-hp" style="font-size:.75rem">${priceHint(it)}</div></div>` : ''}
                <div class="row-hint" style="color:${color}">›</div>
            </div>`).join('')}
    `).join('');
}

/* Compact price hint for list rows — first segment of whatever price format the game uses */
function priceHint(it) {
    const p = (it.price || '').toString().trim();
    if (!p) return '';
    const first = p.split('|')[0].trim();
    return first.length > 14 ? first.slice(0, 13) + '…' : first;
}

function openItem(name) {
    const key = `items_${S.game}`;
    const it = S.rawData[key].find(i => i.name === name);
    if (!it) return;
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    
    document.getElementById('itemDetailPlaceholder').style.display = 'none';
    document.getElementById('itemDetailContentWrap').style.display = 'flex';
    document.getElementById('itemDetailTitle').textContent = it.name;
    
    let html = `
        <div class="section-card">
            <div class="section-title" style="color:${color}">${it.category || 'Item'}</div>
            <div class="info-row"><div class="info-label">Effect</div><div class="info-val">${it.effect || it.description || 'No effect listed'}</div></div>
            ${it.price?`<div class="info-row"><div class="info-label">Price</div><div class="info-val">${it.price}</div></div>`:''}
            ${it.location?`<div class="info-row"><div class="info-label">Location</div><div class="info-val">${it.location}</div></div>`:''}
        </div>
    `;
    document.getElementById('itemDetailContent').innerHTML = html;
}

function onItemSearch(val) { S.itemQuery=val; document.getElementById('itemSearchClear').style.display=val?'block':'none'; debounceSearch(()=>buildItemsScreen()); }
function clearItemSearch() { document.getElementById('itemSearch').value=''; onItemSearch(''); }
function showLoadingItem() { document.getElementById('itemContent').innerHTML=`<div class="loading-wrap"><div class="spinner"></div><div>Loading…</div></div>`; }
function showEmptyItem(msg) { document.getElementById('itemContent').innerHTML=`<div class="empty-state">${msg}</div>`; }

/* ── Skills ────────────────────────────────────────────────────────────────── */
async function buildSkillsScreen() {
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    document.getElementById('skillSearch').value = S.skillQuery;
    document.getElementById('skillSearchClear').style.display = S.skillQuery ? 'block' : 'none';
    
    const key = `skills_${S.game}`;
    if (!S.rawData[key]) {
        showLoadingSkill();
        try {
            const r = await fetch(SKILL_PATHS[S.game]);
            if (!r.ok) throw new Error(r.statusText);
            const raw = await r.json();
            S.rawData[key] = normalizeListData(raw, 'skills');
        } catch(e) { showEmptySkill('Failed to load: '+e.message); return; }
    }
    renderSkills(S.rawData[key], color);
}

function renderSkills(data, color) {
    const q = S.skillQuery.toLowerCase();
    const el = document.getElementById('skillContent');
    let items = data.filter(sk => sk.name.toLowerCase().includes(q) || (sk.element||sk.type||'').toLowerCase().includes(q));
    
    if (!items.length) { el.innerHTML = `<div class="empty-state">No skills found</div>`; return; }

    const grouped = {};
    items.forEach(sk => {
        const cat = sk.element || sk.type || 'Other';
        if (!grouped[cat]) grouped[cat] = [];
        grouped[cat].push(sk);
    });

    const countLine = q ? `<div class="result-count">${items.length} of ${data.length} shown</div>` : '';
    el.innerHTML = countLine + Object.keys(grouped).sort().map(cat => `
        <div class="arcana-header">
            <div class="arcana-bar" style="background:${color}"></div>
            <div class="arcana-label" style="color:${color}">${cat} <span class="header-count">(${grouped[cat].length})</span></div>
        </div>
        ${grouped[cat].map(sk => `
            <div class="row-card" onclick="openSkill('${esc(sk.name)}')">
                <div class="row-main">
                    <div class="row-name">${sk.name}</div>
                    <div class="row-sub">${sk.effect || ''}</div>
                </div>
                <div class="row-hint">${sk.cost || ''}</div>
            </div>`).join('')}
    `).join('');
}

function openSkill(name) {
    const key = `skills_${S.game}`;
    const sk = S.rawData[key].find(s => s.name === name);
    if (!sk) return;
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';

    document.getElementById('skillDetailPlaceholder').style.display = 'none';
    document.getElementById('skillDetailContentWrap').style.display = 'flex';
    document.getElementById('skillDetailTitle').textContent = sk.name;

    let html = `
        <div class="section-card">
            <div class="section-title" style="color:${color}">${sk.element || sk.type || 'Skill'}</div>
            <div class="info-row"><div class="info-label">Effect</div><div class="info-val">${sk.effect || 'No effect listed'}</div></div>
            ${sk.cost?`<div class="info-row"><div class="info-label">Cost</div><div class="info-val">${sk.cost}</div></div>`:''}
            <div style="margin-top:12px">
                <button class="slot-action-btn" style="width:100%;padding:10px 14px;font-size:.85rem;font-weight:700;color:${color}" onclick="setSkillRoutePreload(null, '${esc(sk.name)}')">
                    Transfer to Persona (Find Fusion Route) ›
                </button>
            </div>
        </div>
    `;
    html += `<div class="section-card" id="skillLearnedBy"><div class="section-title">Learned by</div><div class="loading-wrap" style="padding:12px"><div class="spinner" style="width:22px;height:22px"></div></div></div>`;
    document.getElementById('skillDetailContent').innerHTML = html;
    renderSkillLearnedBy(sk.name, color);
}

/* Cross-reference: which personas learn this skill, and at what level */
async function renderSkillLearnedBy(skillName, color) {
    const box = document.getElementById('skillLearnedBy');
    if (!box) return;
    const key = `personas_${S.game}`;
    if (!S.rawData[key]) {
        try {
            const r = await fetch(PERSONA_PATHS[S.game]);
            if (!r.ok) throw new Error(r.statusText);
            S.rawData[key] = await r.json();
        } catch(e) { box.style.display = 'none'; return; }
    }
    const data = S.rawData[key];
    const entries = Array.isArray(data) ? data.map(p=>[p.name,p]) : Object.entries(data);
    const learners = [];
    entries.forEach(([pname, p]) => {
        if (!p || !p.skills) return;
        const lvl = p.skills[skillName];
        if (lvl === undefined) return;
        learners.push({ name: pname, plevel: p.level ?? p.lvl ?? 0, at: lvl });
    });
    if (!learners.length) { box.style.display = 'none'; return; }
    learners.sort((a,b)=>a.plevel-b.plevel);
    const label = l => l.at < 1 ? 'Innate' : l.at >= 100 ? 'Special' : `Lv. ${Math.floor(l.at)}`;
    box.innerHTML = `<div class="section-title">Learned by <span class="header-count">(${learners.length})</span></div>` +
        learners.map(l => `
        <div class="skill-row" style="cursor:pointer" onclick="jumpToPersona('${esc(l.name)}')">
            <div class="skill-name">${l.name} <span style="color:var(--text3);font-size:.8em">Lv. ${l.plevel}</span></div>
            <div class="skill-level" style="color:${l.at<1?color:'var(--text2)'}">${label(l)}</div>
        </div>`).join('');
}

function jumpToPersona(name) {
    S.listMode = 'personas';
    navigate('list');
    // open once the persona data is guaranteed loaded by the list build
    const tryOpen = (attempts) => {
        if (S.rawData[`personas_${S.game}`]) { openPersona(name); return; }
        if (attempts > 0) setTimeout(()=>tryOpen(attempts-1), 200);
    };
    tryOpen(15);
}

function onSkillSearch(val) { S.skillQuery=val; document.getElementById('skillSearchClear').style.display=val?'block':'none'; debounceSearch(()=>buildSkillsScreen()); }
function clearSkillSearch() { document.getElementById('skillSearch').value=''; onSkillSearch(''); }
function showLoadingSkill() { document.getElementById('skillContent').innerHTML=`<div class="loading-wrap"><div class="spinner"></div><div>Loading…</div></div>`; }
function showEmptySkill(msg) { document.getElementById('skillContent').innerHTML=`<div class="empty-state">${msg}</div>`; }

/* ── Requests ──────────────────────────────────────────────────────────────── */
async function buildRequestsScreen() {
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    document.getElementById('requestSearch').value = S.requestQuery;
    document.getElementById('requestSearchClear').style.display = S.requestQuery ? 'block' : 'none';
    
    const key = `requests_${S.game}`;
    if (!S.rawData[key]) {
        showLoadingRequest();
        try {
            const r = await fetch(REQUEST_PATHS[S.game]);
            if (!r.ok) throw new Error(r.statusText);
            const raw = await r.json();
            S.rawData[key] = normalizeListData(raw, 'requests');
        } catch(e) { showEmptyRequest('Failed to load: '+e.message); return; }
    }
    renderRequests(S.rawData[key], color);
}

function renderRequests(data, color) {
    const q = S.requestQuery.toLowerCase();
    const el = document.getElementById('requestContent');
    const doneCount = data.filter(req => S.completedRequests.has(`${S.game}_req_${req.id || req.name}`)).length;
    const pct = data.length ? Math.round(doneCount / data.length * 100) : 0;
    const progressHtml = `
        <div class="req-progress-wrap">
            <div class="req-progress-top">
                <span>${doneCount} / ${data.length} completed</span>
                <button class="sort-chip ${S.hideCompletedReq?'active':''}" style="flex:0 0 auto;padding:4px 10px;font-size:.75rem;${S.hideCompletedReq?`color:${color};background:${color}22`:''}"
                        onclick="toggleHideCompleted()">Hide done</button>
            </div>
            <div class="req-progress-bar"><div class="req-progress-fill" style="width:${pct}%;background:${color}"></div></div>
        </div>`;
    let items = data.filter(req => req.name.toLowerCase().includes(q) || (req.giver||'').toLowerCase().includes(q));
    if (S.hideCompletedReq) items = items.filter(req => !S.completedRequests.has(`${S.game}_req_${req.id || req.name}`));

    if (!items.length) { el.innerHTML = progressHtml + `<div class="empty-state">${S.hideCompletedReq?'All matching requests are completed':'No requests found'}</div>`; return; }

    el.innerHTML = progressHtml + items.map(req => {
        const id = `${S.game}_req_${req.id || req.name}`;
        const isDone = S.completedRequests.has(id);
        return `
            <div class="row-card ${isDone?'completed':''}" onclick="openRequest('${esc(req.name)}')" style="${isDone?'opacity:0.6;background:var(--bg2)':''}">
                <div class="row-main">
                    <div class="row-name" style="${isDone?'text-decoration:line-through;color:var(--text3)':''}">${req.name}</div>
                    <div class="row-sub">${req.reward || '-'}</div>
                </div>
                ${isDone?`<div style="color:#4CAF50"><svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/></svg></div>`:''}
            </div>`;
    }).join('');
}

function openRequest(name) {
    const key = `requests_${S.game}`;
    const req = S.rawData[key].find(r => r.name === name);
    if (!req) return;
    S.currentRequest = req;
    const series = SERIES.find(s=>s.id===S.series);
    const color = series?.color||'#2196F3';
    
    document.getElementById('requestDetailPlaceholder').style.display = 'none';
    document.getElementById('requestDetailContentWrap').style.display = 'flex';
    document.getElementById('requestDetailTitle').textContent = req.name;
    
    const id = `${S.game}_req_${req.id || req.name}`;
    const isDone = S.completedRequests.has(id);
    document.getElementById('requestCompleteBtn').style.color = isDone ? '#4CAF50' : '';
    
    let html = `
        <div class="section-card">
            <div class="section-title" style="color:${color}">${req.available || 'Request'}</div>
            <div class="info-row"><div class="info-label">Reward</div><div class="info-val">${req.reward || '-'}</div></div>
            ${req.deadline?`<div class="info-row"><div class="info-label">Deadline</div><div class="info-val">${req.deadline}</div></div>`:''}
            ${req.giver||req.quest_giver?`<div class="info-row"><div class="info-label">Giver</div><div class="info-val">${req.giver||req.quest_giver}</div></div>`:''}
            ${req.description?`<div class="desc-box" style="margin-top:12px">${req.description}</div>`:''}
        </div>
    `;
    document.getElementById('requestDetailContent').innerHTML = html;
}

function toggleRequestComplete() {
    if (!S.currentRequest) return;
    const req = S.currentRequest;
    const id = `${S.game}_req_${req.id || req.name}`;
    if (S.completedRequests.has(id)) {
        S.completedRequests.delete(id);
    } else {
        S.completedRequests.add(id);
    }
    localStorage.setItem('completed_requests', JSON.stringify([...S.completedRequests]));
    openRequest(req.name);
    buildRequestsScreen(); // refresh list
}

function toggleHideCompleted() { S.hideCompletedReq = !S.hideCompletedReq; buildRequestsScreen(); }
function onRequestSearch(val) { S.requestQuery=val; document.getElementById('requestSearchClear').style.display=val?'block':'none'; debounceSearch(()=>buildRequestsScreen()); }
function clearRequestSearch() { document.getElementById('requestSearch').value=''; onRequestSearch(''); }
function showLoadingRequest() { document.getElementById('requestContent').innerHTML=`<div class="loading-wrap"><div class="spinner"></div><div>Loading…</div></div>`; }
function showEmptyRequest(msg) { document.getElementById('requestContent').innerHTML=`<div class="empty-state">${msg}</div>`; }
