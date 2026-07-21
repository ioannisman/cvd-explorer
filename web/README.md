# cluster-voronoi-diagrams-explorer (web)

Browser shell for the Cluster Voronoi Diagrams explorer. Classify runs in a **Web Worker** via TeaVM-compiled `:core` (`cvd-core.js`). **No load/save** in v1.

## Prerequisites

- JDK 25 (same as desktop)
- Node.js 20+

## Build TeaVM artifact

From the repo root:

```bash
./gradlew :web-bridge:generateJavaScript
```

Copies `cvd-core.js` into [`public/teavm/`](public/teavm/).

## Run locally

```bash
cd web
npm install
npm run dev
```

Open the URL Vite prints (default `http://localhost:5173`).

## Static build

```bash
./gradlew :web-bridge:generateJavaScript
cd web && npm ci && npm run build
```

Output is `web/dist/` (self-contained; relative `base`). GitHub Pages publishes it under `/explorer/` via [`.github/workflows/pages.yml`](../.github/workflows/pages.yml).

Preview the production build:

```bash
cd web && npm run preview
```

## Notes

- Drag uses low-res preview (~0.32×, same idea as desktop) then a full-res pass on release (toggleable).
- Classify stays off the UI thread; requests coalesce while a frame is in flight.
- Canvas backing store tracks layout size (up to ~960px CSS) × `devicePixelRatio` (capped at 2, edge capped at 1536) for a balance of sharpness and TeaVM speed. Pan/zoom uses the same low-res preview scale as handle drag (~0.32×), then a full-res pass when the view settles.
- Visible controls: metric, neighbor order, `k`, diagram/members/skeleton, region subdivision, fast preview. Authoring: active cluster, site kind, add/remove member & cluster, place-on-click. Member overlays for segment/circle/ellipse/line. Camera: wheel zoom, empty/middle-drag pan, reset. Snap to handles. Colocated handle co-move (Shift+drag detaches). Shortcuts: `h` `c` `m` `k` `v` `f` `n`/`p` (member) `Shift+n`/`Shift+p` (cluster) `a`/`d` `Shift+a`/`Shift+d` (shown in the panel). Help panel. No load/save.
