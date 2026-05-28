# Site types

[← Documentation home](README.md)

Members in a cluster can be `POINT`, `LINE_SEGMENT`, `CIRCLE`, or `LINE`.

**Polygons** and open polygonal chains are not a separate member kind: build them from `LINE_SEGMENT` members and use **`Snap to handles`** in the app to align segment endpoints into closed or chained shapes.

## Points

The distance from a point in the plane to a point member is the Euclidean distance between them.

Figures where each cluster uses **point** members (`POINT`) follow in **[Metrics](metrics.md)** (all five metrics and both neighbor orders).

## Line segments

The distance from a point in the plane to a line segment is the Euclidean distance to the **closest point on the segment**: project onto the line through the endpoints, clamp to the segment, then measure distance (equivalently, the minimum distance to any point on the segment).

Figures where each cluster uses **line segment** members (`LINE_SEGMENT`) follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/segments_min_min.png" width="420" alt="Segments, minimum distance, nearest" /> | <img src="figures/instances/segments_max_min.png" width="420" alt="Segments, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/segments_min_max.png" width="420" alt="Segments, maximum distance, nearest" /> | <img src="figures/instances/segments_max_max.png" width="420" alt="Segments, maximum distance, farthest" /> |

## Polygons

The distance to each edge is the same as for a line segment (closest point on that segment). Clusters are closed polygons built from `LINE_SEGMENT` members.

Figures where each cluster is a polygon follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/polygons_min_min.png" width="420" alt="Polygons, minimum distance, nearest" /> | <img src="figures/instances/polygons_max_min.png" width="420" alt="Polygons, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/polygons_min_max.png" width="420" alt="Polygons, maximum distance, nearest" /> | <img src="figures/instances/polygons_max_max.png" width="420" alt="Polygons, maximum distance, farthest" /> |

## Lines

The distance from a point in the plane to an infinite **line** member is the Euclidean distance to the **closest point on the line** (orthogonal projection onto the lineß).

Figures where each cluster uses **line** members (`LINE`) follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/lines_min_min.png" width="420" alt="Lines, minimum distance, nearest" /> | <img src="figures/instances/lines_max_min.png" width="420" alt="Lines, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/lines_min_max.png" width="420" alt="Lines, maximum distance, nearest" /> | <img src="figures/instances/lines_max_max.png" width="420" alt="Lines, maximum distance, farthest" /> |

## Circles

The distance from a point in the plane to a **circle** member is the distance to the nearest point on the circle **boundary**: `|distance to center − radius|` (interior points are measured to the boundary, not to the center).

Figures where each cluster uses **circle** members (`CIRCLE`) follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/circles_min_min.png" width="420" alt="Circles, minimum distance, nearest" /> | <img src="figures/instances/circles_max_min.png" width="420" alt="Circles, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/circles_min_max.png" width="420" alt="Circles, maximum distance, nearest" /> | <img src="figures/instances/circles_max_max.png" width="420" alt="Circles, maximum distance, farthest" /> |
