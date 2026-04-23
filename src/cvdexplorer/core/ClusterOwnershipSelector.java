package cvdexplorer.core;

import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.model.ClusterSite;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class ClusterOwnershipSelector {
    public Result selectOwner(Vector point, List<ClusterSite> clusters, ClusterMetric metric) {
        int bestIndex = -1;
        double bestScore = Double.POSITIVE_INFINITY;

        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++) {
            ClusterSite cluster = clusters.get(clusterIndex);
            double score = metric.score(point, cluster);
            if ((score < bestScore) || ((score == bestScore) && (clusterIndex < bestIndex))) {
                bestIndex = clusterIndex;
                bestScore = score;
            }
        }

        return new Result(bestIndex, bestScore);
    }

    public record Result(int clusterIndex, double score) {
    }
}
