package cvdexplorer.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderKOwnershipSelectorTest {
    @Test
    void orderOneReturnsNearestSingletonSet() {
        OrderKOwnershipSelector selector = new OrderKOwnershipSelector(1);

        ClusterOwnershipSelector.RegionMembership region = selector.select(List.of(
                new ClusterOwnershipSelector.ClusterScore(0, 7.0),
                new ClusterOwnershipSelector.ClusterScore(1, 3.0),
                new ClusterOwnershipSelector.ClusterScore(2, 5.0)
        ));

        assertEquals(1, region.clusterCount());
        assertTrue(region.containsCluster(1));
        assertFalse(region.containsCluster(0));
        assertEquals(3.0, region.boundaryScore(), 1.0e-9);
    }

    @Test
    void middleOrderReturnsKNearestSet() {
        OrderKOwnershipSelector selector = new OrderKOwnershipSelector(2);

        ClusterOwnershipSelector.RegionMembership region = selector.select(List.of(
                new ClusterOwnershipSelector.ClusterScore(0, 7.0),
                new ClusterOwnershipSelector.ClusterScore(1, 3.0),
                new ClusterOwnershipSelector.ClusterScore(2, 5.0),
                new ClusterOwnershipSelector.ClusterScore(3, 2.0)
        ));

        assertEquals(2, region.clusterCount());
        assertTrue(region.containsCluster(1));
        assertTrue(region.containsCluster(3));
        assertFalse(region.containsCluster(0));
        assertEquals(3.0, region.boundaryScore(), 1.0e-9);
    }

    @Test
    void orderNMinusOneMatchesFarthestComplementBehavior() {
        OrderKOwnershipSelector selector = new OrderKOwnershipSelector(3);

        ClusterOwnershipSelector.RegionMembership region = selector.select(List.of(
                new ClusterOwnershipSelector.ClusterScore(0, 11.0),
                new ClusterOwnershipSelector.ClusterScore(1, 3.0),
                new ClusterOwnershipSelector.ClusterScore(2, 5.0),
                new ClusterOwnershipSelector.ClusterScore(3, 2.0)
        ));

        assertEquals(3, region.clusterCount());
        assertFalse(region.containsCluster(0));
        assertTrue(region.containsCluster(1));
        assertTrue(region.containsCluster(2));
        assertTrue(region.containsCluster(3));
        assertEquals(5.0, region.boundaryScore(), 1.0e-9);
    }

    @Test
    void tiesStayDeterministicByClusterIndex() {
        OrderKOwnershipSelector selector = new OrderKOwnershipSelector(2);

        ClusterOwnershipSelector.RegionMembership region = selector.select(List.of(
                new ClusterOwnershipSelector.ClusterScore(2, 4.0),
                new ClusterOwnershipSelector.ClusterScore(0, 4.0),
                new ClusterOwnershipSelector.ClusterScore(1, 9.0)
        ));

        assertTrue(region.containsCluster(0));
        assertTrue(region.containsCluster(2));
        assertFalse(region.containsCluster(1));
        assertEquals(4.0, region.boundaryScore(), 1.0e-9);
    }
}
