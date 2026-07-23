import { cpSync, mkdirSync } from 'node:fs';
import { resolve } from 'node:path';
import { defineConfig, type Plugin } from 'vite';

/** Keep web/public/scenes/gallery in sync with repo scenes/gallery (single source of truth). */
function copySceneGallery(): Plugin {
  const src = resolve(__dirname, '../scenes/gallery');
  const dest = resolve(__dirname, 'public/scenes/gallery');
  const sync = () => {
    mkdirSync(dest, { recursive: true });
    cpSync(src, dest, { recursive: true });
  };
  return {
    name: 'copy-scene-gallery',
    buildStart() {
      sync();
    },
    configureServer() {
      sync();
    },
  };
}

export default defineConfig({
  base: './',
  plugins: [copySceneGallery()],
  server: {
    port: 5173,
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});
