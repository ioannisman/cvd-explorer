package cvdexplorer.core;

import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.metric.FarthestMemberMetric;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.metric.NearestMemberMetric;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.SceneState;

import java.util.List;

public final class ScenePreparation {
    private static final ClusterMetric NEAREST = new NearestMemberMetric();
    private static final ClusterMetric FARTHEST = new FarthestMemberMetric();

    private ScenePreparation() {
    }

    public static PreparedScene prepare(SceneState state) {
        return new PreparedScene(List.copyOf(state.clusters()), metricFor(state.metricKind));
    }

    private static ClusterMetric metricFor(MetricKind metricKind) {
        return switch (metricKind) {
            case NEAREST -> NEAREST;
            case FARTHEST -> FARTHEST;
        };
    }

    public record PreparedScene(List<ClusterSite> clusters, ClusterMetric metric) {
    }
}
