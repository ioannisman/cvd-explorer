package cvdexplorer.metric;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.Rgba;
import cvdexplorer.model.SiteMemberKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import cvdexplorer.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricMemberCompatibilityTest {
    private static final List<ClusterSite> CIRCLE_ONLY_CLUSTERS = List.of(
            new ClusterSite(
                    "Alpha",
                    Rgba.RED,
                    List.of(new CircleMember(Vector.xy(0, 0), Vector.xy(10, 0)))
            )
    );

    private static final List<ClusterSite> ELLIPSE_CLUSTERS = List.of(
            new ClusterSite(
                    "Beta",
                    Rgba.BLUE,
                    List.of(new EllipseMember(
                            Vector.xy(-5, 0),
                            Vector.xy(5, 0),
                            Vector.xy(0, 10)
                    ))
            )
    );

    @ParameterizedTest
    @EnumSource(value = MetricKind.class, names = {"SUM_OF_DISTANCES", "MEAN_DISTANCE", "KTH_NEAREST_DISTANCE"})
    void pointOnlyMetricsRejectCircleClusters(MetricKind metricKind) {
        assertTrue(MetricMemberCompatibility.invalidMetricMessage(metricKind, CIRCLE_ONLY_CLUSTERS).isPresent());
    }

    @ParameterizedTest
    @EnumSource(value = MetricKind.class, names = {"SUM_OF_DISTANCES", "MEAN_DISTANCE", "KTH_NEAREST_DISTANCE"})
    void pointOnlyMetricsRejectEllipseClusters(MetricKind metricKind) {
        assertTrue(MetricMemberCompatibility.invalidMetricMessage(metricKind, ELLIPSE_CLUSTERS).isPresent());
    }

    @Test
    void minimumDistanceAllowsCircleClusters() {
        assertFalse(MetricMemberCompatibility.invalidMetricMessage(MetricKind.MINIMUM_DISTANCE, CIRCLE_ONLY_CLUSTERS).isPresent());
    }

    @ParameterizedTest
    @EnumSource(value = MetricKind.class, names = {"SUM_OF_DISTANCES", "MEAN_DISTANCE", "KTH_NEAREST_DISTANCE"})
    void pointOnlyMetricsRejectNewCircleAndEllipseMembers(MetricKind metricKind) {
        assertTrue(MetricMemberCompatibility.invalidNewMemberMessage(metricKind, SiteMemberKind.CIRCLE).isPresent());
        assertTrue(MetricMemberCompatibility.invalidNewMemberMessage(metricKind, SiteMemberKind.ELLIPSE).isPresent());
        assertFalse(MetricMemberCompatibility.invalidNewMemberMessage(metricKind, SiteMemberKind.POINT).isPresent());
    }
}
