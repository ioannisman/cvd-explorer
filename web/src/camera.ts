export const DEFAULT_HALF_EXTENT = 350;
export const MIN_HALF_EXTENT = 40;
export const MAX_HALF_EXTENT = 4000;

export type WorldView = {
  cx: number;
  cy: number;
  half: number;
};

export type WorldBounds = {
  minX: number;
  maxX: number;
  minY: number;
  maxY: number;
};

export function defaultWorldView(): WorldView {
  return { cx: 0, cy: 0, half: DEFAULT_HALF_EXTENT };
}

export function boundsOf(view: WorldView): WorldBounds {
  return {
    minX: view.cx - view.half,
    maxX: view.cx + view.half,
    minY: view.cy - view.half,
    maxY: view.cy + view.half,
  };
}

/** True when a classified frame's world box matches the live camera. */
export function worldBoundsMatch(
  a: WorldBounds | null | undefined,
  b: WorldBounds,
  epsilon = 1e-6
): boolean {
  if (a == null) {
    return false;
  }
  return (
    Math.abs(a.minX - b.minX) <= epsilon &&
    Math.abs(a.maxX - b.maxX) <= epsilon &&
    Math.abs(a.minY - b.minY) <= epsilon &&
    Math.abs(a.maxY - b.maxY) <= epsilon
  );
}

export function pixelToWorld(
  px: number,
  py: number,
  width: number,
  height: number,
  view: WorldView
): { x: number; y: number } {
  const b = boundsOf(view);
  const x = b.minX + ((px + 0.5) / width) * (b.maxX - b.minX);
  const y = b.maxY + ((py + 0.5) / height) * (b.minY - b.maxY);
  return { x, y };
}

export function worldToPixel(
  wx: number,
  wy: number,
  width: number,
  height: number,
  view: WorldView
): { x: number; y: number } {
  const b = boundsOf(view);
  const x = ((wx - b.minX) / (b.maxX - b.minX)) * width - 0.5;
  const y = ((wy - b.maxY) / (b.minY - b.maxY)) * height - 0.5;
  return { x, y };
}

/** Zoom keeping the world point under (px,py) fixed. factor > 1 zooms in. */
export function zoomAt(
  view: WorldView,
  px: number,
  py: number,
  width: number,
  height: number,
  factor: number
): WorldView {
  const before = pixelToWorld(px, py, width, height, view);
  const half = Math.min(MAX_HALF_EXTENT, Math.max(MIN_HALF_EXTENT, view.half / factor));
  const next = { cx: view.cx, cy: view.cy, half };
  const after = pixelToWorld(px, py, width, height, next);
  return {
    cx: view.cx + (before.x - after.x),
    cy: view.cy + (before.y - after.y),
    half,
  };
}

export function panByPixels(
  view: WorldView,
  dpx: number,
  dpy: number,
  width: number,
  height: number
): WorldView {
  const worldPerPxX = (2 * view.half) / width;
  const worldPerPxY = (2 * view.half) / height;
  return {
    cx: view.cx - dpx * worldPerPxX,
    cy: view.cy + dpy * worldPerPxY, // canvas Y down → world Y up
    half: view.half,
  };
}

export function snapToNearestHandle(
  x: number,
  y: number,
  handles: { x: ArrayLike<number>; y: ArrayLike<number>; n: number },
  excludeIndex: number | null,
  radius: number
): { x: number; y: number; snapped: boolean } {
  let best = radius * radius;
  let sx = x;
  let sy = y;
  let snapped = false;
  for (let i = 0; i < handles.n; i++) {
    if (excludeIndex !== null && i === excludeIndex) {
      continue;
    }
    const dx = handles.x[i] - x;
    const dy = handles.y[i] - y;
    const d2 = dx * dx + dy * dy;
    if (d2 <= best) {
      best = d2;
      sx = handles.x[i];
      sy = handles.y[i];
      snapped = true;
    }
  }
  return { x: sx, y: sy, snapped };
}

/** World-space snap radius roughly matching ~12 display pixels. */
export function handleSnapRadius(view: WorldView, displayWidth: number): number {
  return (2 * view.half) / displayWidth * 12;
}
