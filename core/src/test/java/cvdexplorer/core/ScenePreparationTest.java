package cvdexplorer.core;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScenePreparationTest {
    @Test
    void prepareUsesNearestNeighborOrderByDefault() {
        List<ClusterSite> clusters = basePointClusters();

        ScenePreparation.PreparedScene preparedScene = ScenePreparation.prepare(
                clusters,
                MetricKind.MINIMUM_DISTANCE,
                NeighborOrder.NEAREST,
                1
        );
        ClusterOwnershipSelector.Result ownership = preparedScene.ownershipSelector().selectOwner(
                Vector.xy(2, 0),
                preparedScene.clusters(),
                preparedScene.metric()
        );

        assertEquals(0, ownership.clusterIndex());
    }

    @Test
    void prepareUsesFarthestNeighborOrderWhenRequested() {
        List<ClusterSite> clusters = basePointClusters();

        ScenePreparation.PreparedScene preparedScene = ScenePreparation.prepare(
                clusters,
                MetricKind.MINIMUM_DISTANCE,
                NeighborOrder.FARTHEST,
                1
        );
        ClusterOwnershipSelector.Result ownership = preparedScene.ownershipSelector().selectOwner(
                Vector.xy(2, 0),
                preparedScene.clusters(),
                preparedScene.metric()
        );

        assertEquals(1, ownership.clusterIndex());
    }

    private static List<ClusterSite> basePointClusters() {
        return List.of(
                clusterAt("A", 0, 0, Rgba.RED),
                clusterAt("B", 10, 0, Rgba.BLUE)
        );
    }

    private static ClusterSite clusterAt(String name, double x, double y, Rgba color) {
        return new ClusterSite(name, color, List.of(new PointMember(Vector.xy(x, y))));
    }
}
