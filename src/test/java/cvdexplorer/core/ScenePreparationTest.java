package cvdexplorer.core;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.SceneState;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScenePreparationTest {
    @Test
    void prepareUsesNearestNeighborOrderByDefault() {
        SceneState state = basePointScene();
        state.metricKind = MetricKind.MINIMUM_DISTANCE;
        state.neighborOrder = NeighborOrder.NEAREST;

        ScenePreparation.PreparedScene preparedScene = ScenePreparation.prepare(state);
        ClusterOwnershipSelector.Result ownership = preparedScene.ownershipSelector().selectOwner(
                Vector.xy(2, 0),
                preparedScene.clusters(),
                preparedScene.metric()
        );

        assertEquals(0, ownership.clusterIndex());
    }

    @Test
    void prepareUsesFarthestNeighborOrderWhenRequested() {
        SceneState state = basePointScene();
        state.metricKind = MetricKind.MINIMUM_DISTANCE;
        state.neighborOrder = NeighborOrder.FARTHEST;

        ScenePreparation.PreparedScene preparedScene = ScenePreparation.prepare(state);
        ClusterOwnershipSelector.Result ownership = preparedScene.ownershipSelector().selectOwner(
                Vector.xy(2, 0),
                preparedScene.clusters(),
                preparedScene.metric()
        );

        assertEquals(1, ownership.clusterIndex());
    }

    private static SceneState basePointScene() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(clusterAt("A", 0, 0, Color.RED));
        state.clusters().add(clusterAt("B", 10, 0, Color.BLUE));
        state.numberOfClusters = state.clusters().size();
        return state;
    }

    private static ClusterSite clusterAt(String name, double x, double y, Color color) {
        return new ClusterSite(name, color, List.of(new PointMember(Vector.xy(x, y))));
    }
}
