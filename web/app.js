// State management
const state = {
    currentTab: 'personas',
    currentGame: '',
    data: {},
    searchQuery: ''
};

// Data paths mapping
const dataPaths = {
    personas: {
        p3fes: './data/persona3/personas.json',
        p3p: './data/persona3/portable_personas.json',
        p3r: './data/persona3/reload_personas.json',
        p4: './data/persona4/personas.json',
        p4g: './data/persona4/golden_personas.json',
        p5: './data/persona5/personas.json',
        p5r: './data/persona5/royal_personas.json'
    },
    enemies: {
        p3fes: './data/enemies/p3fes_enemies.json',
        p3p: './data/enemies/p3p_enemies.json',
        p3r: './data/enemies/p3r_enemies.json',
        p4: './data/enemies/p4_enemies.json',
        p4g: './data/enemies/p4g_enemies.json',
        p5: './data/enemies/p5_enemies.json',
        p5r: './data/enemies/p5r_enemies.json'
    },
    classroom: {
        p3: './data/classroom/p3_classroom_answers.json',
        p4: './data/classroom/p4_classroom_answers.json',
        p5: './data/classroom/p5_classroom_answers.json'
    }
};

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
});

function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            state.currentTab = tab.dataset.tab;
            loadData();
        });
    });

    // Game selection
    document.getElementById('gameSelect').addEventListener('change', (e) => {
        state.currentGame = e.target.value;
        loadData();
    });

    // Search
    document.getElementById('searchInput').addEventListener('input', (e) => {
        state.searchQuery = e.target.value.toLowerCase();
        renderData();
    });
}

async function loadData() {
    if (!state.currentGame) {
        showPlaceholder('Select a game to get started');
        return;
    }

    const content = document.getElementById('content');
    content.innerHTML = '<p class="loading">Loading...</p>';

    try {
        let dataPath;
        
        if (state.currentTab === 'classroom') {
            // Classroom uses series-level data
            const series = state.currentGame.substring(0, 2); // p3, p4, p5
            dataPath = dataPaths.classroom[series];
        } else {
            dataPath = dataPaths[state.currentTab]?.[state.currentGame];
        }

        if (!dataPath) {
            showPlaceholder('No data available for this combination');
            return;
        }

        const response = await fetch(dataPath);
        if (!response.ok) throw new Error('Failed to load data');
        
        state.data = await response.json();
        renderData();
    } catch (error) {
        content.innerHTML = `<p class="error">Error loading data: ${error.message}</p>`;
    }
}

function renderData() {
    const content = document.getElementById('content');
    
    if (!state.data || Object.keys(state.data).length === 0) {
        showPlaceholder('No data loaded');
        return;
    }

    let items = Object.entries(state.data);
    
    // Filter by search query
    if (state.searchQuery) {
        items = items.filter(([name, data]) => {
            const searchStr = JSON.stringify(data).toLowerCase();
            return name.toLowerCase().includes(state.searchQuery) || 
                   searchStr.includes(state.searchQuery);
        });
    }

    if (items.length === 0) {
        showPlaceholder('No results found');
        return;
    }

    // Render based on current tab
    switch (state.currentTab) {
        case 'personas':
            renderPersonas(items);
            break;
        case 'enemies':
            renderEnemies(items);
            break;
        case 'social-links':
            renderSocialLinks(items);
            break;
        case 'classroom':
            renderClassroom(items);
            break;
    }
}

function renderPersonas(items) {
    const content = document.getElementById('content');
    const grid = document.createElement('div');
    grid.className = 'grid';

    items.forEach(([name, persona]) => {
        const card = document.createElement('div');
        card.className = 'card';
        card.onclick = () => showPersonaDetail(name, persona);

        card.innerHTML = `
            <div class="card-title">${name}</div>
            <div class="card-info">${persona.arcana || 'Unknown'} • Lv. ${persona.level || '?'}</div>
            ${persona.stats ? `
                <div class="card-stats">
                    <span class="stat">ST: ${persona.stats[0]}</span>
                    <span class="stat">MA: ${persona.stats[1]}</span>
                    <span class="stat">EN: ${persona.stats[2]}</span>
                    <span class="stat">AG: ${persona.stats[3]}</span>
                    <span class="stat">LU: ${persona.stats[4]}</span>
                </div>
            ` : ''}
        `;

        grid.appendChild(card);
    });

    content.innerHTML = '';
    content.appendChild(grid);
}

function renderEnemies(items) {
    const content = document.getElementById('content');
    const grid = document.createElement('div');
    grid.className = 'grid';

    items.forEach(([name, enemy]) => {
        const card = document.createElement('div');
        card.className = 'card';
        card.onclick = () => showEnemyDetail(name, enemy);

        const weaknesses = enemy.weaknesses || [];
        const resistances = enemy.resistances || [];

        card.innerHTML = `
            <div class="card-title">${name}</div>
            <div class="card-info">Lv. ${enemy.level || '?'} • ${enemy.arcana || 'Shadow'}</div>
            ${weaknesses.length > 0 ? `<div class="card-info">Weak: ${weaknesses.join(', ')}</div>` : ''}
            ${resistances.length > 0 ? `<div class="card-info">Resist: ${resistances.join(', ')}</div>` : ''}
        `;

        grid.appendChild(card);
    });

    content.innerHTML = '';
    content.appendChild(grid);
}

function renderSocialLinks(items) {
    showPlaceholder('Social Links feature coming soon');
}

function renderClassroom(items) {
    const content = document.getElementById('content');
    const list = document.createElement('div');
    list.style.display = 'flex';
    list.style.flexDirection = 'column';
    list.style.gap = '15px';

    items.forEach(([key, qa]) => {
        const card = document.createElement('div');
        card.className = 'card';

        card.innerHTML = `
            <div class="card-info" style="margin-bottom: 10px;"><strong>Date:</strong> ${qa.Date || 'Unknown'}</div>
            <div class="card-info" style="margin-bottom: 10px;"><strong>Q:</strong> ${qa.Question || 'Question not available'}</div>
            <div class="card-title" style="font-size: 1rem;">A: ${qa.Answer || 'N/A'}</div>
        `;

        list.appendChild(card);
    });

    content.innerHTML = '';
    content.appendChild(list);
}

function showPersonaDetail(name, persona) {
    const modal = createModal();
    
    const skills = persona.skills ? Object.entries(persona.skills).map(([skill, level]) => 
        `<div class="skill-item">${skill} (${level})</div>`
    ).join('') : 'No skills data';

    const weaknesses = persona.weaknesses?.join(', ') || 'None';
    const resistances = persona.resistances?.join(', ') || 'None';

    modal.querySelector('.modal-content').innerHTML = `
        <button class="modal-close" onclick="closeModal()">×</button>
        <h2 class="modal-title">${name}</h2>
        
        <div class="detail-section">
            <h3>Basic Info</h3>
            <p>Arcana: ${persona.arcana || 'Unknown'}</p>
            <p>Level: ${persona.level || '?'}</p>
            ${persona.stats ? `<p>Stats: ST ${persona.stats[0]} / MA ${persona.stats[1]} / EN ${persona.stats[2]} / AG ${persona.stats[3]} / LU ${persona.stats[4]}</p>` : ''}
        </div>

        <div class="detail-section">
            <h3>Affinities</h3>
            <p>Weaknesses: ${weaknesses}</p>
            <p>Resistances: ${resistances}</p>
        </div>

        <div class="detail-section">
            <h3>Skills</h3>
            <div class="skill-list">${skills}</div>
        </div>
    `;

    document.body.appendChild(modal);
    modal.classList.add('active');
}

function showEnemyDetail(name, enemy) {
    const modal = createModal();
    
    const skills = enemy.skills ? enemy.skills.map(skill => 
        `<div class="skill-item">${skill}</div>`
    ).join('') : 'No skills data';

    const weaknesses = enemy.weaknesses?.join(', ') || 'None';
    const resistances = enemy.resistances?.join(', ') || 'None';
    const nulls = enemy.nulls?.join(', ') || 'None';
    const absorbs = enemy.absorbs?.join(', ') || 'None';
    const reflects = enemy.reflects?.join(', ') || 'None';

    modal.querySelector('.modal-content').innerHTML = `
        <button class="modal-close" onclick="closeModal()">×</button>
        <h2 class="modal-title">${name}</h2>
        
        <div class="detail-section">
            <h3>Basic Info</h3>
            <p>Level: ${enemy.level || '?'}</p>
            <p>Type: ${enemy.arcana || 'Shadow'}</p>
            ${enemy.hp ? `<p>HP: ${enemy.hp}</p>` : ''}
            ${enemy.sp ? `<p>SP: ${enemy.sp}</p>` : ''}
        </div>

        <div class="detail-section">
            <h3>Affinities</h3>
            <p>Weak: ${weaknesses}</p>
            <p>Resist: ${resistances}</p>
            <p>Null: ${nulls}</p>
            <p>Absorb: ${absorbs}</p>
            <p>Reflect: ${reflects}</p>
        </div>

        <div class="detail-section">
            <h3>Skills</h3>
            <div class="skill-list">${skills}</div>
        </div>
    `;

    document.body.appendChild(modal);
    modal.classList.add('active');
}

function createModal() {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.onclick = (e) => {
        if (e.target === modal) closeModal();
    };
    
    const content = document.createElement('div');
    content.className = 'modal-content';
    modal.appendChild(content);
    
    return modal;
}

function closeModal() {
    const modal = document.querySelector('.modal');
    if (modal) {
        modal.remove();
    }
}

function showPlaceholder(message) {
    const content = document.getElementById('content');
    content.innerHTML = `<p class="placeholder">${message}</p>`;
}
