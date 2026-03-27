package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.PointMember;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClusterMetricTest {
    @Test
    void nearestMetricUsesClosestPointInCluster() {
        ClusterMetric metric = new NearestMemberMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(Math.sqrt(80), score, 1.0e-9);
    }

    @Test
    void farthestMetricUsesMostDistantPointInCluster() {
        ClusterMetric metric = new FarthestMemberMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(10.0, score, 1.0e-9);
    }
}
