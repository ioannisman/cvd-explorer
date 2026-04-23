package cvdexplorer.core;

import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.PointMember;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClusterOwnershipSelectorTest {
    private final ClusterOwnershipSelector selector = new ClusterOwnershipSelector();

    @Test
    void selectsClusterWithLowestScore() {
        ClusterMetric metric = (point, members) -> members.get(0).distanceTo(point);
        List<ClusterSite> clusters = List.of(
                clusterAt(0, 0),
                clusterAt(10, 0)
        );

        ClusterOwnershipSelector.Result ownership = selector.selectOwner(Vector.xy(2, 0), clusters, metric);

        assertEquals(0, ownership.clusterIndex());
        assertEquals(2.0, ownership.score(), 1.0e-9);
    }

    @Test
    void breaksTiesTowardLowerClusterIndex() {
        ClusterMetric metric = (point, members) -> members.get(0).distanceTo(point);
        List<ClusterSite> clusters = List.of(
                clusterAt(-5, 0),
                clusterAt(5, 0)
        );

        ClusterOwnershipSelector.Result ownership = selector.selectOwner(Vector.xy(0, 0), clusters, metric);

        assertEquals(0, ownership.clusterIndex());
        assertEquals(5.0, ownership.score(), 1.0e-9);
    }

    private static ClusterSite clusterAt(double x, double y) {
        return new ClusterSite(
                "Cluster",
                Color.BLACK,
                List.of(new PointMember(Vector.xy(x, y)))
        );
    }
}
