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
const ELEMENTS = {
    p3:['Slash','Strike','Pierce','Fire','Ice','Elec','Wind','Light','Dark','Almighty'],
    p4:['Phys','Fire','Ice','Elec','Wind','Light','Dark','Almighty'],
    p5:['Phys','Gun','Fire','Ice','Elec','Wind','Psy','Nuke','Bless','Curse']
};

/* ── State ─────────────────────────────────────────────────────────────────── */
const S = {
    screen:'home', series:null, game:null,
    listMode:null, // 'personas'|'enemies'|'classroom'
    sort:'arcana', enemyTab:'enemies', query:'',
    detail:null, favorites:new Set(),
    rawData:{},
    slData:null, slQuery:'', slDetail:null,
    settings:{ showDlc:true, showEpisodeAigis:true, p3pProtagonist:'MALE' }
};

/* ── Boot ──────────────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    S.favorites = new Set(JSON.parse(localStorage.getItem('favs')||'[]'));
    const saved = localStorage.getItem('settings');
    if (saved) S.settings = {...S.settings, ...JSON.parse(saved)};
    buildHome();
});

/* ── Navigation ────────────────────────────────────────────────────────────── */
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
    if (to==='settings')    buildSettingsScreen();
}

/* ── Home ──────────────────────────────────────────────────────────────────── */
function buildHome() {
    document.getElementById('seriesList').innerHTML = SERIES.map(s => `
        <div class="series-card" style="background:linear-gradient(135deg,${s.color}dd,${s.color}88)"
             onclick="navigate('game','${s.id}')">
            <div class="series-card-bg-num">${s.id.replace('p','')}</div>
            <div class="series-card-text">
                <div class="series-card-title">${s.title}</div>
                <div class="series-card-sub">${s.games.length} game${s.games.length>1?'s':''}</div>
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
        { label:'Personas',        icon:'📖', available:true,  action:()=>{ S.listMode='personas'; navigate('list'); } },
        { label:'Fusion Calculator',icon:'✨', available:false, action:null },
        { label:'Enemies',         icon:'🛡', available:true,  action:()=>{ S.listMode='enemies';  navigate('list'); } },
        { label:slLabel,           icon:'👥', available:true,  action:()=>openSocialLinks() },
        { label:'Classroom Answers',icon:'🎓', available:true,  action:()=>{ S.listMode='classroom'; navigate('list'); } },
    ];

    document.getElementById('categoryList').innerHTML = categories.map(c => `
        <div class="category-row ${c.available?'':'category-row--locked'}"
             onclick="${c.available&&c.action ? 'categoryAction(\''+c.label+'\')' : ''}">
            <span class="category-icon">${c.icon}</span>
            <span class="category-label" style="${c.available?'color:var(--text)':'color:var(--text3)'}">${c.label}</span>
            ${c.available
                ? `<span class="category-chevron" style="color:${color}">›</span>`
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

/* ── List Screen ───────────────────────────────────────────────────────────── */
function buildListScreen(mode) {
    if (mode) S.listMode = mode;
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    const titles = { personas:'Personas', enemies:'Enemies', classroom:'Classroom Answers' };
    document.getElementById('listScreenTitle').textContent = titles[S.listMode]||'';

    // Sort bar (personas only)
    const sortBar = document.getElementById('sortBar');
    if (S.listMode==='personas') {
        sortBar.style.display='flex';
        sortBar.innerHTML = ['arcana','level','name'].map(opt=>`
            <button class="sort-chip ${S.sort===opt?'active':''}"
                    style="${S.sort===opt?`color:${color};background:${color}22`:''}"
                    onclick="setSort('${opt}','${color}')">
                ${opt[0].toUpperCase()+opt.slice(1)}
            </button>`).join('');
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
            const paths = { personas:PERSONA_PATHS, enemies:ENEMY_PATHS, classroom:CLASSROOM_PATHS };
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
    let items = Object.entries(data).filter(([name, p]) => {
        if (!S.settings.showDlc && p.isDlc) return false;
        if (!S.settings.showEpisodeAigis && p.episodeAigis) return false;
        return !q || name.toLowerCase().includes(q) || (p.arcana||p.race||'').toLowerCase().includes(q);
    });
    if (!items.length) { showEmpty('No personas found'); return; }

    let html = '';
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
    return `<div class="row-card" onclick="openPersona('${esc(name)}')">
        <div class="level-badge" style="background:${color}22;color:${color}">${level}</div>
        <div class="row-main">
            <div class="row-name">${name}</div>
            <div class="row-sub">${arcana}</div>
        </div>
        ${skills?`<div class="row-hint">${skills} skills</div>`:''}
    </div>`;
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
    if (q) pool = pool.filter(([name,e])=>name.toLowerCase().includes(q)||(e.arcana||'').toLowerCase().includes(q)||(e.area||'').toLowerCase().includes(q));
    if (!pool.length) { showEmpty('No enemies found'); return; }
    el.innerHTML = pool.map(([name,e])=>`
        <div class="row-card" onclick="openEnemy('${esc(name)}')">
            <div class="row-main">
                <div class="row-name">${name}</div>
                <div class="row-sub">${e.arcana||'Shadow'} · Lv. ${e.level||'?'}</div>
                ${e.area&&e.area!=='Unknown'?`<div class="row-hint" style="font-size:.75rem;color:var(--text3);margin-top:2px">${e.area}</div>`:''}
            </div>
            <div class="row-right">
                <div class="row-hp">${e.hp||''} HP</div>
                <div class="row-exp">${e.exp||''} EXP</div>
            </div>
        </div>`).join('');
}

/* ── Classroom ─────────────────────────────────────────────────────────────── */
function flattenClassroom(data) {
    if (Array.isArray(data)) return data;
    const items = [];
    Object.entries(data).forEach(([month, monthData]) => {
        if (typeof monthData!=='object') return;
        const inner = monthData.Classroom||monthData;
        Object.entries(inner).forEach(([date, qas]) => {
            if (!Array.isArray(qas)) return;
            qas.forEach(qa => items.push({Date:date,...qa}));
        });
    });
    return items;
}

function renderClassroom(data, q, el) {
    let items = flattenClassroom(data);
    if (q) items = items.filter(qa=>(qa.Question||'').toLowerCase().includes(q)||(qa.Answer||'').toLowerCase().includes(q));
    if (!items.length) { showEmpty('No answers found'); return; }
    el.innerHTML = items.map(qa=>`
        <div class="qa-card">
            ${qa.Date?`<div class="qa-date">${qa.Date}</div>`:''}
            <div class="qa-question">${qa.Question||'Question not available'}</div>
            <div class="qa-answer">${qa.Answer||'—'}</div>
        </div>`).join('');
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
            // Filter P5/P5R exclusives
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
        const ranks = Object.keys(data).filter(k=>!['P4G Exclusive','P5R Exclusive','P5R Reworked'].includes(k)&&typeof data[k]==='object');
        return `<div class="row-card" onclick="openSlDetail('${esc(arcana)}')">
            <div class="row-main">
                <div class="row-name">${arcana}</div>
                <div class="row-sub">${ranks.length} ranks</div>
            </div>
            <div class="level-badge" style="background:${color}22;color:${color}">Rank ${ranks.length}</div>
        </div>`;
    }).join('');
}

function openSlDetail(arcana) {
    S.slDetail = arcana;
    navigate('sldetail');
}

function buildSlDetailScreen() {
    if (!S.slDetail||!S.slData) return;
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    const entry  = S.slData.find(([a])=>a===S.slDetail);
    if (!entry) return;
    const [arcana, data] = entry;
    document.getElementById('slDetailTitle').textContent = arcana;

    const rankKeys = Object.keys(data).filter(k=>
        !['P4G Exclusive','P5R Exclusive','P5R Reworked'].includes(k) && typeof data[k]==='object'
    );

    let html = '';
    rankKeys.forEach((rankKey, i) => {
        const rank = data[rankKey];
        const nextRank = rank['Next Rank']||0;
        const isAuto = rankKey.toLowerCase().includes('auto');
        html += `<div class="section-card">
            <div class="section-title" style="color:${color}">${rankKey}${isAuto?' (Auto)':''}</div>`;
        if (nextRank) html += `<div class="info-row"><div class="info-label">Points to next</div><div class="info-val">${nextRank}</div></div>`;

        // Dialogue choices
        Object.entries(rank).forEach(([k,v]) => {
            if (k==='Next Rank'||typeof v==='object') return;
            if (k.match(/^[QA]?[0-9]+[:.]/)||k.startsWith('Phone')||k.startsWith('Any')) {
                const pts = parseInt(v)||0;
                const ptColor = pts>=3?'#4CAF50':pts>0?color:'var(--text3)';
                html += `<div class="skill-row">
                    <div class="skill-name" style="font-size:.875rem">${k}</div>
                    <div class="skill-level" style="color:${ptColor}">${pts>0?'+'+pts+' pts':'—'}</div>
                </div>`;
            } else if (typeof v==='string'&&v&&!k.match(/Exclusive|Reworked/)) {
                html += `<div class="info-row"><div class="info-label">${k}</div><div class="info-val">${v}</div></div>`;
            }
        });
        html += `</div>`;
    });
    document.getElementById('slDetailContent').innerHTML = html||`<div class="empty-state">No rank data</div>`;
    document.getElementById('slDetailContent').scrollTop = 0;
}

function onSlSearch(val) {
    S.slQuery = val;
    document.getElementById('slSearchClear').style.display = val?'block':'none';
    const series = SERIES.find(s=>s.id===S.series);
    renderSlList(series?.color||'#2196F3');
}
function clearSlSearch() { document.getElementById('slSearch').value=''; onSlSearch(''); }

/* ── Detail Screen ─────────────────────────────────────────────────────────── */
function openPersona(name) {
    const key = `personas_${S.game}`;
    const data = S.rawData[key];
    if (!data||!data[name]) return;
    S.detail = {type:'persona', name, data:data[name]};
    navigate('detail');
}
function openEnemy(name) {
    const key = `enemies_${S.game}`;
    const data = S.rawData[key];
    if (!data) return;
    const enemy = Array.isArray(data)?data.find(e=>e.name===name):data[name];
    if (!enemy) return;
    S.detail = {type:'enemy', name, data:enemy};
    navigate('detail');
}

function buildDetailScreen() {
    if (!S.detail) return;
    const series = SERIES.find(s=>s.id===S.series);
    const color  = series?.color||'#2196F3';
    document.getElementById('detailTitle').textContent = S.detail.name;
    const favId = `${S.game}_${S.detail.name}`;
    const isFav = S.favorites.has(favId);
    document.getElementById('favBtn').textContent = isFav?'♥':'♡';
    document.getElementById('favBtn').style.color  = isFav?color:'';
    if (S.detail.type==='persona') renderPersonaDetail(S.detail.name, S.detail.data, color);
    else renderEnemyDetail(S.detail.name, S.detail.data, color);
}

function renderPersonaDetail(name, p, color) {
    const el = document.getElementById('detailContent');
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
    </div>`;

    if (p.description) html += `<div class="desc-box">${p.description}</div>`;
    if (p.unlock) html += `<div class="unlock-box"><div class="unlock-icon">🔒</div><div><div class="unlock-label">Unlock</div><div class="unlock-text">${p.unlock}</div></div></div>`;

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

function renderEnemyDetail(name, e, color) {
    const el = document.getElementById('detailContent');
    const elems = ELEMENTS[S.series]||ELEMENTS.p5;
    let html = `<div class="section-card">
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
    </div>`;
    if (e.version) html += `<div class="desc-box">${e.version}</div>`;
    if (e.stats) {
        html += `<div class="section-card"><div class="section-title">Stats</div>`;
        [['Strength',e.stats.strength],['Magic',e.stats.magic],['Endurance',e.stats.endurance],['Agility',e.stats.agility],['Luck',e.stats.luck]].forEach(([l,v])=>{
            html += `<div class="info-row"><div class="info-label">${l}</div><div class="info-val">${v}</div></div>`;
        });
        html += `</div>`;
    }
    if (e.resists) html += `<div class="section-card"><div class="section-title">Resistances</div><div class="resist-text">${parseResists(e.resists,elems)}</div></div>`;
    if (e.skills&&e.skills.length) {
        html += `<div class="section-card"><div class="section-title">Skills</div>`;
        e.skills.forEach(s=>{ html += `<div class="skill-row"><div class="skill-name">${s}</div></div>`; });
        html += `</div>`;
    }
    if (e.phases&&e.phases.length) {
        e.phases.forEach(ph=>{
            html += `<div class="section-card"><div class="section-title">Phase: ${ph.name}</div>
                <div class="info-row"><div class="info-label">HP</div><div class="info-val">${ph.hp}</div></div>
                <div class="info-row"><div class="info-label">SP</div><div class="info-val">${ph.sp}</div></div>`;
            if (ph.resists) html += `<div style="margin-top:10px"><div class="section-title" style="font-size:.875rem">Resistances</div><div class="resist-text">${parseResists(ph.resists,elems)}</div></div>`;
            if (ph.skills?.length) { html += `<div style="margin-top:10px"><div class="section-title" style="font-size:.875rem">Skills</div>`; ph.skills.forEach(s=>{html+=`<div class="skill-row"><div class="skill-name">${s}</div></div>`;}); html+=`</div>`; }
            html += `</div>`;
        });
    }
    if (e.drops) {
        html += `<div class="section-card"><div class="section-title">Location & Drops</div>`;
        if (e.area&&e.area!=='Unknown') html+=`<div class="info-row"><div class="info-label">Area</div><div class="info-val">${e.area}</div></div>`;
        if (e.exp>0) html+=`<div class="info-row"><div class="info-label">EXP</div><div class="info-val">${e.exp}</div></div>`;
        if (e.drops.gem!=='-') html+=`<div class="info-row"><div class="info-label">Gem</div><div class="info-val">${e.drops.gem}</div></div>`;
        if (e.drops.item!=='-') html+=`<div class="info-row"><div class="info-label">Item</div><div class="info-val">${e.drops.item}</div></div>`;
        html += `</div>`;
    }
    el.innerHTML = html;
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
    const map={w:'weak',s:'resist',n:'null_',r:'repel',d:'absorb'};
    resistStr.split('').forEach((c,i)=>{ if(i<elems.length&&map[c]) result[map[c]].push(elems[i]); });
    return result;
}
function parseResists(str, elems) {
    const map={'-':'Normal',w:'Weak',s:'Strong',r:'Resist',n:'Null',d:'Drain'};
    return str.split('').map((c,i)=>{
        const r=map[c]||'Normal';
        if(r==='Normal'||i>=elems.length) return null;
        const colors={Weak:'#E57373',Null:'#B0BEC5',Drain:'#FFD54F',Resist:'#81C784',Strong:'#81C784'};
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

function onSearch(val) {
    S.query=val;
    document.getElementById('searchClear').style.display=val?'block':'none';
    const series=SERIES.find(s=>s.id===S.series);
    renderList(S.rawData[`${S.listMode}_${S.game}`]||{}, series?.color||'#2196F3');
}
function clearSearch() { document.getElementById('searchInput').value=''; onSearch(''); }

function toggleFavorite() {
    if (!S.detail) return;
    const id=`${S.game}_${S.detail.name}`;
    const c=SERIES.find(s=>s.id===S.series)?.color||'#f00';
    if (S.favorites.has(id)) { S.favorites.delete(id); document.getElementById('favBtn').textContent='♡'; document.getElementById('favBtn').style.color=''; }
    else { S.favorites.add(id); document.getElementById('favBtn').textContent='♥'; document.getElementById('favBtn').style.color=c; }
    localStorage.setItem('favs', JSON.stringify([...S.favorites]));
}
function shareDetail() {
    if (!S.detail) return;
    if (navigator.share) navigator.share({title:S.detail.name,text:S.detail.name});
    else navigator.clipboard?.writeText(S.detail.name+'\n'+window.location.href).then(()=>alert('Copied!'));
}
function showLoading() { document.getElementById('listContent').innerHTML=`<div class="loading-wrap"><div class="spinner"></div><div>Loading…</div></div>`; }
function showEmpty(msg) { document.getElementById('listContent').innerHTML=`<div class="empty-state">${msg}</div>`; }
function esc(s) { return s.replace(/'/g,"\\'"); }
