# 🚀 Deployment Guide — Spring Boot JAR + PWA

---

## PART 1: Deploy Your Spring Boot JAR (Free — Railway.app)

### Step 1 — Enable CORS in your Spring Boot app
Add this to your Spring Boot main class or a config file:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH");
    }
}
```

### Step 2 — Deploy to Railway.app (recommended free host)

1. Go to https://railway.app and sign up (free)
2. Click **New Project → Deploy from GitHub**
   - OR use **New Project → Empty Project → Add Service → Upload**
3. In service settings, set:
   - **Start command**: `java -jar your-app.jar`
   - **Port**: `8080` (or whatever your app uses)
4. Railway will give you a public URL like:
   `https://your-app-name.up.railway.app`

### Step 3 — Add Spring Boot Actuator (optional but recommended)
This lets the PWA check if your server is online.

Add to `pom.xml`:
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## PART 2: Deploy Your PWA (Free — GitHub Pages / Netlify / Vercel)

### Option A — Netlify (easiest, drag & drop)

1. Go to https://app.netlify.com
2. Drag your **entire PWA folder** (index.html, manifest.json, service-worker.js, icons/) onto the page
3. You'll get a live URL like: `https://your-app.netlify.app`
4. Done! ✓

### Option B — GitHub Pages

1. Create a GitHub repo
2. Push your PWA files to the `main` branch
3. Go to repo **Settings → Pages → Deploy from branch (main)**
4. Your PWA will be live at: `https://yourusername.github.io/repo-name`

---

## PART 3: Connect PWA to Backend

1. Open your deployed PWA
2. Go to **Settings tab**
3. Enter your Railway URL as the **Default Base URL**
4. Save — the app will now call your Spring Boot API

---

## PART 4: Install on Mobile

### Android
1. Open PWA URL in Chrome
2. Tap the **Install** banner that appears at the bottom
3. Or tap **⋮ menu → Add to Home Screen**

### iOS (iPhone/iPad)
1. Open PWA URL in Safari
2. Tap **Share button (□↑)**
3. Tap **Add to Home Screen**
4. Tap **Add** — it now appears as an app icon!

---

## PWA File Structure

```
pwa/
├── index.html          ← Main app (all UI + logic)
├── manifest.json       ← Makes it installable
├── service-worker.js   ← Offline support
└── icons/
    ├── icon-192.png    ← Required app icon
    └── icon-512.png    ← Required app icon (splash screen)
```

> ⚠️ **Icons needed**: Add your own 192×192 and 512×512 PNG icons
> to the `icons/` folder before deploying. Use any image editor or
> a free tool like https://realfavicongenerator.net

---

## Checklist

- [ ] CORS enabled in Spring Boot
- [ ] JAR deployed to Railway (or other host)
- [ ] PWA deployed to Netlify/GitHub Pages
- [ ] Icons added (192px and 512px PNG)
- [ ] Base URL set in PWA Settings tab
- [ ] Tested on Android (Chrome) and iOS (Safari)
