package cvdexplorer.core;

import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.metric.FarthestMemberMetric;
import cvdexplorer.metric.NearestMemberMetric;
import cvdexplorer.metric.KthNearestPointDistanceMetric;
import cvdexplorer.metric.MeanOfDistancesMetric;
import cvdexplorer.metric.SumOfDistancesMetric;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.SceneState;

import java.util.List;

public final class ScenePreparation {
    private static final ClusterMetric MINIMUM_DISTANCE = new NearestMemberMetric();
    private static final ClusterMetric MAXIMUM_DISTANCE = new FarthestMemberMetric();
    private static final ClusterMetric SUM_OF_DISTANCES = new SumOfDistancesMetric();
    private static final ClusterMetric MEAN_DISTANCE = new MeanOfDistancesMetric();
    private static final ClusterOwnershipSelector NEAREST_OWNERSHIP = new ClusterOwnershipSelector(true);
    private static final ClusterOwnershipSelector FARTHEST_OWNERSHIP = new ClusterOwnershipSelector(false);

    private ScenePreparation() {
    }

    public static PreparedScene prepare(SceneState state) {
        return new PreparedScene(
                List.copyOf(state.clusters()),
                metricFor(state),
                ownershipSelectorFor(state.neighborOrder)
        );
    }

    private static ClusterMetric metricFor(SceneState state) {
        return switch (state.metricKind) {
            case MINIMUM_DISTANCE -> MINIMUM_DISTANCE;
            case MAXIMUM_DISTANCE -> MAXIMUM_DISTANCE;
            case SUM_OF_DISTANCES -> SUM_OF_DISTANCES;
            case MEAN_DISTANCE -> MEAN_DISTANCE;
            case KTH_NEAREST_DISTANCE -> {
                int minSize = state.minMemberCountAcrossClusters();
                int k = minSize < 1 ? 1 : Math.max(1, Math.min(state.nearestNeighborK, minSize));
                yield new KthNearestPointDistanceMetric(k);
            }
        };
    }

    private static ClusterOwnershipSelector ownershipSelectorFor(NeighborOrder neighborOrder) {
        return switch (neighborOrder) {
            case NEAREST -> NEAREST_OWNERSHIP;
            case FARTHEST -> FARTHEST_OWNERSHIP;
        };
    }

    public record PreparedScene(
            List<ClusterSite> clusters,
            ClusterMetric metric,
            ClusterOwnershipSelector ownershipSelector
    ) {
    }
}
