package cvdexplorer.core;

import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.geometry.Vector;

import java.util.List;

public final class ClusterOwnershipSelector {
    private final boolean preferLowerScores;

    public ClusterOwnershipSelector(boolean preferLowerScores) {
        this.preferLowerScores = preferLowerScores;
    }

    public Result selectOwner(Vector point, List<ClusterSite> clusters, ClusterMetric metric) {
        int bestClusterIndex = -1;
        int bestMemberIndex = -1;
        double bestScore = preferLowerScores ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++) {
            ClusterSite cluster = clusters.get(clusterIndex);
            ClusterMetric.Result metricResult = metric.evaluate(point, cluster);
            double score = metricResult.score();
            if (isBetterScore(score, bestScore) || ((score == bestScore) && (clusterIndex < bestClusterIndex))) {
                bestClusterIndex = clusterIndex;
                bestMemberIndex = metricResult.memberIndex();
                bestScore = score;
            }
        }

        return new Result(bestClusterIndex, bestScore, bestMemberIndex);
    }

    private boolean isBetterScore(double candidateScore, double currentBestScore) {
        return preferLowerScores
                ? candidateScore < currentBestScore
                : candidateScore > currentBestScore;
    }

    public record Result(int clusterIndex, double score, int memberIndex) {
    }
}
