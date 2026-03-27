package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import xyz.marsavic.geometry.Vector;

import java.util.List;

public final class FarthestMemberMetric implements ClusterMetric {
    @Override
    public double score(Vector point, List<ClusterMember> members) {
        if (members.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }

        double best = Double.NEGATIVE_INFINITY;
        for (ClusterMember member : members) {
            best = Math.max(best, member.distanceTo(point));
        }
        return best;
    }
}
