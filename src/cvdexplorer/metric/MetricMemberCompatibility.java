package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.SiteMemberKind;

import java.util.List;
import java.util.Optional;

public final class MetricMemberCompatibility {
    private static final String SUM_OF_DISTANCES_ONLY_POINTS =
            "SUM_OF_DISTANCES is only supported for clusters made entirely of points.";

    private MetricMemberCompatibility() {
    }

    public static Optional<String> invalidMetricMessage(MetricKind metricKind, List<ClusterSite> clusters) {
        if (metricKind != MetricKind.SUM_OF_DISTANCES) {
            return Optional.empty();
        }

        for (ClusterSite cluster : clusters) {
            for (ClusterMember member : cluster.members()) {
                if (!(member instanceof PointMember)) {
                    return Optional.of(SUM_OF_DISTANCES_ONLY_POINTS);
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<String> invalidNewMemberMessage(MetricKind metricKind, SiteMemberKind memberKind) {
        if (metricKind == MetricKind.SUM_OF_DISTANCES && memberKind != SiteMemberKind.POINT) {
            return Optional.of(SUM_OF_DISTANCES_ONLY_POINTS);
        }
        return Optional.empty();
    }
}
