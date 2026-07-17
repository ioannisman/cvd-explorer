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
    public Result evaluate(Vector point, List<ClusterMember> members) {
        if (members.isEmpty() || members.size() < k) {
            return new Result(Double.POSITIVE_INFINITY, -1);
        }
        
        record DistanceWithIndex(double distance, int index) implements Comparable<DistanceWithIndex> {
            @Override
            public int compareTo(DistanceWithIndex o) {
                return Double.compare(distance, o.distance);
            }
        }
        
        List<DistanceWithIndex> distances = new ArrayList<>(members.size());
        for (int i = 0; i < members.size(); i++) {
            ClusterMember member = members.get(i);
            if (!(member instanceof PointMember pm)) {
                return new Result(Double.POSITIVE_INFINITY, -1);
            }
            distances.add(new DistanceWithIndex(pm.position().distanceTo(point), i));
        }
        Collections.sort(distances);
        DistanceWithIndex kth = distances.get(k - 1);
        return new Result(kth.distance(), kth.index());
    }
}
