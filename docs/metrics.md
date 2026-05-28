# Metrics and neighbor order

[← Documentation home](README.md)

## Neighbor order

After the distance to each cluster is computed, **neighbor order** picks the owner:

| Order | Wins when |
|-------|-----------|
| `NEAREST` | The cluster with the **smallest** distance |
| `FARTHEST` | The cluster with the **largest** distance |


## Distance metrics

Each metric defines how the distance from a query point to a cluster is measured. The same neighbor order is then applied across clusters.

The metrics below are illustrated with **clusters of point members** only (`points_*` figures). Each definition is followed by two images: **nearest** and **farthest** neighbor order.

The sum, mean, and *k*-th nearest metrics apply only when every member in every cluster is a point.

### `MINIMUM_DISTANCE`

Distance to the nearest member in that cluster.

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/points_min_min.png" width="420" alt="Points, minimum distance, nearest" /> | <img src="figures/instances/points_max_min.png" width="420" alt="Points, minimum distance, farthest" /> |

### `MAXIMUM_DISTANCE`

Distance to the farthest member in that cluster.

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/points_min_max.png" width="420" alt="Points, maximum distance, nearest" /> | <img src="figures/instances/points_max_max.png" width="420" alt="Points, maximum distance, farthest" /> |

### `SUM_OF_DISTANCES`

Sum of distances to every point member in the cluster. **Points only.**

| Nearest (min-sum) | Farthest (max-sum) |
|:---:|:---:|
| <img src="figures/instances/points_min_sum.png" width="420" alt="Points, sum of distances, nearest" /> | <img src="figures/instances/points_max_sum.png" width="420" alt="Points, sum of distances, farthest" /> |

### `MEAN_DISTANCE`

Average of distances to all point members in the cluster. **Points only.**

| Nearest (min-mean) | Farthest (max-mean) |
|:---:|:---:|
| <img src="figures/instances/points_min_mean.png" width="420" alt="Points, mean distance, nearest" /> | <img src="figures/instances/points_max_mean.png" width="420" alt="Points, mean distance, farthest" /> |

### `KTH_NEAREST_DISTANCE`

Distance to the *k*-th nearest point member in that cluster. *k* = 1 is the same as `MINIMUM_DISTANCE`. **Points only.** Examples use *k* = 2.

| Nearest (min-2nd) | Farthest (max-2nd) |
|:---:|:---:|
| <img src="figures/instances/points_min_2nd.png" width="420" alt="Points, 2nd nearest, nearest neighbor order" /> | <img src="figures/instances/points_max_2nd.png" width="420" alt="Points, 2nd nearest, farthest neighbor order" /> |
