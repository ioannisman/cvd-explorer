package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.PointMember;
import xyz.marsavic.geometry.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Distance from a query point to the k-th nearest member point (1-based order statistic of distances).
 */
public final class KthNearestPointDistanceMetric implements ClusterMetric {
    private final int k;

    public KthNearestPointDistanceMetric(int k) {
        this.k = k;
    }

    @Override
    public double score(Vector point, List<ClusterMember> members) {
        if (members.isEmpty() || members.size() < k) {
            return Double.POSITIVE_INFINITY;
        }
        List<Double> distances = new ArrayList<>(members.size());
        for (ClusterMember member : members) {
            if (!(member instanceof PointMember pm)) {
                return Double.POSITIVE_INFINITY;
            }
            distances.add(pm.position().distanceTo(point));
        }
        Collections.sort(distances);
        return distances.get(k - 1);
    }
}
