const CACHE_NAME = 'spring-pwa-v4';
const STATIC_ASSETS = [
  '/manifest.json',
  '/icons/icon-192.png',
  '/icons/icon-512.png'
];

// Install: cache only static assets (NOT index.html)
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

// Activate: delete ALL old caches immediately
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    ).then(() => self.clients.claim())
  );
});

// Fetch:
// - index.html → ALWAYS network (never serve stale UI)
// - /api/*     → ALWAYS network
// - assets     → cache-first
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);

  // Always fresh HTML
  if (url.pathname === '/' || url.pathname === '/index.html') {
    event.respondWith(
      fetch(event.request).catch(() => caches.match('/index.html'))
    );
    return;
  }

  // Always fresh API
  if (url.pathname.startsWith('/api') || url.pathname.startsWith('/actuator')) {
    event.respondWith(
      fetch(event.request).catch(() =>
        new Response(JSON.stringify({error:'offline'}), {
          headers: {'Content-Type':'application/json'}
        })
      )
    );
    return;
  }

  // Cache-first for icons, manifest
  event.respondWith(
    caches.match(event.request).then(cached =>
      cached || fetch(event.request).then(res => {
        caches.open(CACHE_NAME).then(c => c.put(event.request, res.clone()));
        return res;
      })
    )
  );
});
