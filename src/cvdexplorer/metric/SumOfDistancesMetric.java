package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class SumOfDistancesMetric implements ClusterMetric {
    @Override
    public double score(Vector point, List<ClusterMember> members) {
        if (members.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        double sum = 0.0;
        for (ClusterMember member : members) {
            sum += member.distanceTo(point);
        }
        return sum;
    }
}
