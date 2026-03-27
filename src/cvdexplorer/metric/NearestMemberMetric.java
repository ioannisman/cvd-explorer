package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class NearestMemberMetric implements ClusterMetric {
    @Override
    public double score(Vector point, List<ClusterMember> members) {
        double best = Double.POSITIVE_INFINITY;
        for (ClusterMember member : members) {
            best = Math.min(best, member.distanceTo(point));
        }
        return best;
    }
}
