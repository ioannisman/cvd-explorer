package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.PointMember;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClusterMetricTest {
    @Test
    void minimumDistanceUsesClosestPointInCluster() {
        ClusterMetric metric = new NearestMemberMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(Math.sqrt(80), score, 1.0e-9);
    }

    @Test
    void maximumDistanceUsesMostDistantPointInCluster() {
        ClusterMetric metric = new FarthestMemberMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(10.0, score, 1.0e-9);
    }

    @Test
    void sumOfDistancesAddsAllMemberDistances() {
        ClusterMetric metric = new SumOfDistancesMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(10.0 + Math.sqrt(80), score, 1.0e-9);
    }

    @Test
    void minimumDistanceUsesCircleCircumferenceDistance() {
        ClusterMetric metric = new NearestMemberMetric();
        List<ClusterMember> members = List.of(
                new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0))
        );

        double score = metric.score(Vector.xy(0, 1), members);

        assertEquals(2.0, score, 1.0e-9);
    }
}
