package cvdexplorer.metric;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.SiteMemberKind;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricMemberCompatibilityTest {
    private static final List<ClusterSite> CIRCLE_ONLY_CLUSTERS = List.of(
            new ClusterSite(
                    "Alpha",
                    Color.RED,
                    List.of(new CircleMember(Vector.xy(0, 0), Vector.xy(10, 0)))
            )
    );

    @ParameterizedTest
    @EnumSource(value = MetricKind.class, names = {"SUM_OF_DISTANCES", "MEAN_DISTANCE", "KTH_NEAREST_DISTANCE"})
    void pointOnlyMetricsRejectCircleClusters(MetricKind metricKind) {
        assertTrue(MetricMemberCompatibility.invalidMetricMessage(metricKind, CIRCLE_ONLY_CLUSTERS).isPresent());
    }

    @Test
    void minimumDistanceAllowsCircleClusters() {
        assertFalse(MetricMemberCompatibility.invalidMetricMessage(MetricKind.MINIMUM_DISTANCE, CIRCLE_ONLY_CLUSTERS).isPresent());
    }

    @ParameterizedTest
    @EnumSource(value = MetricKind.class, names = {"SUM_OF_DISTANCES", "MEAN_DISTANCE", "KTH_NEAREST_DISTANCE"})
    void pointOnlyMetricsRejectNewCircleMembers(MetricKind metricKind) {
        assertTrue(MetricMemberCompatibility.invalidNewMemberMessage(
                metricKind,
                SiteMemberKind.CIRCLE
        ).isPresent());
        assertFalse(MetricMemberCompatibility.invalidNewMemberMessage(
                metricKind,
                SiteMemberKind.POINT
        ).isPresent());
    }
}
