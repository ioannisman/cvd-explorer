package cvdexplorer.model;

import cvdexplorer.metric.MetricKind;
import org.junit.jupiter.api.Test;
import xyz.marsavic.geometry.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneStateTest {

    @Test
    void minMemberCountAcrossClustersIsMinimumOfSizes() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(new ClusterSite("A", javafx.scene.paint.Color.RED, List.of(new PointMember(Vector.xy(0, 0)))));
        state.clusters().add(new ClusterSite(
                "B",
                javafx.scene.paint.Color.BLUE,
                List.of(
                        new PointMember(Vector.xy(1, 0)),
                        new PointMember(Vector.xy(2, 0)),
                        new PointMember(Vector.xy(3, 0))
                )
        ));
        assertEquals(1, state.minMemberCountAcrossClusters());
    }

    @Test
    void minMemberCountAcrossClustersIsZeroWhenNoClusters() {
        SceneState state = new SceneState();
        state.clusters().clear();
        assertEquals(0, state.minMemberCountAcrossClusters());
    }

    @Test
    void clampNearestNeighborKSetsOneWhenNoMembers() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.nearestNeighborK = 5;
        state.clampNearestNeighborK();
        assertEquals(1, state.nearestNeighborK);
    }

    @Test
    void clampNearestNeighborKRespectsSmallestClusterSize() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(new ClusterSite("A", javafx.scene.paint.Color.RED, List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(1, 0)),
                new PointMember(Vector.xy(2, 0))
        )));
        state.clusters().add(new ClusterSite("B", javafx.scene.paint.Color.GREEN, List.of(
                new PointMember(Vector.xy(0, 1)),
                new PointMember(Vector.xy(1, 1))
        )));
        state.nearestNeighborK = 100;
        state.clampNearestNeighborK();
        assertEquals(2, state.nearestNeighborK);
    }

    @Test
    void applyLoadedSceneReplacesClustersAndClampsK() {
        SceneState state = new SceneState();
        state.activeClusterOneBased = 1;
        state.clusters().clear();
        state.clusters().add(new ClusterSite("Old", javafx.scene.paint.Color.BLACK, List.of(new PointMember(Vector.xy(0, 0)))));

        List<ClusterSite> incoming = List.of(
                new ClusterSite("C1", javafx.scene.paint.Color.RED, List.of(
                        new PointMember(Vector.xy(1, 1)),
                        new PointMember(Vector.xy(2, 2))
                )),
                new ClusterSite("C2", javafx.scene.paint.Color.BLUE, List.of(new PointMember(Vector.xy(3, 3))))
        );

        state.applyLoadedScene(
                MetricKind.KTH_NEAREST_DISTANCE,
                NeighborOrder.FARTHEST,
                SiteMemberKind.POINT,
                incoming,
                99
        );

        assertEquals(MetricKind.KTH_NEAREST_DISTANCE, state.metricKind);
        assertEquals(NeighborOrder.FARTHEST, state.neighborOrder);
        assertEquals(SiteMemberKind.POINT, state.siteMemberKind);
        assertEquals(2, state.clusters().size());
        assertEquals("C1", state.clusters().get(0).name());
        assertEquals(1, state.nearestNeighborK);
    }

    @Test
    void applyLoadedSceneThrowsWhenClusterListEmpty() {
        SceneState state = new SceneState();
        state.clusters().add(new ClusterSite("X", javafx.scene.paint.Color.GRAY, List.of(new PointMember(Vector.xy(0, 0)))));
        assertThrows(IllegalArgumentException.class, () ->
                state.applyLoadedScene(
                        MetricKind.MINIMUM_DISTANCE,
                        NeighborOrder.NEAREST,
                        SiteMemberKind.POINT,
                        List.of(),
                        1
                )
        );
    }

    @Test
    void applyLoadedSceneThrowsWhenTooManyClusters() {
        SceneState state = new SceneState();
        List<ClusterSite> tooMany = new ArrayList<>();
        for (int i = 0; i < SceneState.MAX_CLUSTERS + 1; i++) {
            tooMany.add(new ClusterSite("c" + i, javafx.scene.paint.Color.GRAY, List.of(new PointMember(Vector.xy(i, 0)))));
        }
        assertThrows(IllegalArgumentException.class, () ->
                state.applyLoadedScene(
                        MetricKind.MINIMUM_DISTANCE,
                        NeighborOrder.NEAREST,
                        SiteMemberKind.POINT,
                        tooMany,
                        1
                )
        );
    }

    @Test
    void ensureClusterCountMatchesGadgetRejectsIncompatibleNewClusterMember() {
        ClusterSite first = new ClusterSite("A", javafx.scene.paint.Color.ORANGE, List.of(new PointMember(Vector.xy(0, 0))));
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(first);
        state.numberOfClusters = 1;
        state.metricKind = MetricKind.SUM_OF_DISTANCES;
        state.siteMemberKind = SiteMemberKind.LINE;
        state.activeClusterOneBased = 1;
        state.numberOfClusters = 2;

        Optional<String> msg = state.ensureClusterCountMatchesGadget();

        assertTrue(msg.isPresent());
        assertTrue(msg.get().contains("SUM_OF_DISTANCES"));
        assertEquals(1, state.clusters().size());
        assertEquals(1, state.numberOfClusters);
    }

    @Test
    void ensureActiveClusterMemberCountRejectsAddingIncompatibleMember() {
        ClusterSite cluster = new ClusterSite("A", javafx.scene.paint.Color.CYAN, List.of(new PointMember(Vector.xy(10, 10))));
        List<ClusterSite> loaded = List.of(cluster);
        SceneState state = new SceneState();
        state.activeClusterOneBased = 1;
        state.applyLoadedScene(
                MetricKind.MEAN_DISTANCE,
                NeighborOrder.NEAREST,
                SiteMemberKind.CIRCLE,
                loaded,
                1
        );
        state.targetPointCountForActiveCluster = 2;

        Optional<String> msg = state.ensureActiveClusterMemberCount();

        assertTrue(msg.isPresent());
        assertTrue(msg.get().contains("MEAN_DISTANCE"));
        assertEquals(1, state.clusters().get(0).size());
        assertEquals(1, state.targetPointCountForActiveCluster);
    }
}
