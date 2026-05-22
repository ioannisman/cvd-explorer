package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public interface ClusterMetric {
    /**
     * Result of a metric evaluation.
     * @param score The computed distance or score for the query point.
     * @param memberIndex The index of the specific member that defines this score (e.g. the nearest or farthest point), or -1 if the metric is aggregate.
     */
    record Result(double score, int memberIndex) {}

    /**
     * Evaluates the metric for a given point against a list of cluster members, returning both the score and the responsible member.
     */
    Result evaluate(Vector point, List<ClusterMember> members);

    default double score(Vector point, List<ClusterMember> members) {
        return evaluate(point, members).score();
    }

    default double score(Vector point, ClusterSite cluster) {
        return evaluate(point, cluster.members()).score();
    }

    default Result evaluate(Vector point, ClusterSite cluster) {
        return evaluate(point, cluster.members());
    }
}
