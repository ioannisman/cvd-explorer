package cvdexplorer.core;

import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.metric.FarthestMemberMetric;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.metric.NearestMemberMetric;
import cvdexplorer.metric.SumOfDistancesMetric;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.SceneState;

import java.util.List;

public final class ScenePreparation {
    private static final ClusterMetric MINIMUM_DISTANCE = new NearestMemberMetric();
    private static final ClusterMetric MAXIMUM_DISTANCE = new FarthestMemberMetric();
    private static final ClusterMetric SUM_OF_DISTANCES = new SumOfDistancesMetric();

    private ScenePreparation() {
    }

    public static PreparedScene prepare(SceneState state) {
        return new PreparedScene(List.copyOf(state.clusters()), metricFor(state.metricKind));
    }

    private static ClusterMetric metricFor(MetricKind metricKind) {
        return switch (metricKind) {
            case MINIMUM_DISTANCE -> MINIMUM_DISTANCE;
            case MAXIMUM_DISTANCE -> MAXIMUM_DISTANCE;
            case SUM_OF_DISTANCES -> SUM_OF_DISTANCES;
        };
    }

    public record PreparedScene(List<ClusterSite> clusters, ClusterMetric metric) {
    }
}
