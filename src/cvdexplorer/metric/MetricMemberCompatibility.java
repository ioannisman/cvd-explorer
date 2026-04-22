package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.SiteMemberKind;

import java.util.List;
import java.util.Optional;

public final class MetricMemberCompatibility {
    private MetricMemberCompatibility() {
    }

    private static boolean requiresPointOnlyMembers(MetricKind metricKind) {
        return metricKind == MetricKind.SUM_OF_DISTANCES
                || metricKind == MetricKind.MEAN_DISTANCE
                || metricKind == MetricKind.KTH_NEAREST_DISTANCE;
    }

    private static String pointOnlyMetricMessage(MetricKind metricKind) {
        return metricKind.name() + " is only supported for clusters made entirely of points.";
    }

    public static Optional<String> invalidMetricMessage(MetricKind metricKind, List<ClusterSite> clusters) {
        if (!requiresPointOnlyMembers(metricKind)) {
            return Optional.empty();
        }

        for (ClusterSite cluster : clusters) {
            for (ClusterMember member : cluster.members()) {
                if (!(member instanceof PointMember)) {
                    return Optional.of(pointOnlyMetricMessage(metricKind));
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<String> invalidNewMemberMessage(MetricKind metricKind, SiteMemberKind memberKind) {
        if (requiresPointOnlyMembers(metricKind) && memberKind != SiteMemberKind.POINT) {
            return Optional.of(pointOnlyMetricMessage(metricKind));
        }
        return Optional.empty();
    }
}
