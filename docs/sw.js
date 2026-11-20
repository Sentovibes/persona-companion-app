const CACHE_NAME = 'persona-companion-v657';

const PRECACHE_URLS = [
    './',
    './index.html',
    './styles.css?v=657',
    './app.js?v=657',
    './manifest.json',
    './icon.webp'
];

self.addEventListener('install', (event) => {
    self.skipWaiting();
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) =>
            Promise.all(
                PRECACHE_URLS.map((url) =>
                    fetch(url, { cache: 'reload' })
                        .then((res) => (res && res.ok ? cache.put(url, res) : null))
                        .catch(() => null)
                )
            )
        )
    );
});

self.addEventListener('activate', (event) => {
    event.waitUntil((async () => {
        const keys = await caches.keys();
        const oldKeys = keys.filter((key) => key !== CACHE_NAME);
        const hadOldCaches = oldKeys.some((key) => key.startsWith('persona-companion-'));
        await Promise.all(oldKeys.map((key) => caches.delete(key)));
        await self.clients.claim();

        // Force-refresh all open tabs that were still running an older cached build
        if (hadOldCaches) {
            const windowClients = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
            await Promise.all(
                windowClients.map((client) => {
                    if (client && 'navigate' in client) {
                        return client.navigate(client.url).catch(() => {});
                    }
                    return Promise.resolve();
                })
            );
        }
    })());
});

self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});

self.addEventListener('fetch', (event) => {
    const req = event.request;
    if (req.method !== 'GET') return;

    const url = new URL(req.url);
    if (url.origin !== self.location.origin) return;
    if (url.pathname.endsWith('/sw.js')) {
        event.respondWith(fetch(req, { cache: 'no-store' }).catch(() => caches.match(req)));
        return;
    }

    // App shell & HTML/JS/CSS/JSON: always network-first (bypassing browser HTTP cache) when online,
    // falling back to Cache Storage only when offline.
    const isAlwaysFresh = req.mode === 'navigate' ||
        url.pathname.endsWith('/') ||
        url.pathname.endsWith('.html') ||
        url.pathname.endsWith('.js') ||
        url.pathname.endsWith('.css') ||
        url.pathname.endsWith('.json');

    if (isAlwaysFresh) {
        event.respondWith(
            fetch(req, { cache: 'no-cache' })
                .then((res) => {
                    if (res && res.ok) {
                        const copy = res.clone();
                        caches.open(CACHE_NAME).then((cache) => cache.put(req, copy));
                    }
                    return res;
                })
                .catch(() =>
                    caches.match(req)
                        .then((cached) => cached || caches.match(req, { ignoreSearch: true }))
                        .then((cached) => cached || (req.mode === 'navigate' ? caches.match('./index.html') : null))
                )
        );
        return;
    }

    // Images and static sprites: stale-while-revalidate when online, cached when offline
    event.respondWith(
        caches.open(CACHE_NAME).then((cache) =>
            cache.match(req).then((cached) => {
                const netFetch = fetch(req).then((res) => {
                    if (res && res.ok) cache.put(req, res.clone());
                    return res;
                }).catch(() => cached);
                return cached || netFetch;
            })
        )
    );
});
