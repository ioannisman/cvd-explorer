package cvdexplorer.metric;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.SiteMemberKind;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricMemberCompatibilityTest {
    @Test
    void sumOfDistancesRejectsCircleClusters() {
        List<ClusterSite> clusters = List.of(
                new ClusterSite(
                        "Alpha",
                        Color.RED,
                        List.of(new CircleMember(Vector.xy(0, 0), Vector.xy(10, 0)))
                )
        );

        assertTrue(MetricMemberCompatibility.invalidMetricMessage(MetricKind.SUM_OF_DISTANCES, clusters).isPresent());
    }

    @Test
    void minimumDistanceAllowsCircleClusters() {
        List<ClusterSite> clusters = List.of(
                new ClusterSite(
                        "Alpha",
                        Color.RED,
                        List.of(new CircleMember(Vector.xy(0, 0), Vector.xy(10, 0)))
                )
        );

        assertFalse(MetricMemberCompatibility.invalidMetricMessage(MetricKind.MINIMUM_DISTANCE, clusters).isPresent());
    }

    @Test
    void sumOfDistancesRejectsNewCircleMembers() {
        assertTrue(MetricMemberCompatibility.invalidNewMemberMessage(
                MetricKind.SUM_OF_DISTANCES,
                SiteMemberKind.CIRCLE
        ).isPresent());
        assertFalse(MetricMemberCompatibility.invalidNewMemberMessage(
                MetricKind.SUM_OF_DISTANCES,
                SiteMemberKind.POINT
        ).isPresent());
    }
}
